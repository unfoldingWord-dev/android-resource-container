package org.unfoldingword.resourcecontainer;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.io.File;

/**
 * Represents an instance of a resource container.
 */
public class ResourceContainer {

    /**
     * Instantiates a new resource container object
     * @param containerDirectory the directory of the resource container
     * @param containerInfo the resource container info (package.json)
     */
    private ResourceContainer(File containerDirectory, JSONObject containerInfo) {
        // TODO: 8/31/16 initialize the resource container
    }

    /**
     * Loads a resource container from the disk.
     * Rejects with an error if the container is not supported. Or does not exist, or is not a directory.
     * @param containerDirectory
     * @return
     */
    @Nullable
    public static ResourceContainer load(File containerDirectory) throws Error {
        if(!containerDirectory.exists()) throw new Error("The resource container does not exist");
        if(!containerDirectory.isDirectory()) throw new Error("Not an open resource container");

        File packageFile = new File(containerDirectory, "package.json");
        // TODO: 8/31/16 use file utilities library that I will create

        return null;
    }

    /**
     * Creates a new resource container.
     * Rejects with an error if the container exists.
     * @param containerDirectory
     * @return
     */
    @Nullable
    public static ResourceContainer make(File containerDirectory) {
        // TODO: 8/31/16  make it!
        return null;
    }


    /**
     * Opens an archived resource container.
     * If the container is already opened it will be loaded
     * @param containerArchive
     * @param containerDirectory
     * @return
     */
    @Nullable
    public static ResourceContainer open(File containerArchive, File containerDirectory) {
        // TODO: 8/31/16 open it!
        return null;
    }

    /**
     * Closes (archives) a resource container.
     * @param containerDirectory
     * @return
     */
    @Nullable
    public static ResourceContainer close(File containerDirectory) {
        // TODO: 8/31/16 close it!
        return null;
    }

    /**
     * Specifies the valid resource container types
     */
    public enum Type {
        BOOK("book"),
        HELP("help"),
        DICTIONARY("dict"),
        MANUAL("man");

        final String name;
        Type(String name) {
            this.name = name;
        }
    }
}
