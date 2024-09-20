package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class CircleTool extends ShapeTool {

    public CircleTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }

        gc.drawImage(canvasSnapshot, 0, 0);
        double radius = Math.abs(event.getX() - startX);  // Use horizontal distance as the radius for the circle
        double x = Math.min(startX, event.getX()) - radius;
        double y = Math.min(startY, event.getY()) - radius;

        gc.setFill(fillColor);
        gc.fillOval(x, y, radius * 2, radius * 2);  // Draw the circle
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokeOval(x, y, radius * 2, radius * 2);
    }
}
