/**
 * The {@code ShapeTool} abstract class serves as the base for tools that draw shapes on a
 * JavaFX {@code Canvas}. It provides common functionality for handling mouse events
 * (pressed, dragged, and released) and allows subclasses to implement specific drawing behavior.
 *
 * <p>The class is designed to handle shape drawing using a {@code GraphicsContext} and a
 * {@code ToggleButton}. The tool is only active when the corresponding toggle button is
 * selected. The class also manages event handlers for interacting with the canvas.
 *
 * <h2>Features</h2>
 * <ul>
 *     <li>Manages mouse events for shape drawing on a canvas (press, drag, release).</li>
 *     <li>Captures the canvas state as a snapshot to enable redrawing during dragging.</li>
 *     <li>Allows subclasses to define custom drawing logic through abstract methods.</li>
 *     <li>Supports dynamic switching of the canvas and automatically reapplies event handlers.</li>
 * </ul>
 *
 * <p>This class is intended to be extended by specific shape tools, such as squares, circles, etc.
 * Subclasses must implement the abstract {@code onMouseDragged} method to define how the
 * shape should be drawn.
 *
 * @see javafx.scene.canvas.GraphicsContext
 * @see javafx.scene.control.ToggleButton
 */

package org.example.paintfx;

// ShapeTool.java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public abstract class ShapeTool {
    /** The x-coordinate where the mouse was pressed. */
    protected double startX;

    /** The y-coordinate where the mouse was pressed. */
    protected double startY;

    /** The {@code GraphicsContext} used to draw on the canvas. */
    protected GraphicsContext gc;

    /** A snapshot of the canvas, used to reset the canvas during dragging. */
    protected WritableImage canvasSnapshot;

    /** The width of the border for the shape being drawn. */
    protected double borderWidth;

    /** The fill color of the shape. */
    protected Color fillColor;

    /** The border color of the shape. */
    protected Color borderColor;

    /** The toggle button that controls whether the tool is active or inactive. */
    public final ToggleButton toggleButton;

    protected Logger logger;

    public String name = "Generic shape";
    private boolean isLogged = false;  // Prevents multiple logging
    private boolean isDrawing = false;  // Tracks if the shape is actually being drawn


    /**
     * Constructs a {@code ShapeTool} with the specified {@code GraphicsContext} and {@code ToggleButton}.
     * The toggle button controls the activation of the tool, and event handlers are set up accordingly.
     *
     * @param gc           The {@code GraphicsContext} used for drawing on the canvas.
     * @param toggleButton The {@code ToggleButton} that activates or deactivates the tool.
     */
    public ShapeTool(GraphicsContext gc, Logger logger, ToggleButton toggleButton) {
        this.gc = gc;
        this.logger = logger;
        this.toggleButton = toggleButton;
        setupEventHandlers();
    }

    /**
     * Sets up the event handlers based on the toggle button's state. When the tool is selected,
     * event handlers are applied to the canvas. When the tool is deselected, the handlers are removed.
     */
    private void setupEventHandlers() {
        toggleButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // Apply handlers when the tool is selected
                applyEventHandlers();
            } else {
                // Remove handlers when the tool is deselected
                removeEventHandlers();
            }
        });
    }

    /**
     * Applies event handlers to the canvas for responding to mouse events.
     * These handlers manage the mouse press, drag, and release actions.
     */
    public void applyEventHandlers() {
        gc.getCanvas().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        gc.getCanvas().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDraggedWrapper);
        gc.getCanvas().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleasedWrapper);
    }

    /**
     * Removes event handlers from the canvas, stopping the tool from responding to mouse events.
     */
    public void removeEventHandlers() {
        gc.getCanvas().removeEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        gc.getCanvas().removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDraggedWrapper);
        gc.getCanvas().removeEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleasedWrapper);
    }

    /**
     * Updates the {@code GraphicsContext} to a new one and reapplies event handlers if the tool is selected.
     *
     * @param newGc The new {@code GraphicsContext} for drawing.
     */
    public void updateGraphicsContext(GraphicsContext newGc) {
        // Remove existing handlers from the old canvas
        removeEventHandlers();
        this.gc = newGc;
        // Apply handlers to the new canvas if the tool is selected
        if (this.toggleButton.isSelected()) {
            applyEventHandlers();
        }
    }

    /**
     * Wrapper method for handling the mouse drag event, passing relevant drawing parameters
     * to the abstract {@code onMouseDragged} method.
     *
     * @param event The {@code MouseEvent} containing information about the drag.
     */
    private void onMouseDraggedWrapper(MouseEvent event) {
        isDrawing = true;  // Mark that drawing is happening
        onMouseDragged(event, fillColor, borderColor, borderWidth);
    }

    /**
     * Wrapper method for handling the mouse release event, ensuring the drawing is finalized.
     *
     * @param event The {@code MouseEvent} containing information about the release.
     */
    private void onMouseReleasedWrapper(MouseEvent event) {
        onMouseReleased(event, fillColor, borderColor, borderWidth);

        // Ensure logging only happens if a shape was drawn
        if (isDrawing && !isLogged) {
            logShapeDrawn();
            isLogged = true;  // Mark as logged
        }
        isDrawing = false;  // Reset drawing state
    }

    /**
     * Called when the mouse is pressed. Captures the initial position and takes a snapshot
     * of the current canvas to allow for redrawing during drag events.
     *
     * @param event The {@code MouseEvent} containing information about the press.
     */
    public void onMousePressed(MouseEvent event) {
        if (!this.toggleButton.isSelected()) {
            return;
        }
        startX = event.getX();
        startY = event.getY();
        canvasSnapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, canvasSnapshot);
        isLogged = false;  // Reset logging state for the new shape
        isDrawing = false;  // Reset drawing state
    }

    /**
     * Abstract method that must be implemented by subclasses to define the behavior
     * of the shape being drawn during a drag event.
     *
     * @param event       The {@code MouseEvent} containing information about the drag.
     * @param fillColor   The {@code Color} to fill the shape with.
     * @param borderColor The {@code Color} to use for the border.
     * @param borderWidth The width of the border.
     */
    public abstract void onMouseDragged(MouseEvent event, Color fillColor, Color borderColor, double borderWidth);

    /**
     * Called when the mouse is released, finalizing the shape drawing by calling
     * {@code onMouseDragged} one last time.
     *
     * @param event       The {@code MouseEvent} containing information about the release.
     * @param fillColor   The {@code Color} to fill the shape with.
     * @param borderColor The {@code Color} to use for the border.
     * @param borderWidth The width of the border.
     */
    public void onMouseReleased(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if (!this.toggleButton.isSelected()) {
            return;
        }
        onMouseDragged(event, fillColor, borderColor, borderWidth);  // Finalize drawing
    }

    private void logShapeDrawn() {
        // Retrieve the name of the current tab (you may already have a way to get the tab name)
        String tabName = getCurrentTabName();  // Implement this method to get the active tab's name
        name = getShapeName();
        // Log the event using the logger
        logger.logEvent(tabName, name + " drawn");  // 'name' is the shape name, customized per shape tool
    }

    /**
     * Each subclass can provide the name of the shape that was drawn.
     *
     * @return The name of the shape.
     */
    protected abstract String getShapeName();
    //  retrieve the name of the active tab
    private String getCurrentTabName() {
        return "Tab 0";  // Placeholder
    }

}