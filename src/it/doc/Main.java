package it.doc;

import it.doc.support.FileSupport;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class Main {

    public static final String COPY_FILE = ".\\DA COPIARE";
    public static final String UPDATE_FILE = ".\\DA AGGIORNARE E CARICARE";

    public static final String FILE_TO_SEARCH = "/assets/js/CPM.js";

    public static void main(String[] args) throws IOException {
        JOptionPane.showMessageDialog(null, "Premi su OK e attendi fino al messaggio di fine... Potrebbe volerci un po", "InfoBox: Attendere", JOptionPane.INFORMATION_MESSAGE);
        File updateDirectory = FileSupport.prepareFile(UPDATE_FILE,true,true);
        String[] allCourses = updateDirectory.list();
        HashSet<String> map = new HashSet<>();
        map.addAll(Arrays.asList(allCourses));
        for(String course : allCourses){
            File courseDir = FileSupport.prepareFile(UPDATE_FILE+"/"+course,true,true);
            String[] allCourseZipLanguages = courseDir.list();
            for(String courseZipLanguage : allCourseZipLanguages){
                if(courseZipLanguage.contains(".zip")) {
                    String directory = FileSupport.unzipFile(UPDATE_FILE + "/" + course + "/" + courseZipLanguage);
                    String copyPath = FileSupport.unzipFile(COPY_FILE + "/" + course + "/" + courseZipLanguage);
                    File fileToSearch = FileSupport.prepareFile(directory + FILE_TO_SEARCH, true, true);
                    File fileToCopy = FileSupport.prepareFile(copyPath + FILE_TO_SEARCH, true, true);
                    FileSupport.copyAndSubstitute(fileToCopy,fileToSearch);
                }
            }
            allCourseZipLanguages = courseDir.list();
            for(String courseZipLanguage : allCourseZipLanguages){
                if(!courseZipLanguage.contains(".zip")) {
                    File dir = FileSupport.prepareFile(UPDATE_FILE + "/" + course + "/" + courseZipLanguage, true, true);
                    FileSupport.zip(Arrays.asList(dir.listFiles()),UPDATE_FILE + "/" + course + "/" + courseZipLanguage+".zip");
                    FileSupport.deleteDirectory(dir);
                }
            }
            System.out.println("Finito di processare il corso: " + course);
        }
        JOptionPane.showMessageDialog(null, "Finito", "InfoBox: Finito", JOptionPane.INFORMATION_MESSAGE);
    }



}
