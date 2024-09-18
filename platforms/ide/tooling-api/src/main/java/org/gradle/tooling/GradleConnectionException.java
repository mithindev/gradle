/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.tooling;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Incubating;
import org.gradle.tooling.events.problems.ProblemReport;
import org.gradle.tooling.internal.consumer.AbstractLongRunningOperation;
import org.gradle.tooling.internal.consumer.BuildFailedProgressListener;

import java.util.Collection;
import java.util.Map;

/**
 * Thrown when there is some problem using a Gradle connection.
 *
 * @since 1.0-milestone-3
 */
public class GradleConnectionException extends RuntimeException {
    private final AbstractLongRunningOperation operation;

    public GradleConnectionException(String message) {
        super(message);
        this.operation = null;
    }

    public GradleConnectionException(String message, Throwable throwable) {
        super(message, throwable);
        this.operation = null;
    }

    /**
     * TODO description.
     *
     * @since 8.11
     */
    @Incubating
    public GradleConnectionException(String message, Throwable throwable, AbstractLongRunningOperation operation) {
        super(message, throwable);
        this.operation = operation;
    }

    /**
     * TODO description.
     *
     * @return the reported problems for this failure.
     * @since 8.11
     */
    @Incubating
    public Map<Failure, Collection<ProblemReport>> problemReports() {
        if (operation == null) {
            return ImmutableMap.of();
        }
        BuildFailedProgressListener listener = operation.buildFailedProgressListener;
        if (listener == null) {
            return ImmutableMap.of();
        }
        return listener.getProblems();
    }
}
