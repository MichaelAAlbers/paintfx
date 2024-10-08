//"Paint" in javafx
//Author: Michael Albers
//Opens .png .jpg and .bmp files and displays them, save displayed image with or without new file path


package org.example.paintfx;


import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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
import javafx.scene.input.*;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;


public class PaintApp extends Application {

    private int initialWidth = 800;
    private int initialHeight = 600;
    private Canvas canvas = new Canvas(initialWidth, initialHeight);                   //canvas that can be drawn on
    Logger logger;
    public Canvas overlayCanvas = new Canvas( );
    public GraphicsContext overlayGc = overlayCanvas.getGraphicsContext2D();
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

    UndoRedo undoRedo = new UndoRedo(canvas, logger, gc);

    private AutosaveManager autosaveManager;
    private Label countdownLabel;
    private CheckBox displayCountdownCheckBox, enableNotificationsCheckBox;

    @Override
    public void start(Stage primaryStage) throws IOException {
        logger = new Logger();
        tabPane = new TabPane();
        ScrollPane scrollPane = new ScrollPane(tabPane);
        // Add an initial tab on startup
        addNewTab();  // This creates the first tab and sets up the canvas and GraphicsContext


        // Apply handlers to the initial tab's canvas
        Tab firstTab = tabPane.getTabs().get(0);  // Get the first tab
        TabContent firstTabContent = (TabContent) firstTab.getUserData();
        this.canvas = firstTabContent.getCanvas();   // Set the active canvas
        this.gc = firstTabContent.getGraphicsContext();  // Set the active GraphicsContext
        this.overlayCanvas=firstTabContent.getOverlayCanvas();
        this.overlayGc=firstTabContent.getOverlayGraphicsContext();

        // Add the "+" tab for adding new tabs
        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);  // Disable closing for the "+" tab
        tabPane.getTabs().add(plusTab);  // Add the "+" tab as the last tab



        tabPane.setSide(Side.TOP);
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

        //custom icon button constructor class
        IconButtonFactory iconButtonFactory = new IconButtonFactory();

        //toggle buttons
        ToggleButton rectButton = iconButtonFactory.createIconToggleButton("rectangle.png", "Rectangle");
        ToggleButton circleButton = iconButtonFactory.createIconToggleButton("circle.png", "Circle");
        ToggleButton ellipseButton = iconButtonFactory.createIconToggleButton("ellipse.png", "Ellipse");
        ToggleButton squareButton = iconButtonFactory.createIconToggleButton("square.png", "Square");
        ToggleButton triangleButton = iconButtonFactory.createIconToggleButton("triangle.png", "Triangle");
        ToggleButton starButton = iconButtonFactory.createIconToggleButton("star.png", "Star");
        ToggleButton polygonButton = iconButtonFactory.createIconToggleButton("polygon.png", "Polygon");
        ToggleButton moveSelectionButton = iconButtonFactory.createIconToggleButton("selection.png", "Move Selection");
        ToggleButton textButton = iconButtonFactory.createIconToggleButton("text.png", "Add Text");
        ToggleButton varStarButton = iconButtonFactory.createIconToggleButton("varstar.png", "Variable Star");

        // Create regular buttons with icons
        Button clearButton = iconButtonFactory.createIconButton("eraser.png", "Clear Canvas");
        Button clockWiseButton = iconButtonFactory.createIconButton("redo.png", "Rotate Clockwise");
        Button counterClockWiseButton = iconButtonFactory.createIconButton("undo.png", "Rotate Counterclockwise");
        Button mirrorHorizontalButton = iconButtonFactory.createIconButton("flip.png", "Mirror Horizontally");
        Button mirrorVerticalButton = iconButtonFactory.createIconButton("vertical.png", "Mirror Vertically");


        ShapeTool rectangleTool = new RectangleTool(gc, logger, rectButton);
        ShapeTool circleTool = new CircleTool(gc, logger, circleButton);
        ShapeTool ellipseTool = new EllipseTool(gc, logger, ellipseButton);
        ShapeTool squareTool = new SquareTool(gc, logger, squareButton);
        ShapeTool triangleTool = new TriangleTool(gc, logger, triangleButton);
        ShapeTool starTool = new StarTool(gc, logger, starButton);
        ShapeTool polygonTool = new PolygonTool(gc, logger, polygonButton);
        MoveSelectionTool moveSelectionTool = new MoveSelectionTool(gc, overlayGc, overlayCanvas, logger, moveSelectionButton);
        TextTool textTool = new TextTool(gc, logger, textButton);
        VarStarTool varStarTool = new VarStarTool(gc, logger, varStarButton);


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


