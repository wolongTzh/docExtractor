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
    // 文档过滤开关
    boolean startFilter = true;
    boolean startFilterTable = true;
    int pairCountPor = 500;

    /**
     * 初始化关键词词典
     * @throws IOException
     */
    public void init() throws IOException {
        File file = new File("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\filterTag.txt");
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
    public JudgeFontSizeEnum exportJudgement(XWPFParagraph para1, XWPFParagraph para2, XWPFParagraph para3) {
        int fz1 = getStyleOrFontSize(para1);
        int fz2 = getStyleOrFontSize(para2);
        int fz3 = getStyleOrFontSize(para3);
        if(fz1 <= unknown || fz2 <= unknown || fz3 <= unknown) {
            if(fz2 == titleTag) {
                return JudgeFontSizeEnum.TITILE_TAG;
            }
            else if(fz2 == contentTag) {
                return JudgeFontSizeEnum.CONTENT_TAG;
            }
            else {
                return JudgeFontSizeEnum.UNKHOWN_TAG;
            }
        }
        if(fz1 > fz2 && fz2 > fz3) {
            return JudgeFontSizeEnum.TITILE_TAG;
        }
        else if(fz1 > fz2) {
            return JudgeFontSizeEnum.CONTENT_TAG;
        }
        else if(fz1 == fz2) {
            return JudgeFontSizeEnum.NEED_CHECK_TAG;
        }
        else if(fz1 < fz2) {
            return JudgeFontSizeEnum.TITILE_TAG;
        }
        return JudgeFontSizeEnum.UNKHOWN_TAG;
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

    public boolean filterPara(List<String> headList, List<String> contentList) {
        if(headList.size() != contentList.size()) {
            return false;
        }
        String contentToFilter = "";
        for(int i=0; i<headList.size(); i++) {
            String head = headList.get(i);
            String content = contentList.get(i);
            contentToFilter += head + content;
            if(filterKeyWord(contentToFilter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤无用段落，通过关键词的匹配
     * @param content
     * @return
     */
    public boolean filterKeyWord(String content) {
        if(!startFilter) {
            return true;
        }
        content = content.replace("\n\n", "\n");
        // 标题中含有关键词则直接保留
        String[] contents = content.split("\n");
        for(String str : contents) {
            if(str.startsWith("《") && str.endsWith("》")) {
                for(String c : filterChar) {
                    if(str.contains(c)) {
                        return true;
                    }
                }
            }
        }
        int count = 0;
        // 通过比例计算需要匹配多少个关键词
        int neededCount = content.length() / pairCountPor;
        if(neededCount == 0) {
            neededCount = 1;
        }
        // 关键词匹配
        for(String str : filterChar) {
            if(count >= neededCount) {
                return true;
            }
            count += appearTime(content, str);
        }
        return false;
    }

    /**
     * 过滤无用段落，通过关键词的匹配（表格版本）
     * @param content
     * @return
     */
    public boolean filterKeyWordTable(String content) {
        if(!startFilterTable) {
            return true;
        }
        for(String str : filterChar) {
            if(content.contains(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算一个字符串包含另一个字符串的次数
     * @param source
     * @param target
     * @return
     */
    public int appearTime(String source, String target) {
        int times = 0;
        String handledStr = source;
        while (handledStr.contains(target)) {
            int index = handledStr.indexOf(target);
            times++;
            handledStr = handledStr.substring(index + target.length());
        }
        return times;
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
