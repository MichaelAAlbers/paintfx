package org.example.paintfx;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class AutosaveNotifier {

    private TrayIcon trayIcon;

    public AutosaveNotifier() {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("autosave_icon.png");  // Replace with your icon path

                // Create the tray icon with an autosave image and tooltip
                trayIcon = new TrayIcon(image, "Autosave Notification");
                trayIcon.setImageAutoSize(true);  // Automatically adjust icon size
                tray.add(trayIcon);

            } catch (Exception e) {
                System.err.println("System tray is not supported or an error occurred.");
                e.printStackTrace();
            }
        } else {
            System.out.println("SystemTray is not supported.");
        }
    }

    // Method to display the autosave notification
    public void showNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, MessageType.INFO);
        }
    }

    // Method to turn off autosave notifications
    public void removeTrayIcon() {
        if (trayIcon != null && SystemTray.getSystemTray() != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
}