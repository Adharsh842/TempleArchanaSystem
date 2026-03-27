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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QRCodeService {

    // ✅ Returns Base64 string instead of file path
    public String generateQRCodeBase64(String bookingId, String qrContent)
            throws WriterException, IOException {

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(
            qrContent,
            BarcodeFormat.QR_CODE,
            300, 300,
            hints
        );

        MatrixToImageConfig config = new MatrixToImageConfig(
            0xFF000000,
            0xFFFFFFFF
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream, config);
        byte[] qrBytes = outputStream.toByteArray();

        String base64 = Base64.getEncoder().encodeToString(qrBytes);
        System.out.println("QR Code generated as Base64 for: " + bookingId);
        return base64;
    }
}