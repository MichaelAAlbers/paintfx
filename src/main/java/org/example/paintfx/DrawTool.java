package org.example.paintfx;

//functional but does not properly update lineWidth, does not replace function in PaintApp until issue is fixed

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.beans.property.ObjectProperty;

public class DrawTool {

    private final ToggleButton drawToggle;
    private boolean drawingEnabled = false;
    private final ObjectProperty<Color> currentColor;
    private final double lineWidth;

    // Constructor to initialize the drawing tool
    public DrawTool(ObjectProperty<Color> currentColor, double lineWidth) {
        this.currentColor = currentColor;
        this.lineWidth = lineWidth;
        this.drawToggle = createDrawButton();
    }

    // Method to create the ToggleButton and set up event handlers
    private ToggleButton createDrawButton() {
        ToggleButton drawToggle = new ToggleButton("Draw Line");

        // Add action listener for the toggle button to enable/disable drawing
        drawToggle.setOnAction(event -> {
            drawingEnabled = drawToggle.isSelected();
        });

        return drawToggle;
    }

    // Apply drawing event handlers to the given canvas and graphics context
    public void applyHandlers(Canvas canvas, GraphicsContext gc) {
        if (drawingEnabled) {
            // Remove existing handlers if necessary
            removeHandlers(canvas);

            // Add mouse event handlers to the active canvas
            canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, drawMousePressedHandler(gc));
            canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, drawMouseDraggedHandler(gc));
            canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, drawMouseReleasedHandler(gc));
        }
    }

    // Remove drawing event handlers from the given canvas
    public void removeHandlers(Canvas canvas) {
        canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, drawMousePressedHandler(null));
        canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drawMouseDraggedHandler(null));
        canvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, drawMouseReleasedHandler(null));
    }

    // Handlers for mouse events, dynamically applying to the active canvas
    private EventHandler<MouseEvent> drawMousePressedHandler(GraphicsContext gc) {
        return event -> {
            if (drawingEnabled && gc != null) {
                gc.setLineWidth(lineWidth);
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
                gc.setStroke(currentColor.getValue());
            }
        };
    }

    private EventHandler<MouseEvent> drawMouseDraggedHandler(GraphicsContext gc) {
        return event -> {
            if (drawingEnabled && gc != null) {
                gc.setLineWidth(lineWidth);
                gc.setStroke(currentColor.getValue());
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
        };
    }

    private EventHandler<MouseEvent> drawMouseReleasedHandler(GraphicsContext gc) {
        return event -> {
            if (drawingEnabled && gc != null) {
                gc.closePath();  // Optional: closes the path if needed
            }
        };
    }

    // Public method to get the ToggleButton
    public ToggleButton getDrawToggle() {
        return drawToggle;
    }
}