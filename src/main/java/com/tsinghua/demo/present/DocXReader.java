package com.tsinghua.demo.present;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DocXReader {

    static SplitPara splitPara = new SplitPara();

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
    public static List<String> distributor(String filePath, String outputPath){
        try{
            FileInputStream in = new FileInputStream(filePath);//载入文档
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            List<String> article = new ArrayList<>();
            String singlePara = "";
            XWPFParagraph para1 = null;
            XWPFParagraph para2 = null;
            XWPFParagraph para3 = null;
            String content = "";
            String title = "";
            FileWriter fileWriter = new FileWriter(outputPath);
            StringBuilder builder = new StringBuilder();
            for(IBodyElement element : xwpf.getBodyElements()) {
                if(element instanceof XWPFParagraph) {
                    XWPFParagraph para = (XWPFParagraph) element;
                    if(!splitPara.filterMeaninglessPara(para)) {
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
                        judge = splitPara.exportJudgement(para1, para2, para3);
                    }
                    if(judge == 2 && content.equals("")) {
                        judge = 0;
                    }
                    para = para2;
                    String text = readText(para);
                    if(text == null) {
                        continue;
                    }
                    if(judge == 0) {
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
                    article.add(text);
//                    builder.append(text + "\n");
//                    System.out.println(text);
                }
                if(element instanceof XWPFTable) {
                    String text = readTable((XWPFTable) element);
                    if(text == null) {
                        continue;
                    }
                    article.add(text);
//                    builder.append(text);
//                    System.out.println(text);
                }
            }
            fileWriter.write(builder.toString());
            fileWriter.flush();
            fileWriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取段落文本格式
     * @param para 段落
     * @return
     */
    public static String readText(XWPFParagraph para) {
        String text = para.getText();
        text = filterCharacters(text);
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
            String splitLine = "==========================================================================================================================================================\n";
            return splitLine;
        }
        return text;
    }

    /**
     * 读取表格格式
     * @param table 传入的表格
     * @return
     */
    public static String readTable(XWPFTable table) {
        List<XWPFTableRow> rows = table.getRows();
        if(rows.size() == 1) {
            return null;
        }
        String retText = "";
        String[] head = new String[rows.get(0).getTableCells().size()];
        //遍历表头
        List<XWPFTableCell> headCells = rows.get(0).getTableCells();
        for (int j = 0; j < headCells.size(); j++) {
            XWPFTableCell cell = headCells.get(j);
            String tempText = cell.getText();
            tempText = filterCharacters(tempText);
            if(tempText.equals("")) {
                tempText = "未知项";
            }
            if(j != 0) {
                head[j] = "的" + tempText + "是";
            }
            else {
                head[j] = tempText + "是";
            }
        }
        //遍历表体
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            //读取每一列数据
            List<XWPFTableCell> cells = row.getTableCells();
            String text = "";
            String headText = head[0] + cells.get(0).getText();
            for (int j = 1; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                String tempText = cell.getText();
                tempText = filterCharacters(tempText);
                if(tempText.equals("")) {
                    continue;
                }
                if(j < head.length) {
                    text += headText + head[j] + tempText + "\n";
                }
                else {
                    text += headText + "的未知项是" + tempText + "\n";
                }
            }
            if(text.equals("")) {
                continue;
            }
            retText += text;
        }
        if(retText.equals("")) {
            return null;
        }
        return retText;
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
}
