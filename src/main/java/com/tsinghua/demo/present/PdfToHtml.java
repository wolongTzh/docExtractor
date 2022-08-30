package com.tsinghua.demo.present;

import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PDFDomTreeConfig;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class PdfToHtml {

    public static void main(String[] args) throws Exception {
        String source = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\1210827387.PDF";
        String target = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\html";
//        test_convert_pdf_to_html(source, target);
        pdf2html(source);
    }

    public static void test_convert_pdf_to_html(String source, String target) throws Exception {
        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setImageHandler(PDFDomTreeConfig.saveToDirectory(new File(target)));
        config.setFontHandler(config.getImageHandler());
        String htmlOutput = parseWithPdfDomTree(new FileInputStream(source), 0, 10, config);
        FileUtils.write(new File(target + "\\test.html"), htmlOutput, "utf-8");
    }

    public static String parseWithPdfDomTree(InputStream is, int startPage, int endPage, PDFDomTreeConfig config)
            throws IOException, ParserConfigurationException {
        PDDocument pdf = PDDocument.load(is);
        PDFDomTree parser = new PDFDomTree(config);
        parser.setStartPage(startPage);
        parser.setEndPage(endPage);
        Writer output = new StringWriter();
        parser.writeText(pdf, output);
        pdf.close();
        String htmlOutput = output.toString();
        return htmlOutput;
    }

    //转html
    public static void pdf2html(String pdfpath) {
        try {
            String htmlpath=pdfpath.substring(0,pdfpath.lastIndexOf("."))+".html";
            Document doc = new Document(pdfpath);
            doc.save(htmlpath, SaveFormat.Html);
        } catch (Exception e) {
            System.out.println("pdf 转 html 失败...");
            e.printStackTrace();
        }
    }
}
