package org.unfoldingword.resourcecontainer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

/**
 * Created by joel on 10/11/16.
 */
public class GenericLinkTest {

    // anonymous

    @Test
    public void anonymousContainerLink() throws Exception {
        Link l = ContainerTools.parseLink("[[language/project/resource]]");
        assertEquals(null, l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
    }

    @Test
    public void anonymousHttpsLink() throws Exception {
        Link l = ContainerTools.parseLink("[[https://www.google.com]]");
        assertEquals(null, l.title);
    }

    @Test
    public void anonymousHttpLink() throws Exception {
        Link l = ContainerTools.parseLink("[[http://www.google.com]]");
        assertEquals(null, l.title);
    }

    @Test
    public void anonymousShorthandPassageLink() throws Exception {
        Link l = ContainerTools.parseLink("[[language/project/01:02]]");
        assertEquals(null, l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("project", l.resource);
        assertEquals("01:02", l.arguments);
    }

    @Test
    public void anonymousShorthandResourceLink() throws Exception {
        Link l = ContainerTools.parseLink("[[language/project]]");
        assertEquals(null, l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("project", l.resource);
        assertEquals(null, l.arguments);
    }

    @Test
    public void anonymousAbbreviatedLink() throws Exception {
        Link l = ContainerTools.parseLink("[[slug]]");
        assertEquals(null, l.title);
        assertEquals(null, l.language);
        assertEquals(null, l.project);
        assertEquals(null, l.resource);
        assertEquals("slug", l.arguments);
    }

    @Test
    public void anonymousAnyLanguageLink() throws Exception {
        Link l = ContainerTools.parseLink("[[//project/resource/args]]");
        assertEquals(null, l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals("args", l.arguments);
    }

    @Test
    public void anonymousAnyLanguageNoArgsLink() throws Exception {
        Link l = ContainerTools.parseLink("[[//project/resource]]");
        assertEquals(null, l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
    }

    // titled

    @Test
    public void titledContainerLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](language/project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
    }

    @Test
    public void titledHttpsLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](https://www.google.com)");
        assertEquals("Link Title", l.title);
    }

    @Test
    public void titledHttpLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](http://www.google.com)");
        assertEquals("Link Title", l.title);
    }

    @Test
    public void titledShorthandPassageLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](language/project/01:02)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("project", l.resource);
        assertEquals("01:02", l.arguments);
    }

    @Test
    public void titledShorthandResourceLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](language/project)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("project", l.resource);
        assertEquals(null, l.arguments);
    }

    @Test
    public void titledAbbreviatedLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](slug)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals(null, l.project);
        assertEquals(null, l.resource);
        assertEquals("slug", l.arguments);
    }

    @Test
    public void titledAnyLanguageLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](//project/resource/args)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals("args", l.arguments);
    }

    @Test
    public void titledAnyLanguageNoArgsLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](//project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
    }

    @Test
    public void titledMediaLink() throws Exception {
        // without preceding slash
        Link l = ContainerTools.parseLink("[Link Title](image:language/project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertEquals("image", l.protocal);
    }

    @Test
    public void titledMediaAltLink() throws Exception {
        // with perceding slash
        Link l = ContainerTools.parseLink("[Link Title](image:/language/project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertEquals("image", l.protocal);
    }

    @Test
    public void titledMediaAnyLanguageLink() throws Exception {
        Link l = ContainerTools.parseLink("[Link Title](image://project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertEquals("image", l.protocal);
    }
}
