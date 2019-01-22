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

package org.gradle.api.publish.internal.validation;

import com.google.common.collect.Sets;
import org.gradle.api.logging.Logger;
import org.gradle.internal.DisplayName;
import org.gradle.internal.logging.text.TreeFormatter;

import java.util.Set;

public class PublicationWarningsCollector {

    private final Logger logger;
    private final String incompatibleFeature;
    Set<String> unsupportedUsages = null;

    public PublicationWarningsCollector(Logger logger, String incompatibleFeature) {
        this.logger = logger;
        this.incompatibleFeature = incompatibleFeature;
    }

    public void add(String text) {
        if (unsupportedUsages == null) {
            unsupportedUsages = Sets.newHashSet();
        }
        unsupportedUsages.add(text);
    }

    public void complete(DisplayName displayName) {
        if (unsupportedUsages != null) {
            TreeFormatter formatter = new TreeFormatter();
            formatter.node(displayName + incompatibleFeature);
            formatter.startChildren();
            for (String usage : unsupportedUsages) {
                formatter.node(usage);
            }
            formatter.endChildren();
            logger.lifecycle(formatter.toString());
        }

    }
}
