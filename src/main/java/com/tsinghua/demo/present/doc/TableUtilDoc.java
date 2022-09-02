package com.tsinghua.demo.present.doc;

import com.tsinghua.demo.present.docx.TextFilterUtil;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableUtilDoc {

    TextFilterUtil textFilterUtil = new TextFilterUtil();
    Map<Integer, Integer> widthRecorder = new HashMap<>();
    List<String> titleRecorder = new ArrayList<>();

    /**
     * 读取表格格式
     * @param table 传入的表格
     * @return
     */
    public String readTable(Table table, boolean consistancy) {
        String retText = "";
        if(!consistancy) {
            retText += "\n";
            System.out.println();
        }
        boolean titlePos = true;
        if(!consistancy) {
            widthRecorder = new HashMap<>();
            titleRecorder = new ArrayList<>();
        }
        //遍历表体
        for (int i = 0; i < table.numRows(); i++) {
            TableRow row = table.getRow(i);
            //读取每一列数据
            Map<Integer, Integer> tempWidth = new HashMap<>();
            List<String> tempTitle = new ArrayList<>();
            int widthAcc = 0;
            int curIndex = 0;
            String fstCol = "";
            for (int j = 0; j < row.numCells(); j++) {
                TableCell cell = row.getCell(j);
                int width = cell.getWidth();
                widthAcc += width;
                String text = cell.text();
                text = textFilterUtil.filterCharacters(text);
                if(titleRecorder.size() == 0) {
                    tempTitle.add(text);
                    tempWidth.put(j, width);
                }
                else if(row.numCells() == titleRecorder.size()) {
                    titlePos = false;
                    if(text.equals("")) {
                        continue;
                    }
                    if(j == 0) {
                        fstCol = titleRecorder.get(j) + ":" + text;
                    }
                    else {
                        retText += "【" + fstCol + "——" + titleRecorder.get(j) + ":" + text + "】\n";
                        System.out.println(fstCol + "——" + titleRecorder.get(j) + ":" + text);
                    }
                }
                else {
                    if(!titlePos) {
                        continue;
                    }
                    int lastWidth = widthRecorder.get(curIndex);
                    String lastTitleText = titleRecorder.get(curIndex);
                    String curTitleText = lastTitleText;
                    if(!text.equals("")) {
                        curTitleText = lastTitleText + "|" + text;
                    }
                    tempWidth.put(j, width);
                    tempTitle.add(curTitleText);
                    if(widthAcc == lastWidth) {
                        widthAcc = 0;
                        curIndex++;
                    }
                }
            }
            if(titlePos) {
                widthRecorder = tempWidth;
                titleRecorder = tempTitle;
            }
        }
        return retText + "\n";
    }


    /**
     * 读取表格格式
     * @param table 传入的表格
     * @return
     */
    public String readTableOld(XWPFTable table) {
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
            tempText = textFilterUtil.filterCharacters(tempText);
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
                tempText = textFilterUtil.filterCharacters(tempText);
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
}
