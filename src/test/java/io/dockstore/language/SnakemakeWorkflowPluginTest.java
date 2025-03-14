package io.dockstore.language;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class SnakemakeWorkflowPluginTest {

    @Test
    public void testWorkflowParsingHelloWorld() {
        final SnakemakeWorkflowPlugin.SnakemakeWorkflowPluginImpl plugin =
            new SnakemakeWorkflowPlugin.SnakemakeWorkflowPluginImpl();
        final ResourceFileReader reader = new ResourceFileReader("nathanhaigh/snakemake-hello-world");
        final String initialPath = "/Snakefile";
        final String contents = reader.readFile(initialPath);
        final Map<String, MinimalLanguageInterface.FileMetadata> fileMap =
            plugin.indexWorkflowFiles(initialPath, contents, reader);

        assertFalse(fileMap.isEmpty());
    }

    @Test
    public void testWorkflowParsingComplex() {
        final SnakemakeWorkflowPlugin.SnakemakeWorkflowPluginImpl plugin =
            new SnakemakeWorkflowPlugin.SnakemakeWorkflowPluginImpl();
        final ResourceFileReader reader = new ResourceFileReader("snakemake-workflows/rna-seq-star-deseq2");
        final String initialPath = "workflow/Snakefile";
        final String contents = reader.readFile(initialPath);
        final Map<String, MinimalLanguageInterface.FileMetadata> fileMap =
            plugin.indexWorkflowFiles(initialPath, contents, reader);

        assertFalse(fileMap.isEmpty());
        assertTrue(fileMap.containsKey("rules/align.smk"));
    }

    public static class HttpFileReader implements MinimalLanguageInterface.FileReader {
        @Override
        public String readFile(String path) {
            try {
                return Resources.toString(new URL("https://raw.githubusercontent.com/dockstore-testing/silly-example/master/" + path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<String> listFiles(String pathToDirectory) {
            return Lists.newArrayList("Dockstore.cwl", "README.md", "foo.swl");
        }
    }

    abstract static class URLFileReader implements MinimalLanguageInterface.FileReader {
        // URL to repo
        protected final String repo;
        // extracted ID
        protected final Optional<String> id;

        URLFileReader(final String repo) {
            this.repo = repo;
            final String[] split = repo.split("/");
            if (split.length >= 2) {
                id = Optional.of(split[split.length - 2] + "/" + split[split.length - 1]);
            } else {
                id = Optional.empty();
            }
        }

        protected abstract URL getUrl(final String path) throws IOException;

        @Override
        public String readFile(String path) {
            try {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                URL url = this.getUrl(path);
                return Resources.toString(url, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<String> listFiles(String pathToDirectory) {
            if (id.isEmpty()) {
                return new ArrayList<>();
            }
            Gson gson = new GsonBuilder().create();
            try {
                // TODO can grab actual directory listing to avoid using listing.json files
                final String fileContent =
                    FileUtils.readFileToString(
                        new File("src/test/resources/io/dockstore/language/" + this.id.get() + "/" + pathToDirectory + "/listing.json"),
                        StandardCharsets.UTF_8);
                return gson.fromJson(
                    fileContent, TypeToken.getParameterized(List.class, String.class).getType());
            } catch (IOException e) {
                return Lists.newArrayList();
            }
        }
    }

    static class ResourceFileReader extends URLFileReader {

        ResourceFileReader(final String repo) {
            super(repo);
        }

        @Override
        protected URL getUrl(String path) throws IOException {
            final String classPath = this.repo + "/" + path;
            final URL url = SnakemakeWorkflowPluginTest.class.getResource(classPath);
            if (url == null) {
                throw new IOException("No such file " + classPath);
            }
            return url;
        }
    }
}
