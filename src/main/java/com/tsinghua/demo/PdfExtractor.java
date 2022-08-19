package com.tsinghua.demo;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class PdfExtractor {

    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\1210827387.PDF";
        readPdfTable(path);
    }

    public static void readPdfTable(String path) throws IOException {
        //加载PDF文档
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromFile(path);

        //创建StringBuilder类的实例
        StringBuilder builder = new StringBuilder();

        //抽取表格
        PdfTableExtractor extractor = new PdfTableExtractor(pdf);
        PdfTable[] tableLists;
        for (int page = 0; page < pdf.getPages().getCount(); page++)
        {
            tableLists = extractor.extractTable(page);
            if (tableLists != null && tableLists.length > 0)
            {
                for (PdfTable table : tableLists)
                {
                    int row = table.getRowCount();
                    int column = table.getColumnCount();
                    for (int i = 0; i < row; i++)
                    {
                        for (int j = 0; j < column; j++)
                        {
                            String text = table.getText(i, j);
                            System.out.print(text + "\t");
                            builder.append(text+" ");
                        }
                        System.out.println();
                        builder.append("\r\n");
                    }
                }
            }
        }
        //将提取的表格内容写入txt文档
        FileWriter fileWriter = new FileWriter("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\ExtractedTable.txt");
        fileWriter.write(builder.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    public static void readPdfPara(String path) {
        //加载Word文档
        PdfDocument document = new PdfDocument(path);
        //定义一个int型变量
        int index = 0;

        //遍历PDF文档中每页
        PdfPageBase page;
        for (int i= 0; i<document.getPages().getCount();i++) {
            page = document.getPages().get(i);
            //调用extractText()方法提取文本
            System.out.println(page.extractText(true));
        }
    }

    /**

     * 读取PDF文件的文字内容

     * @param pdfPath

     * @throws Exception

     */

    public static void getTextFromPdf(String pdfPath) throws Exception {

        File file = new File(pdfPath);
        InputStream is = null;
        PDDocument document = null;
        try {
            if (pdfPath.endsWith(".PDF")) {
                document = PDDocument.load(file);
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                int pageSize = document.getNumberOfPages();
                // 一页一页读取
                for (int i = 0; i < pageSize; i++) {
                    // 文本内容
                    PDFTextStripper stripper = new PDFTextStripper();

                    // 设置按顺序输出
                    stripper.setSortByPosition(true);
                    stripper.setStartPage(i + 1);
                    stripper.setEndPage(i + 1);
                    String text = stripper.getText(document);
                    System.out.println(text.trim());
                    System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
                }
            }
        } catch (InvalidPasswordException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
