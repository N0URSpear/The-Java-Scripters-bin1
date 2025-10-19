package typingNinja;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class CertificatePdfUtilTest {

    @Test
    @DisplayName("saveSimpleCertificate(): writes a readable PDF containing name/WPM/Accuracy/date/lesson")
    void saveSimpleCertificate_writesExpectedPdf() throws Exception {
        Path tempDir = Files.createTempDirectory("cert-test-");
        Path outFile = tempDir.resolve("certificate-sample.pdf");
        try {
            String name = "Alice Johnson";
            int wpm = 72;
            double accuracy = 98.5;
            LocalDate date = LocalDate.now();
            String lesson = "Lesson 5: Home Row";
            String title  = "Certificate of Completion";

            invokeSaveSimpleCertificate(outFile, name, wpm, accuracy, date, lesson, title);

            assertTrue(Files.exists(outFile), "PDF file should be created");
            assertTrue(outFile.toString().toLowerCase().endsWith(".pdf"), "Output should have .pdf extension");

            try (PDDocument doc = Loader.loadPDF(outFile.toFile())) {
                assertFalse(doc.isEncrypted(), "Generated PDF should not be encrypted");
                assertTrue(doc.getNumberOfPages() >= 1, "PDF should have at least one page");

                String text = extractText(doc);

                assertTrue(text.contains("Awarded to") || text.contains("awarded to"),
                        "Should contain the 'Awarded to' label");
                assertTrue(text.contains("Typing Speed") || text.contains("WPM") || text.contains("typing speed"),
                        "Should contain a typing speed label");
                assertTrue(text.toLowerCase().contains("accuracy"),
                        "Should contain 'Accuracy' label");

                assertTrue(text.contains("Alice") && text.contains("Johnson"),
                        "Should contain the student's name");
                assertTrue(text.contains(String.valueOf(wpm)),
                        "Should contain the WPM number");
                assertTrue(text.contains(String.valueOf((int) accuracy)) || text.contains("98.5"),
                        "Should contain accuracy number (e.g., 98 or 98.5)");

                String year = String.valueOf(date.getYear());
                assertTrue(text.contains(year), "Should contain the current year");

                assertTrue(text.toLowerCase().contains("lesson"),
                        "Should contain a 'Lesson' label");
                assertTrue(text.contains("Lesson 5") || text.contains("Home Row"),
                        "Should contain the lesson content");
            }
        } finally {
            deleteRecursivelyQuiet(tempDir);
        }
    }

    @Test
    @DisplayName("saveSimpleCertificate(): multiple writes produce valid PDFs")
    void saveSimpleCertificate_multipleWrites() throws Exception {
        Path tempDir = Files.createTempDirectory("cert-test-");
        try {
            for (int i = 0; i < 2; i++) {
                Path out = tempDir.resolve("certificate-" + i + ".pdf");
                invokeSaveSimpleCertificate(
                        out,
                        "Bob Smith",
                        85,
                        94.0,
                        LocalDate.now().minusDays(i),
                        "Lesson " + (i + 1),
                        "Certificate"
                );
                assertTrue(Files.exists(out), "PDF " + i + " should be created");
                try (PDDocument doc = Loader.loadPDF(out.toFile())) {
                    assertFalse(doc.isEncrypted(), "Generated PDF should not be encrypted");
                    assertTrue(doc.getNumberOfPages() >= 1, "PDF should have at least one page");
                }
            }
        } finally {
            deleteRecursivelyQuiet(tempDir);
        }
    }

    private static void invokeSaveSimpleCertificate(Path outFile,
                                                    String name,
                                                    int wpm,
                                                    double accuracy,
                                                    LocalDate date,
                                                    String lesson,
                                                    String title) throws Exception {
        String[] candidateClassNames = new String[] {
                "typingNinja.view.pdf.CertificatePdfUtil",
                "typingNinja.CertificatePdfUtil",
                "typingNinja.util.pdf.CertificatePdfUtil",
                "typingNinja.view.CertificatePdfUtil",
                "typingNinja.util.CertificatePdfUtil"
        };

        Class<?> cls = null;
        StringBuilder tried = new StringBuilder();
        for (String cn : candidateClassNames) {
            try {
                cls = Class.forName(cn);
                break;
            } catch (ClassNotFoundException e) {
                tried.append("  - ").append(cn).append('\n');
            }
        }
        if (cls == null) {
            throw new ClassNotFoundException(
                    "Could not locate CertificatePdfUtil. Tried:\n" + tried);
        }

        Method m = null;
        Object[] args = null;

        m = getMethodOrNull(cls, "saveSimpleCertificate",
                Path.class, String.class, int.class, double.class, LocalDate.class, String.class, String.class);
        if (m != null) { args = new Object[]{outFile, name, wpm, accuracy, date, lesson, title}; }

        if (m == null) {
            m = getMethodOrNull(cls, "saveSimpleCertificate",
                    Path.class, String.class, int.class, double.class, LocalDate.class, String.class);
            if (m != null) { args = new Object[]{outFile, name, wpm, accuracy, date, lesson}; }
        }

        if (m == null) {
            m = getMethodOrNull(cls, "saveSimpleCertificate",
                    Path.class, String.class, int.class, int.class, LocalDate.class, String.class, String.class);
            if (m != null) { args = new Object[]{outFile, name, wpm, (int) Math.round(accuracy), date, lesson, title}; }
        }

        if (m == null) {
            m = getMethodOrNull(cls, "saveSimpleCertificate",
                    Path.class, String.class, int.class, int.class, LocalDate.class, String.class);
            if (m != null) { args = new Object[]{outFile, name, wpm, (int) Math.round(accuracy), date, lesson}; }
        }

        if (m == null) {
            StringBuilder sb = new StringBuilder("No suitable saveSimpleCertificate(...) found on ")
                    .append(cls.getName()).append(". Available:\n");
            for (Method mm : cls.getDeclaredMethods()) sb.append("  ").append(mm).append('\n');
            throw new IllegalStateException(sb.toString());
        }

        m.invoke(null, args);
    }

    private static Method getMethodOrNull(Class<?> cls, String name, Class<?>... types) {
        try { return cls.getMethod(name, types); }
        catch (NoSuchMethodException e) { return null; }
    }


    private static String extractText(PDDocument doc) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        return stripper.getText(doc);
    }

    private static void deleteRecursivelyQuiet(Path root) {
        try {
            if (root == null || !Files.exists(root)) return;
            Files.walk(root).sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }
}
