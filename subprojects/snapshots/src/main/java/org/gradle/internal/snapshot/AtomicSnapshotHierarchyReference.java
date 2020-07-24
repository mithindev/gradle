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

package org.gradle.internal.snapshot;

import java.util.concurrent.locks.ReentrantLock;

public class AtomicSnapshotHierarchyReference {
    private volatile SnapshotHierarchy root;
    private final SnapshotHierarchy.DiffCapturingUpdateFunctionDecorator updateFunctionDecorator;
    private final ReentrantLock updateLock = new ReentrantLock();

    public AtomicSnapshotHierarchyReference(SnapshotHierarchy root, SnapshotHierarchy.DiffCapturingUpdateFunctionDecorator updateFunctionDecorator) {
        this.root = root;
        this.updateFunctionDecorator = updateFunctionDecorator;
    }

    public SnapshotHierarchy get() {
        return root;
    }

    public void update(SnapshotHierarchy.DiffCapturingUpdateFunction updateFunction) {
        updateLock.lock();
        try {
            // Store the current root in a local variable to make the call atomic
            SnapshotHierarchy currentRoot = root;
            root = updateFunctionDecorator.decorate(updateFunction).updateRoot(currentRoot);
        } finally {
            updateLock.unlock();
        }
    }

    public interface UpdateFunction {
        SnapshotHierarchy updateRoot(SnapshotHierarchy root);
    }
}
