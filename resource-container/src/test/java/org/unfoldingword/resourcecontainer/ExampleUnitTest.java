package org.unfoldingword.resourcecontainer;

import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
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
        URL resource = classLoader.getResource("closed-en_tit_ulb.ts");
        File archivePath = new File(resource.getPath());
        File dir = new File(archivePath.getParentFile(), "closed-en_tit_ulb");
        ResourceContainer container = ResourceContainer.open(archivePath, dir);
        assertTrue(dir.exists());
    }
}