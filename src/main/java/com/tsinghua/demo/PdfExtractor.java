package com.tsinghua.demo;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfMargins;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class PdfExtractor {

    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\output";
        String basePath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\output\\";
        //创建File对象，指定路径文件
        File file1 = new File(path);
        //判断是否有目录
        if(file1.isDirectory()) {
            //获取目录中的所有文件名称
            String[] fileName = file1.list();
            Arrays.sort(fileName);
            for(String str : fileName) {
                getTextFromPdf(basePath + str);
            }
        }
//        splitPdf(path);
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

    public static void splitPdf(String path) {
        //加载PDF文档
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromFile(path);
        String outPathHead = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\output\\splitPdf";
        String outPathTail = ".pdf";
        int i = 0;
        for (int j=10; i < pdf.getPages().getCount(); j+=10) {
            PdfPageBase page;
            PdfDocument newDoc1 = new PdfDocument();
            for(; i<j; i++) {
                if(i >= pdf.getPages().getCount()) {
                    break;
                }
                page = newDoc1.getPages().add(pdf.getPages().get(i).getSize(), new PdfMargins(0));
                pdf.getPages().get(i).createTemplate().draw(page, new Point2D.Float(0,0));
            }
            newDoc1.saveToFile(outPathHead + j + outPathTail);
        }


        //拆分为多个PDF文档1页1拆
//        pdf.split("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\splitDocument-{0}.pdf", 0);
//        pdf.close();
    }

    public static void readPdfPara(String path) throws IOException {
        //加载Word文档
        PdfDocument document = new PdfDocument(path);

        //定义一个int型变量
        int index = 0;

        //遍历PDF文档中每页
        PdfPageBase page;
        for (int i= 0; i<document.getPages().getCount();i++) {
            page = document.getPages().get(i);
            //调用extractText()方法提取文本
            System.out.println(page.extractText(false));
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
            if (pdfPath.endsWith(".pdf")) {
                document = PDDocument.load(file);
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                int pageSize = document.getNumberOfPages();
                // 一页一页读取
                for (int i = 0; i < pageSize; i++) {
                    // 文本内容
                    MyPDFTextStripper stripper = new MyPDFTextStripper();

                    // 设置按顺序输出
                    stripper.setSortByPosition(true);
                    stripper.setStartPage(i + 1);
                    stripper.setEndPage(i + 1);
//                    List<List<TextPosition>> textList = stripper.getCharactersByArticle();
                    String text = stripper.getText(document);
                    List<List<TextPosition>> textList = stripper.myGetCharactersByArticle();
                    if(textList.size() != 1) {
                        System.out.println(1);
                    }
                    for(TextPosition textPosition : textList.get(0)) {
                        String fontName = textPosition.getFont().getFontDescriptor().getFontName();
                        String fontFamily = textPosition.getFont().getFontDescriptor().getFontFamily();
                        String fontStretch = textPosition.getFont().getFontDescriptor().getFontStretch();
                        String charSet = textPosition.getFont().getFontDescriptor().getCharSet();
                        String type = textPosition.getFont().getType();
                        String subType = textPosition.getFont().getSubType();
                        String name = textPosition.getFont().getName();
                        float size = textPosition.getFontSize();
                        float weight = textPosition.getFont().getFontDescriptor().getFontWeight();
                        System.out.println();
                    }
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
