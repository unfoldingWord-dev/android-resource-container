package org.unfoldingword.resourcecontainer;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by joel on 10/11/16.
 */

public class BookLinkTest {

    // standard links

    @Test
    public void chapterVerseLink() throws Exception {
        Link l = ContainerTools.parseLink("[[language/project/resource/01:02]]");
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals("01:02", l.arguments);
        assertEquals("01", l.chapter);
        assertEquals("02", l.chunk);
    }

    @Test
    public void chapterLink() throws Exception {
        Link l = ContainerTools.parseLink("[[language/project/resource/01]]");
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals("01", l.arguments);
        assertEquals("01", l.chapter);
    }

    @Test
    public void chapterVerseRangeLink() throws Exception {
        Link l = ContainerTools.parseLink("[[language/project/resource/01:02-06]]");
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals("01:02-06", l.arguments);
        assertEquals("01", l.chapter);
        assertEquals("02", l.chunk);
        assertEquals("06", l.lastChunk);
    }

    @Test
    public void invlaidChapterVerseLink() throws Exception {
        try {
            Link l = ContainerTools.parseLink("[[language/project/resource/01:02,06]]");
            assertEquals(null, l);
        } catch(Exception e) {
            assertNotEquals(null, e);
        }
    }

    // automatic linking

    @Test
    public void autoLink() throws Exception {
        List<Link> links = ContainerTools.findLinks("Genesis 1:1");
        assertEquals(1, links.size());
        assertEquals("Genesis", links.get(0).project);
        assertEquals("1:1", links.get(0).arguments);
        assertEquals("1", links.get(0).chapter);
        assertEquals("1", links.get(0).chunk);
    }

    @Test
    public void autoRangeLink() throws Exception {
        List<Link> links = ContainerTools.findLinks("genesis 1:1-3");
        assertEquals(1, links.size());
        assertEquals("genesis", links.get(0).project);
        assertEquals("1:1-3", links.get(0).arguments);
        assertEquals("1", links.get(0).chapter);
        assertEquals("1", links.get(0).chunk);
        assertEquals("3", links.get(0).lastChunk);
    }

    @Test
    public void autoMultipleLinks() throws Exception {
        List<Link> links = ContainerTools.findLinks("John 1–3; 3:16; 6:14, 44, 46-47; 7:1-5");
        assertEquals(6, links.size());
        Link l1 = links.get(0);
        assertEquals("John", l1.project);
        assertEquals("1", l1.chapter);
        assertEquals(null, l1.chunk);

        Link l2 = links.get(1);
        assertEquals("John", l2.project);
        assertEquals("3", l2.chapter);
        assertEquals("16", l1.chunk);

        Link l3 = links.get(2);
        assertEquals("John", l3.project);
        assertEquals("6", l3.chapter);
        assertEquals("14", l1.chunk);

        Link l4 = links.get(3);
        assertEquals("John", l4.project);
        assertEquals("6", l4.chapter);
        assertEquals("44", l1.chunk);

        Link l5 = links.get(4);
        assertEquals("John", l5.project);
        assertEquals("6", l5.chapter);
        assertEquals("46", l1.chunk);
        assertEquals("47", l1.lastChunk);

        Link l6 = links.get(5);
        assertEquals("John", l6.project);
        assertEquals("7", l6.chapter);
        assertEquals("1", l1.chunk);
        assertEquals("5", l1.lastChunk);
    }
}
