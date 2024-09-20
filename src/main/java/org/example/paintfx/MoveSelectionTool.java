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
    private boolean isSelecting = false;
    private boolean isDragging = false;
    private double selectionX, selectionY, selectionWidth, selectionHeight;
    private double offsetX, offsetY;  // Track offset when dragging
    private Canvas overlayCanvas;  // The overlay canvas for visual feedback
    private GraphicsContext overlayGc;  // GraphicsContext for the overlay

    public MoveSelectionTool(GraphicsContext gc, ToggleButton toggleButton, StackPane stackPane) {
        super(gc, toggleButton);

        // Create the overlay canvas and add it to the StackPane
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

            // Clear the original selection area on the main canvas (fill it with white)
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

            // Draw the selected image at the new location on the overlay
            overlayGc.drawImage(selectedImage, newX, newY);

            // Draw a temporary dashed outline of the selection
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
        // Ensure correct coordinates and dimensions for capturing the selected area
        PixelReader reader = gc.getCanvas().snapshot(null, null).getPixelReader();

        selectedImage = new WritableImage(reader, (int) selectionX, (int) selectionY,
                (int) selectionWidth, (int) selectionHeight);

        // Optionally, store the canvas snapshot before the move
        canvasSnapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, canvasSnapshot);
    }

    private void finalizeMove(MouseEvent event) {
        double newX = event.getX() - offsetX;
        double newY = event.getY() - offsetY;

        // Draw the moved image in its final position on the main canvas
        gc.drawImage(selectedImage, newX, newY);

        // Optionally, update the canvas snapshot
        canvasSnapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, canvasSnapshot);

        // Reset the selected image after the move
        selectedImage = null;
    }

}

//////////working checkpoint ////////////////////