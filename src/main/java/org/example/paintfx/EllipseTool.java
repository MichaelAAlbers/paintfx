package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class EllipseTool extends ShapeTool {

    public EllipseTool(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }

        gc.drawImage(canvasSnapshot, 0, 0);
        double width = Math.abs(event.getX() - startX);
        double height = Math.abs(event.getY() - startY);
        double x = Math.min(startX, event.getX());
        double y = Math.min(startY, event.getY());

        gc.setFill(fillColor);
        gc.fillOval(x, y, width, height);  // Draw the ellipse
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokeOval(x, y, width, height);
    }
}