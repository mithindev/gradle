/*
 * Copyright 2022 the original author or authors.
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

package org.gradle.util;

/**
 * Optional utility-oriented base-class for precondition classes.
 */
public abstract class BaseTestPreconditions {

    public static final class True implements TestPrecondition {
        @Override
        public boolean isSatisfied() {
            return true;
        }
    }

    public static final class False implements TestPrecondition {
        @Override
        public boolean isSatisfied() {
            return false;
        }
    }

    public static final class Not implements TestPrecondition {

        private final Class<? extends TestPrecondition> basePrecondition;

        public Not(Class<? extends TestPrecondition> basePrecondition) {
            this.basePrecondition = basePrecondition;
        }

        @Override
        public boolean isSatisfied() throws Exception {
            return TestPrecondition.doSatisfies(basePrecondition);
        }
    }

}