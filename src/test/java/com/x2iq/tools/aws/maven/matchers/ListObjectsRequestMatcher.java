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

package com.x2iq.tools.aws.maven.matchers;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import org.mockito.ArgumentMatcher;

final class ListObjectsRequestMatcher implements ArgumentMatcher<ListObjectsRequest> {

    private final ListObjectsRequest listObjectsRequest;

    ListObjectsRequestMatcher(ListObjectsRequest listObjectsRequest) {
        this.listObjectsRequest = listObjectsRequest;
    }

    // CHECKSTYLE:OFF

    @Override
    public boolean matches(ListObjectsRequest other) {
        if (this.listObjectsRequest == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.listObjectsRequest.getBucketName() == null) {
            if (other.getBucketName() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getBucketName().equals(other.getBucketName())) {
            return false;
        }
        if (this.listObjectsRequest.getPrefix() == null) {
            if (other.getPrefix() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getPrefix().equals(other.getPrefix())) {
            return false;
        }
        if (this.listObjectsRequest.getDelimiter() == null) {
            if (other.getDelimiter() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getDelimiter().equals(other.getDelimiter())) {
            return false;
        }
        if (this.listObjectsRequest.getMarker() == null) {
            if (other.getMarker() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getMarker().equals(other.getMarker())) {
            return false;
        }
        if (this.listObjectsRequest.getMaxKeys() == null) {
            if (other.getMaxKeys() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getMaxKeys().equals(other.getMaxKeys())) {
            return false;
        }
        return true;
    }

    // CHECKSTYLE:ON
}
