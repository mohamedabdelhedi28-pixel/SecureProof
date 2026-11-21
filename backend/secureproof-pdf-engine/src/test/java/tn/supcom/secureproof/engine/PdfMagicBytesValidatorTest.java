package tn.supcom.secureproof.engine;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class PdfMagicBytesValidatorTest {

    @Test
    void shouldAcceptValidPdfHeaderWithinSizeLimit() {
        byte[] sample = "%PDF-1.7\n%...".getBytes(StandardCharsets.US_ASCII);
        assertTrue(PdfMagicBytesValidator.isValidHeaderAndSize(sample, 1024));
    }

    @Test
    void shouldRejectNonPdfMagicBytes() {
        byte[] fakePdf = "MZ\u0090\u0000\u0003".getBytes(StandardCharsets.ISO_8859_1);
        assertFalse(PdfMagicBytesValidator.isValidHeaderAndSize(fakePdf, 1024));
    }

    @Test
    void shouldRejectOversizedPdfAbove20MiB() {
        byte[] sample = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
        long overLimit = PdfMagicBytesValidator.MAX_PDF_SIZE_BYTES + 1;
        assertFalse(PdfMagicBytesValidator.isValidHeaderAndSize(sample, overLimit));
    }
}
