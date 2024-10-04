package org.example.paintfx;

import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconButtonFactory {

    // Hardcoded path to the icon folder in the resources directory
    private static final String ICON_PATH = "/icons/";

    // Method to create a toggle button with an icon and tooltip
    public ToggleButton createIconToggleButton(String iconFileName, String tooltipText) {
        // Load the image icon from the hardcoded path
        Image icon = new Image(getClass().getResourceAsStream(ICON_PATH + iconFileName));
        ImageView iconView = new ImageView(icon);

        // Optionally set icon size
        iconView.setFitWidth(30);  // Adjust width
        iconView.setFitHeight(30);  // Adjust height

        // Create a toggle button with the icon
        ToggleButton button = new ToggleButton();
        button.setGraphic(iconView);

        // Set the tooltip with the text label
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);

        return button;
    }

    // Method to create a regular button with an icon and tooltip
    public Button createIconButton(String iconFileName, String tooltipText) {
        // Load the image icon from the hardcoded path
        Image icon = new Image(getClass().getResourceAsStream(ICON_PATH + iconFileName));
        ImageView iconView = new ImageView(icon);

        // Optionally set icon size
        iconView.setFitWidth(30);  // Adjust width
        iconView.setFitHeight(30);  // Adjust height

        // Create a regular button with the icon
        Button button = new Button();
        button.setGraphic(iconView);

        // Set the tooltip with the text label
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);

        return button;
    }
}
