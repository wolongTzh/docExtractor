package com.tsinghua.demo.present.docx;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

public class DocXReader {

    static TextFilterUtil textFilterUtil = new TextFilterUtil();
    static TableUtil tableUtil = new TableUtil();

    public static void main(String[] args) throws Exception {

        singelGeneartor();
//        batchGenerator();
    }

    /**
     * 转化单篇文档
     */
    public static void singelGeneartor() {
        String inputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\1.docx";
        String outputPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\ExtractedTable.txt";
        distributor(inputPath, outputPath);
    }

    /**
     * 批量生成文档
     */
    public static void batchGenerator() {
        String basePath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\output\\";
        File file1 = new File(basePath);
        //判断是否有目录
        if(file1.isDirectory()) {
            //获取目录中的所有文件名称
            String[] fileName = file1.list();
            for(String str : fileName) {
                distributor(basePath + str, basePath + str.replace("docx", "txt"));
            }
        }
    }

    /**
     * 分配器，用于判断是读取表格还是纯文本
     * @param filePath 文件路径
     * @return
     */
    public static void distributor(String filePath, String outputPath){
        try{
            if(!filePath.endsWith(".docx")) {
                return;
            }
            File file = new File("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\filterTag.txt");
            // 过滤信息用的信息初始化
            textFilterUtil.init(file);
            FileInputStream in = new FileInputStream(filePath);//载入文档
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            // 三个连续行判断字号确定是否是标题
            XWPFParagraph para1 = null;
            XWPFParagraph para2 = null;
            XWPFParagraph para3 = null;
            // 存储内容 or 标题文本
            String content = "";
            String title = "";
            // 全文开始的标志
            boolean pageStartTag = true;
            // 表示当前行为文本，这样来判断后面表格的连续性
            boolean paraTag = true;
            FileWriter fileWriter = new FileWriter(outputPath);
            StringBuilder builder = new StringBuilder();
            for(IBodyElement element : xwpf.getBodyElements()) {
                if(element instanceof XWPFParagraph) {
                    XWPFParagraph para = (XWPFParagraph) element;
                    // 跳过无意义的内容
                    if(!textFilterUtil.filterMeaninglessPara(para)) {
                        continue;
                    }
                    // 文章开始，弟一行作为标题加上，写死了
                    if(pageStartTag) {
                        String text = textFilterUtil.readText(para);
                        title += "《" + text + "》\n";
                        pageStartTag = false;
                    }
                    // 装载连续的三行
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
                    // 判断前后行的字体大小来确定是否为标题行
                    int judge = -1;
                    if(para1 != null && para2 != null && para3 != null) {
                        judge = textFilterUtil.exportJudgement(para1, para2, para3);
                    }
                    // para2是当前去关注的行
                    para = para2;
                    String text = textFilterUtil.readText(para);
                    if(text == null) {
                        continue;
                    }
                    // para3是当前实际读到的行
                    String text3 = textFilterUtil.readText(para3);
                    if(text3 != null) {
                        paraTag = true;
                    }
                    else {
                        pageStartTag = false;
                    }
                    // 如果是前一行和当前行字号一致，但是前一行是标题，那也应该视为标题
                    if(judge == 2 && content.equals("")) {
                        judge = 0;
                    }
                    // 是标题的情况：前一行比当前行字号小，或者前一行大于当前行大于下一行
                    if(judge == 0 || judge == 3) {
                        if(!content.equals("")) {
                            builder.append(content + "\n");
                        }
                        content = "";
                        title += "《" + text + "》\n";
                    }
                    // 是内容的情况：前一行比当前行字号大或者判断不出来的情况
                    else {
                        if(!title.equals("")) {
                            builder.append(title + "\n");
                        }
                        title = "";
                        content += text + "\n";
                    }
                }
                if(element instanceof XWPFTable) {
                    String text = tableUtil.readTable((XWPFTable) element, !paraTag);
                    if(text == null) {
                        continue;
                    }
                    // 是表格的情况，推缓存buffer
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
}
