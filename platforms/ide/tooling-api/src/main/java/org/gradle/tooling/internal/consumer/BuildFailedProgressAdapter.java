/*
 * Copyright 2024 the original author or authors.
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

import org.gradle.api.NonNullApi;
import org.gradle.tooling.BuildOutcomeHandler;
import org.gradle.tooling.Failure;
import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationResult;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.problems.BuildFailureEvent;

import java.util.ArrayList;
import java.util.List;

@NonNullApi
public class BuildFailedProgressAdapter implements ProgressListener {

    private boolean failureReported = false;
    private BuildOutcomeHandler buildOutcomeHandler = null;

    @Override
    public void statusChanged(ProgressEvent event) {
        if (event instanceof BuildFailureEvent) {
            BuildFailureEvent failureEvent = (BuildFailureEvent) event;
            notifyFailure(failureEvent.getFailures());
            failureReported = true;
        }

        if (event instanceof FinishEvent && event.getDescriptor().getParent() == null && !failureReported) {
            OperationResult result = ((FinishEvent) event).getResult();
            if (result instanceof FailureResult) {
                notifyFailure(new ArrayList<>(((FailureResult) result).getFailures()));
            } else {
                notifySuccess();
            }
        }
    }

    private void notifySuccess() {
        if (buildOutcomeHandler != null) {
            buildOutcomeHandler.onSuccess();
        }
    }

    private void notifyFailure(List<Failure> failures) {
        if (buildOutcomeHandler != null) {
            buildOutcomeHandler.onFailure(failures);
        }
    }

    void setHandler(BuildOutcomeHandler buildOutcomeHandler) {
        this.buildOutcomeHandler = buildOutcomeHandler;
    }

}
