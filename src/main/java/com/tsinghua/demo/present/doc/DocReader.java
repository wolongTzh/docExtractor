package com.tsinghua.demo.present.doc;

import com.tsinghua.demo.present.doc.TableUtilDoc;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DocReader {

    static TableUtilDoc tableUtilDoc = new TableUtilDoc();
    static String splitLine = "===========================================\n\n";

    public static void main(String[] args) throws Exception {
        String inputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\1210827387.doc";
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
        boolean startPage = true;
        Paragraph para1 = null;
        Paragraph para2 = null;
        Paragraph para3 = null;
        String content = "";
        String title = "";
        boolean articleStart = true;
        for (int i=0; i<paraNum; i++) {
            Paragraph para = range.getParagraph(i);
            if(!para.isInTable()) {
                if(!filterMeaninglessPara(para)) {
                    continue;
                }
                if(articleStart) {
                    String text = readText(para);
                    title += "《" + text + "》\n";
                    articleStart = false;
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
                    judge = exportJudgement(para1, para2, para3);
                }
                para = para2;
                String text = readText(para);
                if(text == null) {
                    continue;
                }
                if(judge == 2 && content.equals("")) {
                    judge = 0;
                }
                String text3 = readText(para3);
                if(text3 != null && text3.equals(splitLine)) {
                    if(!content.equals("")) {
                        builder.append(content + "\n");
                    }
                    content = "";
                    if(!title.equals("")) {
                        builder.append(title + "\n");
                    }
                    title = "";
                    builder.append(splitLine);
                    startPage = true;
                }
                else {
                    startPage = false;
                }
                if(text.equals(splitLine)) {
                    continue;
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
//                builder.append(text);
                article.add(text);
            }
            if(para.isInTable()) {
                if(!content.equals("")) {
                    builder.append(content + "\n");
                }
                content = "";
                if(!title.equals("")) {
                    builder.append(title + "\n");
                }
                title = "";
                try {
                    Table table = range.getTable(para);
                    if (tablePre != null && tablePre.getEndOffset() == table.getEndOffset()) {
                        continue;
                    }
                    tablePre = table;
                    String text = tableUtilDoc.readTable(table, startPage);
                    if (text == null) {
                        continue;
                    }
                    article.add(text);
                    builder.append(text);
//                    System.out.println(text);
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
        return text;
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

    public static boolean filterMeaninglessPara(Paragraph para) {
        String text = para.text();
        if(text == null) {
            return false;
        }
        if(text.equals("") || text.equals("\n") || text.equals("\t") || text.equals(" ") || text.equals("\r") || text.equals("\b")) {
            return false;
        }
        return true;
    }

    public static int exportJudgement(Paragraph para1, Paragraph para2, Paragraph para3) {
        int fz1 = getFontSize(para1);
        int fz2 = getFontSize(para2);
        int fz3 = getFontSize(para3);
        if(fz1 <= -1 || fz2 <= -1 || fz3 <= -1) {
            return -1;
        }
        if(fz1 > fz2 && fz2 > fz3) {
            return 0;
        }
        else if(fz1 > fz2) {
            return 1;
        }
        else if(fz1 == fz2) {
            return 2;
        }
        else if(fz1 < fz2) {
            return 3;
        }
        return 0;
    }

    public static int getFontSize(Paragraph para) {
        int k = 0;
        while(true) {
            CharacterRun run = para.getCharacterRun(k++);
            if(run.getFontSize() != -1) {
                return run.getFontSize();
            }
            if (run.getEndOffset() == para.getEndOffset()) {
                break;
            }
        }
        return 0;
    }
}
