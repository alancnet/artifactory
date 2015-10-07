package org.artifactory.ui.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Chen Keinan
 */
public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
    private static final long MEGABYTE = 1024L * 1024L;
    public static long bytesToMeg(long bytes) {
        return bytes / MEGABYTE;
    }

    /**
     * write byte array to file
     *
     * @param filename file name
     * @param content  byte array
     */
    public static void writeFile(String filename, byte[] content) {
        File file = new File(filename);
        FileOutputStream fop = null;
        try {
            /// check if file exist
            if (!file.exists()) {
                file.createNewFile();
            }
            // save file
            fop = new FileOutputStream(file);
            fop.write(content);
            fop.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
    }


    /**
     * copy input stream to file
     *
     * @param in   - input stream
     * @param file - files
     */
    public static void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    public static long bytesToMB(long sizeInBytes) {
        return sizeInBytes / (1024 * 1024);
    }

}
