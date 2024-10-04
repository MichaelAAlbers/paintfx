/**
 * The {@code SquareTool} class is responsible for drawing squares on a JavaFX {@code Canvas}
 * when the mouse is dragged. It extends the {@code ShapeTool} class, inheriting its behavior
 * and adding specific functionality to handle square drawing.
 *
 * <p>Squares are drawn in response to mouse drag events, with the size being determined
 * by the distance dragged. The tool also supports customizable fill colors, border colors,
 * and border widths for the drawn squares. The drawing behavior is controlled through
 * a {@code ToggleButton}, allowing the user to enable or disable the tool's functionality.
 *
 * <p>This tool is part of paintfx, intended to be used as one of several shape-drawing tools.
 *
 * <h2>Features</h2>
 * <ul>
 *     <li>Draws squares with equal side lengths based on the distance between the starting
 *     and current mouse positions.</li>
 *     <li>Supports customizable fill and border colors.</li>
 *     <li>Allows the user to set the border width of the drawn squares.</li>
 * </ul>
 *
 * <p>The tool only performs actions when its corresponding {@code ToggleButton} is selected.
 * If the button is not selected, the tool will ignore any mouse drag events.
 *
 * @see ShapeTool
 * */


package org.example.paintfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class SquareTool extends ShapeTool {

    /**
     * Constructs a {@code SquareTool} with the specified {@code GraphicsContext}
     * and {@code ToggleButton}.
     *
     * @param gc The {@code GraphicsContext} used to draw on the canvas.
     * @param toggleButton The {@code ToggleButton} controlling the activation of the tool.
     */

    public SquareTool(GraphicsContext gc, Logger logger, ToggleButton toggleButton) {

        super(gc, logger, toggleButton);
    }

    /**
     * Handles the mouse drag event to draw a square on the canvas.
     *
     * <p>This method is triggered when the user drags the mouse while the square tool is
     * selected. It calculates the side length of the square based on the mouse's current
     * position and the starting point, ensuring that the square's sides remain equal.
     *
     * @param event        The {@code MouseEvent} that contains information about the mouse dragging action.
     * @param fillColor    The {@code Color} to use for the color of the inside of the square.
     * @param borderColor  The {@code Color} to use for the color of the border of the square.
     * @param borderWidth  The width of the square's border.
     */
    @Override
    public void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }

        gc.drawImage(canvasSnapshot, 0, 0);
        double side = Math.min(Math.abs(event.getX() - startX), Math.abs(event.getY() - startY));  // Calculate the side length
        double x = Math.min(startX, event.getX());
        double y = Math.min(startY, event.getY());

        gc.setFill(fillColor);
        gc.fillRect(x, y, side, side);  // Draw the square
        gc.setStroke(borderColor);
        gc.setLineWidth(borderWidth);
        gc.strokeRect(x, y, side, side);
    }

    @Override
    protected String getShapeName() {
        return "Sqaure";
    }
}