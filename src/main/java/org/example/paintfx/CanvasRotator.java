package org.example.paintfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class CanvasRotator {

    Logger logger;
    public CanvasRotator(Logger logger){
        this.logger = logger;
    }


    /**
     * Rotates the given canvas 90 degrees clockwise (to the right).
     *
     * @param canvas The canvas to rotate.
     */
    public void rotateRight(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int newWidth = (int) snapshot.getHeight();
        int newHeight = (int) snapshot.getWidth();

        // Create a new writable image for the rotated content
        WritableImage rotatedImage = new WritableImage(newWidth, newHeight);
        PixelReader reader = snapshot.getPixelReader();

        // Rotate each pixel 90 degrees clockwise
        for (int y = 0; y < snapshot.getHeight(); y++) {
            for (int x = 0; x < snapshot.getWidth(); x++) {
                rotatedImage.getPixelWriter().setArgb(newWidth - 1 - y, x, reader.getArgb(x, y));
            }
        }

        // Resize the canvas and redraw the rotated image
        resizeCanvas(canvas, newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, newWidth, newHeight);
        gc.drawImage(rotatedImage, 0, 0);
        logger.logEvent("Tab 0", "Rotate Right");
    }

    /**
     * Rotates the given canvas 90 degrees counterclockwise (to the left).
     *
     * @param canvas The canvas to rotate.
     */
    public void rotateLeft(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int newWidth = (int) snapshot.getHeight();
        int newHeight = (int) snapshot.getWidth();

        // Create a new writable image for the rotated content
        WritableImage rotatedImage = new WritableImage(newWidth, newHeight);
        PixelReader reader = snapshot.getPixelReader();

        // Rotate each pixel 90 degrees counterclockwise
        for (int y = 0; y < snapshot.getHeight(); y++) {
            for (int x = 0; x < snapshot.getWidth(); x++) {
                rotatedImage.getPixelWriter().setArgb(y, newHeight - 1 - x, reader.getArgb(x, y));
            }
        }

        // Resize the canvas and redraw the rotated image
        resizeCanvas(canvas, newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, newWidth, newHeight);
        gc.drawImage(rotatedImage, 0, 0);
        logger.logEvent("Tab 0", "Rotate Left");

    }


    /**
     * Mirrors the canvas content horizontally (left-to-right).
     *
     * @param canvas The canvas to mirror.
     */
    public void mirrorHorizontally(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();

        WritableImage mirroredImage = new WritableImage(width, height);
        PixelReader reader = snapshot.getPixelReader();

        // Mirror each pixel horizontally
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mirroredImage.getPixelWriter().setArgb(width - 1 - x, y, reader.getArgb(x, y));
            }
        }

        // Redraw the mirrored image
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.drawImage(mirroredImage, 0, 0);
        logger.logEvent("Tab 0", "Mirror Horizontally");

    }

    /**
     * Mirrors the canvas content vertically (top-to-bottom).
     *
     * @param canvas The canvas to mirror.
     */
    public void mirrorVertically(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();

        WritableImage mirroredImage = new WritableImage(width, height);
        PixelReader reader = snapshot.getPixelReader();

        // Mirror each pixel vertically
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mirroredImage.getPixelWriter().setArgb(x, height - 1 - y, reader.getArgb(x, y));
            }
        }

        // Redraw the mirrored image
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.drawImage(mirroredImage, 0, 0);
        logger.logEvent("Tab 0", "Mirror Vertically");
    }

    /**
     * Resizes the canvas to the new width and height while preserving its content.
     *
     * @param canvas The canvas to resize.
     * @param newWidth The new width of the canvas.
     * @param newHeight The new height of the canvas.
     */
    private void resizeCanvas(Canvas canvas, double newWidth, double newHeight) {
        canvas.setWidth(newWidth);
        canvas.setHeight(newHeight);
    }
}