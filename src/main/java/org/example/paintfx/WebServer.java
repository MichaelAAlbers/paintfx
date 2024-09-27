package org.example.paintfx;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * The {@code WebServer} class implements an HTTP server handler that responds
 * to GET requests by sending a PNG image of a JavaFX {@code Canvas}.
 * It converts the canvas to an image format and streams it to the client over HTTP.
 */
public class WebServer implements HttpHandler {

    /** The JavaFX canvas that this server will take snapshots of. */
    private final Canvas canvas;

    /**
     * Constructs a new {@code WebServer} instance that will capture snapshots of the given canvas.
     *
     * @param canvas The {@code Canvas} that will be captured and sent as an image.
     */
    public WebServer(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Handles an incoming HTTP request, capturing a snapshot of the canvas and sending it as a PNG image.
     * Only GET requests are supported; all other methods return a 405 (Method Not Allowed) response.
     *
     * <p>The image snapshot is taken on the JavaFX Application Thread using {@link Platform#runLater(Runnable)}
     * and a {@link CountDownLatch} is used to ensure the snapshot operation completes before sending the response.
     *
     * @param exchange The HTTP exchange containing the request and response objects.
     * @throws IOException If an I/O error occurs during handling the request.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);  // 405 Method Not Allowed
                return;
            }

            // Use a CountDownLatch to ensure we wait for the snapshot to complete on the JavaFX thread
            final WritableImage[] imageHolder = new WritableImage[1];
            CountDownLatch latch = new CountDownLatch(1);

            // Take the snapshot on the JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.WHITE);  // Optional: Set the background fill color
                    imageHolder[0] = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                    canvas.snapshot(params, imageHolder[0]);
                } finally {
                    latch.countDown();  // Ensure latch is decremented when the operation completes
                }
            });

            // Wait for the snapshot to complete
            latch.await();

            // Convert WritableImage to BufferedImage
            BufferedImage bufferedImage = convertWritableImageToBufferedImage(imageHolder[0]);

            // Convert BufferedImage to PNG byte array
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", os);
            byte[] imageBytes = os.toByteArray();

            // Send HTTP response headers
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, imageBytes.length);

            // Write the image to the response
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(imageBytes);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);  // 500 Internal Server Error
        } finally {
            exchange.close();
        }
    }

    /**
     * Converts a {@link WritableImage} (used by JavaFX) into a {@link BufferedImage} (used by AWT).
     * This is necessary to allow image manipulation and saving using standard Java I/O libraries.
     *
     * @param writableImage The {@code WritableImage} to be converted.
     * @return A {@code BufferedImage} containing the pixel data from the {@code WritableImage}.
     */
    private BufferedImage convertWritableImageToBufferedImage(WritableImage writableImage) {
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        // Create a BufferedImage to hold the pixels from the WritableImage
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Extract pixel data from WritableImage
        PixelReader pixelReader = writableImage.getPixelReader();

        // Write each pixel to the BufferedImage
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int argb = (int) (color.getOpacity() * 255) << 24 |
                        (int) (color.getRed() * 255) << 16 |
                        (int) (color.getGreen() * 255) << 8 |
                        (int) (color.getBlue() * 255);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }
}