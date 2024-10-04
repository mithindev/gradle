/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.nativeplatform.fixtures.app

import org.gradle.util.internal.VersionNumber

class Swift5WithSwift4XCTest extends MainWithXCTestSourceElement {
    final Swift5 main
    final XCTestSourceElement test

    Swift5WithSwift4XCTest(String projectName) {
        super(projectName, VersionNumber.version(4))
        this.main = new Swift5(projectName)
        this.test = new XCTestSourceElement(projectName) {
            @Override
            List<XCTestSourceFileElement> getTestSuites() {
                return [new Swift4Test().withImport(main.moduleName)]
            }
        }
    }
}
