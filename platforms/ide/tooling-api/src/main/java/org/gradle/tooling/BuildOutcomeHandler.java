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

package org.gradle.tooling;

import org.gradle.api.Incubating;

import java.util.List;

/**
 * Callback interface for handling the outcome of a build.
 * <p
 * Upon a build failure, the handler may receive more detailed information about the failures that occurred, such as the problems associated with the failures.
 *
 * @see ProblemAwareFailure
 * @since 8.11
 */
@Incubating
public interface BuildOutcomeHandler {

    /**
     * Called when the build finished successfully.
     *
     * @since 8.11
     */
    void onSuccess();

    /**
     * Called when the build failed.
     *
     * @param failures The list of failures that occurred during the build.
     * @since 8.11
     */
    void onFailure(List<Failure> failures);
}
