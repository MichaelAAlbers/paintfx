package org.example.paintfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;

// Helper class to store the tab's content (Canvas, GraphicsContext, StackPane, UndoRedo, and Overlay Canvas)
class TabContent {
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private final Canvas overlayCanvas;
    private final GraphicsContext overlayGraphicsContext;
    private final StackPane stackPane;
    private final UndoRedo undoRedo;

    public TabContent(Canvas canvas, GraphicsContext graphicsContext, Canvas overlayCanvas, GraphicsContext overlayGraphicsContext, StackPane stackPane, UndoRedo undoRedo) {
        this.canvas = canvas;
        this.graphicsContext = graphicsContext;
        this.overlayCanvas = overlayCanvas;
        this.overlayGraphicsContext = overlayGraphicsContext;
        this.stackPane = stackPane;
        this.undoRedo = undoRedo;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }

    public Canvas getOverlayCanvas() {
        return overlayCanvas;
    }

    public GraphicsContext getOverlayGraphicsContext() {
        return overlayGraphicsContext;
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    public UndoRedo getUndoRedo() {
        return undoRedo;
    }
}