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

package org.gradle.internal.instrumentation.model;

import org.objectweb.asm.Type;

import java.util.List;

public class CallInterceptionRequestImpl implements CallInterceptionRequest {
    private final CallableInfo interceptedCallable;
    private final ImplementationInfo implementationInfo;
    private final RequestExtrasContainer requestExtras;

    public CallInterceptionRequestImpl(
        CallableInfo interceptedCallable,
        ImplementationInfo implementationInfo,
        List<RequestExtra> requestExtras
    ) {
        this.interceptedCallable = interceptedCallable;
        this.implementationInfo = implementationInfo;

        this.requestExtras = new RequestExtrasContainer();
        requestExtras.forEach(this.requestExtras::add);
    }

    @Override
    public CallableInfo getInterceptedCallable() {
        return interceptedCallable;
    }

    @Override
    public ImplementationInfo getImplementationInfo() {
        return implementationInfo;
    }

    @Override
    public RequestExtrasContainer getRequestExtras() {
        return requestExtras;
    }
}
