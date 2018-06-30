package org.homenet.raneri;

import org.homenet.raneri.extension.ExtensionFilter;
import org.homenet.raneri.extension.JPEGExtensionFilter;
import org.homenet.raneri.extension.RAWExtensionFilter;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        System.out.print("Enter path for JPEG images (D:\\pictures\\jpeg): ");
        String jpeginitialdir = input.nextLine();

        System.out.print("Enter path for RAW images (D:\\pictures\\raw): ");
        String rawinitialdir = input.nextLine();

        System.out.print("Enter destination for JPEG images (D:\\renamed\\jpeg): ");
        String jpegdestdir = input.nextLine();

        System.out.print("Enter destination for RAW images (D:\\renamed\\raw): ");
        String rawdestdir = input.nextLine();

        System.out.println("Scanning JPEG images...");
        HashMap<String, File> jpegFiles; //<filename.jpeg, File>
        jpegFiles = getFilesByExtensionType(jpeginitialdir, new JPEGExtensionFilter());
        if (jpegFiles.size() > 0) {
            System.out.println("Found " + jpegFiles.size() + " JPEG images");
        } else {
            System.out.println("Didn't find any JPEG images.");
            System.exit(0);
        }

        System.out.println("Scanning RAW images...");
        HashMap<String, File> rawFiles; //<filename.jpeg, File>
        rawFiles = getFilesByExtensionType(rawinitialdir, new RAWExtensionFilter());
        if (rawFiles.size() > 0) {
            System.out.println("Found " + rawFiles.size() + " RAW images");
        } else {
            System.out.println("Didn't find any RAW images.");
            System.exit(0);
        }
    }

    /**
     *
     * @param dirpath Path to scan for files
     * @return HashMap<filename, File> a map of files
     */
    public static HashMap<String, File> getFilesByExtensionType(String dirpath, ExtensionFilter filter) {
        HashMap<String, File> fileMap = new HashMap<>();

        File dir = new File(dirpath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (filter.check(file.getName())) {
                    fileMap.put(file.getName(), file);
                }
            }
            return fileMap;
        } else {
            System.err.println(dirpath + " is not a directory!");
            System.exit(1);
        }
        return null;
    }
}
