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
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.ResultHandler
import org.gradle.tooling.events.OperationType
import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.problems.ProblemEvent
import spock.lang.IgnoreRest

@ToolingApiVersion('>=8.11')
@TargetGradleVersion('>=8.11')
class ProblemWithBuildFailureBuildLauncherCrossVersionSpec extends ToolingApiSpecification {

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
                .run(resultHandler)
        }

        then:
        (resultHandler.failure as GradleConnectionException).problemReports.size() == 0
    }

    @IgnoreRest
    def "clients receive single problem report associated with build failure"() {
        // TODO (donat) This is clunky. Clients may want to get all problems and the ones associated with build failures.
        // Reporting the latter one should not require a seemingly random listener registration.
        given:
        settingsFile << """
            rootProject.name = 'root'
        """
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
                .addProgressListener(new ProblemProgressListener(), OperationType.PROBLEMS)
                .run(resultHandler)
        }
        def reports = (resultHandler.failure as GradleConnectionException).problemReports

        then:
        reports.size() == 1
        reports.entrySet().iterator().next().key.message == 'Task \'c\' is ambiguous in root project \'root\'. Candidates are: \'check\', \'classes\', \'clean\', \'components\'.'
        reports.entrySet().iterator().next().value[0].definition.id.displayName == 'Ambiguous matches'
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

        @Override
        void statusChanged(ProgressEvent event) {
            if (event instanceof ProblemEvent) {
            }
        }
    }
}
