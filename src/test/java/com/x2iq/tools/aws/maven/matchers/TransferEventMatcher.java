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

import org.apache.maven.wagon.events.TransferEvent;
import org.mockito.ArgumentMatcher;

final class TransferEventMatcher implements ArgumentMatcher<TransferEvent> {

    private final TransferEvent transferEvent;

    TransferEventMatcher(TransferEvent transferEvent) {
        this.transferEvent = transferEvent;
    }

    // CHECKSTYLE:OFF

    @Override
    public boolean matches(TransferEvent other) {
        if (this.transferEvent == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.transferEvent.getEventType() != other.getEventType()) {
            return false;
        }
        if (this.transferEvent.getRequestType() != other.getRequestType()) {
            return false;
        }
        if (this.transferEvent.getWagon() == null) {
            if (other.getWagon() != null) {
                return false;
            }
        } else if (!this.transferEvent.getWagon().equals(other.getWagon())) {
            return false;
        }
        if (this.transferEvent.getResource() == null) {
            if (other.getResource() != null) {
                return false;
            }
        } else if (!this.transferEvent.getResource().equals(other.getResource())) {
            return false;
        }
        if (this.transferEvent.getException() == null) {
            if (other.getException() != null) {
                return false;
            }
        } else if (!this.transferEvent.getException().equals(other.getException())) {
            return false;
        }
        return true;
    }

    // CHECKSTYLE:ON
}
