package org.example.paintfx;

// RectangleTool.java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class RectangleTool extends ShapeTool {

    public RectangleTool(GraphicsContext gc, Logger logger, ToggleButton toggleButton) {
        super(gc, logger, toggleButton);
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if(!this.toggleButton.isSelected()){
            return;
        }
        gc.drawImage(canvasSnapshot, 0, 0);
        double width = Math.abs(event.getX() - startX);
        double height = Math.abs(event.getY() - startY);
        double x = Math.min(startX, event.getX());
        double y = Math.min(startY, event.getY());

        gc.setFill(fillColor);
        gc.fillRect(x, y, width, height);
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokeRect(x, y, width, height);
    }
    @Override
    protected String getShapeName() {
        return "Rectangle";
    }
}