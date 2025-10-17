// File: src/test/java/typingNinja/CertificatePdfUtilTest.java
package typingNinja;

import org.apache.pdfbox.Loader;                  // PDFBox 3.x 推荐的装载入口
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

            // 使用 PDFBox 打开并抽取文本
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
        Class<?> cls = Class.forName("typingNinja.CertificatePdfUtil");

        // Path, String, int, double, LocalDate, String, String
        try {
            Method m = cls.getMethod("saveSimpleCertificate",
                    Path.class, String.class, int.class, double.class, LocalDate.class, String.class, String.class);
            m.invoke(null, outFile, name, wpm, accuracy, date, lesson, title);
            return;
        } catch (NoSuchMethodException ignore) { /* 回退尝试 */ }

        // no title ver：Path, String, int, double, LocalDate, String
        try {
            Method m = cls.getMethod("saveSimpleCertificate",
                    Path.class, String.class, int.class, double.class, LocalDate.class, String.class);
            m.invoke(null, outFile, name, wpm, accuracy, date, lesson);
        } catch (NoSuchMethodException e) {
            StringBuilder sb = new StringBuilder("No suitable saveSimpleCertificate(...) found. Available:\n");
            for (Method mth : cls.getDeclaredMethods()) sb.append("  ").append(mth).append('\n');
            throw new IllegalStateException(sb.toString(), e);
        }
    }

    //PDFBox
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
