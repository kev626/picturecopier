package org.homenet.raneri.extension;

public class RAWExtensionFilter implements ExtensionFilter {
    @Override
    public boolean check(String filename) {
        return filename.toLowerCase().endsWith(".arw");
    }
}
