package org.example.paintfx;


import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import java.util.Stack;

public class UndoRedo {

    private final Stack<WritableImage> undoStack = new Stack<>();
    private final Stack<WritableImage> redoStack = new Stack<>();
    private Canvas canvas;
    private GraphicsContext gc;

    public void UndoRedoManager(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public UndoRedo(Canvas canvas, GraphicsContext gc) {
        this.canvas = canvas;
        this.gc = gc;
    }

    // Capture current state of the canvas and push to undo stack
    public void pushToUndoStack() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        undoStack.push(snapshot);
        redoStack.clear();  // Clear redo stack when a new action is made
    }

    // Undo the last change
    public void undo() {
        if (!undoStack.isEmpty()) {
            // Save current state to redo stack
            WritableImage currentImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, currentImage);
            redoStack.push(currentImage);

            // Restore the last image from the undo stack
            WritableImage previousImage = undoStack.pop();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());  // Clear the canvas
            gc.drawImage(previousImage, 0, 0);
        }
    }

    // Redo the last undone action
    public void redo() {
        if (!redoStack.isEmpty()) {
            // Save current state to undo stack
            WritableImage currentImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, currentImage);
            undoStack.push(currentImage);

            // Restore the last image from the redo stack
            WritableImage redoImage = redoStack.pop();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());  // Clear the canvas
            gc.drawImage(redoImage, 0, 0);
        }
    }

    // Check if undo operation is possible
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    // Check if redo operation is possible
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}