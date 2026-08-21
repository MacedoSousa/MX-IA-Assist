package com.macedxs.mx.attachment.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentTextExtractorTest {

    private final AttachmentTextExtractor extractor = new AttachmentTextExtractor();

    @Test
    void shouldExtractReadableTextFromPdf(@TempDir Path temporaryDirectory) throws Exception {
        Path file = temporaryDirectory.resolve("relatorio.pdf");
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            try (PDPageContentStream content = new PDPageContentStream(document, document.getPage(0))) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 700);
                content.showText("Relatorio MX: documento extraido com seguranca.");
                content.endText();
            }
            document.save(file.toFile());
        }

        String text = extractor.extract(file, "application/pdf", 500);

        assertThat(text).contains("Relatorio MX").contains("documento extraido");
    }

    @Test
    void shouldExtractReadableTextFromDocx(@TempDir Path temporaryDirectory) throws Exception {
        Path file = temporaryDirectory.resolve("anotacoes.docx");
        try (XWPFDocument document = new XWPFDocument()) {
            document.createParagraph().createRun().setText("Notas de projeto para contexto do MX.");
            try (var output = java.nio.file.Files.newOutputStream(file)) {
                document.write(output);
            }
        }

        String text = extractor.extract(
                file,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                500
        );

        assertThat(text).contains("Notas de projeto").contains("contexto do MX");
    }

    @Test
    void shouldBoundExtractedContext(@TempDir Path temporaryDirectory) throws Exception {
        Path file = temporaryDirectory.resolve("notas.txt");
        java.nio.file.Files.writeString(file, "abcdefghij");

        String text = extractor.extract(file, "text/plain", 5);

        assertThat(text).startsWith("abcde").contains("Conteúdo truncado");
    }
}
