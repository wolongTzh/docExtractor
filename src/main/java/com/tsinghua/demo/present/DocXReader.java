package com.tsinghua.demo.present;

import com.tsinghua.demo.present.docx.TableUtil;
import com.tsinghua.demo.present.docx.TextFilterUtil;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

public class DocXReader {

    static TextFilterUtil textFilterUtil = new TextFilterUtil();
    static TableUtil tableUtil = new TableUtil();
    static String splitLine = "==============================\n";

    public static void main(String[] args) throws Exception {
        String inputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\1.docx";
        String outputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\ExtractedTable.txt";
        distributor(inputPath, outputPath);
    }

    /**
     * 分配器，用于判断是读取表格还是纯文本
     * @param filePath 文件路径
     * @return
     */
    public static void distributor(String filePath, String outputPath){
        try{
            File file = new File("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\filterTag.txt");
            textFilterUtil.init(file);
            FileInputStream in = new FileInputStream(filePath);//载入文档
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            XWPFParagraph para1 = null;
            XWPFParagraph para2 = null;
            XWPFParagraph para3 = null;
            String content = "";
            String title = "";
            boolean pageStartTag = true;
            boolean paraTag = true;
            FileWriter fileWriter = new FileWriter(outputPath);
            StringBuilder builder = new StringBuilder();
            for(IBodyElement element : xwpf.getBodyElements()) {
                if(element instanceof XWPFParagraph) {
                    XWPFParagraph para = (XWPFParagraph) element;
                    if(!textFilterUtil.filterMeaninglessPara(para)) {
                        continue;
                    }
                    if(pageStartTag) {
                        String text = readText(para);
                        title += "《" + text + "》\n";
                        pageStartTag = false;
                    }
                    if(para1 == null) {
                        para1 = para;
                        continue;
                    }
                    else if(para2 == null) {
                        para2 = para;
                        continue;
                    }
                    else if(para3 == null) {
                        para3 = para;
                    }
                    else {
                        para1 = para2;
                        para2 = para3;
                        para3 = para;
                    }
                    int judge = -1;
                    if(para1 != null && para2 != null && para3 != null) {
                        judge = textFilterUtil.exportJudgement(para1, para2, para3);
                    }
                    para = para2;
                    String text = readText(para);
                    if(text == null) {
                        continue;
                    }
                    String text3 = readText(para3);
                    if(text3 != null) {
                        paraTag = true;
                    }
//                    if(text.equals(splitLine)) {
//                        if(!content.equals("")) {
//                            builder.append(content + "\n");
//                        }
//                        content = "";
//                        if(!title.equals("")) {
//                            builder.append(title + "\n");
//                        }
//                        title = "";
//                        builder.append(splitLine);
//                        pageStartTag = true;
//                    }
                    else {
                        pageStartTag = false;
                    }
                    if(judge == 2 && content.equals("")) {
                        judge = 0;
                    }
                    if(judge == 0 || judge == 3) {
                        if(!content.equals("")) {
                            builder.append(content + "\n");
                        }
                        content = "";
                        title += "《" + text + "》\n";
                    }
                    else {
                        if(!title.equals("")) {
                            builder.append(title + "\n");
                        }
                        title = "";
                        content += text + "\n";
                    }
//                    builder.append(text + "\n");
//                    System.out.println(text);
                }
                if(element instanceof XWPFTable) {
                    String text = tableUtil.readTable((XWPFTable) element, !paraTag);
                    if(text == null) {
                        continue;
                    }
                    if(!content.equals("")) {
                        builder.append(content + "\n");
                    }
                    content = "";
                    if(!title.equals("")) {
                        builder.append(title + "\n");
                    }
                    title = "";
                    paraTag = false;
                    builder.append(text);
                    System.out.println(text);
                }
            }
            fileWriter.write(builder.toString());
            fileWriter.flush();
            fileWriter.close();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void testPrint(String filePath, String outputPath){
        try{
            File file = new File("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\filterTag.txt");
            textFilterUtil.init(file);
            FileInputStream in = new FileInputStream(filePath);//载入文档
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            XWPFDocument outDoc = new XWPFDocument();//得到word文档的信息
            FileOutputStream out = new FileOutputStream("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\test.docx");//载入文档
            List<Integer> ls = Arrays.asList(0,1,2,3,4,5,6,7,8,9,10);
            for(int i=0; i<ls.size(); i++) {
                xwpf.removeBodyElement(ls.get(i) - i);
            }
            xwpf.write(out);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取段落文本格式
     * @param para 段落
     * @return
     */
    public static String readText(XWPFParagraph para) {
        String text = para.getText();
        text = textFilterUtil.filterCharacters(text);
        if(text.equals("")) {
            return null;
        }
        return text;
    }
}
