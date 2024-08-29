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

import com.google.common.collect.ImmutableMap;
import org.gradle.tooling.events.problems.ProblemReport;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Thrown when there is some problem using a Gradle connection.
 *
 * @since 1.0-milestone-3
 */
public class GradleConnectionException extends RuntimeException {
    private final Map<Failure, List<ProblemReport>> problemReports;

    public GradleConnectionException(String message) {
        super(message);
        this.problemReports = Collections.emptyMap();
    }

    public GradleConnectionException(String message, Throwable throwable) {
        super(message, throwable);
        this.problemReports = Collections.emptyMap();
    }

    public GradleConnectionException(String message, Throwable throwable, Map<Failure, List<ProblemReport>> problems) {
        super(message, throwable);
        this.problemReports = ImmutableMap.copyOf(problems);
    }

    public Map<Failure, List<ProblemReport>> getProblemReports() {
        return problemReports;
    }
}
