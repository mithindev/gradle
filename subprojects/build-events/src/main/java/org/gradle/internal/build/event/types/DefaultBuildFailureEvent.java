/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.internal.build.event.types;

import org.gradle.api.NonNullApi;
import org.gradle.tooling.internal.protocol.InternalProblemEventVersion2;
import org.gradle.tooling.internal.protocol.events.DefaultBuildFailureDescriptor;
import org.gradle.tooling.internal.protocol.problem.InternalProblemToFailureDetails;

@NonNullApi
    public class DefaultBuildFailureEvent extends AbstractProgressEvent<DefaultBuildFailureDescriptor> implements InternalProblemEventVersion2 {
    private final InternalProblemToFailureDetails details;

    public DefaultBuildFailureEvent(
        DefaultBuildFailureDescriptor descriptor,
        InternalProblemToFailureDetails details
    ) {
        super(System.currentTimeMillis(), descriptor);
        this.details = details;
    }

    @Override
    public String getDisplayName() {
        return "problem to build failure association";
    }

    @Override
    public InternalProblemToFailureDetails getDetails() {
        return details;
    }
}
