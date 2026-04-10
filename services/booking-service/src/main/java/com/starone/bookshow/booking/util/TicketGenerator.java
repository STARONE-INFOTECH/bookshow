package com.starone.bookshow.booking.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.starone.bookshow.booking.entity.Booking;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketGenerator {
    @Value("${app.ticket.base-url:https://yourapp.com/ticket}")
    private String ticketBaseUrl;

    /**
     * Generates a QR code containing a unique ticket verification URL
     * Returns Base64-encoded PNG image string (for embedding in email or API response)
     */
    public String generateQrCode(Booking booking) {
        String ticketUrl = ticketBaseUrl + "/" + booking.getBookingReference();

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(ticketUrl, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            byte[] pngData = pngOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code for ticket", e);
        }
    }

    /**
     * Optional: Generate simple ticket reference (fallback if QR fails)
     */
    public String generateTicketReference() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
