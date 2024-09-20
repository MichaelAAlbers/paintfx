package org.example.paintfx;

// ShapeTool.java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public abstract class ShapeTool {
    protected double startX, startY;
    protected GraphicsContext gc;
    protected WritableImage canvasSnapshot;
    protected double borderWidth;
    protected javafx.scene.paint.Color fillColor, borderColor;
    public final ToggleButton toggleButton;

    public ShapeTool(GraphicsContext gc, ToggleButton toggleButton) {
        this.gc = gc;;
        this.toggleButton = toggleButton;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        toggleButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                gc.getCanvas().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
                gc.getCanvas().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDraggedWrapper);
                gc.getCanvas().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleasedWrapper);
            } else {
                gc.getCanvas().removeEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
                gc.getCanvas().removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDraggedWrapper);
                gc.getCanvas().removeEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleasedWrapper);
            }
        });
    }

    private void onMouseDraggedWrapper(MouseEvent event) {
        onMouseDragged(event, fillColor, borderColor, borderWidth);
    }

    private void onMouseReleasedWrapper(MouseEvent event) {
        onMouseReleased(event, fillColor, borderColor, borderWidth);
    }

    // To be implemented by each shape subclass
    // To be implemented by each shape subclass
    public void onMousePressed(MouseEvent event){
        if(!this.toggleButton.isSelected()){
            return;
        }
        startX = event.getX();
        startY = event.getY();
        canvasSnapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, canvasSnapshot);
    }

    public abstract void onMouseDragged(MouseEvent event, javafx.scene.paint.Color fillColor,
                                        javafx.scene.paint.Color borderColor, double borderWidth);
    public void onMouseReleased(MouseEvent event, Color fillColor, Color borderColor, double borderWidth) {
        if(!this.toggleButton.isSelected()){
            return;
        }
        onMouseDragged(event, fillColor, borderColor, borderWidth);  // Finalize drawing
    }

}