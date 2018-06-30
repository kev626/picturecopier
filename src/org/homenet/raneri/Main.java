package org.homenet.raneri;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.homenet.raneri.extension.ExtensionFilter;
import org.homenet.raneri.extension.JPEGExtensionFilter;
import org.homenet.raneri.extension.RAWExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        //Prompt user for paths to get/save pictures from/to
        System.out.print("Enter path for JPEG images (D:\\pictures\\jpeg): ");
        String jpeginitialdir = input.nextLine();

        System.out.print("Enter path for RAW images (D:\\pictures\\raw): ");
        String rawinitialdir = input.nextLine();

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
        Set<String> extraJPEGs = new HashSet<>();
        for (String jpeg : jpegFiles.keySet()) {
            boolean matchFound = false;
            for (String raw : rawFiles.keySet()) {
                if (removeFileExtension(jpeg).equals(removeFileExtension(raw))) matchFound = true;
            }

            if (!matchFound) {
                extraJPEGs.add(jpeg);
                //System.out.println("JPEG file " + jpeg + " does not have a corresponding RAW file!");
            }
        }

        Set<String> extraRAWs = new HashSet<>();
        for (String raw : rawFiles.keySet()) {
            boolean matchFound = false;
            for (String jpeg : jpegFiles.keySet()) {
                if (removeFileExtension(raw).equals(removeFileExtension(jpeg))) matchFound = true;
            }

            if (!matchFound) {
                extraRAWs.add(raw);
                //System.out.println("RAW file " + raw + " does not have a corresponding JPEG file!");
            }
        }

        if (extraJPEGs.size() == 0 && extraRAWs.size() == 0) {
            System.out.println("All files are in perfect JPEG/RAW pairs.");
        } else {
            System.out.println("There are " + extraJPEGs.size() + " extra JPEGs and " + extraRAWs.size() + " extra RAWs.");
            System.out.print("Would you like to rename these files as well? [Y/n]");
            boolean renameExtras = input.nextLine().equalsIgnoreCase("Y");
            if (!renameExtras) {
                for (String jpeg : extraJPEGs) jpegFiles.remove(jpeg);
                for (String raw : extraRAWs) rawFiles.remove(raw);
            } else {
                for (String raw : extraRAWs) rawFiles.remove(raw);
            }
        }


        //Read EXIF data

        System.out.println("Reading EXIF data...");

        HashMap<String, Metadata> exifMap = getMetadata(jpegFiles);
        System.out.println("Done reading EXIF data.                                   "); //Need to clear extra progress bar text
        System.out.println("EXIF data not found for " + (jpegFiles.size() - exifMap.size()) + " files. These will not be renamed.");

        //Calculate rename actions
        List<RenameAction> renameActions = new ArrayList<>();

        DuplicateDateCounter counter = new DuplicateDateCounter();
        SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");

        for (String key : exifMap.keySet()) {
            Metadata meta = exifMap.get(key);

            ExifSubIFDDirectory directory = meta.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

            String camera = meta.getFirstDirectoryOfType(ExifIFD0Directory.class).getString(ExifIFD0Directory.TAG_MODEL);
            camera = camera.replace("ILCE-", "A");

            String pictureNumber = counter.getDateNumber(date) + "";
            if (Integer.parseInt(pictureNumber) > 99) continue;
            if (Integer.parseInt(pictureNumber) < 10) pictureNumber = "0" + pictureNumber;

            String targetFilenameNoExt = dateformatter.format(date) + "-" + pictureNumber + " " + camera.toUpperCase();
            String targetFilenameJpeg = targetFilenameNoExt + ".jpg";
            String targetFilenameRaw = targetFilenameNoExt + ".arw";

            if (jpegFiles.containsKey(key)) {
                renameActions.add(new RenameAction(jpegFiles.get(key), new File(jpeginitialdir + "\\" + targetFilenameJpeg)));
            }
            if (rawFiles.containsKey(removeFileExtension(key) + ".arw")) {
                renameActions.add(new RenameAction(rawFiles.get(removeFileExtension(key) + ".arw"), new File(rawinitialdir + "\\" + targetFilenameRaw)));
            }

        }

        List<RenameAction> removeActions = new ArrayList<>();
        for (RenameAction action : renameActions) {
            if (action.getTo().exists()) {
                removeActions.add(action);
            }
        }
        for (RenameAction action : removeActions) {
            renameActions.remove(action);
        }

        System.out.println("Will rename " + renameActions.size() + " image files.");
        System.out.print("Do you wish to continue? [Y/n]");
        if (!input.nextLine().equalsIgnoreCase("Y")) return;

        //Rename files

        System.out.println("Renaming files...");

        for (RenameAction action : renameActions) {
            try {
                action.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        System.out.println("Done.");


    }

    public static HashMap<String, Metadata> getMetadata(HashMap<String, File> files) {
        HashMap<String, Metadata> exifMap = new HashMap<>();
        double completed = 0;
        double total = files.keySet().size();
        for (String key : files.keySet()) {
            try {
                exifMap.put(key, ImageMetadataReader.readMetadata(files.get(key)));
            } catch (IOException | ImageProcessingException e) { } finally {
                completed++;
                printProgressBar(completed / total);
            }
        }
        return exifMap;
    }

    public static void printProgressBar(double percent) {
        int length = 48; //50, with 2 end characters

        System.out.print("[");

        for (int i = 0; i < length; i++) {
            System.out.print(percent*length < i ? " " : "=");
        }

        System.out.print("] " + Math.round(percent * 1000)/10d + "%\r"); //10th's place
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
