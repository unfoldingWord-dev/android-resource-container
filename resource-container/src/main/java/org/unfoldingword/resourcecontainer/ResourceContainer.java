package org.unfoldingword.resourcecontainer;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.unfoldingword.tools.jtar.TarInputStream;
import org.unfoldingword.tools.jtar.TarOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an instance of a resource container.
 */
public class ResourceContainer {

    /**
     * Returns the path to the resource container directory
     */
    public final File path;

    /**
     * Returns the resource container package information.
     * This is the package.json file
     */
    public Map manifest = null;

    /**
     * Instantiates a new resource container object
     *
     * @param dir the directory of the resource container
     * @throws Exception
     */
    public ResourceContainer(File dir) throws Exception {
        this.path = dir;

        File manifestFile = new File(dir, "manifest.yaml");
        if(manifestFile.exists()) {
            YamlReader reader = new YamlReader(new FileReader(manifestFile));
            this.manifest = (HashMap)reader.read();
        }
    }

    public Language language() {
        return  null; // TODO: 1/24/17 return the language
    }

    public Resource resource () {
        return null; // TODO: return the resource
    }

    /**
     * Retrieves the project from the RC
     *
     * @return the project
     * @throws Exception
     */
    public Project project() throws Exception {
        return project(null);
    }

    /**
     * Retrieves a project from the RC.
     *
     * @param identifier the project to be retrieved. This can be null if there is only one project
     * @return the project
     * @throws Exception
     */
    public Project project(String identifier) throws Exception {
        if(!this.manifest.containsKey("projects") || ((List)this.manifest.get("projects")).size() == 0) return null;

        if(identifier != null && !identifier.isEmpty()) {
            for(HashMap<String, String> p:(List<HashMap>)this.manifest.get("projects")) {
                if(p.containsKey("identifier") && p.get("identifier").equals(identifier)) {
                    // TODO: return project
                    return null;
                }
            }
        } else {
            if(((List)this.manifest.get("projects")).size() == 1) {
                HashMap p = ((List<HashMap>)this.manifest.get("projects")).get(0);
                // TODO: 1/24/17 return project
                return null;
            } else if(((List)this.manifest.get("projects")).size() > 1) {
                throw new Exception("Multiple projects found. Specify the project identifier.");
            }
        }
        return null;
    }

    /**
     * Returns the number of projects contained in this RC.
     *
     * @return the project count
     */
    public int projectCount() {
        if(this.manifest.containsKey("projects")) {
            return ((List) this.manifest.get("projects")).size();
        } else {
            return 0;
        }
    }

    /**
     * Returns the version of the RC spec used in this container.
     * This will strip off the 'rc' prefix.
     *
     * @return the RC version e.g. '0.2'
     */
    public String conformsTo() {
        if(this.manifest.containsKey("dublin_core") && this.manifest.get("dublin_core") instanceof HashMap) {
            HashMap<String, Object> dc = (HashMap)this.manifest.get("dublin_core");
            if(dc.containsKey("conformsto")) return ((String)dc.get("conformsto")).replaceAll("^rc", "");
        }
        return null;
    }

    /**
     * Returns an un-ordered list of chapter slugs in this resource container
     * @return
     */
    public String[] chapters() {
        String[] chapters = (new File(path, CONTENT_DIR)).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return new File(dir, filename).isDirectory() && !filename.equals("config.yml") && !filename.equals("toc.yml");
            }
        });
        if(chapters == null) chapters = new String[0];
        return chapters;
    }

    /**
     * Returns an un-ordered list of chunk slugs in the chapter
     * @param chapterSlug
     * @return
     */
    public String[] chunks(String chapterSlug) {
        final List<String> chunks = new ArrayList<>();
        (new File(new File(path, CONTENT_DIR), chapterSlug)).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                chunks.add(filename.split("\\.")[0]);
                return false;
            }
        });
        return chunks.toArray(new String[chunks.size()]);
    }

    /**
     * Returns the contents of a chunk.
     * If the chunk does not exist or there is an exception an empty string will be returned
     * @param chapterSlug
     * @param chunkSlug
     * @return
     */
    public String readChunk(String chapterSlug, String chunkSlug) {
        File chunkFile = new File(new File(new File(path, CONTENT_DIR), chapterSlug), chunkSlug + "." + chunkExt());
        if(chunkFile.exists() && chunkFile.isFile()) {
            try {
                return FileUtil.readFileToString(chunkFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * Returns the file extension to use for content files (chunks)
     * @return the extension name
     */
    private String chunkExt() {
        String defaultExt = "txt";
        switch ((String)manifest.get("content_mime_type")) {
            case "text/usx":
                return "usx";
            case "text/usfm":
                return "usfm";
            case "text/markdown":
                return "md";
            default:
                // unknown format
                return defaultExt;
        }
    }
}
