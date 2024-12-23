package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JumpHostFileUtils {
    private DatabaseManager databaseManager = new DatabaseManager();

    private static HostInfo createHostInfo(String hostname, String fileName, String jumpuserPassword) {
        return new HostInfo(hostname, fileName, jumpuserPassword);
    }

    public List<HostInfo> getHostsFromDirectory(String directoryPath, String rootPath) {
        List<HostInfo> hosts = new ArrayList<>();
        File directory = new File(directoryPath);
        File rootDirectory = new File(rootPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        int jumpUserIndex = fileName.indexOf("_");
                        if (jumpUserIndex != -1) {
                            String hostname = fileName.substring(0, jumpUserIndex);
                       
                            try {
                                String jumpuserPassword = getPasswordFromFile(directoryPath, fileName);
                                hosts.add(createHostInfo(hostname, fileName, jumpuserPassword));
                                databaseManager.saveHostInfo(hostname, fileName, jumpuserPassword);
                                System.out.println("Found host: " + hostname + " (from file: " + fileName + ")");
                            } catch (IOException e) {
                                System.err.println("Error reading jumpuserpassword for host " + hostname + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
  
        return hosts;
    }

    

    public static String getPasswordFromFile(String directoryPath, String hostname) throws IOException {
        File passwordFile = new File(directoryPath, hostname);
        
        // Debugging: Check if the constructed path is correct
        System.out.println("Checking password file at: " + passwordFile.getAbsolutePath());
        
        if (passwordFile.exists() && passwordFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(passwordFile)))) {
                return reader.readLine();
            }
        }
        throw new IOException("Password file not found or empty for host: " + hostname);
    }
} 
