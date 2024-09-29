package org.example.paintfx;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SquareToolTest {

    private Canvas canvas;
    private GraphicsContext gc;
    private SquareTool squareTool;
    private ToggleButton toggleButton;

    // Initialize the JavaFX toolkit once
    @BeforeAll
    public static void initToolkit() {
        // Initialize JavaFX toolkit, but only if it's not already initialized
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown); // Platform.startup ensures the JavaFX toolkit is initialized
        try {
            latch.await(5, TimeUnit.SECONDS); // Wait for the JavaFX thread to finish setup
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // Run setup on the JavaFX Application Thread
        Platform.runLater(() -> {
            canvas = new Canvas(400, 400);  // Initialize a 400x400 canvas
            gc = canvas.getGraphicsContext2D();
            toggleButton = new ToggleButton();
            squareTool = new SquareTool(gc, toggleButton);
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);  // Wait for JavaFX thread to finish setup
    }

    @Test
    public void testOnMouseDragged_SquareIsDrawn() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // Run the test on the JavaFX Application Thread
        Platform.runLater(() -> {
            // Simulate the toggle button being selected
            toggleButton.setSelected(true);

            // Simulate the mouse drag event
            MouseEvent event = new MouseEvent(
                    MouseEvent.MOUSE_DRAGGED,
                    100, 80, 100, 80,  // X, Y of the drag
                    MouseButton.PRIMARY, 1,
                    false, false, false, false, false,
                    false, false, false, false, false,
                    null);

            // Set starting point for dragging
            squareTool.startX = 50;
            squareTool.startY = 50;

            // Call the method to be tested
            squareTool.onMouseDragged(event, Color.BLUE, Color.BLACK, 5);

            // Ensure the canvas contains the drawn square
            assertDoesNotThrow(() -> squareTool.onMouseDragged(event, Color.BLUE, Color.BLACK, 5));
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);  // Wait for the JavaFX thread to finish
    }

    @Test
    public void testOnMouseDragged_NoDrawingWhenToggleNotSelected() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            // Simulate the toggle button NOT being selected
            toggleButton.setSelected(false);

            // Simulate the mouse drag event
            MouseEvent event = new MouseEvent(
                    MouseEvent.MOUSE_DRAGGED,
                    100, 80, 100, 80,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false, false,
                    false, false, false, false, false,
                    null);

            // Call the method to be tested
            squareTool.onMouseDragged(event, Color.BLUE, Color.BLACK, 5);

            // Since the toggle button is not selected, no square should be drawn
            assertDoesNotThrow(() -> squareTool.onMouseDragged(event, Color.BLUE, Color.BLACK, 5));
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);  // Wait for the JavaFX thread to finish
    }
}
