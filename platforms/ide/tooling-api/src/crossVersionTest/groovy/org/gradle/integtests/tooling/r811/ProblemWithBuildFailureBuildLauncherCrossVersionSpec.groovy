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
import org.gradle.tooling.BuildOutcomeHandler
import org.gradle.tooling.Failure
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.ProblemAwareFailure
import org.gradle.tooling.ResultHandler

@ToolingApiVersion('>=8.11')
@TargetGradleVersion('>=8.11')
class ProblemWithBuildFailureBuildLauncherCrossVersionSpec extends ToolingApiSpecification {

    TestBuildFailedHandler failureHandler

    def setup() {
        this.failureHandler = new TestBuildFailedHandler()
    }

    // proposed test coverage:
    // client notified when build with old gradle version succeeds
    // client notified when build with old gradle version fails
    // client notified when build with new gradle version succeeds
    // client notified when build with new gradle version fails with single build failure
    // client notified when build with new gradle version fails with multiple build failure

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
                .withBuildOutcomeHandler(failureHandler)
                .run()
        }

        then:
        thrown(BuildException)
        failureHandler.success == false
        failureHandler.failures == null
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
                .withBuildOutcomeHandler(failureHandler)
                .run()
        }

        then:
        thrown(BuildException)
        failureHandler.success == false
        failureHandler.failures.size() == 1
        failureHandler.failures[0] instanceof Failure  && !(failureHandler.failures[0] instanceof ProblemAwareFailure)
        (failureHandler.failures[0].causes[0] as ProblemAwareFailure).problems[0].contextualLabel.contextualLabel == 'Task \'c\' is ambiguous in root project \'root\'. Candidates are: \'check\', \'classes\', \'clean\', \'components\'.'
        (failureHandler.failures[0].causes[0] as ProblemAwareFailure).problems[0].definition.id.displayName == 'Ambiguous matches'
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

    class TestBuildFailedHandler implements BuildOutcomeHandler {

        boolean success = false
        List<Failure> failures = null

        @Override
        void onSuccess() {
            success = true
        }

        @Override
        void onFailure(List<Failure> failures) {
            this.failures = failures
        }
    }
}
