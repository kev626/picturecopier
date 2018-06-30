package org.homenet.raneri.extension;

public interface ExtensionFilter {
    /**
     * Checks if the given filename passes an extension check
     * @param filename Name of the file to test
     * @return If it is a given filetype
     */
    public boolean check(String filename);
}
