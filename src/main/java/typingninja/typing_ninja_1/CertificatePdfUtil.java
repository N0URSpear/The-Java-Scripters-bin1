package com.example.addressbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class CertificatePdfUtil {

    private CertificatePdfUtil() {}

    // 内置标准字体（无需外部 TTF）
    private static final PDFont FONT_REG  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public static void saveSimpleCertificate(
            Path outPath,
            String name,
            int typingSpeedWpm,
            double accuracyPercent,
            LocalDate dateCompleted,
            String lesson
    ) throws IOException {

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDRectangle box = page.getMediaBox();
            float pageWidth  = box.getWidth();
            float pageHeight = box.getHeight();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // 边框
                float margin = 36f;
                cs.setLineWidth(2f);
                cs.addRect(margin, margin, pageWidth - margin * 2, pageHeight - margin * 2);
                cs.stroke();

                // 标题（居中）
                String title = "Certificate of Completion";
                float titleFontSize = 28f;
                float titleWidth = FONT_BOLD.getStringWidth(title) / 1000f * titleFontSize;
                float titleX = (pageWidth - titleWidth) / 2f;
                float titleY = pageHeight - 120f;

                cs.beginText();
                cs.setFont(FONT_BOLD, titleFontSize);
                cs.newLineAtOffset(titleX, titleY);
                cs.showText(title);
                cs.endText();

                // 副标题线
                cs.setLineWidth(1f);
                cs.moveTo(margin + 40, titleY - 10);
                cs.lineTo(pageWidth - margin - 40, titleY - 10);
                cs.stroke();

                // 正文
                float bodyLeft = margin + 60f;
                float startY   = titleY - 60f;
                float gap      = 28f;

                writeLine(cs, "Awarded to: " + name, bodyLeft, startY, 16f, true);
                writeLine(cs, "Typing Speed (WPM): " + typingSpeedWpm, bodyLeft, startY - gap, 14f, false);
                writeLine(cs, "Accuracy: " + String.format("%.1f%%", accuracyPercent), bodyLeft, startY - gap * 2, 14f, false);
                writeLine(cs, "Date Completed: " + dateCompleted.format(DateTimeFormatter.ISO_DATE), bodyLeft, startY - gap * 3, 14f, false);
                writeLine(cs, "Lesson: " + lesson, bodyLeft, startY - gap * 4, 14f, false);

                // 底部签名区
                float signY = margin + 120f;
                cs.moveTo(bodyLeft, signY);
                cs.lineTo(bodyLeft + 200, signY);
                cs.stroke();
                writeLine(cs, "Instructor Signature", bodyLeft, signY - 18, 12f, false);

                float rightX = pageWidth - margin - 260;
                cs.moveTo(rightX, signY);
                cs.lineTo(rightX + 200, signY);
                cs.stroke();
                writeLine(cs, "Authorized By", rightX, signY - 18, 12f, false);
            }

            doc.save(outPath.toFile());
        }
    }

    private static void writeLine(PDPageContentStream cs, String text,
                                  float x, float y, float fontSize, boolean bold) throws IOException {
        cs.beginText();
        cs.setFont(bold ? FONT_BOLD : FONT_REG, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    // 可选：保存完自动打开
    public static void openWithDesktop(Path path) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(path.toFile());
            }
        } catch (Exception ignored) {}
    }
}
