package org.example.paintfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;

public class TabManager {
    private TabPane tabPane;
    private int tabCounter = 1;

    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;

        // Set tabs to appear at the bottom
        tabPane.setSide(javafx.geometry.Side.BOTTOM);

        // Create the first canvas tab and the "+" add tab button
        addNewTab();
        addAddTabButton();
    }

    // Method to add a new tab with a scrollable canvas
    private void addNewTab() {
        Tab tab = new Tab("Canvas " + tabCounter++);
        tab.setClosable(true); // Allow the tab to be closed

        // Create a ScrollPane to hold the StackPane and Canvas
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true); // Enable panning when the canvas is larger than the viewport
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Create a StackPane to hold the Canvas
        StackPane stackPane = new StackPane();

        // Create a Canvas for the new tab
        Canvas canvas = new Canvas(600, 400); // Set initial canvas size
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.WHITE); // Set initial canvas background to white
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Add mouse events to the canvas for drawing
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        });

        // Add the canvas to the StackPane
        stackPane.getChildren().add(canvas);

        // Set the content of the ScrollPane to the StackPane
        scrollPane.setContent(stackPane);

        // Set the content of the tab to the ScrollPane
        tab.setContent(scrollPane);

        // Insert the new tab before the "Add Tab" button
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);

        // Make the new tab the active tab
        tabPane.getSelectionModel().select(tab);
    }

    // Special tab that acts as the "Add Tab" button
    private void addAddTabButton() {
        Tab addTab = new Tab("+"); // Special tab to add a new canvas
        addTab.setClosable(false); // Prevent the add button from being closed

        // When the "+" tab is selected, create a new tab and switch to it
        addTab.setOnSelectionChanged(event -> {
            if (addTab.isSelected()) {
                addNewTab();
            }
        });

        // Add the "+" tab at the end
        tabPane.getTabs().add(addTab);
    }
}
