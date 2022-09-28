package com.tsinghua.demo.present.docx;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.*;
import java.util.*;

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
    Map<String, String> wordsReflection = new HashMap<>();
    Map<String, Integer> mainWordsCount = new HashMap<>();
    List<String> mainWordsList = new ArrayList<>();
    Map<String, Map<String, Integer>> otherWordsCount = new HashMap<>();
    Map<String, Map<String, Integer>> allDocStatistic = new HashMap<>();

    /**
     * 初始化关键词词典
     * @throws IOException
     */
    public void init() throws IOException {
        File file = new File("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\filterTag.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
        String s = null;
        String curCoreWord = "";
        while((s = br.readLine())!=null) {//使用readLine方法，一次读一行
            if(s.charAt(0) == '【') {
                curCoreWord = s;
                mainWordsList.add(s);
            }
            else {
                wordsReflection.putIfAbsent(s, "");
                wordsReflection.put(s, wordsReflection.get(s) + "&" + curCoreWord);
                if(!filterChar.contains(s)) {
                    filterChar.add(s);
                }
            }
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
        statistic(content);
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
        statistic(content);
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

    public void statistic(String content) {
        String curContent = content;
        int count = 0;
        for(String s : filterChar) {
            curContent = content;
            count = 0;
            while (true) {
                int index = curContent.indexOf(s);
                if(index == -1) {
                    break;
                }
                else {
                    curContent = curContent.substring(index + s.length());
                    count++;
                }
            }
            if(count != 0) {
                String mainWordRaw = wordsReflection.get(s);
                String[] mainWords = mainWordRaw.split("&");
                for(String mainWord : mainWords) {
                    if(mainWord.equals("")) {
                        continue;
                    }
                    mainWordsCount.putIfAbsent(mainWord, 0);
                    mainWordsCount.put(mainWord, mainWordsCount.get(mainWord) + 1);
                    otherWordsCount.putIfAbsent(mainWord, new HashMap<>());
                    Map<String, Integer> innerMap = otherWordsCount.get(mainWord);
                    innerMap.putIfAbsent(s, 0);
                    innerMap.put(s, innerMap.get(s) + 1);
                    otherWordsCount.put(mainWord, innerMap);
                }
            }
        }
    }

    public void writeStatistic(String outPath, String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(outPath);
        StringBuilder builder = new StringBuilder();
        Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String,Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                // TODO Auto-generated method stub
                return o2.getValue()-o1.getValue();
            }
        };
        // map转换成list进行排序
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String,Integer>>(mainWordsCount.entrySet());
        // 排序
        Collections.sort(list,valueComparator);
        for(Map.Entry entry : list) {
            String mainWord = (String) entry.getKey();
            Integer mainCount = (Integer) entry.getValue();
            builder.append(mainWord + "\t" + mainCount + "次\n");
            Map<String, Integer> innerMap = otherWordsCount.get(mainWord);
            for(Map.Entry innerEntry : innerMap.entrySet()) {
                String otherWord = (String) innerEntry.getKey();
                Integer count = (Integer) innerEntry.getValue();
                builder.append(otherWord + "\t" + count + "次\n");
            }
            builder.append("\n");
        }
        fileWriter.write(builder.toString());
        fileWriter.flush();
        fileWriter.close();
        allDocStatistic.put(fileName, new HashMap<>(mainWordsCount));
        mainWordsCount.clear();
        otherWordsCount.clear();
    }

    public void writeFinalStatistic(String outPath) throws IOException {
        FileWriter fileWriter = new FileWriter(outPath);
        StringBuilder builder = new StringBuilder();
        Map<String, Integer> out = new HashMap<>();
        Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String,Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                // TODO Auto-generated method stub
                return o2.getValue()-o1.getValue();
            }
        };
        for(String relationName : mainWordsList) {
            for(Map.Entry innerEntry : allDocStatistic.entrySet()) {
                String fileName = (String)innerEntry.getKey();
                Map<String, Integer> singleDoc = (Map<String, Integer>)innerEntry.getValue();
                out.put(fileName, singleDoc.getOrDefault(relationName, 0));
            }
            List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String,Integer>>(out.entrySet());
            Collections.sort(list,valueComparator);
            builder.append(relationName + ":\n");
            for(int i=0; i<10; i++) {
                int index = i + 1;
                builder.append("top" + index + ": 来自文档：" + list.get(i).getKey() + " 出现次数：" + list.get(i).getValue() + "\n");
            }
            builder.append("\n");
        }
        fileWriter.write(builder.toString());
        fileWriter.flush();
        fileWriter.close();
    }
}
