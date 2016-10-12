package org.unfoldingword.resourcecontainer;

/**
 * Represents a link to a resource container
 */

public class Link {
    public final String title;
    public final String resource;
    public final String project;
    public final String language;
    public final String arguments;
    public final String protocal;
    public final String chapter;
    public final String chunk;
    public final String lastChunk;

    /**
     * Creates a simple external link
     * @param title the human readable title of the link
     * @param url the external link address
     */
    public Link(String title, String url) {
        this.title = title;

        protocal = null;
        resource = null;
        project = null;
        chapter = null;
        chunk = null;
        lastChunk = null;
        arguments = null;
        language = null;
    }

    /**
     * Creates a new resource container link.
     *
     * @param protocal used to indicate if this is a media link
     * @param title the human readable title of the link
     * @param language the language of the linked resource container
     * @param project the project of the linked resource container
     * @param resource the resource of the linked resource container
     * @param arguments the raw arguments on the link
     * @param chapter the chapter in the linked resource container
     * @param chunk the chunk (first one if the arguments included a range of chunks) in the linked resource container
     * @param lastChunk the last chunk (if the arguments included a range of chunks) referenced by this link
     */
    public Link(String protocal, String title, String language, String project, String resource, String arguments, String chapter, String chunk, String lastChunk) {
        this.protocal = protocal;
        this.title = title;
        this.language = language;
        this.project = project;
        this.resource = resource;
        this.arguments = arguments;
        this.chapter = chapter;
        this.chunk = chunk;
        this.lastChunk = lastChunk;
    }
}