        CanvasRotator rotator = new CanvasRotator(logger);

        clockWiseButton.setOnAction(e-> rotator.rotateRight(canvas, moveSelectionTool));
        counterClockWiseButton.setOnAction(e -> rotator.rotateLeft(canvas, moveSelectionTool));
        mirrorHorizontalButton.setOnAction(e -> rotator.mirrorHorizontally(canvas, moveSelectionTool));
        mirrorVerticalButton.setOnAction(e -> rotator.mirrorVertically(canvas, moveSelectionTool));
        currentTool = new RectangleTool(gc, logger, rectButton);

        // Undo and Redo buttons
        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undoRedo.undo());

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> undoRedo.redo());

        //copy and paste buttons
        Button copyButton = new Button("Copy");

        Button pasteButton = new Button("Paste");



        // Handle tab switching, including when the "+" tab is clicked
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == plusTab) {
                // If the "+" tab is selected, create a new tab and insert it before the "+" tab
                addNewTab();
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);  // Select the newly created tab (which is now second-to-last)
            } else if (newTab != null) {
                logger.logEvent("Tab " + tabPane.getSelectionModel().getSelectedIndex(), "Switched Tab");
                // Otherwise, handle switching to the selected tab
                TabContent selectedContent = (TabContent) newTab.getUserData();
                this.canvas = selectedContent.getCanvas();
                this.gc = selectedContent.getGraphicsContext();
                this.overlayCanvas = selectedContent.getOverlayCanvas();
                this.overlayGc = selectedContent.getOverlayGraphicsContext();
                this.canvas.requestFocus();

                // Apply drawing handlers to the new active canvas
                currentTool.updateGraphicsContext(gc);
                circleTool.updateGraphicsContext(gc);
                ellipseTool.updateGraphicsContext(gc);
                squareTool.updateGraphicsContext(gc);
                triangleTool.updateGraphicsContext(gc);
                starTool.updateGraphicsContext(gc);
                polygonTool.updateGraphicsContext(gc);
                moveSelectionTool.updateGraphicsContext(gc);
                textTool.updateGraphicsContext(gc);
                varStarTool.updateGraphicsContext(gc);

                currentTool.removeEventHandlers();
                if(currentTool.toggleButton.isSelected()){
                    currentTool.applyEventHandlers();
                }
            }
        });


        //adds menu dropdowns to menubar, see functions themselves for more details
        addFileMenu(primaryStage, menuBar);
        addEditMenu(menuBar, moveSelectionTool);
        addHelpMenu(menuBar, primaryStage);

        countdownLabel = new Label();
        displayCountdownCheckBox = new CheckBox("Show autosave countdown");
        enableNotificationsCheckBox = new CheckBox("Enable autosave notifications");

        Button saveButton = new Button("Save Now");
        saveButton.setOnAction(e -> {
            autosaveManager.autosaveCanvas();  // Trigger manual save
            autosaveManager.resetTimer();  // Reset the timer after manual save
        });

        VBox autoSaver = new VBox(10, countdownLabel, displayCountdownCheckBox, enableNotificationsCheckBox, saveButton);

        //tool bar for drawing tools
        ToolBar toolBar1 = new ToolBar(
            clockWiseButton,
            counterClockWiseButton,
            mirrorHorizontalButton,
            mirrorVerticalButton,
            autoSaver,
            createDrawButton(),                 //draw toggle
            createLineColorPicker(),                         //color chooser
            createLineWidthSlider(),           //width slider
            createStraightLineButton(),
            createEraser(),
            createEraserWidthSlider(),
            undoButton,
            redoButton,
            clearButton,
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
                varStarButton,
                moveSelectionButton,
                textButton
        );


        // Add MenuBar and scrollpane to gridpane
        GridPane gridPane = new GridPane();
        gridPane.add(menuBar,0,0);
        gridPane.add(toolBar1, 0 ,1);
        gridPane.add(toolBar2, 0 ,2);
        gridPane.add(scrollPane,0, 3);


        // Scene setup
        Scene scene = new Scene(gridPane, initialWidth, initialHeight);
        primaryStage.setTitle("PaintFX");
        undoRedo.pushToUndoStack();
        primaryStage.setScene(scene);
        primaryStage.show();


        // Start autosave manager
        autosaveManager = new AutosaveManager(canvas, countdownLabel, displayCountdownCheckBox, enableNotificationsCheckBox);
        autosaveManager.startAutosave();  // Start autosaving






        // Global event filter for key events
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // CTRL + Z to UNDO
            if (new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN).match(event)) {
                undoRedo.undo();
                event.consume();  // Consume the event so it's not processed further
            }

            // CTRL + Y to REDO
            if (new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN).match(event)) {
                undoRedo.redo();
                event.consume();  // Consume the event so it's not processed further
            }

            // CTRL + S to SAVE
            if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
                saveImageAs(primaryStage);
                event.consume();  // Consume the event so it's not processed further
            }

            //CTRL + S to save as
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                    () -> saveImageAs(primaryStage) // The method you want to run when the shortcut is pressed
            );

            //f1 to show "help"
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
                    this::showHelp
            );

        });



        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Use the CanvasHttpHandler to serve the live canvas image
        server.createContext("/canvas", new WebServer(canvas));

        // Use a thread pool executor for handling requests concurrently
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("Web server started at http://localhost:8080/canvas");

        //window popup before closing
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the close event to prevent the window from closing immediately
            autosaveManager.stopAutosave();
            server.stop(1);
            logger.shutdown();
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
                logger.logEvent("Tab 0", "Opened new image " + file);
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
    void saveImageAs(Stage stage) {
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
                // Check for changing formats and if there could be data loss
                String originalFormat = getFileExtension(currentFile.getName());
                String newFormat = getFileExtension(file.getName());

                if (isLossyFormatChange(originalFormat, newFormat)) {
                    boolean continueSave = showWarningPopup("Warning: Saving this image in the " + newFormat.toUpperCase() +
                            " format may cause loss of transparency or other data.");

                    if (!continueSave) {
                        return;  // User canceled the save operation
                    }
                }

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

    // Check if there's a potential data loss when switching formats
    private boolean isLossyFormatChange(String originalFormat, String newFormat) {
        // Example: Converting from PNG to JPG can cause transparency loss
        if (("png".equals(originalFormat) && ("jpg".equals(newFormat) || "bmp".equals(newFormat)))) {
            return true;
        }
        // Add other conditions for potential data loss as needed
        return false;
    }

    // Show a warning popup and return true if the user wants to continue
    private boolean showWarningPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Image Format Warning");
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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
                logger.logEvent("Tab 0", "Canvas Dimensions changed to " + newWidth + "x" + newHeight);
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
    void showHelp() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.setContentText("This is a basic image editor. You can open, edit, and save images.\n" +
                "Use the 'Draw Line' option to draw on the image and 'Line Properties' to select drawing color or thickness.");
        alert.showAndWait();
    }

    //pop up window with information about the softwar
    void showAbout(Stage primaryStage) {
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

    private ToggleButton createDrawButton() {
        // Toggle button for drawing a line
        ToggleButton drawToggle = new ToggleButton("Draw Line");

        // Add action listener for the toggle button to enable/disable drawing
        drawToggle.setOnAction(event -> {
            drawingEnabled = drawToggle.isSelected();
            if (drawingEnabled) {
                if (canvas == null || gc == null) {
                    return;  // No active canvas
                }
                // Add mouse event handlers to the active canvas
                canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, drawMousePressedHandler);
                canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, drawMouseDraggedHandler);
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, drawMouseReleasedHandler);
            } else {
                if (canvas == null) {
                    return;  // No active canvas
                }

                // Remove the drawing event handlers
                canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, drawMousePressedHandler);
                canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drawMouseDraggedHandler);
                canvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, drawMouseReleasedHandler);
            }
        });

        return drawToggle;
    }

    // Handlers for drawn mouse events
    private final EventHandler<MouseEvent> drawMousePressedHandler = event -> {
        if (drawingEnabled) {
            gc.setLineWidth(lineWidth);
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
            gc.setStroke(currentColor.getValue());
        }
    };

    private final EventHandler<MouseEvent> drawMouseDraggedHandler = event -> {
        if (drawingEnabled) {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            gc.setStroke(currentColor.getValue());
        }
    };

    private final EventHandler<MouseEvent> drawMouseReleasedHandler = event -> {
        if (drawingEnabled) {
            gc.closePath();  // Optional: closes the path if needed
            logger.logEvent("Tab 0", "Draw Line");
        }
    };

    private ToggleButton createEraser(){
        //toggle button for drawing a line
        ToggleButton eraser = new ToggleButton("Eraser");
        eraser.setOnAction(event -> eraserEnabled = eraser.isSelected());

        // Add action listener for the toggle button to enable/disable drawing
        eraser.setOnAction(event -> {
            eraserEnabled = eraser.isSelected();
            if (eraserEnabled) {
                if (canvas == null || gc == null) {
                    return;  // No active canvas
                }
                // Add mouse event handlers to the active canvas
                canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, eraserMousePressedHandler);
                canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, eraserMouseDraggedHandler);
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, eraserMouseReleasedHandler);
            } else {
                if (canvas == null) {
                    return;  // No active canvas
                }
                // Remove the drawing event handlers
                canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, eraserMousePressedHandler);
                canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, eraserMouseDraggedHandler);
                canvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, eraserMouseReleasedHandler);
            }
        });

        return eraser;
    }

    //handlers for eraser
    private final EventHandler<MouseEvent> eraserMousePressedHandler = event -> {
        if (eraserEnabled) {
            gc.setLineWidth(eraserWidth);
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
            gc.setStroke(Color.WHITE);
        }
    };
    private final EventHandler<MouseEvent> eraserMouseDraggedHandler = event -> {
        if (eraserEnabled) {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            gc.setStroke(Color.WHITE);
        }
    };

    private final EventHandler<MouseEvent> eraserMouseReleasedHandler = event -> {
        if (eraserEnabled) {
            gc.closePath(); // Optional: closes the path if needed
            logger.logEvent("Tab 0", "Eraser Used");
        }
    };

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
            logger.logEvent("Tab 0", "Draw Straight Line");
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
        Tab tab = new Tab("Canvas " + (tabPane.getTabs().size()));
        tab.setClosable(true);

        // Create a StackPane to hold the Canvas and Overlay Canvas
        StackPane localStackPane = new StackPane();
        localStackPane.setAlignment(Pos.TOP_LEFT);

        // Create a Canvas for the new tab
        Canvas localCanvas = new Canvas(initialWidth, initialHeight);
        GraphicsContext localGc = localCanvas.getGraphicsContext2D();
        localGc.setFill(Color.WHITE);
        localGc.fillRect(0, 0, localCanvas.getWidth(), localCanvas.getHeight());

        // Create the overlay canvas for temporary visuals
        Canvas localOverlayCanvas = new Canvas(initialWidth, initialHeight);
        localOverlayCanvas.setMouseTransparent(true);  // Ensure it doesn't intercept mouse events
        GraphicsContext localOverlayGc = localOverlayCanvas.getGraphicsContext2D();

        // Add both canvases to the StackPane
        localStackPane.getChildren().addAll(localCanvas, localOverlayCanvas);

        // Initialize UndoRedo for this canvas
        UndoRedo localUndoRedo = new UndoRedo(localCanvas, logger, localGc);

        // Set the content of the tab to the StackPane
        tab.setContent(localStackPane);

        // Store relevant data for the tab (canvas, gc, stackPane, overlayGc, undoRedo)
        TabContent tabContent = new TabContent(localCanvas, localGc, localOverlayCanvas, localOverlayGc, localStackPane, localUndoRedo);
        tab.setUserData(tabContent);

        // Setup event handlers for the new canvas
        setupCanvasEventHandlers(localCanvas, localUndoRedo);

        // Add the new tab to the TabPane
        tabPane.getTabs().add(tab);

        // Select the newly added tab
        tabPane.getSelectionModel().select(tab);

        // Update the current tool and canvas when switching between tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                TabContent selectedContent = (TabContent) newTab.getUserData();
                this.canvas = selectedContent.getCanvas();
                this.gc = selectedContent.getGraphicsContext();
                this.stackPane = selectedContent.getStackPane();
                this.overlayGc = selectedContent.getOverlayGraphicsContext();
                this.undoRedo = selectedContent.getUndoRedo();  // Update undoRedo for the selected tab

                // Ensure overlay canvas is correctly synchronized with the current canvas
                selectedContent.getOverlayCanvas().setWidth(this.canvas.getWidth());
                selectedContent.getOverlayCanvas().setHeight(this.canvas.getHeight());
            }
        });
    }
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

    // Set up event handlers for the given canvas and undoRedo stack
    private void setupCanvasEventHandlers(Canvas canvas, UndoRedo undoRedo) {
        canvas.setOnMousePressed(event -> {
            if (currentTool != null) {
                currentTool.onMousePressed(event);
                undoRedo.pushToUndoStack(); // Save the current state for undo
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (currentTool != null) {
                currentTool.onMouseDragged(event, fillColor.getValue(), borderColor.getValue(), borderWidth);
            }
        });

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
