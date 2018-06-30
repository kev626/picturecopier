package org.homenet.raneri.extension;

public class JPEGExtensionFilter implements ExtensionFilter {

    @Override
    public boolean check(String filename) {
        return filename.toLowerCase().endsWith(".jpg") ||
                filename.toLowerCase().endsWith(".jpeg");
    }
}
