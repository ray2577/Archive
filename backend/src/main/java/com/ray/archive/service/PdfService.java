package com.ray.archive.service;

import com.ray.archive.entity.ArchiveTemplate;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {
    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    @Value("${pdf.watermark.text:档案管理系统}")
    private String defaultWatermarkText;

    @Value("${pdf.watermark.color:#000000}")
    private String defaultWatermarkColor;

    @Value("${pdf.watermark.opacity:0.3}")
    private float defaultWatermarkOpacity;

    @Value("${pdf.watermark.rotation:45}")
    private double defaultWatermarkRotation;

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateTemplatePdf(
            ArchiveTemplate template,
            String watermarkText,
            String watermarkColor,
            Float watermarkOpacity,
            Double watermarkRotation,
            String userPassword,
            String ownerPassword) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            WriterProperties writerProperties = new WriterProperties();
            
            // 如果设置了密码，添加密码保护
            if (userPassword != null || ownerPassword != null) {
                writerProperties.setStandardEncryption(
                    userPassword != null ? userPassword.getBytes() : null,
                    ownerPassword != null ? ownerPassword.getBytes() : null,
                    EncryptionConstants.ALLOW_PRINTING,
                    EncryptionConstants.ENCRYPTION_AES_128
                );
            }

            PdfWriter writer = new PdfWriter(baos, writerProperties);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 使用提供的水印参数，如果没有则使用默认值
            String finalWatermarkText = watermarkText != null ? watermarkText : defaultWatermarkText;
            String finalWatermarkColor = watermarkColor != null ? watermarkColor : defaultWatermarkColor;
            float finalWatermarkOpacity = watermarkOpacity != null ? watermarkOpacity : defaultWatermarkOpacity;
            double finalWatermarkRotation = watermarkRotation != null ? watermarkRotation : defaultWatermarkRotation;

            // 添加水印
            if (finalWatermarkText != null && !finalWatermarkText.trim().isEmpty()) {
                pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new WatermarkEventHandler(
                    finalWatermarkText,
                    parseColor(finalWatermarkColor),
                    finalWatermarkOpacity,
                    finalWatermarkRotation
                ));
            }

            // 设置中文字体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            document.setFont(font);

            // 处理模板内容
            Context context = new Context();
            // 这里可以添加模板变量
            String htmlContent = templateEngine.process(template.getContent(), context);

            // 添加标题
//            Paragraph title = new Paragraph(template.getName())
//                .setFontSize(20)
//                .setTextAlignment(TextAlignment.CENTER)
//                .setBold();
//            document.add(title);

            // 添加创建时间
//            Paragraph dateTime = new Paragraph(
//                "创建时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
//                .setFontSize(10)
//                .setTextAlignment(TextAlignment.RIGHT);
 //           document.add(dateTime);

            // 添加模板内容
            document.add(new Paragraph(htmlContent));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    private DeviceRgb parseColor(String colorHex) {
        try {
            if (colorHex == null || !colorHex.startsWith("#")) {
                return new DeviceRgb(0, 0, 0);
            }
            String hex = colorHex.substring(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new DeviceRgb(r, g, b);
        } catch (Exception e) {
            logger.warn("解析颜色失败: {}, 使用默认黑色", colorHex);
            return new DeviceRgb(0, 0, 0);
        }
    }

    private static class WatermarkEventHandler implements IEventHandler {
        private final String watermarkText;
        private final DeviceRgb color;
        private final float opacity;
        private final double rotation;

        public WatermarkEventHandler(String watermarkText, DeviceRgb color, float opacity, double rotation) {
            this.watermarkText = watermarkText;
            this.color = color;
            this.opacity = opacity;
            this.rotation = rotation;
        }

        @Override
        public void handleEvent(Event event) {
//            try {
//                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
//                PdfDocument pdf = docEvent.getDocument();
//                PdfPage page = docEvent.getPage();
//                Rectangle pageSize = page.getPageSize();
//
//                PdfCanvas canvas = new PdfCanvas(page);
//                canvas.saveState()
//                      .setFillColor(color)
//                      .setFillOpacity(opacity);
//
//                // 设置水印位置和旋转
//                canvas.beginText()
//                      .setFontAndSize(PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H"), 30)
//                      .setTextMatrix(
//                          pageSize.getWidth() / 2 - 100,
//                          pageSize.getHeight() / 2,
//                          (float) Math.cos(Math.toRadians(rotation)),
//                          (float) -Math.sin(Math.toRadians(rotation)),
//                          (float) Math.sin(Math.toRadians(rotation)),
//                          (float) Math.cos(Math.toRadians(rotation)),
//                          pageSize.getWidth() / 2,
//                          pageSize.getHeight() / 2)
//                      .showText(watermarkText)
//                      .endText()
//                      .restoreState();
//            } catch (Exception e) {
//                logger.error("添加水印失败", e);
//            }
        }
    }
} 