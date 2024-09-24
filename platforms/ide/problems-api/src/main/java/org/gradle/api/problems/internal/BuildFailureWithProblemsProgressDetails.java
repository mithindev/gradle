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

package org.gradle.api.problems.internal;

import java.util.Collection;
import java.util.Map;

/*
 * Details object exposing a build failure and its associated problem reports.
 */
public interface BuildFailureWithProblemsProgressDetails {

    /**
     * Returns a map of exceptions emitted via the Problems API and their associated problem reports.
     */
    Map<Throwable, Collection<Problem>> getProblemsForThrowables();

    /**
     * Returns the exception representing the build failure. Clients are expected to traverse the cause chain of the returned exception and use the {@link #getProblemsForThrowables()} method to obtain
     * problem reports associated with the exceptions in the chain.
     *
     * @return The build failure exception
     */
    Throwable getBuildFailure();
}
