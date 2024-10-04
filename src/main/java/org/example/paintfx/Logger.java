package org.example.paintfx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Logger {
    private final ExecutorService executorService;
    private final String logFilePath;

    // Constructor that initializes the ExecutorService and generates a unique log file path
    public Logger() {
        this.executorService = Executors.newSingleThreadExecutor();  // Single-threaded executor for logging

        // Define the log directory path (relative to the current working directory)
        String logDirectory = System.getProperty("user.dir") + File.separator + "logs";

        // Ensure that the "logs" directory exists; if not, create it
        File directory = new File(logDirectory);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Created directory: " + logDirectory);
            } else {
                System.out.println("Failed to create directory: " + logDirectory);
            }
        }

        // Generate a unique log file name based on the current date and time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        this.logFilePath = logDirectory + File.separator + "log_" + timeStamp + ".txt";  // logs/log_20240919_113300.txt
    }

    // Method to log an event, runs on a separate thread
    public void logEvent(String tabName, String action) {
        executorService.submit(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
                // Format the current date and time
                String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());

                // Create the log message
                String logMessage = String.format("%s [%s] %s", timeStamp, tabName, action);

                // Write the log message to the file
                writer.write(logMessage);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Method to shutdown the executor service when logging is no longer needed
    public void shutdown() {
        executorService.shutdown();
    }
}