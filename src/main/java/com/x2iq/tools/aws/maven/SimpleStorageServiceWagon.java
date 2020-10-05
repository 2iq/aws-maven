/*
 * Copyright 2010-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.x2iq.tools.aws.maven;

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static software.amazon.awssdk.services.s3.model.ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL;

/**
 * An implementation of the Maven Wagon interface that allows you to access the Amazon S3 service. URLs that reference
 * the S3 service should be in the form of <code>s3://bucket.name</code>. As an example
 * <code>s3://static.springframework.org</code> would put files into the <code>static.springframework.org</code> bucket
 * on the S3 service.
 * <p>
 * This implementation uses the <code>username</code> and <code>passphrase</code> portions of the server authentication
 * metadata for credentials.
 */
public final class SimpleStorageServiceWagon extends AbstractWagon {

    private static final String KEY_FORMAT = "%s%s";

    private static final String RESOURCE_FORMAT = "%s(.*)";

    private static final String roleArnKey = "AWS_ASSUME_ROLE_ARN";

    private static final String roleSessionName = "AWS_ASSUME_ROLE_NAME";

    private static final String configPathEnvKey = "S3_MAVEN_CONFIG_FILE";

    private static final String s3DefaultConfigPath = ".s3_config";

    private volatile S3Client s3;

    private volatile String bucketName;

    private volatile String baseDirectory;

    /**
     * Creates a new instance of the wagon
     */
    public SimpleStorageServiceWagon() {
        super(true);
    }

    @Override
    protected void connectToRepository(Repository repository, AuthenticationInfo authenticationInfo,
                                       ProxyInfoProvider proxyInfoProvider) throws AuthenticationException {
        if (this.s3 != null) {
            return;
        }
        this.bucketName = S3Utils.getBucketName(repository);
        this.baseDirectory = S3Utils.getBaseDirectory(repository);

        AwsCredentialsProvider credentials = getCredentials(authenticationInfo);
        SdkHttpClient httpClient = S3Utils.getSdkHttpClient(proxyInfoProvider);
        Region region = getRegionForBucket(credentials, httpClient);

        this.s3 = S3Client.builder()
            .credentialsProvider(credentials)
            .httpClient(httpClient)
            .region(region)
            .build();
    }

    private Region getRegionForBucket(AwsCredentialsProvider credentials, SdkHttpClient httpClient) {
        String region = S3Client.builder()
            .credentialsProvider(credentials)
            .httpClient(httpClient)
            .build()
            .getBucketLocation(builder -> builder.bucket(this.bucketName))
            .locationConstraintAsString();

        return Region.of(region);
    }

    protected AwsCredentialsProvider getCredentials(AuthenticationInfo authenticationInfo) {
        AwsCredentialsProvider credentials = AwsCredentialsProviderChain.of(
            DefaultCredentialsProvider.create(),
            new AuthenticationInfoAWSCredentialsProvider(authenticationInfo)
        );

        if (!isAssumedRoleRequested()) {
            return credentials;
        }

        StsClient sts = StsClient.builder()
            .credentialsProvider(credentials)
            .build();

        String ARN = getAssumedRoleARN();
        String SESSION = getAssumedRoleSessionName();

        return StsAssumeRoleCredentialsProvider.builder()
            .stsClient(sts)
            .refreshRequest(builder -> builder
                .roleArn(ARN)
                .roleSessionName(SESSION))
            .build();
    }

