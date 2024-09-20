package org.example.paintfx;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class CopyPasteTool extends ShapeTool {

    private double startX, startY;
    private double endX, endY;
    private boolean isDragging;
    private WritableImage selectedImage;
    private double pasteX, pasteY;
    private boolean isPasting;

    public CopyPasteTool(GraphicsContext gc, ToggleButton button, StackPane stackPane) {
        super(gc, button);
    }

    @Override
    public void onMousePressed(MouseEvent event) {
        if (isPasting) {
            // If pasting, place the image at the new position
            gc.drawImage(selectedImage, pasteX, pasteY);
            isPasting = false;
            return;
        }

        // Start selection
        startX = event.getX();
        startY = event.getY();
        isDragging = true;
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (isDragging) {
            // Update the end coordinates of the selection
            endX = event.getX();
            endY = event.getY();

            // Clear the canvas for live draw
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

            // Redraw current content (if needed)
            // You can store existing canvas content and redraw it here if necessary.

            // Draw the selection rectangle (live feedback)
            gc.setStroke(Color.BLACK);
            gc.setLineDashes(5);
            gc.strokeRect(startX, startY, endX - startX, endY - startY);
            gc.setLineDashes(0);
        }
    }

    @Override
    public void onMouseReleased(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (isDragging) {
            isDragging = false;

            // Capture the selected area as an image
            selectedImage = captureSelectedArea(startX, startY, endX - startX, endY - startY);

            // Now we are in pasting mode
            isPasting = true;
        }
    }

    public void onMouseDragged(MouseEvent event) {
        if (isPasting) {
            // Update paste position
            pasteX = event.getX();
            pasteY = event.getY();

            // Clear the canvas and redraw everything
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

            // Redraw the canvas content (if needed)
            // Redraw any stored content before pasting

            // Draw the copied image as the user moves the mouse (live draw for pasting)
            gc.drawImage(selectedImage, pasteX, pasteY);
        }
    }

    // Utility method to capture the selected area as an image
    private WritableImage captureSelectedArea(double x, double y, double width, double height) {
        // Ensure width and height are positive
        double correctedWidth = Math.abs(width);
        double correctedHeight = Math.abs(height);

        // Create a new WritableImage for the selected region
        WritableImage writableImage = new WritableImage((int) correctedWidth, (int) correctedHeight);

        // Copy the pixels from the canvas
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setViewport(new Rectangle2D(x, y, correctedWidth, correctedHeight));

        // Capture the region from the canvas into the writableImage
        gc.getCanvas().snapshot(parameters, writableImage);

        return writableImage;
    }
}