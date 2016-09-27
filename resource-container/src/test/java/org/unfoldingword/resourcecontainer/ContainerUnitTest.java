package org.unfoldingword.resourcecontainer;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
public class ContainerUnitTest {
    @Rule
    public TemporaryFolder resourceDir = new TemporaryFolder();

    @Test
    public void loadContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("open-en_tit_ulb");
        File containerDir = new File(resource.getPath());

        ResourceContainer container = ResourceContainer.load(containerDir);
        assertNotNull(container);
        assertEquals(container.info.getString("package_version"), ResourceContainer.version);
        assertNotNull(container.toc);
        assertNotNull(container.config);
    }
    @Test
    public void closeResourceContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("open-en_tit_ulb");
        File archive = ResourceContainer.close(new File(resource.getPath()));
        assertTrue(archive.exists());
    }
    @Test
    public void openResourceContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("closed-en_tit_ulb.tsrc");
        File archivePath = new File(resource.getPath());
        File dir = new File(archivePath.getParentFile(), "closed-en_tit_ulb");

        ResourceContainer container = ResourceContainer.open(archivePath, dir);
        assertNotNull(container);
        assertTrue(dir.exists());
        assertNotNull(container.toc);
        assertNotNull(container.config);
        assertEquals(container.info.getString("package_version"), ResourceContainer.version);
    }
    @Test
    public void inspectClosedContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("closed-en_tit_ulb.tsrc");
        File archivePath = new File(resource.getPath());

        JSONObject json = ContainerTools.inspect(archivePath);
        assertNotNull(json);
        assertEquals(json.getString("package_version"), ResourceContainer.version);
    }
    @Test
    public void inspectOpenedContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("open-en_tit_ulb");
        File containerDir = new File(resource.getPath());

        JSONObject json = ContainerTools.inspect(containerDir);
        assertNotNull(json);
        assertEquals(json.getString("package_version"), ResourceContainer.version);
    }

    @Test
    public void convertResource() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource("raw_source.json");
        File sourceFile = new File(url.getPath());
        String data = FileUtil.readFileToString(sourceFile);

        JSONObject project = new JSONObject();
        project.put("slug", "gen");
        project.put("name", "Genesis");
        project.put("desc", "");
        project.put("icon", "");
        project.put("sort", 0);
        JSONObject language = new JSONObject();
        language.put("slug", "en");
        language.put("name", "English");
        language.put("dir", "ltr");
        JSONObject resource = new JSONObject();
        resource.put("slug", "ulb");
        resource.put("name", "Unlocked Literal Bible");
        resource.put("type", "book");
        resource.put("status", new JSONObject());
        resource.getJSONObject("status").put("translate_mode", "all");
        resource.getJSONObject("status").put("checking_level", "3");
        resource.getJSONObject("status").put("license", "");

        JSONObject json = new JSONObject();
        json.put("project", project);
        json.put("language", language);
        json.put("resource", resource);
        json.put("modified_at", 0);
        ResourceContainer container = ContainerTools.convertResource(data, new File(resourceDir.getRoot(), "en_gen_ulb"), json);
        assertNotNull(container);
    }

    @Test
    public void convertTAResource() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource("raw_ta.json");
        File sourceFile = new File(url.getPath());
        String data = FileUtil.readFileToString(sourceFile);

        JSONObject project = new JSONObject();
        project.put("slug", "ta-translate");
        project.put("name", "Translate Manual");
        project.put("desc", "");
        project.put("icon", "");
        project.put("sort", 0);
        JSONObject language = new JSONObject();
        language.put("slug", "en");
        language.put("name", "English");
        language.put("dir", "ltr");
        JSONObject resource = new JSONObject();
        resource.put("slug", "vol1");
        resource.put("name", "Volume 1");
        resource.put("type", "man");
        resource.put("status", new JSONObject());
        resource.getJSONObject("status").put("translate_mode", "gl");
        resource.getJSONObject("status").put("checking_level", "3");
        resource.getJSONObject("status").put("license", "");

        JSONObject json = new JSONObject();
        json.put("project", project);
        json.put("language", language);
        json.put("resource", resource);
        json.put("modified_at", 0);
        ResourceContainer container = ContainerTools.convertResource(data, new File(resourceDir.getRoot(), "en_ta-translate_vol1"), json);
        assertNotNull(container);
    }

    @Test
    public void semverComparison() throws Exception {
        final int EQUAL = 0;
        final int GREATER_THAN = 1;
        final int LESS_THAN = -1;

        assertEquals(EQUAL, Semver.compare("10.0.1", "10.0.1"));
        assertEquals(EQUAL, Semver.compare("10.0", "10.0.0"));
        assertEquals(EQUAL, Semver.compare("10.*", "10.0.0"));
        assertEquals(EQUAL, Semver.compare("10.*", "10.9.0"));
        assertEquals(EQUAL, Semver.compare("10.0.0", "10.0-alpha.0"));
        assertEquals(EQUAL, Semver.compare("10.0.0", "v10.0.0"));
        assertEquals(EQUAL, Semver.compare("10.*.1", "10.9.1"));
        assertEquals(EQUAL, Semver.compare("0.8.1", "0.8.1"));

        assertEquals(GREATER_THAN, Semver.compare("10.0.0", "1.0.0"));
        assertEquals(GREATER_THAN, Semver.compare("10.1.0", "10.0.0"));
        assertEquals(GREATER_THAN, Semver.compare("10", "9.9.0"));
        assertEquals(GREATER_THAN, Semver.compare("10.1-alpha.0", "10.0.0"));
        assertEquals(GREATER_THAN, Semver.compare("10.9.6", "10.*.1"));
        assertEquals(GREATER_THAN, Semver.compare("0.9.6", "0.9.1"));
        assertEquals(GREATER_THAN, Semver.compare("0.10.0", "0.9.*"));

        assertEquals(LESS_THAN, Semver.compare("1.0.0", "10.0.0"));
        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1.0"));
        assertEquals(LESS_THAN, Semver.compare("9.9.0", "10"));
        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1-alpha.0"));
        assertEquals(LESS_THAN, Semver.compare("10.*.1", "10.9.6"));
        assertEquals(LESS_THAN, Semver.compare("0.9.1", "0.9.6"));
        assertEquals(LESS_THAN, Semver.compare("0.9.*", "0.10.0"));
    }
}