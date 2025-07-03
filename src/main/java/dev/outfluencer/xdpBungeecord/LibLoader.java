package dev.outfluencer.xdpBungeecord;

import com.google.common.io.ByteStreams;

import java.io.*;

public class LibLoader {

    public static boolean load() {
        if (!isSupportedPlatformAndArch()) return false;

        try (InputStream soFile = LibLoader.class.getClassLoader().getResourceAsStream("libaya_map_jni_bindings.so")) {
            // Else we will create and copy it to a temp file
            File temp = File.createTempFile("libaya_map_jni_bindings", ".so");
            // Don't leave cruft on filesystem
            temp.deleteOnExit();

            try (OutputStream outputStream = new FileOutputStream(temp)) {
                ByteStreams.copy(soFile, outputStream);
            }

            System.load(temp.getPath());
            return true;
        } catch (IOException ex) {
            // Can't write to tmp?
            ex.printStackTrace();
        } catch (UnsatisfiedLinkError ex) {
            ex.printStackTrace();
        }
        return false;
    }


    private static boolean isSupportedPlatformAndArch() {
        return "Linux".equals(System.getProperty("os.name")) && (isAmd64());
    }

    private static boolean isAmd64() {
        return "amd64".equals(System.getProperty("os.arch"));
    }


}
