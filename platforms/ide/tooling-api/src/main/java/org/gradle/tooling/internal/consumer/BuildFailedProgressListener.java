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
import org.gradle.tooling.Failure;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.problems.ProblemReport;
import org.gradle.tooling.events.problems.ProblemToFailureEvent;

import java.util.Collection;
import java.util.Map;

@NonNullApi
public class BuildFailedProgressListener implements ProgressListener {
    public Map<Failure, Collection<ProblemReport>> problems;

    @Override
    public void statusChanged(ProgressEvent event) {
        // TODO (donat) countdown latch when the root build operation finishes
        if (event instanceof ProblemToFailureEvent) {
            this.problems = ((ProblemToFailureEvent) event).getProblemsForFailures();
        }
    }

    public Map<Failure, Collection<ProblemReport>> getProblems() {
        return problems;
    }
}
