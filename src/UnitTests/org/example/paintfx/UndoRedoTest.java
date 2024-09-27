package org.example.paintfx;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class UndoRedoTest {

    private Canvas canvas;
    private GraphicsContext gc;
    private UndoRedo undoRedoManager;

    @Before
    public void setUp() throws Exception {
        // Ensure that JavaFX is initialized
        CountDownLatch latch = new CountDownLatch(1);

        // Initialize the JavaFX toolkit if it's not already started
        Platform.startup(() -> {
            // Do nothing, just initialize the JavaFX toolkit
        });

        Platform.runLater(() -> {
            canvas = new Canvas(200, 200);
            gc = canvas.getGraphicsContext2D();
            undoRedoManager = new UndoRedo(canvas, gc);
            latch.countDown();
        });

        // Wait for the JavaFX thread to finish initialization
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testPushToUndoStack() throws Exception {
        // Use a CountDownLatch to wait for the test to complete on the JavaFX thread
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            // Call the pushToUndoStack method
            undoRedoManager.pushToUndoStack();

            // Verify the undo stack is not empty
            assertFalse(undoRedoManager.getUndoStack().isEmpty());

            // Verify the redo stack is cleared
            assertTrue(undoRedoManager.getRedoStack().isEmpty());

            latch.countDown();
        });

        // Wait for the JavaFX thread to finish
        latch.await(5, TimeUnit.SECONDS);
    }
}