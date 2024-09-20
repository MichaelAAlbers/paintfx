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

    public MoveSelectionTool(GraphicsContext gc, ToggleButton toggleButton, StackPane stackPane) {
        super(gc, toggleButton);

        overlayCanvas = new Canvas(gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        overlayCanvas.setMouseTransparent(true);  // Ensure mouse events pass through
        overlayGc = overlayCanvas.getGraphicsContext2D();
        stackPane.getChildren().add(overlayCanvas);  // Add overlay to the StackPane
    }

    @Override
    public void onMousePressed(MouseEvent event) {
        if (!toggleButton.isSelected()) {
            return;  // Allow other tools to handle mouse events if the selection tool is not active
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
            return;  // Allow other tools to handle mouse events if the selection tool is not active
        }

        if (isSelecting) {
            // Update selection rectangle while dragging the mouse
            selectionX = Math.min(startX, event.getX());
            selectionY = Math.min(startY, event.getY());
            selectionWidth = Math.abs(event.getX() - startX);
            selectionHeight = Math.abs(event.getY() - startY);

            overlayGc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());  // Clear previous visuals

            // Draw the selection rectangle with a gray dashed border on the overlay
            overlayGc.setStroke(Color.BLACK);
            overlayGc.setLineDashes(5);
            overlayGc.strokeRect(selectionX, selectionY, selectionWidth, selectionHeight);
        } else if (isDragging && selectedImage != null) {
            // Drag the selected image
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
            return;  // Allow other tools to handle mouse events if the selection tool is not active
        }

        if (isSelecting) {
            isSelecting = false;
            captureSelection();  // Capture the selected area
        } else if (isDragging) {
            isDragging = false;
            finalizeMove(event);  // Finalize the move on the main canvas
            overlayGc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());  // Clear the overlay
        }
    }

    public void copySelection() {
        if (selectedImage != null) {
            // Copy the selected image to clipboard
            clipboardImage = selectedImage;
        }
    }

    public void pasteSelection(double x, double y) {
        if (clipboardImage != null) {
            // Draw the copied image at the specified location
            gc.drawImage(clipboardImage, x, y);
        }
    }

    public void updateCanvasSnapshot() {
        // Create a new snapshot with the new canvas dimensions
        canvasSnapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, canvasSnapshot);
        overlayCanvas.setHeight(gc.getCanvas().getHeight());
        overlayCanvas.setWidth(gc.getCanvas().getWidth());
        // Reset the selection if necessary
        selectedImage = null;
    }

    private void captureSelection() {
        PixelReader reader = gc.getCanvas().snapshot(null, null).getPixelReader();
        selectedImage = new WritableImage(reader, (int) selectionX, (int) selectionY, (int) selectionWidth, (int) selectionHeight);
        canvasSnapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, canvasSnapshot);
    }

    private void finalizeMove(MouseEvent event) {
        double newX = event.getX() - offsetX;
        double newY = event.getY() - offsetY;

        gc.drawImage(selectedImage, newX, newY);
        canvasSnapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, canvasSnapshot);
        selectedImage = null;
    }
}

//////////working checkpoint ////////////////////