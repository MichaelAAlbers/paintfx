package org.example.paintfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class CanvasRotator {
    Logger logger;

    public CanvasRotator(Logger logger) {
        this.logger = logger;
    }

    // Rotate Right
    public void rotateRight(Canvas canvas, MoveSelectionTool moveSelectionTool) {
        if (moveSelectionTool.isSelectionActive()) {
            rotateSelectionRight(moveSelectionTool);
        } else {
            rotateCanvasRight(canvas);
        }
    }

    // Rotate Left
    public void rotateLeft(Canvas canvas, MoveSelectionTool moveSelectionTool) {
        if (moveSelectionTool.isSelectionActive()) {
            rotateSelectionLeft(moveSelectionTool);
        } else {
            rotateCanvasLeft(canvas);
        }
    }

    // Mirror Horizontally
    public void mirrorHorizontally(Canvas canvas, MoveSelectionTool moveSelectionTool) {
        if (moveSelectionTool.isSelectionActive()) {
            mirrorSelectionHorizontally(moveSelectionTool);
        } else {
            mirrorCanvasHorizontally(canvas);
        }
    }

    // Mirror Vertically
    public void mirrorVertically(Canvas canvas, MoveSelectionTool moveSelectionTool) {
        if (moveSelectionTool.isSelectionActive()) {
            mirrorSelectionVertically(moveSelectionTool);
        } else {
            mirrorCanvasVertically(canvas);
        }
    }

    // --- Rotate entire canvas 90 degrees clockwise ---
    private void rotateCanvasRight(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int newWidth = (int) snapshot.getHeight();
        int newHeight = (int) snapshot.getWidth();

        WritableImage rotatedImage = new WritableImage(newWidth, newHeight);
        PixelReader reader = snapshot.getPixelReader();

        for (int y = 0; y < snapshot.getHeight(); y++) {
            for (int x = 0; x < snapshot.getWidth(); x++) {
                rotatedImage.getPixelWriter().setArgb(newWidth - 1 - y, x, reader.getArgb(x, y));
            }
        }

        resizeCanvas(canvas, newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, newWidth, newHeight);
        gc.drawImage(rotatedImage, 0, 0);
        logger.logEvent("Canvas", "Rotate Right");
    }

    // --- Rotate entire canvas 90 degrees counterclockwise ---
    private void rotateCanvasLeft(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int newWidth = (int) snapshot.getHeight();
        int newHeight = (int) snapshot.getWidth();

        WritableImage rotatedImage = new WritableImage(newWidth, newHeight);
        PixelReader reader = snapshot.getPixelReader();

        for (int y = 0; y < snapshot.getHeight(); y++) {
            for (int x = 0; x < snapshot.getWidth(); x++) {
                rotatedImage.getPixelWriter().setArgb(y, newHeight - 1 - x, reader.getArgb(x, y));
            }
        }

        resizeCanvas(canvas, newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, newWidth, newHeight);
        gc.drawImage(rotatedImage, 0, 0);
        logger.logEvent("Canvas", "Rotate Left");
    }

    // --- Mirror entire canvas horizontally ---
    private void mirrorCanvasHorizontally(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();

        WritableImage mirroredImage = new WritableImage(width, height);
        PixelReader reader = snapshot.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mirroredImage.getPixelWriter().setArgb(width - 1 - x, y, reader.getArgb(x, y));
            }
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.drawImage(mirroredImage, 0, 0);
        logger.logEvent("Canvas", "Mirror Horizontally");
    }

    // --- Mirror entire canvas vertically ---
    private void mirrorCanvasVertically(Canvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();

        WritableImage mirroredImage = new WritableImage(width, height);
        PixelReader reader = snapshot.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mirroredImage.getPixelWriter().setArgb(x, height - 1 - y, reader.getArgb(x, y));
            }
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.drawImage(mirroredImage, 0, 0);
        logger.logEvent("Canvas", "Mirror Vertically");
    }

    // --- Rotate selected area 90 degrees clockwise ---
    private void rotateSelectionRight(MoveSelectionTool moveSelectionTool) {
        WritableImage selectedImage = moveSelectionTool.getSelectedImage();
        if (selectedImage == null) return;

        WritableImage rotatedImage = rotateImageRight(selectedImage);
        moveSelectionTool.applyRotatedSelection(rotatedImage);

        logger.logEvent("Canvas", "Rotate Selection Right");
    }

    // --- Rotate selected area 90 degrees counterclockwise ---
    private void rotateSelectionLeft(MoveSelectionTool moveSelectionTool) {
        WritableImage selectedImage = moveSelectionTool.getSelectedImage();
        if (selectedImage == null) return;

        WritableImage rotatedImage = rotateImageLeft(selectedImage);
        moveSelectionTool.applyRotatedSelection(rotatedImage);

        logger.logEvent("Canvas", "Rotate Selection Left");
    }

    // --- Mirror selected area horizontally ---
    private void mirrorSelectionHorizontally(MoveSelectionTool moveSelectionTool) {
        WritableImage selectedImage = moveSelectionTool.getSelectedImage();
        if (selectedImage == null) return;

        WritableImage mirroredImage = mirrorImageHorizontally(selectedImage);
        moveSelectionTool.applyRotatedSelection(mirroredImage);

        logger.logEvent("Canvas", "Mirror Selection Horizontally");
    }

    // --- Mirror selected area vertically ---
    private void mirrorSelectionVertically(MoveSelectionTool moveSelectionTool) {
        WritableImage selectedImage = moveSelectionTool.getSelectedImage();
        if (selectedImage == null) return;

        WritableImage mirroredImage = mirrorImageVertically(selectedImage);
        moveSelectionTool.applyRotatedSelection(mirroredImage);

        logger.logEvent("Canvas", "Mirror Selection Vertically");
    }

    // --- Rotate image 90 degrees clockwise ---
    private WritableImage rotateImageRight(WritableImage sourceImage) {
        int newWidth = (int) sourceImage.getHeight();
        int newHeight = (int) sourceImage.getWidth();

        WritableImage rotatedImage = new WritableImage(newWidth, newHeight);
        PixelReader reader = sourceImage.getPixelReader();

        for (int y = 0; y < sourceImage.getHeight(); y++) {
            for (int x = 0; x < sourceImage.getWidth(); x++) {
                rotatedImage.getPixelWriter().setArgb(newWidth - 1 - y, x, reader.getArgb(x, y));
            }
        }

        return rotatedImage;
    }

    // --- Rotate image 90 degrees counterclockwise ---
    private WritableImage rotateImageLeft(WritableImage sourceImage) {
        int newWidth = (int) sourceImage.getHeight();
        int newHeight = (int) sourceImage.getWidth();

        WritableImage rotatedImage = new WritableImage(newWidth, newHeight);
        PixelReader reader = sourceImage.getPixelReader();

        for (int y = 0; y < sourceImage.getHeight(); y++) {
            for (int x = 0; x < sourceImage.getWidth(); x++) {
                rotatedImage.getPixelWriter().setArgb(y, newHeight - 1 - x, reader.getArgb(x, y));
            }
        }

        return rotatedImage;
    }

    // --- Mirror image horizontally ---
    private WritableImage mirrorImageHorizontally(WritableImage sourceImage) {
        int width = (int) sourceImage.getWidth();
        int height = (int) sourceImage.getHeight();

        WritableImage mirroredImage = new WritableImage(width, height);
        PixelReader reader = sourceImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mirroredImage.getPixelWriter().setArgb(width - 1 - x, y, reader.getArgb(x, y));
            }
        }

        return mirroredImage;
    }

    // --- Mirror image vertically ---
    private WritableImage mirrorImageVertically(WritableImage sourceImage) {
        int width = (int) sourceImage.getWidth();
        int height = (int) sourceImage.getHeight();

        WritableImage mirroredImage = new WritableImage(width, height);
        PixelReader reader = sourceImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mirroredImage.getPixelWriter().setArgb(x, height - 1 - y, reader.getArgb(x, y));
            }
        }

        return mirroredImage;
    }

    // --- Resize the canvas ---
    private void resizeCanvas(Canvas canvas, double newWidth, double newHeight) {
        canvas.setWidth(newWidth);
        canvas.setHeight(newHeight);
    }
}
