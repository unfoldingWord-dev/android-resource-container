package org.unfoldingword.resource_container;

import org.json.JSONObject;

import java.io.File;
import java.security.InvalidParameterException;

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
     * @param language the language of the content
     * @param project  the project of the content
     * @param resource the resource of the content
     * @param containerType the type of container to be generated
     * @return the newly converted container
     */
    public static ResourceContainer convertResource(String data, File directory, Language language, Project project, Resource resource, ResourceContainer.Type containerType) {
        // TODO: 8/31/16 build conversion tool
        return null;
    }

    /**
     * Returns a properly formatted container slug.
     * @param languageSlug
     * @param projectSlug
     * @param containerType
     * @param resourceSlug
     * @return
     */
    public static String makeSlug(String languageSlug, String projectSlug, ResourceContainer.Type containerType, String resourceSlug) {
        if(languageSlug == null || languageSlug.isEmpty()
                || projectSlug == null || projectSlug.isEmpty()
                || containerType == null
                || resourceSlug == null || resourceSlug.isEmpty()) throw new InvalidParameterException("Invalid resource container slug parameters");
        return languageSlug + "_" + projectSlug + "_" + containerType + "_" + resourceSlug;
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
     * @param containerType the resource container type. e.g. "book", "help" etc.
     * @return The mime type. e.g. "application/ts+type"
     */
    public static String typeToMime(ResourceContainer.Type containerType) {
        return "application/ts+" + containerType;
    }
}
