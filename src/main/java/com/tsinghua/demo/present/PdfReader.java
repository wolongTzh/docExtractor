package com.tsinghua.demo.present;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfMargins;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class PdfReader {

    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\1210827387.PDF";
        String basePath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\output\\";

        splitPdf(path);

        //创建File对象，指定路径文件
//        Long start = System.currentTimeMillis();
//        File file1 = new File(basePath);
//        //判断是否有目录
//        if(file1.isDirectory()) {
//            //获取目录中的所有文件名称
//            String[] fileName = file1.list();
//            Arrays.sort(fileName);
//            for(String str : fileName) {
//                readPdfPara(basePath + str);
//                readPdfTable(basePath + str);
//            }
//        }
//        Long duration = System.currentTimeMillis() - start;
//        System.out.println("duration = " + duration);
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
                            text = filterCharacters(text);
                            System.out.print(text + "\t");
                            builder.append(text+ " ");
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

    public static void readPdfPara(String path) throws IOException {
        //加载Word文档
        PdfDocument document = new PdfDocument(path);

        //定义一个int型变量
        int index = 0;

        //遍历PDF文档中每页
        PdfPageBase page;
        for (int i= 0; i<document.getPages().getCount();i++) {
            page = document.getPages().get(i);
            String text = page.extractText(false);
//            text = filterCharacters(text);
            if(text.equals("")) {
                continue;
            }
            //调用extractText()方法提取文本
            System.out.println(text);
            System.out.println("\n");
        }
    }

    public static void splitPdf(String path) {
        //加载PDF文档
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromFile(path);
        String outPathHead = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\output\\splitPdf";
        String outPathTail = ".docx";
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
            char order = (char) ((j / 10) + 'a');
            newDoc1.saveToFile(outPathHead + order + outPathTail, FileFormat.DOCX);
        }


        //拆分为多个PDF文档1页1拆
//        pdf.split("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\splitDocument-{0}.pdf", 0);
//        pdf.close();
    }

    private static String filterCharacters(String text) {
        text = text.replace(" ", "");
        text = text.replace("\n", "");
        text = text.replace("\r", "");
        text = text.replace("\b", "");
//        text = text.replace("\f", "");
        text = text.replace("\u000E", "");
        text = text.replace("\u0007", "");
        text = text.replace("\u0001", "");
        return text;
    }

    public static void changeToWord(String basePath, String fileName) throws IOException {
        //加载Word文档
        PdfDocument document = new PdfDocument(basePath + fileName);
        fileName = fileName.replace("PDF", "docx");
        document.saveToFile(basePath + fileName, FileFormat.DOCX);
    }
}
