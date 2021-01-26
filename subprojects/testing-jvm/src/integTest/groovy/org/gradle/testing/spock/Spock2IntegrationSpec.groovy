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

package org.gradle.testing.spock

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class Spock2IntegrationSpec extends AbstractIntegrationSpec {
    private static final String SPOCK_VERSION = "2.0-M4-groovy-2.5"

    def setup() {
        buildScript("""
            plugins {
                id("groovy")
            }

            repositories {
                ${mavenCentralRepository()}
            }
            dependencies {
                testImplementation("org.spockframework:spock-core:$SPOCK_VERSION")
            }
            test {
                useJUnitPlatform()
            }
        """)
    }
}
