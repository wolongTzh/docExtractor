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
    // 根据style判断当前行是标题还是内容
    int titleTag = -1;
    int contentTag = -2;
    int unknown = 0;

    /**
     * 初始化关键词词典
     * @param file
     * @throws IOException
     */
    public void init(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
        String s = null;
        while((s = br.readLine())!=null) {//使用readLine方法，一次读一行
            filterChar.add(s);
        }
    }

    /**
     * 根据连续三行的字号大小判断第二行是否为标题
     * @param para1
     * @param para2
     * @param para3
     * @return
     */
    public int exportJudgement(XWPFParagraph para1, XWPFParagraph para2, XWPFParagraph para3) {
        int fz1 = getStyleOrFontSize(para1);
        int fz2 = getStyleOrFontSize(para2);
        int fz3 = getStyleOrFontSize(para3);
        if(fz1 <= unknown || fz2 <= unknown || fz3 <= unknown) {
            if(fz2 == titleTag) {
                return 3;
            }
            else if(fz2 == contentTag) {
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

    /**
     * 获取当前行的形式（标题 or 内容）或者是字号
     * @param para
     * @return
     */
    public int getStyleOrFontSize(XWPFParagraph para) {
        String style = para.getStyle();
        if(style != null) {
            if(style.contains("Head")) {
                return titleTag;
            }
            if(style.contains("Body")) {
                return contentTag;
            }
        }
        List<XWPFRun> runs = para.getRuns();
        for(XWPFRun run : runs) {
            if(run.getFontSize() != -1) {
                return run.getFontSize();
            }
        }
        return unknown;
    }

    /**
     * 过滤掉无意义的当前行
     * @param para
     * @return
     */
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

    /**
     * 替换无意义字符
     * @param text
     * @return
     */
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

    /**
     * 过滤无用段落，通过关键词的匹配
     * @param content
     * @return
     */
    public boolean filterKeyWord(String content) {
        for(String str : filterChar) {
            if(content.contains(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 读取段落文本格式
     * @param para 段落
     * @return
     */
    public String readText(XWPFParagraph para) {
        String text = para.getText();
        text = filterCharacters(text);
        if(text.equals("")) {
            return null;
        }
        return text;
    }
}
