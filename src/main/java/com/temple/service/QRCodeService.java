package com.temple.service;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QRCodeService {

    /**
     * Generates QR code and saves it to static/qrcodes/ folder.
     * Uses absolute path detection so it works from any working directory.
     *
     * @param bookingId  unique booking ID
     * @param qrContent  text to encode inside QR
     * @return web URL path like /qrcodes/QR_TEMPLE-xxx.png
     */
    public String generateQRCode(String bookingId, String qrContent)
            throws WriterException, IOException {

        // ── Step 1: Find the correct save folder ──────────────────
        Path saveDir = findQRSaveDirectory();

        // ── Step 2: Create folder if it does not exist ─────────────
        if (!Files.exists(saveDir)) {
            Files.createDirectories(saveDir);
            System.out.println("Created QR directory: " + saveDir.toAbsolutePath());
        }

        // ── Step 3: Set up QR code hints ───────────────────────────
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // ── Step 4: Generate QR bit matrix ─────────────────────────
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(
            qrContent,
            BarcodeFormat.QR_CODE,
            300, 300,
            hints
        );

        // ── Step 5: Save QR as PNG ──────────────────────────────────
        String fileName = "QR_" + bookingId + ".png";
        Path filePath   = saveDir.resolve(fileName);

        MatrixToImageConfig config = new MatrixToImageConfig(
            0xFF000000,  // Black modules
            0xFFFFFFFF   // White background
        );

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath, config);

        System.out.println("QR Code saved at: " + filePath.toAbsolutePath());

        // ── Step 6: Return web-accessible URL ──────────────────────
        return "/qrcodes/" + fileName;
    }

    /**
     * Finds the correct absolute path to save QR codes.
     * Tries multiple locations to handle both Eclipse run and JAR run.
     */
    private Path findQRSaveDirectory() {

        // Option 1: Find via classpath resource (most reliable in Eclipse)
        try {
            URL staticUrl = getClass()
                .getClassLoader()
                .getResource("static");

            if (staticUrl != null) {
                Path staticPath = Paths.get(staticUrl.toURI());
                Path qrPath     = staticPath.resolve("qrcodes");
                System.out.println("QR save path (classpath): "
                    + qrPath.toAbsolutePath());
                return qrPath;
            }
        } catch (Exception e) {
            System.out.println("Classpath method failed: " + e.getMessage());
        }

        // Option 2: Use project working directory
        String workDir = System.getProperty("user.dir");
        System.out.println("Working directory: " + workDir);

        // Try standard Maven project layout
        Path option2 = Paths.get(workDir,
            "src", "main", "resources", "static", "qrcodes");
        if (Files.exists(option2.getParent())) {
            return option2;
        }

        // Option 3: Try target/classes (compiled output)
        Path option3 = Paths.get(workDir,
            "target", "classes", "static", "qrcodes");
        return option3;
    }
}