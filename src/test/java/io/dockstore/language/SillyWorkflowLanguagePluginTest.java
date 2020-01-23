package io.dockstore.language;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class SillyWorkflowLanguagePluginTest {

    @Test
    public void testWorkflowParsing() {
        SillyWorkflowLanguagePlugin.SillyWorkflowLanguagePluginImpl plugin = new SillyWorkflowLanguagePlugin.SillyWorkflowLanguagePluginImpl();
        HttpFileReader reader = new HttpFileReader();
        final Map<String, Pair<String, MinimalLanguageInterface.GenericFileType>> fileMap = plugin
            .indexWorkflowFiles("/Dockstore.swl", reader.readFile("Dockstore.swl"), reader);
        Assert.assertEquals(1, fileMap.size());
        Assert.assertTrue(fileMap.containsKey("foo.swl"));
    }

    public class HttpFileReader implements MinimalLanguageInterface.FileReader {
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
}
