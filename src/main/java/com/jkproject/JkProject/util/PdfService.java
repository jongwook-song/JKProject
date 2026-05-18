package com.jkproject.JkProject.util;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.layout.font.FontProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final String FONT_PATH = "src/main/resources/fonts/NanumGothic.ttf";

    public static String createMarketReport(String content, String title) {
        String fileName = title + LocalDateTime.now().format(DateTimeFormatter.ofPattern("_MMdd_HHmm")) + ".pdf";
        String dest = "reports/" + fileName; // reports 폴더에 저장

        try {
            File folder = new File("reports");
            if (!folder.exists()) folder.mkdirs();

            ConverterProperties properties = new ConverterProperties();
            FontProvider fontProvider = new DefaultFontProvider(false, false, false);
            fontProvider.addFont(FONT_PATH);
            properties.setFontProvider(fontProvider);

            String finalHtml = "<style>body { font-family: 'NanumGothic'; }</style>" + content;

            HtmlConverter.convertToPdf(finalHtml, new FileOutputStream(dest), properties);
            return dest;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}