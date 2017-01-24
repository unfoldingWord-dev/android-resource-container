package org.unfoldingword.resourcecontainer;

import java.io.File;
import java.util.HashMap;

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
            if(rc.manifest == null || rc.conformsTo() == null) throw new Exception("Not a resource container");
            if(Semver.gt(rc.conformsTo(), conformsTo)) throw new Exception("Unsupported resource container version. Found " + rc.conformsTo() + " but expected " + conformsTo);
            if(Semver.lt(rc.conformsTo(), conformsTo)) throw new Exception("Outdated resource container version. Found " + rc.conformsTo() + " but expected " + conformsTo);
        }

        return rc;
    }

    public ResourceContainer create(File dir, HashMap manifest) throws Exception {
        if(dir.exists()) throw new Exception("Resource container already exists");

        // TODO: 1/24/17 finish

        return null;
    }

}
