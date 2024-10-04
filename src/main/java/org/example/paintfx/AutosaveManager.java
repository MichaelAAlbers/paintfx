package org.example.paintfx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AutosaveManager {

    private final Canvas canvas;
    private final Label countdownLabel;
    private final CheckBox displayCountdownCheckBox, enableNotificationsCheckBox;
    private Timer autosaveTimer;
    private int autosaveIntervalSeconds = 30;  // Default autosave interval
    private int countdown;

    private final AutosaveNotifier notifier;

    public AutosaveManager(Canvas canvas, Label countdownLabel, CheckBox displayCountdownCheckBox, CheckBox enableNotificationsCheckBox) {
        this.canvas = canvas;
        this.countdownLabel = countdownLabel;
        this.displayCountdownCheckBox = displayCountdownCheckBox;
        this.enableNotificationsCheckBox = enableNotificationsCheckBox;
        this.notifier = new AutosaveNotifier();
    }

    public void startAutosave() {
        autosaveTimer = new Timer();
        countdown = autosaveIntervalSeconds;

        autosaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (countdown <= 0) {
                        autosaveCanvas();
                        countdown = autosaveIntervalSeconds;  // Reset the countdown after autosave
                    }

                    // Update countdown label if visible
                    if (displayCountdownCheckBox.isSelected()) {
                        countdownLabel.setText("Autosave in: " + countdown + " seconds");
                    } else {
                        countdownLabel.setText("");  // Hide the countdown
                    }

                    countdown--;
                });
            }
        }, 0, 1000);  // Run every second
    }

    public void stopAutosave() {
        if (autosaveTimer != null) {
            autosaveTimer.cancel();  // Stop the autosave timer
            autosaveTimer.purge();   // Removes all canceled tasks from the timer
            autosaveTimer = null;    // Set the timer to null to ensure it stops running
            System.out.println("Autosave stopped.");
        }
    }

    // Triggered when the timer hits 0 or user manually saves
    void autosaveCanvas() {
        File autosaveFile = new File("autosave.png");
        try {
            saveImageToFile(autosaveFile);
            System.out.println("Canvas autosaved to: " + autosaveFile.getAbsolutePath());


            if (enableNotificationsCheckBox.isSelected()) {
                notifier.showNotification("Autosave", "Your work has been autosaved.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save canvas content to file (used for both manual save and autosave)
    public void saveImageToFile(File file) throws IOException {
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        canvas.snapshot(params, writableImage);

        BufferedImage bufferedImage = convertWritableImageToBufferedImage(writableImage);

        // Save as PNG
        ImageIO.write(bufferedImage, "png", file);
    }

    // Reset the timer (useful if the user manually saves)
    public void resetTimer() {
        countdown = autosaveIntervalSeconds;
    }

    // Add your method for converting WritableImage to BufferedImage (from the previous example)
    private BufferedImage convertWritableImageToBufferedImage(WritableImage writableImage) {
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        // Create a BufferedImage to hold the pixels from the WritableImage
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Extract pixel data from WritableImage
        PixelReader pixelReader = writableImage.getPixelReader();

        // Write each pixel to the BufferedImage
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int argb = (int) (color.getOpacity() * 255) << 24 |
                        (int) (color.getRed() * 255) << 16 |
                        (int) (color.getGreen() * 255) << 8 |
                        (int) (color.getBlue() * 255);
                bufferedImage.setRGB(x, y, argb);
            }
        }
        return bufferedImage;
    }
}
