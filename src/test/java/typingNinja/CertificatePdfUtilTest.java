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
        // 准备输出文件（放到临时目录，测试后清理）
        Path tempDir = Files.createTempDirectory("cert-test-");
        Path outFile = tempDir.resolve("certificate-sample.pdf");
        try {
            String name = "Alice Johnson";
            int wpm = 72;
            double accuracy = 98.5;        // 你的实现通常格式化为 "%.1f%%"
            LocalDate date = LocalDate.now();
            String lesson = "Lesson 5: Home Row";
            String title  = "Certificate of Completion";

            // 反射调用 saveSimpleCertificate(...)
            invokeSaveSimpleCertificate(outFile, name, wpm, accuracy, date, lesson, title);

            // 基本文件断言
            assertTrue(Files.exists(outFile), "PDF file should be created");
            assertTrue(outFile.toString().toLowerCase().endsWith(".pdf"), "Output should have .pdf extension");

            // 使用 PDFBox 打开并抽取文本
            try (PDDocument doc = Loader.loadPDF(outFile.toFile())) {
                assertFalse(doc.isEncrypted(), "Generated PDF should not be encrypted");
                assertTrue(doc.getNumberOfPages() >= 1, "PDF should have at least one page");

                String text = extractText(doc);

                // 断言包含关键文本（宽松判断，避免区域/字体差异）
                assertTrue(text.contains("Awarded to") || text.contains("awarded to"),
                        "Should contain the 'Awarded to' label");
                assertTrue(text.contains("Typing Speed") || text.contains("WPM") || text.contains("typing speed"),
                        "Should contain a typing speed label");
                assertTrue(text.toLowerCase().contains("accuracy"),
                        "Should contain 'Accuracy' label");

                // 值匹配：姓名、WPM、Accuracy（仅看数字部分），日期（至少包含年份）
                assertTrue(text.contains("Alice") && text.contains("Johnson"),
                        "Should contain the student's name");
                assertTrue(text.contains(String.valueOf(wpm)),
                        "Should contain the WPM number");

                // accuracy 一般格式为 98.5%（保留 1 位小数），我们只检查数字部分以增强鲁棒性
                assertTrue(text.contains(String.valueOf((int) accuracy)) || text.contains("98.5"),
                        "Should contain accuracy number (e.g., 98 or 98.5)");

                String year = String.valueOf(date.getYear());
                assertTrue(text.contains(year), "Should contain the current year");

                // lesson 文案（至少出现“Lesson”，并带有你传入的内容关键字）
                assertTrue(text.toLowerCase().contains("lesson"),
                        "Should contain a 'Lesson' label");
                assertTrue(text.contains("Lesson 5") || text.contains("Home Row"),
                        "Should contain the lesson content");
            }
        } finally {
            // 清理临时目录（如需手动查看 PDF，可注释掉）
            deleteRecursivelyQuiet(tempDir);
        }
    }

    @Test
    @DisplayName("saveSimpleCertificate(): multiple writes produce valid PDFs")
    void saveSimpleCertificate_multipleWrites() throws Exception {
        Path tempDir = Files.createTempDirectory("cert-test-");
        try {
            // 写两份不同的文件，确保都可打开
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

    // ----------------- 辅助方法（中文注释） -----------------

    // 反射调用 saveSimpleCertificate(...)；优先 7 参（含 title），否则回退 6 参版本。
    private static void invokeSaveSimpleCertificate(Path outFile,
                                                    String name,
                                                    int wpm,
                                                    double accuracy,
                                                    LocalDate date,
                                                    String lesson,
                                                    String title) throws Exception {
        Class<?> cls = Class.forName("typingNinja.CertificatePdfUtil");

        // 优先：Path, String, int, double, LocalDate, String, String
        try {
            Method m = cls.getMethod("saveSimpleCertificate",
                    Path.class, String.class, int.class, double.class, LocalDate.class, String.class, String.class);
            m.invoke(null, outFile, name, wpm, accuracy, date, lesson, title);
            return;
        } catch (NoSuchMethodException ignore) { /* 回退尝试 */ }

        // 回退：无 title 的 6 参版本：Path, String, int, double, LocalDate, String
        try {
            Method m = cls.getMethod("saveSimpleCertificate",
                    Path.class, String.class, int.class, double.class, LocalDate.class, String.class);
            m.invoke(null, outFile, name, wpm, accuracy, date, lesson);
        } catch (NoSuchMethodException e) {
            // 打印可用方法以便定位签名
            StringBuilder sb = new StringBuilder("No suitable saveSimpleCertificate(...) found. Available:\n");
            for (Method mth : cls.getDeclaredMethods()) sb.append("  ").append(mth).append('\n');
            throw new IllegalStateException(sb.toString(), e);
        }
    }

    // 用 PDFBox 抽取文本
    private static String extractText(PDDocument doc) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        return stripper.getText(doc);
    }

    // 递归删除目录（忽略异常）
    private static void deleteRecursivelyQuiet(Path root) {
        try {
            if (root == null || !Files.exists(root)) return;
            Files.walk(root).sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }
}
