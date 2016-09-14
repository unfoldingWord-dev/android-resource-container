package org.unfoldingword.resourcecontainer;

import org.json.JSONObject;
import org.junit.Test;
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
        URL resource = classLoader.getResource("closed-en_tit_ulb.ts");
        File archivePath = new File(resource.getPath());
        File dir = new File(archivePath.getParentFile(), "closed-en_tit_ulb");

        ResourceContainer container = ResourceContainer.open(archivePath, dir);
        assertNotNull(container);
        assertTrue(dir.exists());
        assertEquals(container.info.getInt("package_version"), ContainerSpecification.version);
    }
    @Test
    public void inspectClosedContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("closed-en_tit_ulb.ts");
        File archivePath = new File(resource.getPath());

        JSONObject json = ContainerTools.inspect(archivePath);
        assertNotNull(json);
        assertEquals(json.getInt("package_version"), ContainerSpecification.version);
    }
    @Test
    public void inspectOpenedContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("open-en_tit_ulb");
        File containerDir = new File(resource.getPath());

        JSONObject json = ContainerTools.inspect(containerDir);
        assertNotNull(json);
        assertEquals(json.getInt("package_version"), ContainerSpecification.version);
    }
}