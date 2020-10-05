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
import org.junit.Test;
import software.amazon.awssdk.http.apache.ProxyConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class S3UtilsTest {

    private static final int PORT = 100;

    private final ProxyInfoProvider proxyInfoProvider = mock(ProxyInfoProvider.class);

    private final ProxyInfo proxyInfo = mock(ProxyInfo.class);

    @Test
    public void getBucketName() {
        assertEquals("dist.springsource.com", S3Utils.getBucketName(createRepository("/")));
    }

    @Test
    public void getBaseDirectory() {
        assertEquals("", S3Utils.getBaseDirectory(createRepository("")));
        assertEquals("", S3Utils.getBaseDirectory(createRepository("/")));
        assertEquals("foo/", S3Utils.getBaseDirectory(createRepository("/foo")));
        assertEquals("foo/", S3Utils.getBaseDirectory(createRepository("/foo/")));
        assertEquals("foo/bar/", S3Utils.getBaseDirectory(createRepository("/foo/bar")));
        assertEquals("foo/bar/", S3Utils.getBaseDirectory(createRepository("/foo/bar/")));
    }

    @Test
    public void getClientConfiguration() {
        when(this.proxyInfoProvider.getProxyInfo("s3")).thenReturn(this.proxyInfo);
        when(this.proxyInfo.getHost()).thenReturn("foo");
        when(this.proxyInfo.getPort()).thenReturn(PORT);

        ProxyConfiguration clientConfiguration = S3Utils.getProxyConfiguration(this.proxyInfoProvider);
        assertEquals("foo", clientConfiguration.host());
        assertEquals(100, clientConfiguration.port());
    }

    @Test
    public void getClientConfigurationNoProxyInfoProvider() {
        ProxyConfiguration clientConfiguration = S3Utils.getProxyConfiguration(null);
        assertNull(clientConfiguration.host());
        assertEquals(0, clientConfiguration.port());
    }

    @Test
    public void getClientConfigurationNoProxyInfo() {
        ProxyConfiguration clientConfiguration = S3Utils.getProxyConfiguration(this.proxyInfoProvider);
        assertNull(clientConfiguration.host());
        assertEquals(0, clientConfiguration.port());
    }

    private Repository createRepository(String path) {
        return new Repository("foo", String.format("s3://dist.springsource.com%s", path));
    }
}
