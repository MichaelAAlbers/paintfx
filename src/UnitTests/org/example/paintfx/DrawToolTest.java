package org.example.paintfx;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DrawToolTest {

    @BeforeAll
    static void initToolkit() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void removeHandlersStopsDrawing() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            Canvas canvas = new Canvas(50, 50);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            DrawTool drawTool = new DrawTool(new SimpleObjectProperty<>(Color.BLACK), 1.0);

            drawTool.getDrawToggle().setSelected(true);
            drawTool.applyHandlers(canvas, gc);

            MouseEvent press = new MouseEvent(MouseEvent.MOUSE_PRESSED,
                    5, 5, 5, 5, MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    false, false, false, false,
                    false, false, null);
            MouseEvent drag = new MouseEvent(MouseEvent.MOUSE_DRAGGED,
                    10, 5, 10, 5, MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    false, false, false, false,
                    false, false, null);
            MouseEvent release = new MouseEvent(MouseEvent.MOUSE_RELEASED,
                    10, 5, 10, 5, MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    false, false, false, false,
                    false, false, null);

            // Initial drawing
            canvas.fireEvent(press);
            canvas.fireEvent(drag);
            canvas.fireEvent(release);

            Color drawn = canvas.snapshot(null, null).getPixelReader().getColor(7, 5);
            assertEquals(Color.BLACK, drawn);

            // Clear canvas and remove handlers
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, 50, 50);
            drawTool.removeHandlers(canvas);

            // Attempt to draw again; should have no effect
            canvas.fireEvent(press);
            canvas.fireEvent(drag);
            canvas.fireEvent(release);

            Color after = canvas.snapshot(null, null).getPixelReader().getColor(7, 5);
            assertEquals(Color.WHITE, after);

            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);
    }
}
