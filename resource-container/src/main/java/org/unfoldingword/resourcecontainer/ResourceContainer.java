package org.unfoldingword.resourcecontainer;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public MapReader manifest = null;

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
            this.manifest = new MapReader(reader.read());
        }
    }

    public Language language() {
        Object language = this.manifest.get("dublin_core").get("language");
        if(language == null) return null;

        MapReader reader = new MapReader(language);
        String slug = (String)reader.get("identifier").value();
        String title = (String)reader.get("title").value();
        String direction = (String)reader.get("direction").value();
        return new Language(slug, title, direction);
    }

    public Resource resource () {
        String slug = (String)this.manifest.get("dublin_core").get("identifier").value();
        String title = (String)this.manifest.get("dublin_core").get("title").value();
        String type = (String)this.manifest.get("dublin_core").get("type").value();
        String checkingLevel = (String)this.manifest.get("checking").get("checking_level").value();
        String version = (String)this.manifest.get("dublin_core").get("version").value();
        // TODO: 1/26/17 the translate mode is deprecated
        return new Resource(slug, title, type, null, checkingLevel, version);
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
        if(this.manifest.get("projects").size() == 0) return null;

        if(identifier != null && !identifier.isEmpty()) {
            // look up project
            for(Object project:(List)this.manifest.get("projects")) {
                MapReader p = new MapReader(project);
                if(p.get("identifier").value().equals(identifier)) {
                    String slug = (String)p.get("identifier").value();
                    String name = (String)p.get("title").value();
                    Integer sort = 0;
                    if(p.get("sort").value() instanceof Integer) {
                        sort = (Integer)p.get("sort").value();
                    }
                    return new Project(slug, name, sort);
                }
            }
        } else if(this.manifest.get("projects").size() == 1) {
            // return only project
            String slug = (String)this.manifest.get("projects").get(0).get("identifier").value();
            String name = (String)this.manifest.get("projects").get(0).get("title").value();
            Integer sort = 0;
            if(this.manifest.get("projects").get(0).get("sort").value() instanceof Integer) {
                sort = (Integer)this.manifest.get("projects").get(0).get("sort").value();
            }
            return new Project(slug, name, sort);
        } else if(this.manifest.get("projects").size() > 1) {
            throw new Exception("Multiple projects found. Specify the project identifier.");
        }

        return null;
    }

    /**
     * Returns the number of projects contained in this RC.
     *
     * @return the project count
     */
    public int projectCount() {
        return this.manifest.get("projects").size();
    }

    /**
     * Returns the version of the RC spec used in this container.
     * This will strip off the 'rc' prefix.
     *
     * @return the RC version e.g. '0.2'
     */
    public String conformsTo() {
        String value = (String)this.manifest.get("dublin_core").get("conformsto").value();
        if(value != null) {
            return value.replaceAll("^rc", "");
        }
        return null;
    }

    /**
     * Returns an un-ordered list of chapter slugs in this resource container
     * @return
     */
    public String[] chapters(String projectIdentifier) {
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
