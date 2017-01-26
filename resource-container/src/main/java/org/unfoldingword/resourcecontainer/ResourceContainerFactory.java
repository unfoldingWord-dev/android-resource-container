package org.unfoldingword.resourcecontainer;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handling the creation and loading of RCs
 */
public class ResourceContainerFactory {

    public static final String conformsTo = "0.2";

    public ResourceContainer load(File dir) throws Exception {
        return load(dir, true);
    }

    /**
     * Loads a resource container from the disk.
     *
     * When strict mode is enabled this will throw and exception if validation fails.
     *
     * @param dir the RC directory
     * @param strict When false the RC  will not be validated.
     * @return the loaded RC
     * @throws Exception
     */
    public ResourceContainer load(File dir, boolean strict) throws Exception {
        ResourceContainer rc = new ResourceContainer(dir);

        if(strict) {
            if(rc.manifest.isNull() || rc.conformsTo() == null) throw new Exception("Not a resource container");
            if(Semver.gt(rc.conformsTo(), conformsTo)) throw new Exception("Unsupported resource container version. Found " + rc.conformsTo() + " but expected " + conformsTo);
            if(Semver.lt(rc.conformsTo(), conformsTo)) throw new Exception("Outdated resource container version. Found " + rc.conformsTo() + " but expected " + conformsTo);
        }

        return rc;
    }

    public ResourceContainer create(File dir, HashMap<String, Object> manifest) throws Exception {
        if(dir.exists()) throw new Exception("Resource container already exists");
        MapReader mr = new MapReader(manifest);

        // default values
        Map<String, Object> dublinCore = new HashMap<>();
        dublinCore.put("type", "");
        dublinCore.put("conformsto", "rc" + conformsTo);
        dublinCore.put("format", "");
        dublinCore.put("identifier", "");
        dublinCore.put("title", "");
        dublinCore.put("subject", "");
        dublinCore.put("description", "");
        dublinCore.put("language", new HashMap());
        dublinCore.put("source", new ArrayList());
        dublinCore.put("rights", "");
        dublinCore.put("creator", "");
        dublinCore.put("contributor", new ArrayList());
        dublinCore.put("relation", new ArrayList());
        dublinCore.put("publisher", "");
        dublinCore.put("issued", "");
        dublinCore.put("modified", "");
        dublinCore.put("version", "");

        Map<String, Object> checking = new HashMap<>();
        checking.put("checking_entitiy", new ArrayList());
        checking.put("checking_level", "");

        List projects = new ArrayList();

        // validate user input
        if(mr.get("dublin_core").get("type").isNull()) {
            throw new Error("Missing required key: dublin_core.type");
        }
        if(mr.get("dublin_core").get("format").isNull()) {
            throw new Error("Missing required key: dublin_core.format");
        }
        if(mr.get("dublin_core").get("identifier").isNull()) {
            throw new Error("Missing required key: dublin_core.identifier");
        }
        if(mr.get("dublin_core").get("language").isNull()) {
            throw new Error("Missing required key: dublin_core.language");
        }
        if(mr.get("dublin_core").get("rights").isNull()) {
            throw new Error("Missing required key: dublin_core.rights");
        }

        // merge defaults
        dublinCore.putAll((Map)mr.get("dublin_core").value());
        if(!mr.get("checking").isNull()) {
            checking.putAll((Map) mr.get("checking").value());
        }
        if(!mr.get("projects").isNull()) {
            projects.addAll((List) mr.get("projects").value());
        }

        HashMap newManifest = new HashMap();
        newManifest.put("dublin_core", dublinCore);
        newManifest.put("checking", checking);
        newManifest.put("projects", projects);

        // build dirs and write manifest
        dir.mkdirs();
        File manifestFile = new File(dir, "manifest.yaml");
        YamlWriter yamlWriter = new YamlWriter(new FileWriter(manifestFile));
        yamlWriter.write(newManifest);
        yamlWriter.close();

        return new ResourceContainer(dir);
    }

}
