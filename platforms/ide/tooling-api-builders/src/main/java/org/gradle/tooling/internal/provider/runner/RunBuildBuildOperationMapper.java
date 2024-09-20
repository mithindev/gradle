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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.gradle.api.NonNullApi;
import org.gradle.api.problems.internal.BuildFailureWithProblemsProgressDetails;
import org.gradle.api.problems.internal.Problem;
import org.gradle.api.problems.internal.ProblemAwareFailure;
import org.gradle.internal.build.event.BuildEventSubscriptions;
import org.gradle.internal.build.event.types.AbstractOperationResult;
import org.gradle.internal.build.event.types.DefaultBuildBuildDescriptor;
import org.gradle.internal.build.event.types.DefaultFailure;
import org.gradle.internal.build.event.types.DefaultFailureWithProblemResult;
import org.gradle.internal.build.event.types.DefaultOperationFinishedProgressEvent;
import org.gradle.internal.build.event.types.DefaultOperationStartedProgressEvent;
import org.gradle.internal.build.event.types.DefaultProblemDetails;
import org.gradle.internal.build.event.types.DefaultProblemToFailureDescriptor;
import org.gradle.internal.build.event.types.DefaultProblemToFailureDetails;
import org.gradle.internal.build.event.types.DefaultProblemToFailureEvent;
import org.gradle.internal.build.event.types.DefaultSuccessResult;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationIdFactory;
import org.gradle.internal.operations.OperationFinishEvent;
import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.internal.operations.OperationProgressEvent;
import org.gradle.internal.operations.OperationStartEvent;
import org.gradle.launcher.exec.RunBuildBuildOperationType;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.internal.protocol.InternalBasicProblemDetailsVersion3;
import org.gradle.tooling.internal.protocol.InternalFailure;
import org.gradle.tooling.internal.protocol.events.InternalOperationFinishedProgressEvent;
import org.gradle.tooling.internal.protocol.events.InternalOperationStartedProgressEvent;
import org.gradle.tooling.internal.protocol.events.InternalProgressEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@NonNullApi
class RunBuildBuildOperationMapper implements BuildOperationMapper<RunBuildBuildOperationType.Details, DefaultBuildBuildDescriptor> {

    @Nullable
    private final ProblemsProgressEventConsumer problemConsumer;

    private final Supplier<OperationIdentifier> operationIdentifierSupplier;

    public RunBuildBuildOperationMapper(@Nullable ProblemsProgressEventConsumer problemConsumer, BuildOperationIdFactory idFactory) {
        this.problemConsumer = problemConsumer;
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

            Multimap<InternalFailure, InternalBasicProblemDetailsVersion3> problems = ArrayListMultimap.create();
            InternalFailure rootFailure = DefaultFailure.fromThrowable(failure, (throwable, internalFailure) -> {
                problems.putAll(internalFailure, toProblemDetails(problemsForThrowables.get(throwable)));
                if (throwable instanceof ProblemAwareFailure) {
                    problems.putAll(internalFailure, toProblemDetails(((ProblemAwareFailure) throwable).getProblems()));
                }
            });
            HashMap<InternalFailure, Collection<InternalBasicProblemDetailsVersion3>> problemsMap = new HashMap<>();
            // move entries to HashMap manually because MultiMap.asMap().values() is not serializable
            for (Map.Entry<InternalFailure, Collection<InternalBasicProblemDetailsVersion3>> entry :problems.asMap().entrySet()) {
                problemsMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            return new DefaultProblemToFailureEvent(
                new DefaultProblemToFailureDescriptor(
                    operationIdentifierSupplier.get(),
                    descriptor.getId()
                ),
                new DefaultProblemToFailureDetails(
                    Collections.singletonList(rootFailure),
                    problemsMap
                )
            );
        }
        return null;
    }

    @Override
    public InternalOperationFinishedProgressEvent createFinishedEvent(DefaultBuildBuildDescriptor descriptor, RunBuildBuildOperationType.Details details, OperationFinishEvent finishEvent) {
        return new DefaultOperationFinishedProgressEvent(finishEvent.getEndTime(), descriptor, toOperationResultWithProblems(finishEvent));
    }

    private AbstractOperationResult toOperationResultWithProblems(OperationFinishEvent result) {
        Throwable failure = result.getFailure();
        long startTime = result.getStartTime();
        long endTime = result.getEndTime();
        if (failure != null) {
            Multimap<InternalFailure, InternalBasicProblemDetailsVersion3> problems = ArrayListMultimap.create();
            InternalFailure rootFailure = DefaultFailure.fromThrowable(failure, (throwable, internalFailure) -> {
                problems.putAll(internalFailure, toProblemDetails(problemConsumer.getProblemsForThrowable().get(throwable)));
                if (throwable instanceof ProblemAwareFailure) {
                    List<DefaultProblemDetails> details = toProblemDetails(((ProblemAwareFailure) throwable).getProblems());
                    problems.putAll(internalFailure, details);
                }
            });
            HashMap<InternalFailure, Collection<InternalBasicProblemDetailsVersion3>> problemsMap = new HashMap<>(problems.asMap()); // HashMap to make it serializable
            return new DefaultFailureWithProblemResult(startTime, endTime, Collections.singletonList(rootFailure), problemsMap);
        }
        return new DefaultSuccessResult(startTime, endTime);
    }

    private static List<DefaultProblemDetails> toProblemDetails(Collection<Problem> p) {
        if (p == null) {
            return Collections.emptyList();
        }
        return p.stream().filter(Objects::nonNull).map(ProblemsProgressEventConsumer::createDefaultProblemDetails).collect(Collectors.toList());
    }
}
