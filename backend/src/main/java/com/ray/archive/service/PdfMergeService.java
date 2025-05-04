package com.ray.archive.service;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class PdfMergeService {

    /**
     * 合并多个PDF文件
     */
    public byte[] mergePdfs(List<MultipartFile> pdfFiles, List<MultipartFile> images, 
                           List<Float> imageXPositions, List<Float> imageYPositions,
                           List<Integer> imagePages) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument mergedPdf = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(mergedPdf);

            // 合并PDF文件
            for (MultipartFile pdfFile : pdfFiles) {
                PdfDocument sourcePdf = new PdfDocument(
                    new PdfReader(pdfFile.getInputStream())
                );
                sourcePdf.copyPagesTo(1, sourcePdf.getNumberOfPages(), mergedPdf);
                sourcePdf.close();
            }

            // 添加图片和签名
            if (images != null && !images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    MultipartFile imageFile = images.get(i);
                    float x = imageXPositions.get(i);
                    float y = imageYPositions.get(i);
                    int pageNum = imagePages.get(i);

                    // 创建图片
                    ImageData imageData = ImageDataFactory.create(imageFile.getBytes());
                    Image image = new Image(imageData);
                    
                    // 设置图片位置和大小
                    image.setFixedPosition(pageNum, x, y);
                    
                    // 如果是签名，设置合适的大小
                    if (imageFile.getContentType().toLowerCase().contains("signature")) {
                        image.setWidth(100);
                        image.setHeight(50);
                    }
                    
                    document.add(image);
                }
            }

            document.close();
            return baos.toByteArray();
        }
    }

    /**
     * 在指定页面添加图片或签名
     */
    public byte[] addImageToPdf(byte[] originalPdf, MultipartFile image, 
                               float x, float y, int pageNumber) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdfDoc = new PdfDocument(
                new PdfReader(String.valueOf(new PdfDocument(new PdfReader(originalPdf.toString())))),
                new PdfWriter(baos)
            );
            Document document = new Document(pdfDoc);

            // 添加图片
            ImageData imageData = ImageDataFactory.create(image.getBytes());
            Image pdfImage = new Image(imageData);
            pdfImage.setFixedPosition(pageNumber, x, y);
            
            // 如果是签名，设置合适的大小
            if (image.getContentType().toLowerCase().contains("signature")) {
                pdfImage.setWidth(100);
                pdfImage.setHeight(50);
            }
            
            document.add(pdfImage);
            document.close();
            
            return baos.toByteArray();
        }
    }

    /**
     * 获取PDF文件的页数
     */
    public int getPdfPageCount(byte[] pdfContent) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(String.valueOf(new PdfDocument(new PdfReader(Arrays.toString(pdfContent))))));
        int pageCount = pdfDoc.getNumberOfPages();
        pdfDoc.close();
        return pageCount;
    }

    /**
     * 获取PDF页面尺寸
     */
    public Rectangle getPageSize(byte[] pdfContent, int pageNumber) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(String.valueOf(new PdfDocument(new PdfReader(Arrays.toString(pdfContent))))));
        Rectangle pageSize = pdfDoc.getPage(pageNumber).getPageSize();
        pdfDoc.close();
        return pageSize;
    }
} 