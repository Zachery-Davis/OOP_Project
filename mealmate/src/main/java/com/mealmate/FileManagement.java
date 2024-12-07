package com.mealmate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class FileManagement{
    public static <T> T loadJsonFile(String directoryName, String fileName, TypeReference<T> typeReference) {
        File file = new File(directoryName + "/" + fileName);
        File fileBackup = new File(directoryName + "/" + fileName.replace(".json", "_backup.json"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    
        try {
            if (file.exists()) {
                return mapper.readValue(file, typeReference);
            } 
    
            if (fileBackup.exists()) {
                Files.copy(fileBackup.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return mapper.readValue(file, typeReference);
            } 
    
            System.out.println("No " + file.getName() + " file or backup found. Creating new file.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + file.getName() + ". " + 
                               (fileBackup.exists() ? "Attempting to use backup." : "Backup file not found."));
            if (fileBackup.exists()) {
                try {
                    Files.copy(fileBackup.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return mapper.readValue(file, typeReference);
                } catch (IOException backupException) {
                    System.out.println("Error reading backup file: " + fileBackup.getName());
                }
            }
        }
    
        ensureDataDirectoryExists();
        try {
            T defaultInstance = mapper.readValue("{}", typeReference);
            mapper.writeValue(file, defaultInstance);
            return defaultInstance;
        } catch (IOException createException) {
            System.out.println("Error creating new file: " + file.getName());
        }
        return null;
    }
    
    

    public static <K, V> void saveToJson(Map<K, V> dataMap, String directoryName, String fileName) {
        try {
            ensureDataDirectoryExists();
            File file = new File(directoryName + "/" + fileName);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, dataMap);

            File backupFile = new File(directoryName + "/" + fileName.replace(".json", "_backup.json"));
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Error saving to " + fileName + ": " + e.getMessage(), 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean dirCreated = dataDir.mkdirs();
            if (!dirCreated) {
                JOptionPane.showMessageDialog(null, 
                    "Could not create data directory. Please check permissions.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}