package org.unfoldingword.resourcecontainer;

import com.esotericsoftware.yamlbeans.YamlWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of tools for managing resource containers.
 * These are primarily for testing, internal tools or encapsulating backwards compatibility
 */
public class ContainerTools {

    /**
     * Reads the resource container info without opening it.
     * This will however, work on containers that are both open and closed.
     * @param containerPath path to the container archive or directory
     * @return the resource container info (package.json)
     */
    public static JSONObject inspect(File containerPath) throws Exception {
        if(!containerPath.exists()) throw new Exception("The resource container does not exist at " + containerPath.getAbsolutePath());

        ResourceContainer container;

        if(containerPath.isFile()) {
            String[] nameArray = containerPath.getName().split("\\.");
            String ext = nameArray[nameArray.length - 1];
            if(!ext.equals(ResourceContainer.fileExtension)) throw new Exception("Invalid resource container file extension");
            nameArray[nameArray.length - 1] = "";
            File containerDir = new File(containerPath.getParentFile(), containerPath.getName() + ".inspect.tmp");
            container = ResourceContainer.open(containerPath, containerDir);
            FileUtil.deleteQuietly(containerDir);
        } else {
            container = ResourceContainer.load(containerPath);
        }

        return container.info;
    }

    /**
     * TODO: this will eventually be abstracted to call makeContainer after processing the data
     * Converts a legacy resource into a resource container.
     * Rejects with an error if the container exists.
     *
     * @param data the raw resource data
     * @param directory the destination directory
     * @param props properties of the resource content
     * @return the newly converted container
     */
    public static ResourceContainer convertResource(String data, File directory, JSONObject props) throws Exception {
        // TODO: 9/28/16 now that we have language, project, and resource classes we can pass these in as parameters
        if(!props.has("language") || !props.has("project") || !props.has("resource")
                || !props.getJSONObject("resource").has("type")) throw new Exception("Missing required parameters");

        JSONObject project = props.getJSONObject("project");
        JSONObject language = props.getJSONObject("language");
        JSONObject resource = props.getJSONObject("resource");

        // fix keys
        if(!language.has("direction") && language.has("dir")) {
            language.put("direction", language.getString("dir"));
            language.remove("dir");
        }

        String mimeType;
        if(!project.getString("slug").equals("obs") && resource.getString("type").equals("book")) {
            mimeType = "text/usfm";
        } else {
            mimeType = "text/markdown";
        }
        String chunkExt = mimeType.equals("text/usfm") ? "usfm" : "md";

        File containerArchive = new File(directory.getParentFile(), directory.getName() + "." + ResourceContainer.fileExtension);
        if(containerArchive.exists()) throw new Exception("Resource container already exists");

        try {
            // clean opened container
            FileUtil.deleteQuietly(directory);
            directory.mkdirs();

            // package.json
            JSONObject packageData = new JSONObject();
            packageData.put("package_version", ResourceContainer.version);
            packageData.put("modified_at", props.get("modified_at"));
            packageData.put("content_mime_type", mimeType);
            packageData.put("language", language);
            packageData.put("project", project);
            packageData.put("resource", resource);
            packageData.put("chunk_status", new JSONArray());
            // TRICKY: JSONObject.toString escapes slashes / so we must un-escape them
            FileUtil.writeStringToFile(new File(directory, "package.json"), packageData.toString(2).replace("\\", ""));

            // license
            // TODO: use a proper license based on the resource license
            FileUtil.writeStringToFile(new File(directory, "LICENSE.md"), resource.getJSONObject("status").getString("license"));

            // content
            File contentDir = new File(directory, "content");
            contentDir.mkdirs();
            Map config = new HashMap();
            List toc = new ArrayList();

            // front matter
            File frontDir = new File(contentDir, "front");
            frontDir.mkdirs();
            FileUtil.writeStringToFile(new File(frontDir, "title." + chunkExt), project.getString("name"));
            Map frontToc = new HashMap();
            frontToc.put("chapter", "title");
            frontToc.put("chunks", new String[]{"title"});
            toc.add(frontToc);

            // main content
            if(resource.getString("type").equals("book")) {
                JSONObject json = new JSONObject(data);
                config.put("content", new HashMap());
                if(project.getString("slug").equals("obs")) {
                    // add obs images
                    Map mediaConfig = new HashMap();
                    mediaConfig.put("mime_type", "image/jpg");
                    mediaConfig.put("size", 37620940);
                    mediaConfig.put("url", "https://api.unfoldingword.org/obs/jpg/1/en/obs-images-360px.zip");
                    config.put("media", mediaConfig);
                }
                for(int c = 0; c < json.getJSONArray("chapters").length(); c ++) {
                    JSONObject chapter = json.getJSONArray("chapters").getJSONObject(c);
                    Map chapterConfig = new HashMap();
                    Map chapterTOC = new HashMap();
                    chapterTOC.put("chapter", chapter.getString("number"));
                    chapterTOC.put("chunks", new ArrayList());
                    File chapterDir = new File(contentDir, chapter.getString("number"));
                    chapterDir.mkdirs();

                    // chapter title
                    if(chapter.has("title") && !chapter.getString("title").isEmpty()) {
                        ((List)chapterTOC.get("chunks")).add("title");
                        FileUtil.writeStringToFile(new File(chapterDir, "title." + chunkExt), chapter.getString("title"));
                    }
                    // frames
                    for(int f = 0; f < chapter.getJSONArray("frames").length(); f ++) {
                        JSONObject frame = chapter.getJSONArray("frames").getJSONObject(f);
                        String frameSlug = frame.getString("id").split("-")[1].trim();
                        if(frameSlug.equals("00")) {
                            // fix for chunk 00.txt bug
                            Pattern versePattern = Pattern.compile("/<verse\\s+number=\"(\\d+(-\\d+)?)\"\\s+style=\"v\"\\s*\\/>/");
                            Matcher match = versePattern.matcher(frame.getString("text"));
                            if(match.matches()) {
                                String firstVerseRange = match.group(3);
                                // TRICKY: verses can be num-num
                                frameSlug = firstVerseRange.split("-")[0];
                            }
                        }

                        // build chunk config
                        List questions = new ArrayList();
                        List notes = new ArrayList();
                        List images = new ArrayList();
                        List words = new ArrayList();
                        // TODO: 9/13/16 add questions, notes, and images to the config for the chunk
                        if(props.has("tw_assignments")) {
                            try {
                                JSONArray slugs = props.getJSONObject("tw_assignments").getJSONObject(chapter.getString("number")).getJSONArray(frameSlug);
                                for(int s = 0; s < slugs.length(); s ++) {
                                    words.add(slugs.get(s));
                                }
                            } catch (Exception e) {}
                        }
                        if(questions.size() > 0 || notes.size() > 0 || images.size() > 0 || words.size() > 0) {
                            chapterConfig.put(frameSlug, new HashMap());
                        }
                        if(questions.size() > 0) {
                            ((HashMap)chapterConfig.get(frameSlug)).put("questions", questions);
                        }
                        if(notes.size() > 0) {
                            ((HashMap)chapterConfig.get(frameSlug)).put("notes", notes);
                        }
                        if(images.size() > 0) {
                            ((HashMap)chapterConfig.get(frameSlug)).put("images", images);
                        }
                        if(words.size() > 0) {
                            ((HashMap)chapterConfig.get(frameSlug)).put("words", words);
                        }

                        ((List)chapterTOC.get("chunks")).add(frameSlug);
                        FileUtil.writeStringToFile(new File(chapterDir, frameSlug + "." + chunkExt), frame.getString("text"));
                    }

                    // chapter reference
                    if(chapter.has("ref") && !chapter.getString("ref").isEmpty()) {
                        ((List)chapterTOC.get("chunks")).add("reference");
                        FileUtil.writeStringToFile(new File(chapterDir, "title." + chunkExt), chapter.getString("title"));
                    }
                    if(chapterConfig.size() > 0) {
                        ((Map)config.get("content")).put(chapter.getString("number"), chapterConfig);
                    }
                    toc.add(chapterTOC);
                }
            } else if(resource.getString("type").equals("help")) {
                JSONArray json = new JSONArray(data);
                if(resource.getString("slug").equals("tn")) {
                    for(int c = 0; c < json.length(); c ++) {
                        JSONObject chunk = json.getJSONObject(c);
                        if(!chunk.has("tn")) continue;
                        String[] slugs = chunk.getString("id").split("-");
                        if(slugs.length != 2) continue;

                        File chapterDir = new File(contentDir, slugs[0]);
                        chapterDir.mkdirs();
                        String body = "";
                        for(int n = 0; n < chunk.getJSONArray("tn").length(); n ++) {
                            JSONObject note = chunk.getJSONArray("tn").getJSONObject(n);
                            body += "\n\n#" + note.getString("ref") + "\n\n" + note.getString("text");
                        }
                        FileUtil.writeStringToFile(new File(chapterDir, slugs[1] + "." + chunkExt), body.trim());
                    }
                } else if(resource.getString("slug").equals("tq")) {
                    for(int c = 0; c < json.length(); c ++) {
                        JSONObject chapter = json.getJSONObject(c);
                        if(!chapter.has("cq")) continue;
                        File chapterDir = new File(contentDir, chapter.getString("id"));
                        chapterDir.mkdirs();
                        Map<String, String> normalizedChunks = new HashMap();
                        for(int q = 0; q < chapter.getJSONArray("cq").length(); q ++) {
                            JSONObject question = chapter.getJSONArray("cq").getJSONObject(q);
                            String text = "\n\n#" + question.getString("q") + "\n\n" + question.getString("q");
                            for(int s = 0; s < question.getJSONArray("ref").length(); s ++) {
                                String[] slugs = question.getJSONArray("ref").getString(s).split("-");
                                if(slugs.length != 2) continue;

                                String old = normalizedChunks.get(slugs[1]);
                                normalizedChunks.put(slugs[1], (old != null ? old : "") + text);
                            }
                        }
                        for(String key:normalizedChunks.keySet()) {
                            FileUtil.writeStringToFile(new File(chapterDir, key + "." + chunkExt), normalizedChunks.get(key).trim());
                        }
                    }
                } else {
                    throw new Exception("Unsupported resource " + resource.getString("slug"));
                }
            } else if(resource.getString("type").equals("dict")) {
                JSONArray json = new JSONArray(data);
                for(int w = 0; w < json.length(); w ++) {
                    JSONObject word = json.getJSONObject(w);
                    if(!word.has("id")) continue;
                    File wordDir = new File(contentDir, word.getString("id"));
                    wordDir.mkdirs();
                    String body = "#" + word.getString("term") + "\n\n" + word.getString("def");
                    FileUtil.writeStringToFile(new File(wordDir, "01." + chunkExt), body);

                    if(JSONHasLength(word, "aliases") || JSONHasLength(word, "cf") || JSONHasLength(word, "ex")) {
                        Map<String, List> wordConfig = new HashMap();
                        if(JSONHasLength(word, "cf")) {
                            wordConfig.put("see_also", new ArrayList());
                            for(int i = 0; i < word.getJSONArray("cf").length(); i ++) {
                                wordConfig.get("see_also").add(word.getJSONArray("cf").get(i));
                            }
                        }
                        if(JSONHasLength(word, "aliases")) {
                            wordConfig.put("aliases", new ArrayList());
                            for(int i = 0; i < word.getJSONArray("aliases").length(); i ++) {
                                wordConfig.get("aliases").add(word.getJSONArray("aliases").get(i));
                            }
                        }
                        if(JSONHasLength(word, "ex")) {
                            wordConfig.put("examples", new ArrayList());
                            for(int i = 0; i < word.getJSONArray("ex").length(); i ++) {
                                wordConfig.get("examples").add(word.getJSONArray("ex").get(i));
                            }
                        }
                        config.put(word.getString("id"), wordConfig);
                    }
                }
            } else if(resource.getString("type").equals("man")) {
                JSONObject json = new JSONObject(data);
                for(int a = 0; a < json.getJSONArray("articles").length(); a ++) {
                    JSONObject article = json.getJSONArray("articles").getJSONObject(a);
                    Map articleConfig = new HashMap();
                    List recommended = new ArrayList();
                    List dependencies = new ArrayList();

                    // TRICKY: fix the id's
                    String slug = article.getString("id").replaceAll("\\_", "-");
                    if(article.has("recommend") && !article.isNull("recommend")) {
                        for(int i = 0; i < article.getJSONArray("recommend").length(); i ++) {
                            recommended.add(article.getJSONArray("recommend").getString(i).replaceAll("\\_", "-"));
                        }
                    }
                    if(article.has("depend") && !article.isNull("depend")) {
                        for(int i = 0; i < article.getJSONArray("depend").length(); i ++) {
                            dependencies.add(article.getJSONArray("depend").getString(i).replaceAll("\\_", "-"));
                        }
                    }
                    File articleDir = new File(contentDir, slug);
                    articleDir.mkdirs();

                    // article title
                    FileUtil.writeStringToFile(new File(articleDir, "title." + chunkExt), article.getString("title"));

                    // article sub-title
                    FileUtil.writeStringToFile(new File(articleDir, "sub-title." + chunkExt), article.getString("question"));

                    // article body
                    FileUtil.writeStringToFile(new File(articleDir, "01." + chunkExt), article.getString("text"));

                    // only non-empty config
                    if(recommended.size() > 0 || dependencies.size() > 0) {
                        articleConfig.put("recommended", recommended);
                        articleConfig.put("dependencies", dependencies);
                        config.put(slug, articleConfig);
                    }
                }
            } else {
                throw new Exception("Unsupported resource container type " + resource.getString("type"));
            }

            // write config
            YamlWriter configWriter = null;
            try {
                configWriter = new YamlWriter(new FileWriter(new File(contentDir, "config.yml")));
                configWriter.write(config);
            } catch(Exception e) {
                throw e;
            } finally {
                if(configWriter != null) configWriter.close();
            }

            // write toc
            YamlWriter tocWriter = null;
            try {
                tocWriter = new YamlWriter(new FileWriter(new File(contentDir, "toc.yml")));
                tocWriter.write(toc);
            } catch(Exception e) {
                throw e;
            } finally {
                if(tocWriter != null) tocWriter.close();
            }

        } catch (Exception e) {
            FileUtil.deleteQuietly(directory);
            throw e;
        }

        return ResourceContainer.load(directory);
    }

