/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.integtests.tooling.r811

import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.tooling.BuildException
import org.gradle.tooling.BuildFailureHandler
import org.gradle.tooling.Failure
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.ResultHandler
import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.problems.ProblemReport
import org.gradle.tooling.events.problems.ProblemToFailureEvent

@ToolingApiVersion('>=8.11')
@TargetGradleVersion('>=8.11')
class ProblemWithBuildFailureBuildLauncherCrossVersionSpec extends ToolingApiSpecification {

    TestBuildFailedHandler failureHandler

    def setup() {
        this.failureHandler = new TestBuildFailedHandler()
    }

    @TargetGradleVersion('<8.11')
    def "clients won't receive problems associated to build failures if they are not subscribed to problems"() {
        given:
        buildFile << """
            plugins {
                id 'java'
            }
        """
        def resultHandler = new FailureCollectingResultHandler()

        when:
        withConnection { connection ->
            connection.newBuild()
                .forTasks("c")
                .withBuildFailureHandler(failureHandler)
                .run(resultHandler)
        }

        then:
        failureHandler.success == false
        failureHandler.failure == null
        failureHandler.problemReports == null
    }


    def "clients receive single problem report associated with build failure"() {
        given:
        settingsFile << """
            rootProject.name = 'root'
        """
        buildFile << """
            plugins {
                id 'java'
            }
        """

        when:
        withConnection { connection ->
            connection.newBuild()
                .forTasks("c")
                .withBuildFailureHandler(failureHandler)
                .run()
        }

        then:
        thrown(BuildException)
        failureHandler.success == false
        //failureHandler.failure != null // TODO (donat) maybe we should association the event to root build operation
        failureHandler.problemReports.size() == 1
        failureHandler.problemReports.entrySet().iterator().next().key.message == 'Task \'c\' is ambiguous in root project \'root\'. Candidates are: \'check\', \'classes\', \'clean\', \'components\'.'
        failureHandler.problemReports.entrySet().iterator().next().value[0].definition.id.displayName == 'Ambiguous matches'
    }

    class FailureCollectingResultHandler implements ResultHandler<Void> {

        GradleConnectionException failure

        @Override
        void onComplete(Void result) {}

        @Override
        void onFailure(GradleConnectionException failure) {
            this.failure = failure
        }
    }

    class ProblemProgressListener implements ProgressListener {

        ProblemToFailureEvent event

        @Override
        void statusChanged(ProgressEvent event) {
            if (event instanceof ProblemToFailureEvent) {
                this.event = event
            }
        }
    }

    class TestBuildFailedHandler implements BuildFailureHandler {

        boolean success = false
        Failure failure = null
        Map<Failure, Collection<ProblemReport>> problemReports = null

        @Override
        void onSuccess() {
            success = true
        }

        @Override
        void onFailure(Failure failure, Map<Failure, Collection<ProblemReport>> problemReports) {
            this.failure = failure
            this.problemReports = problemReports
        }
    }
}
