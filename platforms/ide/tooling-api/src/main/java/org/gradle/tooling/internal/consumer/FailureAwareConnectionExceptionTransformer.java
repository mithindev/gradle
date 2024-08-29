/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.tooling.internal.consumer;

import org.gradle.internal.event.ListenerNotificationException;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.Failure;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ListenerFailedException;
import org.gradle.tooling.TestExecutionException;
import org.gradle.tooling.events.problems.ProblemReport;
import org.gradle.tooling.exceptions.UnsupportedBuildArgumentException;
import org.gradle.tooling.exceptions.UnsupportedOperationConfigurationException;
import org.gradle.tooling.internal.protocol.BuildExceptionVersion1;
import org.gradle.tooling.internal.protocol.InternalBuildCancelledException;
import org.gradle.tooling.internal.protocol.exceptions.InternalUnsupportedBuildArgumentException;
import org.gradle.tooling.internal.protocol.test.InternalTestExecutionException;

import java.util.List;
import java.util.Map;

public class FailureAwareConnectionExceptionTransformer extends ConnectionExceptionTransformer {

    private final AbstractLongRunningOperation longRunningOperation;

    public FailureAwareConnectionExceptionTransformer(ConnectionFailureMessageProvider messageProvider, AbstractLongRunningOperation longRunningOperation) {
        super(messageProvider);
        this.longRunningOperation = longRunningOperation;
    }

    @Override
    public GradleConnectionException transform(Throwable failure) {
        BuildFailedProgressListener buildFailedListener = longRunningOperation.buildFailedProgressListener;
        Map<Failure, List<ProblemReport>> problems = buildFailedListener == null ? null : buildFailedListener.problems;

        if (failure instanceof InternalUnsupportedBuildArgumentException) {
            return new UnsupportedBuildArgumentException(connectionFailureMessage(failure)
                + "\n" + failure.getMessage(), failure);
        } else if (failure instanceof UnsupportedOperationConfigurationException) {
            return new UnsupportedOperationConfigurationException(connectionFailureMessage(failure)
                + "\n" + failure.getMessage(), failure.getCause());
        } else if (failure instanceof GradleConnectionException) {
            return (GradleConnectionException) failure;
        } else if (failure instanceof InternalBuildCancelledException) {
            return new BuildCancelledException(connectionFailureMessage(failure), failure.getCause());
        } else if (failure instanceof InternalTestExecutionException) {
            return new TestExecutionException(connectionFailureMessage(failure), failure.getCause());
        } else if (failure instanceof BuildExceptionVersion1) {
            if (problems != null) {
                return new BuildException(connectionFailureMessage(failure), failure.getCause(), problems);
            } else {
                return new BuildException(connectionFailureMessage(failure), failure.getCause());
            }
        } else if (failure instanceof ListenerNotificationException) {
            return new ListenerFailedException(connectionFailureMessage(failure), ((ListenerNotificationException) failure).getCauses());
        } else {
            return new GradleConnectionException(connectionFailureMessage(failure), failure);
        }
    }
}
