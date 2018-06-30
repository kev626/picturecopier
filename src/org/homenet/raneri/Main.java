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

        //Prompt user for paths to get/save pictures from/to

        System.out.print("Enter path for JPEG images (D:\\pictures\\jpeg): ");
        String jpeginitialdir = input.nextLine();

        System.out.print("Enter path for RAW images (D:\\pictures\\raw): ");
        String rawinitialdir = input.nextLine();

        System.out.print("Enter destination for JPEG images (D:\\renamed\\jpeg): ");
        String jpegdestdir = input.nextLine();

        System.out.print("Enter destination for RAW images (D:\\renamed\\raw): ");
        String rawdestdir = input.nextLine();

        //Scan pictures in directories

        HashMap<String, File> jpegFiles; //<filename.jpeg, File>
        jpegFiles = getFilesByExtensionType(jpeginitialdir, new JPEGExtensionFilter());
        if (jpegFiles.size() > 0) {
            System.out.println("Found " + jpegFiles.size() + " JPEG images");
        } else {
            System.out.println("Didn't find any JPEG images.");
            System.exit(0);
        }

        HashMap<String, File> rawFiles; //<filename.jpeg, File>
        rawFiles = getFilesByExtensionType(rawinitialdir, new RAWExtensionFilter());
        if (rawFiles.size() > 0) {
            System.out.println("Found " + rawFiles.size() + " RAW images");
        } else {
            System.out.println("Didn't find any RAW images.");
            System.exit(0);
        }

        //Find pictures which have matches

        System.out.println("Checking to make sure all pictures have matches...");
        int extraJPEGs = 0;
        for (String jpeg : jpegFiles.keySet()) {
            boolean matchFound = false;
            for (String raw : rawFiles.keySet()) {
                if (removeFileExtension(jpeg).equals(removeFileExtension(raw))) matchFound = true;
            }

            if (!matchFound) {
                extraJPEGs++;
                //System.out.println("JPEG file " + jpeg + " does not have a corresponding RAW file!");
            }
        }

        int extraRAWs = 0;
        for (String raw : rawFiles.keySet()) {
            boolean matchFound = false;
            for (String jpeg : jpegFiles.keySet()) {
                if (removeFileExtension(raw).equals(removeFileExtension(jpeg))) matchFound = true;
            }

            if (!matchFound) {
                extraRAWs++;
                //System.out.println("RAW file " + raw + " does not have a corresponding JPEG file!");
            }
        }

        if (extraJPEGs == 0 && extraRAWs == 0) {
            System.out.print("All files are in perfect JPEG/RAW pairs. Do you wish to continue? [Y/n] ");
        } else {
            System.out.print("There are " + extraJPEGs + " extra JPEGs and " + extraRAWs + " extra RAWs. Are you sure you wish to continue? [Y/n] ");
        }


    }

    /**
     * Removes the file extension from a filename
     * @param filename The filename to remove the extension from
     * @return The filename without an extension
     */
    public static String removeFileExtension(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
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
