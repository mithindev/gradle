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

import org.gradle.tooling.events.problems.ProblemReport;

import java.util.List;

/**
 * Thrown when a Gradle build fails or when a model cannot be built.
 *
 * @since 1.0-milestone-3
 */
public class BuildException extends GradleConnectionException {
    private final List<? extends Failure> failures;
    private final List<? extends ProblemReport> problems;

    public BuildException(String message, Throwable throwable) {
        super(message, throwable);
        failures = null;
        problems = null;
    }

    public BuildException(String message, Throwable throwable, List<? extends Failure> failures, List<? extends ProblemReport> problems) {
        super(message, throwable);
        this.failures = failures;
        this.problems = problems;
    }

    public List<? extends Failure> getFailures() {
        return failures;
    }

    public List<? extends ProblemReport> getProblems() {
        return problems;
    }
}
