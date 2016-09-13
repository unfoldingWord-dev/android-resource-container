package org.unfoldingword.resourcecontainer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unfoldingword.resourcecontainer.util.FileUtil;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static JSONObject inspect(File containerPath) {
        // TODO: 8/31/16 build inspection tool
        return new JSONObject();
    }

    /**
     * TODO: this will eventually be abstracted to call makeContainer after processing the data
     * Converts a legacy resource into a resource container.
     * Rejects with an error if the container exists.
     *
     * @param data the raw resource data
     * @param directory the destination directory
     * @param props properties of the resource content
     * @param resourceType the type of container to be generated
     * @return the newly converted container
     */
    public static ResourceContainer convertResource(String data, File directory, JSONObject props, String resourceType) throws Exception {
        if(!props.has("language") || !props.has("project") || !props.has("resource")
                || !props.getJSONObject("resource").has("type")) throw new Exception("Missing required parameters");

        JSONObject project = props.getJSONObject("project");
        JSONObject language = props.getJSONObject("language");
        JSONObject resource = props.getJSONObject("resource");

        String mimeType;
        if(!project.getString("slug").equals("obs") && resource.getString("type").equals("book")) {
            mimeType = "text/usfm";
        } else {
            mimeType = "text/markdown";
        }
        String chunk_ext = mimeType.equals("text/usfm") ? "usfm" : "md";

        File containerArchive = new File(directory.getParentFile(), directory.getName() + "." + ContainerSpecification.fileExtension);
        if(containerArchive.exists()) throw new Exception("Resource container already exists");

        try {
            // clean opened container
            FileUtil.deleteQuietly(directory);
            directory.mkdirs();

            // package.json
            JSONObject packageData = new JSONObject();
            packageData.put("package_version", ContainerSpecification.version);
            packageData.put("modified_at", resource.get("modified_at"));
            packageData.put("content_mime_type", mimeType);
            packageData.put("language", language);
            packageData.put("project", project);
            packageData.put("resource", resource);
            packageData.put("chunk_status", new JSONArray());
            FileUtil.writeStringToFile(new File(directory, "package.json"), packageData.toString(2));

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
            FileUtil.writeStringToFile(new File(frontDir, "title." + chunk_ext), project.getString("name"));
            toc.add(new HashMap<String, Object>(){{
                put("chapter", "title");
                put("chunks", new String[]{"title"});
            }});

            // main content
            // TODO: finish

        } catch (Exception e) {
            throw e;
        } finally {
            FileUtil.deleteQuietly(directory);
        }

        return null;
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
        return languageSlug + "_" + projectSlug + "_" + resourceSlug;
    }

    /**
     * Retrieves the resource container type by parsing the resource container mime type.
     * @param mimeType
     * @return The mime type. e.g. "application/ts+type"
     */
    public static String mimeToType(String mimeType) {
        return mimeType.split("\\+")[1];
    }

    /**
     * Returns a resource container mime type based on the given container type.
     * @param resourceType the resource container type. e.g. "book", "help" etc.
     * @return The mime type. e.g. "application/ts+type"
     */
    public static String typeToMime(String resourceType) {
        return "application/ts+" + resourceType;
    }
}
