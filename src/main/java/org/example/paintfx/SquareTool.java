package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class SquareTool extends ShapeTool {

    public SquareTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }

        gc.drawImage(canvasSnapshot, 0, 0);
        double side = Math.min(Math.abs(event.getX() - startX), Math.abs(event.getY() - startY));  // Calculate the side length
        double x = Math.min(startX, event.getX());
        double y = Math.min(startY, event.getY());

        gc.setFill(fillColor);
        gc.fillRect(x, y, side, side);  // Draw the square
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokeRect(x, y, side, side);
    }
}