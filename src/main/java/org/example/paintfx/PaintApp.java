//"Paint" in javafx
//Author: Michael Albers
//Opens .png .jpg and .bmp files and displays them, save displayed image with or without new file path


package org.example.paintfx;


import javafx.geometry.Pos;
import javafx.scene.control.ToolBar;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert.AlertType;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import javafx.scene.layout.StackPane;

public class PaintApp extends Application {

    private int initialWidth = 800;
    private int initialHeight = 600;
    private Canvas canvas = new Canvas(initialWidth, initialHeight);                   //canvas that can be drawn on
    private ShapeTool currentTool;

    private ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.BLACK);

    private ObjectProperty<Color> fillColor = new SimpleObjectProperty<>(Color.WHITE);
    private ObjectProperty<Color> borderColor = new SimpleObjectProperty<>(Color.BLACK);
    private double borderWidth = 5;                         //shape border width
    private double dashInterval = borderWidth * 2;

    //will be used for adjusting drawing color
    private boolean drawingEnabled = false;                 //boolean for drawing toggle
    private boolean eraserEnabled = false;                  //eraser tool toggled on or off

    private boolean dashEnabled = false;
    private double startX, startY;  // Starting point for the line

    private boolean isDrawingSaved = false; // Track if the drawing is saved


    private boolean straightLineEnabled = false;
    private double lineWidth = 5;                               //default drawing thickness
    private double eraserWidth = 10;

    private File currentFile;                                   //for saving loaded file/displayed image to a file
    GraphicsContext gc = canvas.getGraphicsContext2D();  //interacting with the canvas

    StackPane stackPane = new StackPane(canvas);     //stackpane with imageView as a base and the canvas layered over it

    private TabPane tabPane;

    UndoRedo undoRedo = new UndoRedo(canvas, gc);

    @Override
    public void start(Stage primaryStage) {
        tabPane = new TabPane();

        // Add an initial tab on startup
        addNewTab();
        addNewTab();

        ////////////////////////////////////////////////////////////////////////
        //add blank image for default imageview
        canvas.setWidth(initialWidth);
        canvas.setHeight(initialHeight);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());


        // Create MenuBar
        MenuBar menuBar = new MenuBar();



        //bind stack pane to canvas dimensions
        stackPane.prefWidthProperty().bind(canvas.widthProperty());
        stackPane.prefHeightProperty().bind(canvas.heightProperty());

        // Ensure the StackPane aligns to the top-left corner
        stackPane.setAlignment(Pos.TOP_LEFT);


        ToggleButton rectButton = new ToggleButton("Rectangle");
        ToggleButton circleButton = new ToggleButton("Circle");
        ToggleButton ellipseButton = new ToggleButton("Ellipse");
        ToggleButton squareButton = new ToggleButton("Square");
        ToggleButton triangleButton = new ToggleButton("Triangle");
        ToggleButton starButton = new ToggleButton("Star");
        ToggleButton polygonButton = new ToggleButton("Polygon");
        ToggleButton moveSelectionButton = new ToggleButton("Move Selection");
        ToggleButton copyPasteButton = new ToggleButton("Copy/Paste");
        Button clearButton = new Button("Clear Canvas");
        ToggleButton textButton = new ToggleButton("Add Text");
        ToggleButton varStarButton = new ToggleButton("Variable Star");


        ShapeTool rectangleTool = new RectangleTool(gc, rectButton);
        ShapeTool circleTool = new CircleTool(gc, circleButton);
        ShapeTool ellipseTool = new EllipseTool(gc, ellipseButton);
        ShapeTool squareTool = new SquareTool(gc, squareButton);
        ShapeTool triangleTool = new TriangleTool(gc, triangleButton);
        ShapeTool starTool = new StarTool(gc, starButton);
        ShapeTool polygonTool = new PolygonTool(gc, polygonButton);
        MoveSelectionTool moveSelectionTool = new MoveSelectionTool(gc, moveSelectionButton, this.stackPane);
        TextTool textTool = new TextTool(gc, textButton);
        VarStarTool varStarTool = new VarStarTool(gc, varStarButton);

        // Set up button actions to switch tools
        rectButton.setOnAction(e -> currentTool = rectangleTool);
        circleButton.setOnAction(e -> currentTool = circleTool);
        ellipseButton.setOnAction(e -> currentTool = ellipseTool);
        squareButton.setOnAction(e -> currentTool = squareTool);
        triangleButton.setOnAction(e -> currentTool = triangleTool);
        starButton.setOnAction(e -> currentTool = starTool);
        polygonButton.setOnAction(e->currentTool = polygonTool);
        moveSelectionButton.setOnAction(e->currentTool = moveSelectionTool);
        clearButton.setOnAction(e->clearCanvas());
        textButton.setOnAction(e-> currentTool = textTool);
        varStarButton.setOnAction(e -> currentTool = varStarTool);


        // Undo and Redo buttons
        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undoRedo.undo());

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> undoRedo.redo());

        //copy and paste buttons
        Button copyButton = new Button("Copy");
        copyButton.setOnAction(e -> moveSelectionTool.copySelection());

        Button pasteButton = new Button("Paste");
        pasteButton.setOnAction(e -> {
            // You may want to specify where to paste; for simplicity, we use fixed coordinates
            moveSelectionTool.pasteSelection(50, 50); // Adjust as needed
        });




        //adds menu dropdowns to menubar, see functions themselves for more details
        addFileMenu(primaryStage, menuBar);
        addEditMenu(menuBar, moveSelectionTool);
        addHelpMenu(menuBar, primaryStage);

        //tool bar for drawing tools
        ToolBar toolBar1 = new ToolBar(
            createDrawButton(),                 //draw toggle
            createLineColorPicker(),                         //color chooser
            createLineWidthSlider(),           //width slider
            createStraightLineButton(),
            createEraser(),
            createEraserWidthSlider(),
            undoButton,
            redoButton,
            clearButton,
            textButton,
            moveSelectionButton,
            copyButton, pasteButton
        );

        ToolBar toolBar2 = new ToolBar(
                createBorderSlider(),
                createBorderColorPicker(),
                createFillColorPicker(),
                createDashedToggleButton(),
                rectButton,
                circleButton,
                ellipseButton,
                squareButton,
                triangleButton,
                starButton,
                polygonButton,
                varStarButton
        );


        // Add MenuBar and scrollpane to gridpane
        GridPane gridPane = new GridPane();
        gridPane.add(menuBar,0,0);
        gridPane.add(toolBar1, 0 ,1);
        gridPane.add(toolBar2, 0 ,2);
        gridPane.add(tabPane,0, 3);


        // Scene setup
        Scene scene = new Scene(gridPane, initialWidth, initialHeight);
        primaryStage.setTitle("PaintFX");
        undoRedo.pushToUndoStack();
        primaryStage.setScene(scene);
        primaryStage.show();

        //CTRL + S to save as
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                () -> saveImageAs(primaryStage) // The method you want to run when the shortcut is pressed
        );

        //CTRL + H to show "help"
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
                this::showHelp
        );

        //CTRL + A to show "about"
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN),
                () -> showAbout(primaryStage)
        );

        //CTRL + Z to UNDO
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                undoRedo::undo
        );

        //CTRL + Y to REDO
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
                undoRedo::redo
        );




        //window popup before closing
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the close event to prevent the window from closing immediately
            handleWindowClose(primaryStage);
        });


    }


    //open image from chosen file path
    private void openImage(Stage stage) {
        // Opens file browser
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.bmp"));
        File file = fileChooser.showOpenDialog(stage);

        // Opens image if it has a valid file extension
        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file));

                // Resize the canvas to fit the dimensions of the image
                canvas.setWidth(image.getWidth());
                canvas.setHeight(image.getHeight());

                // Clear the canvas and draw the opened image on it
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.drawImage(image, 0, 0);


                currentFile = file;
            } catch (IOException ex) {
                errorPopup("Could not open image file.");
            }
        }
    }

    //used for displaying an error message(mostly invalid file formats)
    private void errorPopup(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //saves opened image to its SAME filepath with SAME name and SAME extension
    private void saveImage() {
        if (currentFile != null) {
            try {
                saveImageToFile(currentFile);
            } catch (IOException ex) {
                errorPopup("Could not save image file.");
            }
        } else {
            errorPopup("No file to save. Use 'Save As...' instead.");
        }
    }

    //choose a filepath, name, and extension(can convert bmp/jpg/png interchangeably)
    private void saveImageAs(Stage stage) {
        // Check if there is content on the canvas
        if (canvas != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");

            // 3 possible file extensions: .png, .jpg, and .bmp
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp"));

            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try {
                    saveImageToFile(file);
                    currentFile = file;  // Update currentFile to the new file path
                } catch (IOException ex) {
                    errorPopup("Could not save image file.");
                }
            }
        } else {
            errorPopup("No image to save.");
        }
    }



    //saves image to specified file path
    private void saveImageToFile(File file) throws IOException {
        // Capture the snapshot of the Canvas
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);  // Fill with white as background
        canvas.snapshot(params, writableImage);

        // Convert WritableImage to BufferedImage (without SwingFXUtils)
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        String formatName = getFileExtension(file.getName());

        // Determine if the image needs to support transparency (ARGB) or not (RGB)
        int bufferedImageType;
        if (formatName.equals("png")) {
            bufferedImageType = BufferedImage.TYPE_INT_ARGB;
        } else {
            bufferedImageType = BufferedImage.TYPE_INT_RGB;
        }

        // Create a BufferedImage to hold the pixels from the WritableImage
        BufferedImage bufferedImage = new BufferedImage(width, height, bufferedImageType);

        // Extract pixel data from WritableImage
        PixelReader pixelReader = writableImage.getPixelReader();

        // Write each pixel to the BufferedImage
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                // If the pixel is fully transparent, make it white
                if (color.getOpacity() == 0) {
                    int whiteRGB = 0xFFFFFFFF;  // White with no transparency (opaque white)
                    bufferedImage.setRGB(x, y, whiteRGB);
                } else {
                    // Convert the color to ARGB (preserve the original pixel)
                    int argb = (int) (color.getOpacity() * 255) << 24 |
                            (int) (color.getRed() * 255) << 16 |
                            (int) (color.getGreen() * 255) << 8 |
                            (int) (color.getBlue() * 255);

                    // For non-ARGB formats (like JPG, BMP), ignore transparency
                    if (bufferedImageType == BufferedImage.TYPE_INT_RGB) {
                        argb = argb | 0xFF000000;  // Make it fully opaque
                    }

                    bufferedImage.setRGB(x, y, argb);
                }
            }
        }

        // Save the BufferedImage to file using ImageIO
        if (!ImageIO.write(bufferedImage, formatName, file)) {
            throw new IOException("Could not save the image in " + formatName + " format.");
        }
    }


    //gets the format of an image from its extension (.png, .jpg, .bmp)
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }



    //adds "File" dropdown menu to a menuBar, takes primaryStage as an argument so functions can interact with images on the stage
    private void addFileMenu(Stage primaryStage, MenuBar menuBar){
        // Create File menu
        Menu fileMenu = new Menu("File");
        // Create MenuItems
        MenuItem openMenuItem = new MenuItem("Open Image");
        MenuItem saveMenuItem = new MenuItem("Save Image");
        MenuItem saveAsMenuItem = new MenuItem("Save As...");
        // Add MenuItems to File menu
        fileMenu.getItems().addAll(openMenuItem, saveMenuItem, saveAsMenuItem);
        // Add File menu to MenuBar
        menuBar.getMenus().add(fileMenu);

        // Set actions for the MenuItems
        openMenuItem.setOnAction(e -> openImage(primaryStage));
        saveMenuItem.setOnAction(e -> saveImage());
        saveAsMenuItem.setOnAction(e -> saveImageAs(primaryStage));
    }

    //help options displayed on top menu as drop-down options
    private void addHelpMenu(MenuBar menuBar, Stage primaryStage){
        //Help Menu drop down
        Menu helpMenu = new Menu("Help");

        //Help items
        MenuItem helpTab = new MenuItem("Help");
        MenuItem aboutTab = new MenuItem("About");

        helpMenu.getItems().addAll(helpTab, aboutTab);

        menuBar.getMenus().add(helpMenu);

        //open google or something idk
        helpTab.setOnAction(e -> showHelp());
        aboutTab.setOnAction(e -> showAbout(primaryStage));

    }

    private void addEditMenu(MenuBar menuBar, MoveSelectionTool moveSelectionTool){
        Menu editMenu = new Menu("Edit");

        MenuItem adjustCanvas = new MenuItem("Adjust Canvas");

        editMenu.getItems().addAll(adjustCanvas);

        menuBar.getMenus().add(editMenu);

        adjustCanvas.setOnAction((e -> showCanvasDimensions(moveSelectionTool)));

    }

    private void showCanvasDimensions(MoveSelectionTool moveSelectionTool){
        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.setTitle("Adjust Canvas Size");

        // Block input events to other windows while this one is open
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Create a GridPane layout for the inputs
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);

        // Label and TextField for Width
        Label widthLabel = new Label("New Width:");
        TextField widthField = new TextField();
        widthField.setPromptText("Enter width");

        // Label and TextField for Height
        Label heightLabel = new Label("New Height:");
        TextField heightField = new TextField();
        heightField.setPromptText("Enter height");

        // Add the inputs to the gridPane
        gridPane.add(widthLabel, 0, 0);
        gridPane.add(widthField, 1, 0);
        gridPane.add(heightLabel, 0, 1);
        gridPane.add(heightField, 1, 1);

        // Apply Button
        Button applyButton = new Button("Apply");
        applyButton.setOnAction(event -> {
            try {
                // Get the new dimensions
                double newWidth = Double.parseDouble(widthField.getText());
                double newHeight = Double.parseDouble(heightField.getText());

                // Resize the canvas
                double oldWidth = canvas.getWidth();
                double oldHeight = canvas.getHeight();
                canvas.setWidth(newWidth);
                canvas.setHeight(newHeight);
                stackPane.setMaxWidth(newWidth);
                stackPane.setMaxHeight(newHeight);
                moveSelectionTool.updateCanvasSnapshot();



                // Get the graphics context
                GraphicsContext gc = canvas.getGraphicsContext2D();

                // Fill the new area with white if the canvas was extended
                gc.setFill(Color.WHITE);
                if (newWidth > oldWidth) {
                    // Fill the right extended area
                    gc.fillRect(oldWidth, 0, newWidth - oldWidth, newHeight);
                }
                if (newHeight > oldHeight) {
                    // Fill the bottom extended area
                    gc.fillRect(0, oldHeight, newWidth, newHeight - oldHeight);
                }

                // Close the popup window after applying the changes
                popupStage.close();
            } catch (NumberFormatException e) {
                System.out.println("Invalid dimensions entered. Please enter valid numbers.");
            }
        });

        // Add the button to the layout
        gridPane.add(applyButton, 1, 2);

        // Set up the scene and show the popup
        Scene popupScene = new Scene(gridPane, 300, 200);
        popupStage.setScene(popupScene);
        popupStage.showAndWait(); // Block until the window is closed
    }

    //pop up window with some tips on how to use the software
    private void showHelp() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.setContentText("This is a basic image editor. You can open, edit, and save images.\n" +
                "Use the 'Draw Line' option to draw on the image and 'Line Properties' to select drawing color or thickness.");
        alert.showAndWait();
    }

    //pop up window with information about the softwar
    private void showAbout(Stage primaryStage) {
        Stage aboutStage = new Stage();
        aboutStage.setTitle("About");

        Label aboutLabel = new Label("Image Editor Application\nVersion 1.0\nLast updated 9/6/2024");
        aboutLabel.setPadding(new Insets(10));
        VBox vbox = new VBox(aboutLabel);
        vbox.setPadding(new Insets(10));

        Scene aboutScene = new Scene(vbox, 200, 100);
        aboutStage.setScene(aboutScene);
        aboutStage.initOwner(primaryStage);
        aboutStage.show();
    }

    //draw button
    private ToggleButton createDrawButton(){
        //toggle button for drawing a line
        ToggleButton drawToggle = new ToggleButton("Draw Line");
        drawToggle.setOnAction(event -> drawingEnabled = drawToggle.isSelected());

        // Add mouse event handlers to the canvas
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (drawingEnabled) {
                gc.setLineWidth(lineWidth);
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
                gc.setStroke(currentColor.getValue());
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (drawingEnabled) {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.setStroke(currentColor.getValue());
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (drawingEnabled) {
                gc.closePath(); // Optional: closes the path if needed
            }
        });


        return drawToggle;
    }

    private ToggleButton createEraser(){
        //toggle button for drawing a line
        ToggleButton eraser = new ToggleButton("Eraser");
        eraser.setOnAction(event -> eraserEnabled = eraser.isSelected());

        // Add mouse event handlers to the canvas
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (eraserEnabled) {
                gc.setLineWidth(eraserWidth);
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
                gc.setStroke(Color.WHITE);
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (eraserEnabled) {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.setStroke(Color.WHITE);
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (eraserEnabled) {
                gc.closePath(); // Optional: closes the path if needed
            }
        });

        return eraser;
    }

    private ToggleButton createStraightLineButton() {
        ToggleButton straightLine = new ToggleButton("Draw straight line");
        straightLine.setOnAction(event -> straightLineEnabled = straightLine.isSelected());


        // Store the canvas state when the mouse is pressed
        WritableImage canvasSnapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (!straightLineEnabled) return;  // Only allow drawing if the button is active
            gc.setLineWidth(lineWidth);
            // Set the starting point for the line
            startX = event.getX();
            startY = event.getY();

            // Take a snapshot of the current canvas content
            canvas.snapshot(null, canvasSnapshot);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!straightLineEnabled) return;  // Only allow drawing if the button is active

            // Restore the canvas to its state before the line started being drawn
            gc.drawImage(canvasSnapshot, 0, 0);
            // Draw the temporary line from start to the current mouse position
            gc.setStroke(currentColor.getValue());
            gc.strokeLine(startX, startY, event.getX(), event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (!straightLineEnabled) return;  // Only allow drawing if the button is active
            // Finalize the line when the mouse is released
            gc.setStroke(currentColor.getValue());
            gc.strokeLine(startX, startY, event.getX(), event.getY());
        });

        return straightLine;
    }


    private VBox createLineWidthSlider(){
        //line width slider
        Slider lineWidthSlider = new Slider(1, 10, 5); // Min=1, Max=10, Initial=5
        lineWidthSlider.setShowTickMarks(true);
        lineWidthSlider.setShowTickLabels(true);
        lineWidthSlider.setMajorTickUnit(1);
        lineWidthSlider.setBlockIncrement(1);
        lineWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lineWidth = newVal.doubleValue(); // Update line width based on slider value
            gc.setLineWidth(lineWidth); // Set new line width
        });

        //label for width slider
        Label sliderText = new Label("Line Width:");
        return new VBox(sliderText, lineWidthSlider);
    }

    private VBox createEraserWidthSlider(){
        //line width slider
        Slider eraserWidthSlider = new Slider(10, 50, 10); // Min=1, Max=20, Initial=5
        eraserWidthSlider.setShowTickMarks(true);
        eraserWidthSlider.setShowTickLabels(true);
        eraserWidthSlider.setMajorTickUnit(5);
        eraserWidthSlider.setBlockIncrement(5);
        eraserWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            eraserWidth = newVal.doubleValue(); // Update eraser width based on slider value
            gc.setLineWidth(eraserWidth); // Set new line width
        });

        //label for width slider
        Label sliderText = new Label("Eraser Width:");
        return new VBox(sliderText, eraserWidthSlider);
    }

    private VBox createLineColorPicker(){
        ColorPicker lineColorPicker = new ColorPicker(currentColor.getValue());
        lineColorPicker.setOnAction(e -> {
            currentColor.set(lineColorPicker.getValue());  // Update global variable
        });

        //label for width slider
        Label sliderText = new Label("Line Color:");
        return new VBox(sliderText, lineColorPicker);
    }

    private VBox createBorderColorPicker(){
        ColorPicker bordertColorPicker = new ColorPicker(borderColor.getValue());
        bordertColorPicker.setOnAction(e -> {
            borderColor.set(bordertColorPicker.getValue());  // Update global variable
        });

        //label for width slider
        Label sliderText = new Label("Shape Border Color:");
        return new VBox(sliderText, bordertColorPicker);
    }

    private VBox createDashedToggleButton() {
        ToggleButton dashedToggleButton = new ToggleButton("Dashed Outline");
        dashedToggleButton.setOnAction(event -> {
            if (dashedToggleButton.isSelected()) {
                gc.setLineDashes(dashInterval);  // Set dash length (you can adjust this)
                dashEnabled = true;
            } else {
                gc.setLineDashes(null);  // Reset to solid line
                dashEnabled = false;
            }
        });

        Label shapeText = new Label("Draw Shapes:");
        return new VBox(shapeText, dashedToggleButton);
    }

    private VBox createFillColorPicker(){
        ColorPicker fillColorPicker = new ColorPicker(fillColor.getValue());
        fillColorPicker.setOnAction(e -> {
            fillColor.set(fillColorPicker.getValue());  // Update global variable
        });
        //label for width slider
        Label sliderText = new Label("Shape Fill Color:");
        return new VBox(sliderText, fillColorPicker);
    }



    private VBox createBorderSlider(){
        //line width slider
        Slider lineWidthSlider = new Slider(1, 10, 5); // Min=1, Max=10, Initial=1
        lineWidthSlider.setShowTickMarks(true);
        lineWidthSlider.setShowTickLabels(true);
        lineWidthSlider.setMajorTickUnit(1);
        lineWidthSlider.setBlockIncrement(1);
        lineWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            borderWidth = newVal.doubleValue(); // Update line width based on slider value
            gc.setLineWidth(borderWidth); // Set new border width
            dashInterval = borderWidth * 2;
            if (dashEnabled) {
                gc.setLineDashes(dashInterval);  // Set dash length (you can adjust this)
            } else {
                gc.setLineDashes(null);  // Reset to solid line
            }
        });

        //label for width slider
        Label sliderText = new Label("Shape Border Width:");
        return new VBox(sliderText, lineWidthSlider);
    }

    private void handleWindowClose(Stage stage) {
        if (!isDrawingSaved) {
            // Show confirmation dialog to ask the user if they want to save
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Drawing");
            alert.setHeaderText("Your drawing is unsaved.");
            alert.setContentText("Do you want to save your drawing before closing?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel");

            alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

            // Capture user choice
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == saveButton) {
                saveImageAs(stage);
                stage.close(); // Close the window after saving
            } else if (result.isPresent() && result.get() == dontSaveButton) {
                stage.close(); // Close the window without saving
            }
            // If the user presses "Cancel", do nothing (the window stays open)
        } else {
            stage.close(); // If the drawing is already saved, just close the window
        }
    }

    public void clearCanvas() {
        // Create an alert for confirmation
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Clear");
        alert.setHeaderText("Are you sure you want to clear the canvas?");

        // Show the dialog and wait for a response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // User chose OK, clear the canvas
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
            // If the user chose CANCEL or closed the dialog, do nothing
        });
    }

    // Add a method to create a new tab with its own canvas
    private void addNewTab() {
        // Create a new tab
        Tab tab = new Tab("Canvas " + (tabPane.getTabs().size() + 1));
        tab.setClosable(true);

        // Create a StackPane to hold the Canvas
        StackPane localStackPane = new StackPane();
        localStackPane.setAlignment(Pos.TOP_LEFT);

        // Create a Canvas for the new tab
        Canvas localCanvas = new Canvas(initialWidth, initialHeight);
        GraphicsContext localGc = localCanvas.getGraphicsContext2D();
        localGc.setFill(Color.WHITE);
        localGc.fillRect(0, 0, localCanvas.getWidth(), localCanvas.getHeight());

        // Initialize UndoRedo for this canvas
        UndoRedo localUndoRedo = new UndoRedo(localCanvas, localGc);

        // Add the canvas to the StackPane
        localStackPane.getChildren().add(localCanvas);

        // Set the content of the tab to the StackPane
        tab.setContent(localStackPane);

        // Add the new tab to the TabPane
        tabPane.getTabs().add(tab);

        // Select the newly added tab
        tabPane.getSelectionModel().select(tab);

        // Set the current canvas, gc, and stackPane for drawing on this tab
        this.canvas = localCanvas;
        this.gc = localGc;
        this.stackPane = localStackPane;
        this.undoRedo = localUndoRedo;

        // Setup event handlers for the new canvas
        setupCanvasEventHandlers(localCanvas, localUndoRedo);

        // Update the current tool and canvas when switching between tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                StackPane selectedStackPane = (StackPane) newTab.getContent();
                Canvas selectedCanvas = (Canvas) selectedStackPane.getChildren().get(0);
                GraphicsContext selectedGc = selectedCanvas.getGraphicsContext2D();
                this.canvas = selectedCanvas;
                this.gc = selectedGc;
                this.stackPane = selectedStackPane;
                this.undoRedo = new UndoRedo(selectedCanvas, selectedGc);  // Update undoRedo stack for the current canvas
            }
        });
    }

    // Setup event handlers for the given canvas, graphics context, and undoRedo stack
    private void setupCanvasEventHandlers(Canvas canvas, UndoRedo undoRedo) {
        // Handle mouse press for drawing and pushing to undo stack
        canvas.setOnMousePressed(event -> {
            if (currentTool != null) {
                currentTool.onMousePressed(event);
                undoRedo.pushToUndoStack(); // Save the current state for undo
            }
        });

        // Handle mouse drag for drawing
        canvas.setOnMouseDragged(event -> {
            if (currentTool != null) {
                currentTool.onMouseDragged(event, fillColor.getValue(), borderColor.getValue(), borderWidth);
            }
        });

        // Handle mouse release to finalize drawing
        canvas.setOnMouseReleased(event -> {
            if (currentTool != null) {
                currentTool.onMouseReleased(event, fillColor.getValue(), borderColor.getValue(), borderWidth);
            }
        });
    }

    //good ol main, not much to see here
    public static void main(String[] args) {
        launch(args);
    }
}
