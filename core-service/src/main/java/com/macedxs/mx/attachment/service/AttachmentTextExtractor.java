package com.macedxs.mx.attachment.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.extractor.XSLFExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extracts a bounded, non-executable textual representation of supported attachments.
 * The original binary remains authoritative; this class only creates prompt context.
 */
final class AttachmentTextExtractor {

    String extract(Path path, String contentType, int maxCharacters) {
        if (maxCharacters <= 0) {
            throw new IllegalArgumentException("Maximum extracted characters must be positive");
        }
        try {
            return switch (contentType) {
                case "application/pdf" -> extractPdf(path, maxCharacters);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                        extractDocx(path, maxCharacters);
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                        extractXlsx(path, maxCharacters);
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation" ->
                        extractPptx(path, maxCharacters);
                default -> extractUtf8(path, maxCharacters);
            };
        } catch (IOException exception) {
            throw new IllegalStateException("Could not extract attachment text", exception);
        }
    }

    private String extractPdf(Path path, int maxCharacters) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return limit(stripper.getText(document), maxCharacters);
        }
    }

    private String extractDocx(Path path, int maxCharacters) throws IOException {
        try (InputStream input = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(input);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return limit(extractor.getText(), maxCharacters);
        }
    }

    private String extractXlsx(Path path, int maxCharacters) throws IOException {
        try (InputStream input = Files.newInputStream(path);
             XSSFWorkbook workbook = new XSSFWorkbook(input);
             XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook)) {
            return limit(extractor.getText(), maxCharacters);
        }
    }

    private String extractPptx(Path path, int maxCharacters) throws IOException {
        try (InputStream input = Files.newInputStream(path);
             XMLSlideShow presentation = new XMLSlideShow(input);
             XSLFExtractor extractor = new XSLFExtractor(presentation)) {
            return limit(extractor.getText(), maxCharacters);
        }
    }

    private String extractUtf8(Path path, int maxCharacters) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return limit(new String(bytes, StandardCharsets.UTF_8), maxCharacters);
    }

    private String limit(String text, int maxCharacters) {
        if (text == null || text.isBlank()) {
            return "[O arquivo não contém texto extraível.]";
        }
        String normalized = text.trim();
        if (normalized.length() <= maxCharacters) {
            return normalized;
        }
        return normalized.substring(0, maxCharacters) + "\n[Conteúdo truncado para respeitar o limite de contexto.]";
    }
}
