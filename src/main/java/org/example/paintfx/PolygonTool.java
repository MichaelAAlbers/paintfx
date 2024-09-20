package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.Optional;

public class PolygonTool extends ShapeTool {

    private int numberOfSides = 5; // Default to 5 sides
    private boolean isSidesPrompted = false; // Track if the sides have been prompted

    public PolygonTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);

        // Listen for changes to the toggle button
        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !isSidesPrompted) {
                // Only prompt the user when the toggle button is first selected
                promptForNumberOfSides();
                isSidesPrompted = true; // Mark that the sides have been prompted
            } else if (!newValue) {
                // Reset prompt flag when the button is toggled off
                isSidesPrompted = false;
            }
        });
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }

        gc.drawImage(canvasSnapshot, 0, 0);

        double centerX = (startX + event.getX()) / 2;
        double centerY = (startY + event.getY()) / 2;
        double radius = Math.min(Math.abs(event.getX() - startX), Math.abs(event.getY() - startY)) / 2;

        drawPolygon(gc, centerX, centerY, radius, fillColor, borderColor, borderWidth);
    }

    private void promptForNumberOfSides() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(numberOfSides)); // Prepopulate with current value
        dialog.setTitle("Polygon Sides");
        dialog.setHeaderText("Set Polygon Sides");
        dialog.setContentText("Enter the number of sides for the polygon:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(sides -> {
            try {
                numberOfSides = Integer.parseInt(sides);
                if (numberOfSides < 3) {
                    numberOfSides = 3; // Minimum of 3 sides (triangle)
                }
            } catch (NumberFormatException e) {
                numberOfSides = 5; // Default to 5 sides if input is invalid
            }
        });
    }

    private void drawPolygon(GraphicsContext gc, double centerX, double centerY, double radius,
                             Color fillColor, Color borderColor, double borderWidth) {
        double[] xPoints = new double[numberOfSides];
        double[] yPoints = new double[numberOfSides];

        double angleStep = 2 * Math.PI / numberOfSides; // Angle between each vertex

        // Calculate the vertices of the polygon
        for (int i = 0; i < numberOfSides; i++) {
            double angle = i * angleStep - Math.PI / 2; // Start at -90 degrees (top of the polygon)
            xPoints[i] = centerX + Math.cos(angle) * radius;
            yPoints[i] = centerY + Math.sin(angle) * radius;
        }

        // Draw the filled polygon
        gc.setFill(fillColor);
        gc.fillPolygon(xPoints, yPoints, numberOfSides);

        // Draw the border of the polygon
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokePolygon(xPoints, yPoints, numberOfSides);
    }
}