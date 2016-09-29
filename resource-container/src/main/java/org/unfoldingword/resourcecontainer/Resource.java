package org.unfoldingword.resourcecontainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a resource that can be translated
 */
public class Resource {
    public final String slug;
    public final String name;
    public final String type;
    public final String translateMode;
    public final String checkingLevel;

    public final List<Format> formats = new ArrayList<>();

    public String comments = "";
    public String pubDate = "";
    public String license = "";
    public String version = "";

    /**
     * The project this resource belongs to.
     * This is a convenience property
     */
    public String projectSlug = "";


    /**
     * Allows storing legacy data
     */
    @Deprecated
    public Map<String, Object> _legacyData = new HashMap<>();

    /**
     * Creates a new resource
     * @param slug
     * @param name
     * @param type
     */
    public Resource(String slug, String name, String type, String translateMode, String checkingLevel) {
        this.slug = slug;
        this.name = name;
        this.type = type;
        this.translateMode = translateMode;
        this.checkingLevel = checkingLevel;
    }

    /**
     * Adds a format to this resource. e.g. a binary format
     * @param format
     */
    public void addFormat(Format format) {
        formats.add(format);
    }

    /**
     * Returns the object serialized to json
     *
     * @return
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("slug", slug);
        json.put("name", name);
        json.put("type", type);
        JSONObject statusJson = new JSONObject();
        statusJson.put("translate_mode", translateMode);
        statusJson.put("checking_level", checkingLevel);
        statusJson.put("license", deNull(license));
        statusJson.put("version", deNull(version));
        statusJson.put("pub_date", deNull(pubDate));
        statusJson.put("comments", deNull(comments));
        // TODO: 9/28/16 there can be more to write
        json.put("status", statusJson);
        return json;
    }

    /**
     * Turns null values to empty strings
     * @param value
     * @return
     */
    private static String deNull(String value) {
        if(value == null) value = "";
        return value;
    }

    /**
     * Creates a new resource from json
     * @param json
     * @return
     * @throws JSONException
     */
    public static Resource fromJSON(JSONObject json) throws JSONException {
        if(json == null) throw new JSONException("Invalid json");
        JSONObject status = json.getJSONObject("status");
        Resource r = new Resource(json.getString("slug"),
                json.getString("name"),
                json.getString("type"),
                status.getString("translate_mode"),
                status.getString("checking_level"));

        if(status.has("license")) r.license = deNull(status.getString("license"));
        if(status.has("version")) r.version = deNull(status.getString("version"));
        if(status.has("pub_date")) r.pubDate = deNull(status.getString("pub_date"));
        if(status.has("comments")) r.comments = deNull(status.getString("comments"));
        // TODO: 9/28/16 there can be more to load
        return r;
    }

    /**
     * Represents a physical form of the resource
     */
    public static class Format {
        public String packageVersion;
        public String mimeType;
        public int modifiedAt;
        public String url;

        public Format(String packageVersion, String mimeType, int modifiedAt, String url) {
            this.packageVersion = packageVersion;
            this.mimeType = mimeType;
            this.modifiedAt = modifiedAt;
            this.url = url;
        }
    }
}