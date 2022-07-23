package it.doc.support;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileSupport {
    private static String LINK_ALIENTECH = "https://courses.alientech.academy";
    private static String LINK_DOC = "https://www.docdonkey.com/user-account";

    public static final String TEXT_TO_SEARCH_ITA = "Clicca per uscire!";
    public static final String TEXT_TO_INSERT_ITA = "Clicca per rincominciare!";
    public static final String TEXT_TO_SEARCH_ENG = "Click to exit!";
    public static final String TEXT_TO_INSERT_ENG = "Click to restart!";
    private static final int BUFFER_SIZE = 4096;
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void zip(List<File> listFiles, String destZipFile) throws
            IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zos);
            } else {
                zipFile(file, zos);
            }
        }
        zos.flush();
        zos.close();
    }

    private static void zipDirectory(File folder, String parentFolder,
                              ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
            bis.close();
            zos.closeEntry();
        }
    }

    private static void zipFile(File file, ZipOutputStream zos)
            throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        bis.close();
        zos.closeEntry();
    }

    public static String unzipFile(String zipFile) throws IOException {
        String outputDirectory = zipFile.replace(".zip","");
        File destDir = new File(outputDirectory);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return outputDirectory;
    }

    public static File prepareFile(String path, boolean readable, boolean writable) {
        File file = new File(path);
        if(!file.canRead()) { file.setReadable(readable); }
        if(!file.canWrite()) { file.setWritable(writable); }
        return file;
    }

    public static void copyAndSubstitute(File fileToCopy, File fileToPaste){
        File tmp = prepareFile(fileToPaste.toString()+"_tmp",true,true);
        String first= null;
        String second = null;
        try {
            Scanner scannerCopy = new Scanner(fileToCopy);
            while(scannerCopy.hasNextLine()) {
                String line = scannerCopy.nextLine();
                if(line.contains(TEXT_TO_SEARCH_ITA) || line.contains(TEXT_TO_SEARCH_ENG)){
                    while(line.contains(TEXT_TO_SEARCH_ITA)) {
                        int index = line.indexOf(TEXT_TO_SEARCH_ITA);
                        int lastIndex = line.indexOf("!",index)+1;
                        line = line.substring(0,index) + TEXT_TO_INSERT_ITA + line.substring(lastIndex);
                    }
                    while(line.contains(TEXT_TO_SEARCH_ENG)) {
                        int index = line.indexOf(TEXT_TO_SEARCH_ENG);
                        int lastIndex = line.indexOf("!",index)+1;
                        line = line.substring(0,index) + TEXT_TO_INSERT_ENG + line.substring(lastIndex);
                    }
                    if(first == null){
                        first = line;
                    }else{
                        second = line;
                    }
                }
            }
            Scanner scanner = new Scanner(fileToPaste);
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                while(line.contains(TEXT_TO_SEARCH_ITA) || line.contains(TEXT_TO_SEARCH_ENG)) {
                    if(first == null){
                        line = second;
                        second = null;
                    }else{
                        line = first;
                        first = null;
                    }
                }
                while(line.contains(LINK_ALIENTECH)) {
                    line = replaceLink(line);
                }
                writeLine(tmp,line);
            }
            scanner.close();
            fileToPaste.delete();
            tmp.renameTo(fileToPaste);
            tmp.delete();
        } catch (FileNotFoundException e) {
            System.out.print("file cannot be found?");
        }
    }

    public static void writeLine(File file, String line) {
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.append(line+"\n");
            writer.close();
        } catch (IOException e) {
            System.out.print("cannot create writer");
            return;
        }
    }

    public static void deleteDirectory(File directory){
        if(directory.isDirectory() && directory.list().length>0){
           File[] files = directory.listFiles();
           for(File file : files){
               deleteDirectory(file);
           }
        }
        directory.delete();
    }

    public static String replaceLink(String line){
        while(line.contains(LINK_ALIENTECH)) {
            int index = line.indexOf(LINK_ALIENTECH);
            int lastIndex = line.indexOf("\\",index);
            String toReplace = line.substring(index,lastIndex);
            line = line.replace(toReplace,LINK_DOC);
        }
        return line;
    }
}
