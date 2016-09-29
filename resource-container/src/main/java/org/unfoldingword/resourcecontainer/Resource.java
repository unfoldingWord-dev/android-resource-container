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
    public static final String REGULAR_SLUG = "reg";

    public final String slug;
    public final String name;
    public final String type;
    public final String translateMode;
    public final String checkingLevel;
    public final String version;

    /**
     * Comments about this resource
     */
    public String comments = "";

    /**
     * The date this resource was published
     */
    public String pubDate = "";

    /**
     * The license under which this content exists
     */
    public String license = "";

    /**
     * The project this resource belongs to.
     * This is a convenience property
     */
    public String projectSlug = "";

    /**
     * A list of formats in which this resource exists
     * e.g. binary formats
     */
    public final List<Format> formats = new ArrayList<>();

    /**
     * Storage for legacy data while we transition from the old api to the new.
     */
    public Map<String, Object> _legacyData = new HashMap<>();

    /**
     * Creates a new resource
     * @param slug the resource identifier
     * @param name the human readable name of this resource
     * @param type the type of resource this is
     * @param translateMode the mode (of operation within an app) in which this resource can be translated
     * @param checkingLevel the greatest level of checking that has been completed on this resource
     * @param version the human readable version of this resource
     */
    public Resource(String slug, String name, String type, String translateMode, String checkingLevel, String version) {
        this.slug = slug;
        this.name = name;
        this.type = type;
        this.translateMode = translateMode;
        // TODO: 9/29/16 checkingLevel should probably be an integer
        this.checkingLevel = checkingLevel;
        this.version = version;
    }

    /**
     * Adds a format to this resource
     * @param format e.g. a binary format
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
        statusJson.put("pub_date", pubDate);
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
                status.getString("checking_level"),
                status.getString("version"));

        if(status.has("license")) r.license = deNull(status.getString("license"));
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