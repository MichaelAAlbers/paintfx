package org.example.paintfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MoveSelectionTool extends ShapeTool {

    private WritableImage selectedImage;  // Stores the selected part of the canvas
    private WritableImage clipboardImage; // Clipboard for copy/paste
    private boolean isSelecting = false;
    private boolean isDragging = false;
    private double selectionX, selectionY, selectionWidth, selectionHeight;
    private double offsetX, offsetY;  // Track offset when dragging

    private Canvas overlayCanvas;  // The overlay canvas for visual feedback
    private GraphicsContext overlayGc;  // GraphicsContext for the overlay

    public MoveSelectionTool(GraphicsContext gc, GraphicsContext overlayGc, Canvas overlayCanvas, Logger logger, ToggleButton toggleButton) {
        super(gc, logger, toggleButton);
        this.overlayGc = overlayGc;
        this.overlayCanvas = overlayCanvas;
    }

    @Override
    public void onMousePressed(MouseEvent event) {
        if (!toggleButton.isSelected()) {
            return;
        }

        if (selectedImage == null) {
            // Start selection mode
            isSelecting = true;
            startX = event.getX();
            startY = event.getY();
        } else {
            // Start dragging mode
            offsetX = event.getX() - selectionX;
            offsetY = event.getY() - selectionY;
            isDragging = true;

            // Clear the original selection area on the main canvas
            gc.setFill(Color.WHITE);
            gc.fillRect(selectionX, selectionY, selectionWidth, selectionHeight);
        }
    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!toggleButton.isSelected()) {
            return;
        }

        // Clear the overlay canvas before drawing new visuals
        overlayGc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

        if (isSelecting) {
            // Update the selection rectangle while dragging
            selectionX = Math.min(startX, event.getX());
            selectionY = Math.min(startY, event.getY());
            selectionWidth = Math.abs(event.getX() - startX);
            selectionHeight = Math.abs(event.getY() - startY);

            // Draw the selection rectangle with a dashed border on the overlay canvas
            overlayGc.setStroke(Color.BLACK);
            overlayGc.setLineDashes(5);
            overlayGc.strokeRect(selectionX, selectionY, selectionWidth, selectionHeight);

        } else if (isDragging && selectedImage != null) {
            // Move the selected area during drag
            double newX = event.getX() - offsetX;
            double newY = event.getY() - offsetY;

            overlayGc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());  // Clear previous visuals
            overlayGc.drawImage(selectedImage, newX, newY);
            overlayGc.setStroke(Color.GRAY);
            overlayGc.setLineDashes(5);
            overlayGc.strokeRect(newX, newY, selectionWidth, selectionHeight);
        }
    }

    @Override
    public void onMouseReleased(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!toggleButton.isSelected()) {
            return;
        }

        // Clear the overlay canvas after the selection is placed
        overlayGc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

        if (isSelecting) {
            isSelecting = false;
            captureSelection();  // Capture the selected area
        } else if (isDragging) {
            isDragging = false;
            finalizeMove(event);  // Finalize the move on the main canvas
        }
    }

    private void captureSelection() {
        if (selectionWidth > 0 && selectionHeight > 0) {
            PixelReader reader = gc.getCanvas().snapshot(null, null).getPixelReader();
            selectedImage = new WritableImage(reader, (int) selectionX, (int) selectionY, (int) selectionWidth, (int) selectionHeight);

            // Restore the canvas without the selected area to avoid duplication when moving
            gc.drawImage(canvasSnapshot, 0, 0);
            gc.setFill(Color.WHITE);  // Optionally fill the original area with white or another color
            gc.fillRect(selectionX, selectionY, selectionWidth, selectionHeight);
        }
    }

    private void finalizeMove(MouseEvent event) {
        double newX = event.getX() - offsetX;
        double newY = event.getY() - offsetY;

        // Draw the selected image in its new position on the main canvas
        gc.drawImage(selectedImage, newX, newY);

        // Clear the selected image and reset the canvas
        selectedImage = null;
    }

    public void updateOverlayCanvasSize(double newWidth, double newHeight) {
        overlayCanvas.setWidth(newWidth);
        overlayCanvas.setHeight(newHeight);
    }

    @Override
    protected String getShapeName() {
        return "Move Selection";
    }
}
