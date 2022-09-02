package com.tsinghua.demo.present.docx;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextFilterUtil {

    List<String> filterChar = new ArrayList<>();
    public void init(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
        String s = null;
        while((s = br.readLine())!=null) {//使用readLine方法，一次读一行
            filterChar.add(s);
        }
    }

    public int exportJudgement(XWPFParagraph para1, XWPFParagraph para2, XWPFParagraph para3) {
        int fz1 = getFontSize(para1);
        int fz2 = getFontSize(para2);
        int fz3 = getFontSize(para3);
        if(fz1 <= 0 || fz2 <= 0 || fz3 <= 0) {
            if(fz2 == -1) {
                return 3;
            }
            else if(fz2 == -2) {
                return 1;
            }
            else {
                return -1;
            }
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

    public int getFontSize(XWPFParagraph para) {
        String style = para.getStyle();
        if(style != null) {
            if(style.contains("Head")) {
                return -1;
            }
            if(style.contains("Body")) {
                return -2;
            }
        }
        List<XWPFRun> runs = para.getRuns();
        for(XWPFRun run : runs) {
            if(run.getFontSize() != -1) {
                return run.getFontSize();
            }
        }
        return 0;
    }

    public int getStyle(XWPFParagraph para) {
        String style = para.getStyle();
        if(style != null) {
            if(style.contains("Head")) {
                return 1;
            }
            if(style.contains("Body")) {
                return 2;
            }
        }
        return 0;
    }

    public boolean filterMeaninglessPara(XWPFParagraph para) {
        String text = para.getText();
        if(text == null) {
            return false;
        }
        if(text.equals("") || text.equals("\n") || text.equals("\t") || text.equals(" ")) {
            return false;
        }
        return true;
    }

    public String filterCharacters(String text) {
        text = text.replace(" ", "");
        text = text.replace("\n", "");
        text = text.replace("\r", "");
        text = text.replace("\b", "");
//        text = text.replace("\f", "");
        text = text.replace("\u000E", "");
        text = text.replace("\u0007", "");
        text = text.replace("\u0001", "");
        text = text.replace("\t", "");
        return text;
    }

    public boolean filterKeyWord(String content) {
        for(String str : filterChar) {
            if(content.contains(str)) {
                return true;
            }
        }
        return false;
    }
}
