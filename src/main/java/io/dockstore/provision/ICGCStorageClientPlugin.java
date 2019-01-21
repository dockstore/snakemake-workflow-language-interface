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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
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

        private static final String DCC_CLIENT_KEY = "client-key";

        private Map<String, String> config;

        public void setConfiguration(Map<String, String> map) {
            this.config = map;
        }

        public Set<String> schemesHandled() {
            return new HashSet<>(Lists.newArrayList("icgc"));
        }

        public boolean downloadFrom(String sourcePath, Path destination) {
            String clientKey = getKey();

            // ambiguous how to reference synapse files, rip off these kinds of headers
            String prefix = "icgc://";
            if (sourcePath.startsWith(prefix)){
                sourcePath = sourcePath.substring(prefix.length());
            } else {
                System.err.println("Scheme not handled by this plugin.");
                return false;
            }

            try {
                Files.createDirectories(destination);
            } catch (IOException e) {
                System.err.println("Could not create destination directory: " + destination.toFile().getAbsolutePath());
                return false;
            }
            String command = "docker run -e ACCESSTOKEN=" + clientKey + " --mount type=bind,source=" + destination.toFile().getAbsolutePath() + ",target=/data overture/score bin/score-client --quiet download --object-id " + sourcePath + " --output-dir /data";
            try {
                runCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public boolean uploadTo(String destPath, Path sourceFile, Optional<String> metadata) {
            throw new UnsupportedOperationException("ICGC storage client upload not implemented yet");
        }

        private void runCommand(String command) throws IOException, InterruptedException {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command.split("\\s+"));
            builder.directory(new File(System.getProperty("user.home")));
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Could not execute the command: " + command);
                System.exit(1);
            }
        }

        /**
         * Get the ICGC DCC key
         * @return  The ICGC DCC key
         */
        private String getKey() {
            if (config.containsKey(DCC_CLIENT_KEY)) {
                return config.get(DCC_CLIENT_KEY);
            } else {
                System.err.println("Need an access token specified in the [dockstore-file-icgc-storage-client-plugin] section of the ~/.dockstore/config file");
                System.err.println("Should look like \"client-key = <YOUR ACCESS TOKEN>\"");
                System.exit(1);
                return null;
            }
        }

    }

}

