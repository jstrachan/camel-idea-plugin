/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.idea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import org.apache.camel.idea.model.ComponentModel;
import org.apache.camel.idea.model.EndpointOptionModel;

/**
 * Smart completion for editing a Camel endpoint uri, to show a list of possible endpoint options which can be added.
 * For example editing <tt>jms:queue?_CURSOR_HERE_</tt>. Which presents the user
 * with a list of possible options for the JMS endpoint.
 */
public class CamelSmartCompletionEndpointOptions {

    public static List<LookupElement> addSmartCompletionSuggestionsQueryParameters(String val, ComponentModel component, Map<String, String> existing) {
        List<LookupElement> answer = new ArrayList<>();

        for (EndpointOptionModel option : component.getEndpointOptions()) {

            if ("parameter".equals(option.getKind())) {
                String name = option.getName();
                // only add if not already used (or if the option is multi valued then it can have many)
                String old = existing != null ? existing.get(name) : "";
                if ("true".equals(option.getMultiValue()) || existing == null || old == null || old.isEmpty()) {

                    // no tail for prefix, otherwise use = to setup for value
                    String tail = option.getPrefix().isEmpty() ? "=" : "";
                    String key = option.getPrefix().isEmpty() ? name : option.getPrefix();

                    // the lookup should prepare for the new option
                    String lookup;
                    if (!val.contains("?")) {
                        // none existing options so we need to start with a ? mark
                        lookup = val + "?" + key + tail;
                    } else {
                        if (!val.endsWith("&") && !val.endsWith("?")) {
                            lookup = val + "&" + key + tail;
                        } else {
                            // there is already either an ending ? or &
                            lookup = val + key + tail;
                        }
                    }
                    LookupElementBuilder builder = LookupElementBuilder.create(lookup);
                    // only show the option in the UI
                    builder = builder.withPresentableText(name);
                    // we don't want to highlight the advanced options which should be more seldom in use
                    boolean advanced = option.getGroup().contains("advanced");
                    builder = builder.withBoldness(!advanced);
                    if (!option.getJavaType().isEmpty()) {
                        builder = builder.withTypeText(option.getJavaType(), true);
                    }
                    if ("true".equals(option.getDeprecated())) {
                        // mark as deprecated
                        builder = builder.withStrikeoutness(true);
                    }
                    // add icons for various options
                    if ("true".equals(option.getRequired())) {
                        builder = builder.withIcon(AllIcons.Toolwindows.ToolWindowFavorites);
                    } else if ("true".equals(option.getSecret())) {
                        builder = builder.withIcon(AllIcons.Nodes.SecurityRole);
                    } else if ("true".equals(option.getMultiValue())) {
                        builder = builder.withIcon(AllIcons.Nodes.ExpandNode);
                    } else if (!option.getEnums().isEmpty()) {
                        builder = builder.withIcon(AllIcons.Nodes.Enum);
                    } else if ("object".equals(option.getType())) {
                        builder = builder.withIcon(AllIcons.Nodes.Class);
                    }

                    // TODO: we could nice with an icon for producer vs consumer etc
                    answer.add(builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE));
                }
            }
        }

        return answer;
    }

    public static List<LookupElement> addSmartCompletionSuggestionsContextPath(String val, ComponentModel component, Map<String, String> existing) {
        List<LookupElement> answer = new ArrayList<>();

        // show the syntax as the only choice for now
        LookupElementBuilder builder = LookupElementBuilder.create(val);
        builder = builder.withIcon(AllIcons.Toolwindows.ToolWindowFavorites);
        builder = builder.withBoldness(true);
        builder = builder.withPresentableText(component.getSyntax());

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
        answer.add(element);

        return answer;
    }

    @Deprecated
    public static List<LookupElement> oldAddSmartCompletionSuggestionsContextPath(String val, ComponentModel component, Map<String, String> existing) {
        List<LookupElement> answer = new ArrayList<>();

        double priority = 10.0d;

        for (EndpointOptionModel option : component.getEndpointOptions()) {

            if ("path".equals(option.getKind())) {
                String name = option.getName();
                // only add if not already used
                String old = existing != null ? existing.get(name) : "";
                if (existing == null || old == null || old.isEmpty()) {

                    String tail = "";
                    String key = name;
                    String lookup = val + key + tail;

                    LookupElementBuilder builder = LookupElementBuilder.create(lookup);
                    // only show the option in the UI
                    builder = builder.withPresentableText(name);
                    if (!option.getJavaType().isEmpty()) {
                        builder = builder.withTypeText(option.getJavaType(), true);
                    }
                    if ("true".equals(option.getDeprecated())) {
                        // mark as deprecated
                        builder = builder.withStrikeoutness(true);
                    }
                    // add icons for various options
                    if ("true".equals(option.getRequired())) {
                        // for required then make it stand out
                        builder = builder.withIcon(AllIcons.Toolwindows.ToolWindowFavorites);
                        builder = builder.withBoldness(true);
                    } else if ("true".equals(option.getSecret())) {
                        builder = builder.withIcon(AllIcons.Nodes.SecurityRole);
                    } else if (!option.getEnums().isEmpty()) {
                        builder = builder.withIcon(AllIcons.Nodes.Enum);
                    } else if ("object".equals(option.getType())) {
                        builder = builder.withIcon(AllIcons.Nodes.Class);
                    }

                    // we should not offer auto completion as the user should enter some value
                    // the lookup is just a hint what the option/syntax is
                    LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);

                    // they should be in the exact order
                    element = PrioritizedLookupElement.withPriority(element, priority);

                    priority -= 1.0d;

                    answer.add(element);
                }
            }
        }

        return answer;
    }


}
