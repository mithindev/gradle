/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.tooling.internal.provider.runner;

import org.gradle.api.NonNullApi;
import org.gradle.api.problems.internal.BuildFailureWithProblemsProgressDetails;
import org.gradle.api.problems.internal.Problem;
import org.gradle.internal.build.event.BuildEventSubscriptions;
import org.gradle.internal.build.event.types.AbstractOperationResult;
import org.gradle.internal.build.event.types.DefaultBuildBuildDescriptor;
import org.gradle.internal.build.event.types.DefaultOperationFinishedProgressEvent;
import org.gradle.internal.build.event.types.DefaultOperationStartedProgressEvent;
import org.gradle.internal.build.event.types.DefaultProblemAwareFailure;
import org.gradle.internal.build.event.types.DefaultProblemToFailureDescriptor;
import org.gradle.internal.build.event.types.DefaultProblemToFailureDetails;
import org.gradle.internal.build.event.types.DefaultProblemToFailureEvent;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationIdFactory;
import org.gradle.internal.operations.OperationFinishEvent;
import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.internal.operations.OperationProgressEvent;
import org.gradle.internal.operations.OperationStartEvent;
import org.gradle.launcher.exec.RunBuildBuildOperationType;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.internal.protocol.InternalFailure;
import org.gradle.tooling.internal.protocol.events.InternalOperationFinishedProgressEvent;
import org.gradle.tooling.internal.protocol.events.InternalOperationStartedProgressEvent;
import org.gradle.tooling.internal.protocol.events.InternalProgressEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@NonNullApi
class RunBuildBuildOperationMapper implements BuildOperationMapper<RunBuildBuildOperationType.Details, DefaultBuildBuildDescriptor> {

    private final Supplier<OperationIdentifier> operationIdentifierSupplier;

    public RunBuildBuildOperationMapper(BuildOperationIdFactory idFactory) {
        this.operationIdentifierSupplier = () -> new OperationIdentifier(idFactory.nextId());
    }

    @Override
    public boolean isEnabled(BuildEventSubscriptions subscriptions) {
        return subscriptions.isRequested(OperationType.PROBLEMS);
    }

    @Override
    public Class<RunBuildBuildOperationType.Details> getDetailsType() {
        return RunBuildBuildOperationType.Details.class;
    }

    @Override
    public DefaultBuildBuildDescriptor createDescriptor(RunBuildBuildOperationType.Details details, BuildOperationDescriptor buildOperation, @Nullable OperationIdentifier parent) {
        OperationIdentifier id = buildOperation.getId();
        String displayName = buildOperation.getDisplayName();
        return new DefaultBuildBuildDescriptor(id, displayName, displayName, parent);
    }

    @Override
    public InternalOperationStartedProgressEvent createStartedEvent(DefaultBuildBuildDescriptor descriptor, RunBuildBuildOperationType.Details details, OperationStartEvent startEvent) {
        return new DefaultOperationStartedProgressEvent(startEvent.getStartTime(), descriptor);
    }

    @Nullable
    @Override
    public InternalProgressEvent createProgressEvent(DefaultBuildBuildDescriptor descriptor, OperationProgressEvent progressEvent) {
        descriptor.getId();
        Object progressEventDetails = progressEvent.getDetails();
        if (progressEventDetails instanceof BuildFailureWithProblemsProgressDetails) {
            BuildFailureWithProblemsProgressDetails details = (BuildFailureWithProblemsProgressDetails) progressEventDetails;
            Throwable failure = details.getBuildFailure();
            Map<Throwable, Collection<Problem>> problemsForThrowables = details.getProblemsForThrowables();
            InternalFailure rootFailure = DefaultProblemAwareFailure.fromThrowable(failure, problemsForThrowables, ProblemsProgressEventConsumer::createDefaultProblemDetails);

            return new DefaultProblemToFailureEvent(
                new DefaultProblemToFailureDescriptor(
                    operationIdentifierSupplier.get(),
                    descriptor.getId()
                ),
                new DefaultProblemToFailureDetails(
                    Collections.singletonList(rootFailure)
                )
            );
        }
        return null;
    }

    @Override
    public InternalOperationFinishedProgressEvent createFinishedEvent(DefaultBuildBuildDescriptor descriptor, RunBuildBuildOperationType.Details details, OperationFinishEvent finishEvent) {
        return new DefaultOperationFinishedProgressEvent(finishEvent.getEndTime(), descriptor, toOperationResultWithProblems(finishEvent));
    }

    private static AbstractOperationResult toOperationResultWithProblems(OperationFinishEvent result) {
        return ClientForwardingBuildOperationListener.toOperationResult(result);
    }
}
