package org.example.paintfx;


import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import java.util.Stack;

/**
 * The {@code UndoRedo} class manages undo and redo functionality for a canvases.
 * It maintains stacks for undo and redo operations, allowing users to revert or reapply
 * actions taken on the canvas.
 */
public class UndoRedo {

    /** Stack to store canvas states for undo operations. */
    private final Stack<WritableImage> undoStack = new Stack<>();

    /** Stack to store canvas states for redo operations. */
    private final Stack<WritableImage> redoStack = new Stack<>();

    /** The canvas where drawing occurs. */
    private Canvas canvas;

    /** The graphics context used for drawing on the canvas. */
    private GraphicsContext gc;

    /**
     * Initializes the {@code UndoRedo} manager with a given canvas. The graphics context
     * is automatically derived from the canvas.
     *
     * @param canvas The canvas where drawing operations occur.
     */
    public void UndoRedoManager(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    /**
     * Initializes the {@code UndoRedo} manager with a given canvas and graphics context.
     *
     * @param canvas The canvas where drawing operations occur.
     * @param gc The graphics context for drawing on the canvas.
     */
    public UndoRedo(Canvas canvas, GraphicsContext gc) {
        this.canvas = canvas;
        this.gc = gc;
    }

    /**
     * Captures the current state of the canvas and pushes it onto the undo stack.
     * The redo stack is cleared whenever a new action is added to the undo stack.
     */
    public void pushToUndoStack() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        undoStack.push(snapshot);
        redoStack.clear();  // Clear redo stack when a new action is made
    }

    /**
     * Undoes the last action by restoring the previous canvas state from the undo stack.
     * The current state is saved onto the redo stack before performing the undo operation.
     */
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

    /**
     * Redoes the last undone action by restoring the last canvas state from the redo stack.
     * The current state is saved onto the undo stack before performing the redo operation.
     */
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

    /**
     * Checks whether an undo operation can be performed (i.e., if the undo stack is not empty).
     *
     * @return {@code true} if there is an action to undo; {@code false} otherwise.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks whether a redo operation can be performed (i.e., if the redo stack is not empty).
     *
     * @return {@code true} if there is an action to redo; {@code false} otherwise.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Returns the undo stack for external reference.
     *
     * @return The stack containing undoable canvas states.
     */
    public Stack<WritableImage> getUndoStack() {
        return undoStack;
    }

    /**
     * Returns the redo stack for external reference.
     *
     * @return The stack containing redoable canvas states.
     */
    public Stack<WritableImage> getRedoStack() {
        return redoStack;
    }
}