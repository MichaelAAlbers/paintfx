package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.Optional;

public class VarStarTool extends ShapeTool {

    private int numberOfPoints = 5; // Default to 5 points
    private boolean isPointsPrompted = false; // Track if the points have been prompted

    public VarStarTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);

        // Listen for changes to the toggle button
        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !isPointsPrompted) {
                // Only prompt the user when the toggle button is first selected
                promptForNumberOfPoints();
                isPointsPrompted = true; // Mark that the points have been prompted
            } else if (!newValue) {
                // Reset prompt flag when the button is toggled off
                isPointsPrompted = false;
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
        double outerRadius = Math.min(Math.abs(event.getX() - startX), Math.abs(event.getY() - startY)) / 2;
        double innerRadius = outerRadius / 2; // Adjust inner radius for star points

        drawStar(gc, centerX, centerY, outerRadius, innerRadius, fillColor, borderColor, borderWidth);
    }

    private void promptForNumberOfPoints() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(numberOfPoints)); // Prepopulate with current value
        dialog.setTitle("Star Points");
        dialog.setHeaderText("Set Star Points");
        dialog.setContentText("Enter the number of points for the star:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(points -> {
            try {
                numberOfPoints = Integer.parseInt(points);
                if (numberOfPoints < 5) {
                    numberOfPoints = 5; // Minimum of 5 points for a star
                }
            } catch (NumberFormatException e) {
                numberOfPoints = 5; // Default to 5 points if input is invalid
            }
        });
    }

    private void drawStar(GraphicsContext gc, double centerX, double centerY, double outerRadius, double innerRadius,
                          Color fillColor, Color borderColor, double borderWidth) {
        double[] xPoints = new double[numberOfPoints * 2];
        double[] yPoints = new double[numberOfPoints * 2];

        double angleStep = Math.PI / numberOfPoints; // Angle between each vertex (outer and inner points)

        // Calculate the vertices of the star
        for (int i = 0; i < numberOfPoints * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius; // Alternate between outer and inner radius
            double angle = i * angleStep - Math.PI / 2; // Start at -90 degrees (top of the star)
            xPoints[i] = centerX + Math.cos(angle) * radius;
            yPoints[i] = centerY + Math.sin(angle) * radius;
        }

        // Draw the filled star
        gc.setFill(fillColor);
        gc.fillPolygon(xPoints, yPoints, numberOfPoints * 2);

        // Draw the border of the star
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokePolygon(xPoints, yPoints, numberOfPoints * 2);
    }
}