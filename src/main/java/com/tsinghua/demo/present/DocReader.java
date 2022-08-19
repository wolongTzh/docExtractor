package com.tsinghua.demo.present;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DocReader {

    public static void main(String[] args) throws Exception {
        String inputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\wps转换后报告.doc";
        String outputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\ExtractedTableDoc.txt";
        distributor(inputPath, outputPath);
    }

    /**
     * 分配器，用于判断是读取表格还是纯文本
     * @param filePath 文件路径
     * @return
     */
    public static List<String> distributor(String filePath, String outputPath) throws IOException {
        InputStream is = new FileInputStream(filePath);
        HWPFDocument doc = new HWPFDocument(is);
        Range range = doc.getRange();
        //获取段落数
        int paraNum = range.numParagraphs();
        Table tablePre = null;
        FileWriter fileWriter = new FileWriter(outputPath);
        StringBuilder builder = new StringBuilder();
        List<String> article = new ArrayList<>();
        for (int i=0; i<paraNum; i++) {
            Paragraph para = range.getParagraph(i);
            if(!para.isInTable()) {
                String text = readText(para);
                if(text == null) {
                    continue;
                }
                builder.append(text);
                article.add(text);
            }
            if(para.isInTable()) {
                try {
                    Table table = range.getTable(para);
                    if (tablePre != null && tablePre.getEndOffset() == table.getEndOffset()) {
                        continue;
                    }
                    tablePre = table;
                    String text = readTable(table);
                    if (text == null) {
                        continue;
                    }
                    article.add(text);
                    builder.append(text);
                    System.out.println(text);
                }
                catch(Exception e){
                    continue;
                }
            }
        }
        fileWriter.write(builder.toString());
        fileWriter.flush();
        fileWriter.close();
        return article;
    }

    /**
     * 读取段落文本格式
     * @param para 段落
     * @return
     */
    public static String readText(Paragraph para) {
        String text = para.text();
        text = filterCharacters(text);
        if(text.equals("\f")) {
            String splitLine = "==========================================================================================================================================================\n";
            return splitLine;
        }
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
            return null;
        }
        return text + "\n";
    }

    /**
     * 读取表格格式
     * @param table 传入的表格
     * @return
     */
    public static String readTable(Table table) {
        String retText = "";
        if(table.numRows() == 1) {
            return null;
        }
        try{
            String[] head = new String[table.getRow(0).numCells()];
            TableRow trHead = table.getRow(0);
            // 遍历表头
            for (int j = 0; j < table.getRow(0).numCells(); j++) {
                TableCell cell = trHead.getCell(j);
                String tempText = cell.text();
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
            // 遍历表体
            for (int j = 1; j < table.numRows(); j++) {
                TableRow tr = table.getRow(j);
                String innerHeadText = tr.getCell(0).text();
                innerHeadText = filterCharacters(innerHeadText);
                String text = "";
                String headText = head[0] + innerHeadText;
                //迭代列，默认从0开始
                for (int k = 1; k < tr.numCells(); k++) {
                    TableCell td = tr.getCell(k);//取得单元格
                    String tempText = td.text();
                    tempText = filterCharacters(tempText);
                    if(tempText.equals("")) {
                        continue;
                    }
                    if(k < head.length) {
                        text += headText + head[k] + tempText + "\n";
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(retText.equals("")) {
            return null;
        }
        return retText + "\n";
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
