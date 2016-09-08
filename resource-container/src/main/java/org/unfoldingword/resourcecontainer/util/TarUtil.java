package org.unfoldingword.resourcecontainer.util;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by joel on 9/7/16.
 */
public class TarUtil {

    /**
     * Places a directory in a tar
     * @param parent the directory where the path will be saved. leave null if you want to exclude the parent directory
     * @param path the path that will be added
     * @param out
     * @throws IOException
     */
    public static void tarFolder(String parent, String path, TarOutputStream out) throws IOException {
        BufferedInputStream origin = null;
        File f = new File(path);
        String files[] = f.list();

        // is file
        if (files == null) {
            files = new String[1];
            files[0] = f.getName();
        }

        if(parent == null) {
            parent = "";
        } else {
            parent += f.getName() + "/";
        }

        for (int i = 0; i < files.length; i++) {
            System.out.println("Adding: " + parent + files[i]);
            File fe = f;
            byte data[] = new byte[2048];

            if (f.isDirectory()) {
                fe = new File(f, files[i]);
            }

            if (fe.isDirectory()) {
                String[] fl = fe.list();
                if (fl != null && fl.length != 0) {
                    tarFolder(parent, fe.getPath(), out);
                } else {
                    TarEntry entry = new TarEntry(fe, parent + files[i] + "/");
                    out.putNextEntry(entry);
                }
                continue;
            }

            FileInputStream fi = new FileInputStream(fe);
            origin = new BufferedInputStream(fi);
            TarEntry entry = new TarEntry(fe, parent + files[i]);
            out.putNextEntry(entry);

            int count;

            while ((count = origin.read(data)) != -1) {
                out.write(data, 0, count);
            }

            out.flush();

            origin.close();
        }
    }
}
