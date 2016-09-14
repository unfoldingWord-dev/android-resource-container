package org.unfoldingword.resourcecontainer;

import android.annotation.SuppressLint;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.unfoldingword.resourcecontainer.util.FileUtil;
import org.unfoldingword.resourcecontainer.util.TarUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an instance of a resource container.
 */
public class ResourceContainer {
    private static final String CONTENT_DIR = "content";

    /**
     * Returns the path to the resource container directory
     */
    public final File path;

    /**
     * Returns the resource container package information.
     * This is the package.json file
     */
    public final JSONObject info;

    /**
     * Returns the resource container data configuration.
     * This is the config.yml file under the content/ directory
     */
    public final Map config;

    /**
     * Returns the table of contents.
     * This is the toc.yml file under the content/ directory.
     * This can be a list or a map.
     */
    public final Object toc;

    /**
     * Returns the slug of the resource container
     */
    public final String slug;

    /**
     * Instantiates a new resource container object
     * @param containerDirectory the directory of the resource container
     * @param containerInfo the resource container info (package.json)
     * @throws JSONException
     */
    private ResourceContainer(File containerDirectory, JSONObject containerInfo) throws JSONException {
        this.path = containerDirectory;
        this.info = containerInfo;
        this.slug = ContainerTools.makeSlug(
                containerInfo.getJSONObject("language").getString("slug"),
                containerInfo.getJSONObject("project").getString("slug"),
                containerInfo.getJSONObject("resource").getString("slug")
                );

        // load config
        File configFile = new File(containerDirectory, CONTENT_DIR + "/config.yml");
        Map tempConfig = null;
        try {
            YamlReader reader = new YamlReader(new FileReader(configFile));
            Object object = reader.read();
            tempConfig = (Map)object;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (YamlException e) {
            e.printStackTrace();
        } finally {
            if(tempConfig == null) tempConfig = new HashMap();
        }
        this.config = tempConfig;

        // load toc
        File tocFile = new File(containerDirectory, CONTENT_DIR + "/toc.yml");
        Object tempToc = null;
        try {
            YamlReader reader = new YamlReader(new FileReader(tocFile));
            tempToc = reader.read();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (YamlException e) {
            e.printStackTrace();
        } finally {
            if(tempToc == null) tempToc = new HashMap();
        }
        this.toc = tempToc;
    }

    /**
     * Loads a resource container from the disk.
     * Rejects with an error if the container is not supported. Or does not exist, or is not a directory.
     * @param containerDirectory
     * @throws Exception
     * @return
     */
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
     * @param opts
     * @throws Exception
     * @return
     */
    public static ResourceContainer make(File containerDirectory, JSONObject opts) throws Exception {
        if(containerDirectory.exists()) throw new Exception("Resource container directory already exists");
        // TODO: finish this
        throw new Exception("Not implemented yet!");
    }


    /**
     * Opens an archived resource container.
     * If the container is already opened it will be loaded
     * @param containerArchive
     * @param containerDirectory
     * @throws Exception
     * @return
     */
    public static ResourceContainer open(File containerArchive, File containerDirectory) throws Exception{
        if(!containerArchive.exists()) throw new Exception("Missing resource container");
        File tempFile = new File(containerArchive + ".tmp.tar");
        FileOutputStream out = null;
        BZip2CompressorInputStream bzIn = null;

        // decompress bzip2
        try {
            FileInputStream fin = new FileInputStream(containerArchive);
            BufferedInputStream in = new BufferedInputStream(fin);
            out = new FileOutputStream(tempFile);
            bzIn = new BZip2CompressorInputStream(in);
            int n;
            final byte[] buffer = new byte[2048];
            while ((n = bzIn.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            FileUtil.deleteQuietly(tempFile);
            throw e;
        } finally {
            if(out != null) out.close();
            if(bzIn != null) bzIn.close();
        }

        // un-pack
        FileInputStream fin = new FileInputStream(tempFile);
        BufferedInputStream in = new BufferedInputStream(fin);
        TarInputStream tin = new TarInputStream(in);
        try {
            containerDirectory.mkdirs();
            TarUtil.untar(tin, containerDirectory.getAbsolutePath());
        } catch (Exception e) {
            FileUtil.deleteQuietly(containerDirectory);
            throw e;
        } finally {
            tin.close();
            FileUtil.deleteQuietly(tempFile);
        }

        return load(containerDirectory);
    }

    /**
     * Closes (archives) a resource container.
     * @param containerDirectory
     * @return the path to the resource container archive
     * @throws Exception
     */
    public static File close(File containerDirectory) throws Exception {
        if(!containerDirectory.exists()) throw new Exception("Missing resource container");
        // pack
        File tempFile = new File(containerDirectory.getAbsolutePath() + ".tmp.tar");
        TarOutputStream tout = new TarOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        try {
            TarUtil.tar(null, containerDirectory.getAbsolutePath(), tout);
        } catch(Exception e) {
            FileUtil.deleteQuietly(tempFile);
            throw e;
        } finally {
            tout.close();
        }

        // compress
        File archive = new File(containerDirectory.getAbsolutePath() + "." + ContainerSpecification.fileExtension);
        BZip2CompressorOutputStream bzOut = null;
        BufferedInputStream in = null;

        try {
            FileInputStream fin = new FileInputStream(tempFile);
            in = new BufferedInputStream(fin);
            FileOutputStream out = new FileOutputStream(archive);
            bzOut = new BZip2CompressorOutputStream(out);
            int n;
            byte buffer[] = new byte[2048];
            while ((n = in.read(buffer)) != -1) {
                bzOut.write(buffer, 0, n);
            }
            bzOut.close();
            in.close();
        } catch(Exception e) {
            FileUtil.deleteQuietly(archive);
            throw e;
        } finally {
            if(bzOut != null) bzOut.close();
            if(in != null) in.close();
            FileUtil.deleteQuietly(tempFile);
        }

        return archive;
    }
}
