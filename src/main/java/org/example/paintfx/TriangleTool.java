package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class TriangleTool extends ShapeTool {

    public TriangleTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }

        gc.drawImage(canvasSnapshot, 0, 0);

        // Coordinates of the mouse dragged position
        double endX = event.getX();
        double endY = event.getY();

        // Define the three points of the right triangle
        double[] xPoints = {startX, startX, endX};  // One side is vertical, startX is repeated for the height
        double[] yPoints = {startY, endY, endY};    // One side is horizontal, endY is repeated for the base

        // Draw the filled right triangle
        gc.setFill(fillColor);
        gc.fillPolygon(xPoints, yPoints, 3);  // 3 points for the triangle
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokePolygon(xPoints, yPoints, 3);  // Outline the triangle
    }
}