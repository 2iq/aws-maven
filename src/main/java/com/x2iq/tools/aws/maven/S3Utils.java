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

import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

final class S3Utils {

    private S3Utils() {
    }

    static String getBucketName(Repository repository) {
        return repository.getHost();
    }

    static String getBaseDirectory(Repository repository) {
        StringBuilder sb = new StringBuilder(repository.getBasedir()).deleteCharAt(0);

        if ((sb.length() != 0) && (sb.charAt(sb.length() - 1) != '/')) {
            sb.append('/');
        }

        return sb.toString();
    }

    static SdkHttpClient getSdkHttpClient(ProxyInfoProvider proxyInfoProvider) {
        ProxyConfiguration proxyConfig = getProxyConfiguration(proxyInfoProvider);

        return ApacheHttpClient
            .builder()
            .proxyConfiguration(proxyConfig)
            .build();
    }

    static ProxyConfiguration getProxyConfiguration(ProxyInfoProvider proxyInfoProvider) {

        if (proxyInfoProvider != null) {
            ProxyInfo proxyInfo = proxyInfoProvider.getProxyInfo("s3");
            if (proxyInfo != null) {
                try {
                    // TODO are type and schema same property?
                    URI proxy = new URI(proxyInfo.getType(), null, proxyInfo.getHost(), proxyInfo.getPort(), null, null, null);

                    return ProxyConfiguration
                        .builder()
                        .endpoint(proxy)
                        .username(proxyInfo.getUserName())
                        .password(proxyInfo.getPassword())
                        .addNonProxyHost(proxyInfo.getNonProxyHosts())  // TODO for multiple hosts
                        .build();
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Invalid proxy configuration " + proxyInfo, e);  // TODO better exception type
                }
            }
        }

        return ProxyConfiguration.builder().build();
    }
}
