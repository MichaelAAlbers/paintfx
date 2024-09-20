package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Optional;

public class TextTool extends ShapeTool {

    private String enteredText = null;
    private double fontSize = 50;  // Default font size

    public TextTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);
        setupToggleListener();
    }

    private void setupToggleListener() {
        // Listen for changes in the toggle button state
        toggleButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // When the toggle button is first selected, prompt the user for new text
                promptForText();
                enableTextTool();
            } else {
                // Disable the tool when the toggle button is deselected
                disableTextTool();
            }
        });
    }

    private void promptForText() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input Text");
        dialog.setHeaderText("Enter the text to display:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(text -> enteredText = text);  // Save the entered text
    }

    private void enableTextTool() {
        // Add event handler to start drawing text when the tool is active
        gc.getCanvas().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
    }

    private void disableTextTool() {
        // Remove event handler to stop drawing text when the tool is disabled
        gc.getCanvas().removeEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        enteredText = null;  // Reset the entered text when the tool is deselected
    }

    @Override
    public void onMousePressed(MouseEvent event) {
        if (enteredText != null) {
            // Draw the text on the canvas at the clicked position
            gc.setFill(fillColor);
            gc.setStroke(borderColor);
            gc.setLineWidth(1);
            gc.setFont(Font.font(fontSize));
            gc.fillText(enteredText, event.getX(), event.getY());
            gc.strokeText(enteredText, event.getX(), event.getY());
        }
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        // No dragging behavior needed for the TextTool
    }

    @Override
    public void onMouseReleased(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        // No specific release behavior needed for the TextTool
    }
}