package com.tsinghua.demo;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DocExtractorPoi {

    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\wps转换后报告.doc";
        paraInWordDoc(path);
    }


    /**
     * 读取文档中表格（docx）
     * @param filePath 文档路径
     */
    public static List<String> tableInWordDocx(String filePath){
        try{
            FileInputStream in = new FileInputStream(filePath);//载入文档
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            Iterator<XWPFTable> it = xwpf.getTablesIterator();//得到word中的表格
            // 设置需要读取的表格
            List<String> tableList = new ArrayList<>();
            while(it.hasNext()){
                XWPFTable table = it.next();
                List<XWPFTableRow> rows = table.getRows();
                String[] head = new String[rows.get(0).getTableCells().size()];
                String outStr = "";
                //读取每一行数据
                for (int i = 0; i < rows.size(); i++) {
                    XWPFTableRow row = rows.get(i);
                    //读取每一列数据
                    List<XWPFTableCell> cells = row.getTableCells();
                    List<String> rowList = new ArrayList<>();
                    outStr = "";
                    for (int j = 0; j < cells.size(); j++) {
                        XWPFTableCell cell = cells.get(j);
//                        System.out.println(cell.getColor());
                        if(i == 0) {
                            head[j] = cell.getText();
                        }
                        else if(!cell.getText().equals("")) {
//                            outStr += head[j] + "为" + cell.getText() + ",";
                        }
                        outStr += cell.getText() + "+";
                    }
                    System.out.println(outStr);
                    if(i != 0) {
//                        outStr = outStr.substring(0, outStr.length()-1);
                        tableList.addAll(rowList);
//                        System.out.println(outStr);
                    }
                }
            }
            return tableList;

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文档中表格（doc）
     * @param filePath 文档路径
     */
    public static List<String> tableInWordDoc(String filePath){
        try{
            FileInputStream in = new FileInputStream(filePath);//载入文档
            // 处理doc格式 即office2003版本
            POIFSFileSystem pfs = new POIFSFileSystem(in);
            HWPFDocument hwpf = new HWPFDocument(pfs);

            Range range = hwpf.getRange();//得到文档的读取范围
            TableIterator it = new TableIterator(range);
            List<String> tableList = new ArrayList<>();
            while (it.hasNext()) {
                Table tb = it.next();
                //迭代行，默认从0开始,可以依据需要设置i的值,改变起始行数，也可设置读取到那行，只需修改循环的判断条件即可
                for (int i = 0; i < tb.numRows(); i++) {
                    List<String> rowList = new ArrayList<>();
                    TableRow tr = tb.getRow(i);
                    //迭代列，默认从0开始
                    for (int j = 0; j < tr.numCells(); j++) {
                        TableCell td = tr.getCell(j);//取得单元格
                        //取得单元格的内容
                        for(int k = 0; k < td.numParagraphs(); k++){
                            Paragraph para = td.getParagraph(k);
                            String s = para.text();
                            //去除后面的特殊符号
                            if(null != s && !"".equals(s)){
                                s = s.substring(0, s.length()-1);
                            }
                            rowList.add(s);
                            System.out.print(s + "\t");
                        }
                    }
                    tableList.addAll(rowList);
                    System.out.println();
                }
            }
            return tableList;
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文档中段落（docx）
     * @param filePath 文档路径
     */
    public static List<String> paraInWordDocx(String filePath){
        try{
            FileInputStream in = new FileInputStream(filePath);//载入文档
            // 处理docx格式 即office2007以后版本

            //word 2007 图片不会被读取， 表格中的数据会被放在字符串的最后
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            Iterator<XWPFParagraph> it = xwpf.getParagraphsIterator();//得到word中的表格
            while(it.hasNext()) {
                XWPFParagraph para = it.next();
                System.out.println(para.getText());
            }
            // 处理doc格式 即office2003版本

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> test(String filePath){
        try{
            FileInputStream in = new FileInputStream(filePath);//载入文档
            XWPFDocument xwpf = new XWPFDocument(in);//得到word文档的信息
            for(IBodyElement element : xwpf.getBodyElements()) {
                if(element instanceof XWPFParagraph) {
                    XWPFParagraph para = (XWPFParagraph) element;
                    System.out.println(para.getText());
                }
                if(element instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) element;
                    List<XWPFTableRow> rows = table.getRows();
                    String outStr = "";
                    //读取每一行数据
                    for (int i = 0; i < rows.size(); i++) {
                        XWPFTableRow row = rows.get(i);
                        //读取每一列数据
                        List<XWPFTableCell> cells = row.getTableCells();
                        outStr = "";
                        for (int j = 0; j < cells.size(); j++) {
                            XWPFTableCell cell = cells.get(j);
                            outStr += cell.getText() + "+";
                        }
                        System.out.println(outStr);
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文档中段落（doc）
     * @param filePath 文档路径
     */
    public static void paraInWordDoc(String filePath) throws IOException {
        InputStream is = new FileInputStream(filePath);
        HWPFDocument doc = new HWPFDocument(is);
        //输出文本
        System.out.println(doc.getDocumentText());
        Range range = doc.getRange();
        //获取段落数
        int paraNum = range.numParagraphs();
        Table tablePre = null;
        for (int i=0; i<paraNum; i++) {
            Paragraph para = range.getParagraph(i);
            if(!para.isInTable()) {
                System.out.println(range.getParagraph(i).text());
            }
            else {
                try{
                    Table table = range.getTable(para);
                    if(tablePre != null && tablePre.getEndOffset() == table.getEndOffset()) {
                        continue;
                    }
                    tablePre = table;
                    for (int j = 0; j < table.numRows(); j++) {
                        List<String> rowList = new ArrayList<>();
                        TableRow tr = table.getRow(j);
                        //迭代列，默认从0开始
                        for (int k = 0; k < tr.numCells(); k++) {
                            TableCell td = tr.getCell(k);//取得单元格
                            System.out.println(td.text());
                            //取得单元格的内容
                            for (int m = 0; m < td.numParagraphs(); m++) {
                                Paragraph para1 = td.getParagraph(m);
                                String s = para1.text();
                                //去除后面的特殊符号
                                if (null != s && !"".equals(s)) {
                                    s = s.substring(0, s.length() - 1);
                                }
                                rowList.add(s);
//                                System.out.print(s + "\t");
                            }
                        }
                        System.out.println();
                    }
                }
                catch (Exception e) {
                    continue;
                }
            }
        }
    }
}
