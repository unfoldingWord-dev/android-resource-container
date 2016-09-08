package org.unfoldingword.resourcecontainer;

import android.content.ContentValues;
import android.support.annotation.Nullable;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.json.JSONObject;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;
import org.kamranzafar.jtar.TarUtils;
import org.unfoldingword.resourcecontainer.util.FileUtil;
import org.unfoldingword.resourcecontainer.util.TarUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

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
     * @throws Exception
     * @return
     */
    @Nullable
    public static ResourceContainer load(File containerDirectory) throws Exception {
        if(!containerDirectory.exists()) throw new Exception("The resource container does not exist");
        if(!containerDirectory.isDirectory()) throw new Exception("Not an open resource container");
        File packageFile = new File(containerDirectory, "package.json");
        if(!packageFile.exists()) throw new Exception("Not a resource container");
        JSONObject packageJson = new JSONObject(FileUtil.readFileToString(packageFile));
        if(!packageJson.has("package_version")) throw new Exception("Not a resource container");
        if(packageJson.getInt("package_version") > ContainerSpecification.version) throw new Exception("Unsupported container version");
        if(packageJson.getInt("package_version") < ContainerSpecification.version) throw new Exception("Outdated container version");

        return new ResourceContainer(containerDirectory, packageJson);
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
    public static ResourceContainer open(File containerArchive, File containerDirectory) throws Exception{
        File tempFile = new File(containerArchive, ".tmp.tar");

        // decompress bzip2
        CBZip2InputStream in = new CBZip2InputStream(new FileInputStream(containerArchive));
        OutputStream out = FileUtil.openOutputStream(tempFile);



        // TODO: un-pack tar
        return null;
    }

    /**
     * Closes (archives) a resource container.
     * @param containerDirectory
     * @return the path to the resource container archive
     * @throws Exception
     */
    @Nullable
    public static File close(File containerDirectory) throws Exception {
        // pack
        File tempFile = new File(containerDirectory.getAbsolutePath() + ".tmp.tar");
        TarOutputStream tarStream = new TarOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        TarUtil.tarFolder(null, containerDirectory.getAbsolutePath(), tarStream);
        tarStream.close();

        // compress
        // TODO: 9/7/16 the compression does not work
        File archive = new File(containerDirectory.getAbsolutePath() + "." + ContainerSpecification.fileExtension + ".tar");
        BufferedInputStream origin = new BufferedInputStream(new FileInputStream(tempFile));
        CBZip2OutputStream bzip2Stream = new CBZip2OutputStream(new BufferedOutputStream(new FileOutputStream(archive)));
        int count;
        byte data[] = new byte[2048];
        while((count = origin.read(data)) != -1) {
            bzip2Stream.write(data, 0, count);
        }
        bzip2Stream.flush();
        origin.close();
        bzip2Stream.close();

        return archive;
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