    protected String getAssumedRoleVariableFromConfigFile(String key) {
        String configPath = System.getenv(configPathEnvKey);
        File config = new File(configPath != null ? configPath : s3DefaultConfigPath);
        if (!config.exists()) {
            return null;
        }
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(config));
            return props.getProperty(key);
        } catch (Exception e) {
            return null;
        }
    }

    protected String getAssumedRoleARN() {
        if (System.getenv(roleArnKey) != null) {
            return System.getenv(roleArnKey);
        } else {
            return getAssumedRoleVariableFromConfigFile(roleArnKey);
        }
    }

    protected String getAssumedRoleSessionName() {
        if (System.getenv(roleSessionName) != null) {
            return System.getenv(roleSessionName);
        } else {
            return getAssumedRoleVariableFromConfigFile(roleSessionName);
        }
    }

    protected boolean isAssumedRoleRequested() {
        String role = getAssumedRoleARN();
        String session = getAssumedRoleSessionName();
        return role != null && session != null && !role.trim().isEmpty() && !session.trim().isEmpty();
    }

    @Override
    protected void disconnectFromRepository() {
        this.s3.close();
        this.s3 = null;
        this.bucketName = null;
        this.baseDirectory = null;
    }

    @Override
    protected boolean doesRemoteResourceExist(String resourceName) {
        try {
            headS3Object(resourceName);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    protected boolean isRemoteResourceNewer(String resourceName, long timestamp) throws ResourceDoesNotExistException {
        try {
            Instant lastModified = headS3Object(resourceName).lastModified();
            return lastModified == null || lastModified.toEpochMilli() > timestamp;
        } catch (NoSuchKeyException e) {
            throw new ResourceDoesNotExistException(String.format("'%s' does not exist", resourceName), e);
        }
    }

    @Override
    protected List<String> listDirectory(String directory) throws ResourceDoesNotExistException {
        String prefix = getKey(directory);
        Pattern pattern = Pattern.compile(String.format(RESOURCE_FORMAT, prefix));

        ListObjectsV2Iterable response = this.s3.listObjectsV2Paginator(builder -> builder
            .bucket(this.bucketName)
            .prefix(prefix)
            .delimiter("/"));

        return new ArrayList<>(getResourceNames(response, pattern));
    }

    @Override
    protected void getResource(String resourceName, File destination, TransferProgress transferProgress)
            throws TransferFailedException, ResourceDoesNotExistException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = this.s3.getObject(builder -> builder
                .bucket(this.bucketName)
                .key(resourceName));
            out = new TransferProgressFileOutputStream(destination, transferProgress);

            IoUtils.copy(in, out);
        } catch (AwsServiceException e) {
            throw new ResourceDoesNotExistException(String.format("'%s' does not exist", resourceName), e);
        } catch (FileNotFoundException e) {
            throw new TransferFailedException(String.format("Cannot write file to '%s'", destination), e);
        } catch (IOException e) {
            throw new TransferFailedException(String.format("Cannot read from '%s' and write to '%s'", resourceName, destination), e);
        } finally {
            IoUtils.closeQuietly(in, out);
        }
    }

    @Override
    protected void putResource(File source, String destination, TransferProgress transferProgress) throws TransferFailedException,
            ResourceDoesNotExistException {
        String key = getKey(destination);

        mkdirs(key, 0);

        InputStream in = null;
        try {
            in = new TransferProgressFileInputStream(source, transferProgress);

            this.s3.putObject(builder -> builder
                    .bucket(this.bucketName)
                    .key(key)
                    .acl(BUCKET_OWNER_FULL_CONTROL),
                RequestBody.fromInputStream(in, source.length()));

        } catch (AwsServiceException e) {
            throw new TransferFailedException(String.format("Cannot write file to '%s'", destination), e);
        } catch (FileNotFoundException e) {
            throw new ResourceDoesNotExistException(String.format("Cannot read file from '%s'", source), e);
        } finally {
            IoUtils.closeQuietly(in);
        }
    }

    private HeadObjectResponse headS3Object(String resourceName) {
        return this.s3.headObject(builder -> builder
            .bucket(this.bucketName)
            .key(getKey(resourceName)));
    }

    private String getKey(String resourceName) {
        return String.format(KEY_FORMAT, this.baseDirectory, resourceName);
    }

    private List<String> getResourceNames(ListObjectsV2Iterable objectListing, Pattern pattern) {
        Stream<String> directoryStream = objectListing.commonPrefixes().stream()
            .map(CommonPrefix::prefix);

        Stream<String> objectsStream = objectListing.contents().stream()
            .map(software.amazon.awssdk.services.s3.model.S3Object::key);

        return Stream.concat(directoryStream, objectsStream)
            .map((String key) -> getResourceName(key, pattern))
            .collect(Collectors.toList());
    }

    private String getResourceName(String key, Pattern pattern) {
        Matcher matcher = pattern.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return key;
    }

    private void mkdirs(String path, int index) throws TransferFailedException {
        int directoryIndex = path.indexOf('/', index) + 1;

        if (directoryIndex != 0) {
            String directory = path.substring(0, directoryIndex);

            try {
                this.s3.putObject(builder -> builder
                        .bucket(this.bucketName)
                        .key(directory)
                        .acl(BUCKET_OWNER_FULL_CONTROL),
                    RequestBody.empty());
            } catch (AwsServiceException e) {
                throw new TransferFailedException(String.format("Cannot write directory '%s'", directory), e);
            }

            mkdirs(path, directoryIndex);
        }
    }
}
