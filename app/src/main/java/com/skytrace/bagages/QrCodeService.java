package com.skytrace.bagages;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class QrCodeService {

    private static final int TAILLE_PX = 250;

    /**
     * Random public tracking capability: 122 random bits, with no sequence to guess.
     */
    public String genererCodeUnique() {
        return "BAG-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * Genere l'image PNG du QR code pour un texte donne, encodee en base64.
     * Utilisable directement dans le frontend : <img src="data:image/png;base64,...">
     */
    public String genererQrCodeBase64(String contenu) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenu, BarcodeFormat.QR_CODE, TAILLE_PX, TAILLE_PX);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);

            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la generation du QR code", e);
        }
    }
}
