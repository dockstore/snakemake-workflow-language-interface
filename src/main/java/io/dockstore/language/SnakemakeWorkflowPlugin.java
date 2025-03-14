/*
 *    Copyright 2019 OICR
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.dockstore.language;

import com.google.common.collect.Sets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import io.dockstore.common.DescriptorLanguage;
import io.dockstore.common.VersionTypeValidation;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dyuen
 */
public class SnakemakeWorkflowPlugin extends Plugin {

    public static final Logger LOG = LoggerFactory.getLogger(SnakemakeWorkflowPlugin.class);


    /**
     * Constructor to be used by plugin manager for plugin instantiation. Your plugins have to provide
     * constructor with this exact signature to be successfully loaded by manager.
     *
     * @param wrapper
     */
    public SnakemakeWorkflowPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class SnakemakeWorkflowPluginImpl implements RecommendedLanguageInterface {

        @Override
        public String launchInstructions(String trsID) {
            return null;
        }


        @Override
        public VersionTypeValidation validateWorkflowSet(String initialPath, String contents, Map<String, FileMetadata> indexedFiles) {
            VersionTypeValidation validation = new VersionTypeValidation(true, new HashMap<>());
            for (String line : contents.split("\\r?\\n")) {
                if (!line.startsWith("import") && !line.startsWith("author") && !line.startsWith("description")) {
                    validation.setValid(false);
                    validation.getMessage().put(initialPath, "unknown keyword");
                }
            }
            return validation;
        }

        @Override
        public VersionTypeValidation validateTestParameterSet(Map<String, FileMetadata> indexedFiles) {
            return new VersionTypeValidation(true, new HashMap<>());
        }


        @Override
        public DescriptorLanguage getDescriptorLanguage() {
            return DescriptorLanguage.SMK;
        }

        @Override
        public Pattern initialPathPattern() {
            return Pattern.compile("/.*\\.swl");
        }

        @Override
        public Map<String, FileMetadata> indexWorkflowFiles(String initialPath, String contents, FileReader reader) {
            Map<String, FileMetadata> results = new HashMap<>();
            for (String line : contents.split("\\r?\\n")) {
                if (line.startsWith("import")) {
                    final String[] s = line.split(":");
                    final String importedFile = reader.readFile(s[1].trim());
                    // use real language version
                    results.put(s[1].trim(), new FileMetadata(importedFile, GenericFileType.IMPORTED_DESCRIPTOR, "1.0"));
                }
            }
            results.put(
                initialPath,
                //TODO: get real snakemake version number
                new FileMetadata(contents, GenericFileType.IMPORTED_DESCRIPTOR, "1.0"));

            List<String> ruleFiles = findRuleFiles(initialPath, reader);
            for(String rule : ruleFiles) {
                results.put(rule,  new FileMetadata(rule, GenericFileType.IMPORTED_DESCRIPTOR, null));
            }

            return results;
        }

        protected List<String> findRuleFiles(final String initialPath, final FileReader reader) {

            final int extensionPos = initialPath.lastIndexOf("/");
            final String base = initialPath.substring(0, extensionPos);

            final Path rules = Paths.get(base, "rules");
            // listing files is more rate limit friendly (e.g. GitHub counts each 404 "miss" as an API
            // call,
            // but listing a directory can be free if previously requested/cached)
            final Set<String> filenameset = Sets.newHashSet(reader.listFiles(rules.toString()));

            return filenameset.stream().map(s -> "rules/" + s).toList();
        }

        @Override
        public WorkflowMetadata parseWorkflowForMetadata(String initialPath, String contents, Map<String, FileMetadata> indexedFiles) {
            WorkflowMetadata metadata = new WorkflowMetadata();
            for (String line : contents.split("\\r?\\n")) {
                if (line.startsWith("author")) {
                    final String[] s = line.split(":");
                    metadata.setAuthor(s[1].trim());
                }
                if (line.startsWith("description")) {
                    final String[] s = line.split(":");
                    metadata.setDescription(s[1].trim());
                }
            }
            return metadata;
        }
    }
}