    /**
     * Checks if a value in the json object has a length e.g. is a JSONArray with length > 0
     * @param json
     * @param key
     * @return
     */
    private static boolean JSONHasLength(JSONObject json, String key) {
        try {
            return json.has(key) && json.optJSONArray(key) != null && json.getJSONArray(key).length() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns a properly formatted container slug.
     * @param languageSlug
     * @param projectSlug
     * @param resourceSlug
     * @return
     */
    public static String makeSlug(String languageSlug, String projectSlug, String resourceSlug) {
        if(languageSlug == null || languageSlug.isEmpty()
                || projectSlug == null || projectSlug.isEmpty()
                || resourceSlug == null || resourceSlug.isEmpty()) throw new InvalidParameterException("Invalid resource container slug parameters");
        return languageSlug + ResourceContainer.slugDelimiter + projectSlug + ResourceContainer.slugDelimiter + resourceSlug;
    }

    /**
     * Breaks a resource container slug into it's delimited sections.
     * Those are language, project, resource.
     *
     * @param resourceContainerSlug
     * @return
     */
    public static String[] explodeSlug(String resourceContainerSlug) {
        return resourceContainerSlug.split(ResourceContainer.slugDelimiter);
    }

    /**
     * Retrieves the resource container type by parsing the resource container mime type.
     * @param mimeType
     * @return The mime type. e.g. "application/tsrc+type"
     */
    public static String mimeToType(String mimeType) {
        return mimeType.split("\\+")[1];
    }

    /**
     * Returns a resource container mime type based on the given container type.
     * @param resourceType the resource container type. e.g. "book", "help" etc.
     * @return The mime type. e.g. "application/tsrc+type"
     */
    public static String typeToMime(String resourceType) {
        return ResourceContainer.baseMimeType + "+" + resourceType;
    }

    /**
     * Parses a link. This could be an external link or a resource container link
     *
     * @param link
     * @throws Exception if the link is invalid
     */
    public static Link parseLink(String link) throws Exception {
        Pattern anonymousPattern = Pattern.compile("\\[\\[([^\\]]*)\\]\\]", Pattern.DOTALL);
        Pattern titledPattern = Pattern.compile("\\[([^\\]]*)\\]\\(([^\\)]*)\\)", Pattern.DOTALL);

        String linkTitle = null;
        String linkPath = null;
        Matcher m;
        int numMatches = 1;

        // find anonymous links
        m = anonymousPattern.matcher(link);
        while(m.find()) {
            if(numMatches > 1) throw new Exception("Invalid link! Multiple links found");
            numMatches ++;
            linkPath = m.group(1).toLowerCase();
        }

        // find titled links
        m = titledPattern.matcher(link);
        numMatches = 1;
        while(m.find()) {
            if(numMatches > 1) throw new Exception("Invalid link! Multiple links found");
            numMatches ++;
            linkTitle = m.group(1);
            linkPath = m.group(2).toLowerCase();
        }

        // process link path
        if(linkPath != null) {
            // external link
            if(linkPath.startsWith("http")) {
                return new Link(linkTitle, linkPath);
            }
            return parseResourceLink(linkTitle, linkPath);
        }

        return null;
    }

    /**
     * Parses a resource container link
     * @param title
     * @param path
     * @return
     */
    private static Link parseResourceLink(String title, String path) {
        return null;
    }

    /**
     * Returns a list of links found in the text.
     * This is used to turn inline Bible passages into links.
     * The returned links will include their position within the charsequence
     *
     * @param text the text that will be searched for Bible passages
     * @return
     */
    public static List<Link> findLinks(CharSequence text) {
        // TODO: 10/11/16 automatically parse bible passages.
        return null;
    }
}
