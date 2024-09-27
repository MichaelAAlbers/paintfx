package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SquareToolTest extends ShapeTool {

    /**
     * Constructs a {@code ShapeTool} with the specified {@code GraphicsContext} and {@code ToggleButton}.
     * The toggle button controls the activation of the tool, and event handlers are set up accordingly.
     *
     * @param gc           The {@code GraphicsContext} used for drawing on the canvas.
     * @param toggleButton The {@code ToggleButton} that activates or deactivates the tool.
     */
    public SquareToolTest(GraphicsContext gc, ToggleButton toggleButton) {
        super(gc, toggleButton);
    }

    @Test
    void testOnMouseDragged() {

    }

    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {

    }
}