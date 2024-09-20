package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class StarTool extends ShapeTool {

    public StarTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);
    }

    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }

        gc.drawImage(canvasSnapshot, 0, 0);

        // Calculate the center point and radius based on mouse drag position
        double centerX = (startX + event.getX()) / 2;
        double centerY = (startY + event.getY()) / 2;
        double radius = Math.min(Math.abs(event.getX() - startX), Math.abs(event.getY() - startY)) / 2;

        int points = 5;
        double innerRadius = radius * 0.5; // Ratio of inner to outer radius for a star

        // Arrays to store the X and Y coordinates of the star's points
        double[] xPoints = new double[10];
        double[] yPoints = new double[10];

        // Angle between each point of the star
        double angleStep = Math.PI / points;  // 36 degrees for each step (outer and inner)

        for (int i = 0; i < 10; i++) {
            // Alternate between outer and inner points
            double currentRadius = (i % 2 == 0) ? radius : innerRadius;
            double angle = i * angleStep - Math.PI / 2; // Start at -90 degrees (top of the star)

            // Calculate the X and Y coordinates for each point
            xPoints[i] = centerX + Math.cos(angle) * currentRadius;
            yPoints[i] = centerY + Math.sin(angle) * currentRadius;
        }

        // Draw the filled star
        gc.setFill(fillColor);
        gc.fillPolygon(xPoints, yPoints, 10);

        // Draw the border of the star
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokePolygon(xPoints, yPoints, 10);
    }
}