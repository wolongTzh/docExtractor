package com.tsinghua.demo.present;

import com.tsinghua.demo.present.docx.TableUtil;
import com.tsinghua.demo.present.docx.TextFilterUtil;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.FileInputStream;
import java.io.FileWriter;

public class DocXReader {

    static TextFilterUtil textFilterUtil = new TextFilterUtil();
    static TableUtil tableUtil = new TableUtil();
    static String splitLine = "==============================\n";

    public static void main(String[] args) throws Exception {
        String inputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\wps转换后报告.docx";
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
            FileInputStream in = new FileInputStream(filePath);//载入文档
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            XWPFParagraph para1 = null;
            XWPFParagraph para2 = null;
            XWPFParagraph para3 = null;
            String content = "";
            String title = "";
            String head = "";
            String pageStart = splitLine;
            boolean pageStartTag = false;
            boolean startTag = true;
            FileWriter fileWriter = new FileWriter(outputPath);
            StringBuilder builder = new StringBuilder();
            for(IBodyElement element : xwpf.getBodyElements()) {
                if(element instanceof XWPFParagraph) {
                    XWPFParagraph para = (XWPFParagraph) element;
                    if(!textFilterUtil.filterMeaninglessPara(para)) {
                        continue;
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
                    if(startTag && judge == 3) {
                        pageStart = readText(para1);
                        judge = 0;
                    }
                    else {
                        startTag = false;
                    }
                    String text3 = readText(para3);
                    if(text3.equals(pageStart)) {
                        pageStartTag = true;
                    }
                    else {
                        pageStartTag = false;
                    }
                    if(judge == 2 && content.equals("")) {
                        judge = 0;
                    }
                    if(text.equals(pageStart)) {
                        continue;
                    }
                    if(judge == 0 || judge == 3) {
                        if(!content.equals("")) {
                            builder.append("内容：" + content + "\n");
                        }
                        content = "";
                        title += text + "\n";
                    }
                    else {
                        if(!title.equals("")) {
                            builder.append("标题：" + title + "\n");
                        }
                        title = "";
                        content += text + "\n";
                    }
//                    builder.append(text + "\n");
//                    System.out.println(text);
                }
                if(element instanceof XWPFTable) {
                    String text = tableUtil.readTable((XWPFTable) element, pageStartTag);
                    if(text == null) {
                        continue;
                    }
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
        boolean judgeNum = true;
        for(char c : text.toCharArray()) {
            if(!Character.isDigit(c)) {
                judgeNum = false;
                break;
            }
        }
        if(judgeNum) {
            return splitLine;
        }
        return text;
    }
}
