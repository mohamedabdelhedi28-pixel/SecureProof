package tn.supcom.secureproof.engine;

import java.nio.charset.StandardCharsets;

public final class PdfMagicBytesValidator {

    public static final long MAX_PDF_SIZE_BYTES = 20L * 1024L * 1024L;
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private PdfMagicBytesValidator() {
    }

    public static boolean isValidHeaderAndSize(byte[] headerBytes, long totalSizeBytes) {
        if (headerBytes == null || headerBytes.length < PDF_MAGIC.length) {
            return false;
        }
        if (totalSizeBytes <= PDF_MAGIC.length || totalSizeBytes > MAX_PDF_SIZE_BYTES) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (headerBytes[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }
}
