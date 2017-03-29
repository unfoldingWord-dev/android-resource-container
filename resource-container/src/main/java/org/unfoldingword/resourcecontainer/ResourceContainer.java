package org.unfoldingword.resourcecontainer;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public ObjectReader manifest = null;

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
            this.manifest = new ObjectReader(reader.read());
        } else {
            this.manifest = new ObjectReader(new HashMap<>());
        }
    }

    public Language language() {
        Object language = this.manifest.get("dublin_core").get("language");
        if(language == null) return null;

        ObjectReader reader = new ObjectReader(language);
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
    public Object project() throws Exception {
        return project(null);
    }

    /**
     * Retrieves a project from the RC.
     *
     * @param identifier the project to be retrieved. This can be null if there is only one project
     * @return the project
     * @throws Exception
     */
    public Object project(String identifier) throws Exception {
        if(this.manifest.get("projects").size() == 0) return null;

        if(identifier != null && !identifier.isEmpty()) {
            // look up project
            for(Object project:(List)this.manifest.get("projects").value()) {
                ObjectReader p = new ObjectReader(project);
                if(p.get("identifier").value().equals(identifier)) {
                    return p.value();
                }
            }
        } else if(this.manifest.get("projects").size() == 1) {
            return this.manifest.get("projects").get(0).value();
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
     *
     * @return an array of chapter identifiers
     */
    public String[] chapters() throws Exception {
        return chapters(null);
    }

    /**
     * Returns an un-ordered list of chapter slugs in this resource container
     *
     * @param projectIdentifier the project who's chapters will be returned
     * @return an array of chapter identifiers
     */
    public String[] chapters(String projectIdentifier) throws Exception {
        Object p = project(projectIdentifier);
        if(p == null) return new String[]{};

        ObjectReader pReader = new ObjectReader(p);
        File contentPath = new File(path, (String)pReader.get("path").value());
        String[] chapters = contentPath.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return new File(dir, filename).isDirectory();
            }
        });
        if(chapters == null) chapters = new String[0];
        return chapters;
    }

    /**
     * Returns an un-ordered list of chunk slugs in the chapter
     *
     * @param chapterSlug the chapter who's chunks will be returned
     * @return an array of chunk identifiers
     */
    public String[] chunks(String chapterSlug) throws Exception {
        return chunks(null, chapterSlug);
    }

    /**
     * Returns an un-ordered list of chunk slugs in the chapter
     *
     * @param projectIdentifier the project who's chunks will be returned
     * @param chapterSlug the chapter who's chunks will be returned
     * @return an array of chunk identifiers
     */
    public String[] chunks(String projectIdentifier, String chapterSlug) throws Exception {
        Object p = project(projectIdentifier);
        if(p == null) return new String[]{};

        ObjectReader pReader = new ObjectReader(p);
        File contentDir = new File(path, (String)pReader.get("path").value());
        File chapterDir = new File(contentDir, chapterSlug);
        final List<String> chunks = new ArrayList<>();
        chapterDir.list(new FilenameFilter() {
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
     *
     * @param chapterSlug the chapter who's chunk will be read
     * @param chunkSlug the contents of the chunk or an empty string if it does not exist
     * @return the chunk contents
     */
    public String readChunk(String chapterSlug, String chunkSlug) throws Exception {
        return readChunk(null, chapterSlug, chunkSlug);
    }

    /**
     * Returns the contents of a chunk.
     *
     * @param projectIdentifier the project who's chunk will be read
     * @param chapterSlug the chapter who's chunk will be read
     * @param chunkSlug the contents of the chunk or an empty string if it does not exist
     * @return the chunk contents
     */
    public String readChunk(String projectIdentifier, String chapterSlug, String chunkSlug) throws Exception {
        Object p = project(projectIdentifier);
        if(p == null) return "";

        ObjectReader pReader = new ObjectReader(p);
        File contentDir = new File(path, (String)pReader.get("path").value());
        File chunkFile = new File(new File(contentDir, chapterSlug), chunkSlug + "." + chunkExt());
        if(chunkFile.exists() && chunkFile.isFile()) {
            return FileUtil.readFileToString(chunkFile);
        }
        return "";
    }

    /**
     * Writes content to a chunk.
     * The path will be created if it does not already exist.
     *
     * @param chapterIdentifier the chapter who's chunk will be written to
     * @param chunkIdentifier the chunk that will be created
     * @param content the content to be written to the chunk
     * @throws Exception
     */
    public void writeChunk(String chapterIdentifier, String chunkIdentifier, String content) throws Exception {
        writeChunk(null, chapterIdentifier, chunkIdentifier, content);
    }

    /**
     * Writes content to a chunk.
     * The path will be created if it does not already exist.
     *
     * @param projectIdentifier the project who's chunk will be written to
     * @param chapterIdentifier the chapter who's chunk will be written to
     * @param chunkIdentifier the chunk that will be created
     * @param content the content to be written to the chunk
     * @throws Exception
     */
    public void writeChunk(String projectIdentifier, String chapterIdentifier, String chunkIdentifier, String content) throws Exception {
        Object p = project(projectIdentifier);
        if(p == null) return;

        ObjectReader pReader = new ObjectReader(p);
        File contentDir = new File(path, (String)pReader.get("path").value());
        File chunkFile = new File(new File(contentDir, chapterIdentifier), chunkIdentifier + "." + chunkExt());
        chunkFile.getParentFile().mkdirs();
        FileUtil.writeStringToFile(chunkFile, content);
    }

    /**
     * Returns the file extension to use for content files (chunks)
     * @return the extension name
     */
    private String chunkExt() {
        // TODO: 1/26/17 I'd rather not hard code the file extensions in here.
        // it would be better if the library can just figure it out.
        String defaultExt = "txt";
        switch ((String)manifest.get("dublin_core").get("format").value()) {
            case "text/usx":
                return "usx";
            case "text/usfm":
                return "usfm";
            case "text/markdown":
                return "md";
            case "audio/mp3":
                return "mp3";
            case "video/mp4":
                return "mp4";
            default:
                // unknown format
                return defaultExt;
        }
    }

    /**
     * Convenience method to get the type of the resource container.
     *
     * @return the RC type
     */
    public String type() {
        return (String)this.manifest.get("dublin_core").get("type").value();
    }
}
