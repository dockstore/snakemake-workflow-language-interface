/*
 *    Copyright 2016 OICR
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
package io.dockstore.provision;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import io.dockstore.common.Utilities;
import org.apache.commons.lang3.StringUtils;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.RuntimeMode;

/**
 * @author dyuen
 */
public class ICGCStorageClientPlugin extends Plugin {

    public ICGCStorageClientPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            System.out.println(StringUtils.upperCase("ICGCStorageClientPlugin development mode"));
        }
    }

    @Override
    public void stop() {
        System.out.println("ICGCStorageClientPlugin.stop()");
    }

    @Extension
    public static class ICGCStorageClientProvision implements ProvisionInterface {

        private static final String DCC_CLIENT_KEY = "client";

        private Map<String, String> config;

        public void setConfiguration(Map<String, String> map) {
            this.config = map;
        }

        public Set<String> schemesHandled() {
            return new HashSet<>(Lists.newArrayList("icgc"));
        }

        public boolean downloadFrom(String sourcePath, Path destination) {
            String client = "/icgc/dcc-storage/bin/dcc-storage-client";
            if (client.contains(DCC_CLIENT_KEY)) {
                client = config.get(DCC_CLIENT_KEY);
            }

            URI objectIdentifier = URI.create(sourcePath);    // throws IllegalArgumentException if it isn't a valid URI
            String objectId = objectIdentifier.getSchemeSpecificPart().toLowerCase();

            // default layout saves to original_file_name/object_id
            // file name is the directory and object id is actual file name
            String downloadDir = destination.getParent().toFile().getAbsolutePath();
            String bob = client + " --quiet" + " download" + " --object-id " + objectId + " --output-dir " + downloadDir + " --output-layout id";
            Utilities.executeCommand(bob);

            // downloaded file
            String downloadPath = new File(downloadDir).getAbsolutePath() + "/" + objectId;
            System.out.println("download path: " + downloadPath);
            Path downloadedFileFileObj = Paths.get(downloadPath);
            try {
                Files.move(downloadedFileFileObj, destination);
                return true;
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                throw new RuntimeException("Could not move input file: ", ioe);
            }
        }

        public boolean uploadTo(String destPath, Path sourceFile, String metadata) {
            throw new UnsupportedOperationException("ICGC storage client upload not implemented yet");
        }

    }

}

