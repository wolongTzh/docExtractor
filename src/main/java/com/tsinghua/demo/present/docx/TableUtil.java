package com.tsinghua.demo.present.docx;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.*;

public class TableUtil {

    TextFilterUtil textFilterUtil = new TextFilterUtil();
    Map<Integer, Integer> widthRecorder = new HashMap<>();
    List<String> titleRecorder = new ArrayList<>();

    /**
     * 读取表格格式
     * @param table 传入的表格
     * @return
     */
    public String readTable(XWPFTable table, boolean consistancy) {
        String pointSituation = pointSituation(table);
        if(pointSituation != null) {
            return pointSituation + "\n";
        }
        String leftRightTable = leftRightTable(table);
        if(leftRightTable != null) {
            return leftRightTable + "\n";
        }
        String retText = "";
        List<XWPFTableRow> rows = table.getRows();
//        XWPFTableRow firstRow = rows.get(0);
//        boolean consistancy = !judgeTitle(firstRow) && leftRight(firstRow) == null;
        if(!consistancy) {
            retText += "\n";
            System.out.println();
        }

        boolean titlePos = true;
        boolean complicatedTitlePos = false;
        if(!consistancy) {
            widthRecorder = new HashMap<>();
            titleRecorder = new ArrayList<>();
        }
        //遍历表体
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            String leftRight = leftRight(row);
            if(leftRight != null) {
                retText += leftRight;
                continue;
            }
            //读取每一列数据
            List<XWPFTableCell> cells = row.getTableCells();
            if(cells.size() == 1) {
                retText += cells.get(0).getText() + "\n";
                continue;
            }
            Map<Integer, Integer> tempWidth = new HashMap<>();
            List<String> tempTitle = new ArrayList<>();
            int widthAcc = 0;
            int curIndex = 0;
            String fstCol = "";
            for (int j = 0; j < cells.size(); j++) {
                complicatedTitlePos = false;
                XWPFTableCell cell = cells.get(j);
                int width = cell.getWidth();
                widthAcc += width;
                String text = cell.getText();
                text = textFilterUtil.filterCharacters(text);
                if(titleRecorder.size() == 0) {
                    tempTitle.add(text);
                    tempWidth.put(j, width);
                }
                else if(cells.size() == titleRecorder.size() &&  !judgeTitleWithoutColor(row)) {
                    titlePos = false;
                    if(text.equals("")) {
                        continue;
                    }
                    if(j == 0) {
                        if(titleRecorder.get(j).equals("")) {
                            fstCol = text;
                        }
                        else {
                            fstCol = titleRecorder.get(j) + ":" + text;
                        }
                    }
                    else {
                        retText += "【" + fstCol + "——" + titleRecorder.get(j) + ":" + text + "】\n";
                        System.out.println(fstCol + "——" + titleRecorder.get(j) + ":" + text);
                    }
                }
                else {
                    if(!judgeTitle(row) && !judgeTitleWithoutColor(row) && !titlePos) {
                        continue;
                    }
                    if(!titlePos) {
                        tempTitle.add(text);
                        tempWidth.put(j, width);
                        complicatedTitlePos = true;
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
            if(!titlePos && complicatedTitlePos) {
                titlePos = true;
            }
            if(titlePos) {
                widthRecorder = tempWidth;
                titleRecorder = tempTitle;
            }
        }
        return retText + "\n";
    }

    public String pointSituation(XWPFTable table) {
        List<XWPFTableRow> rows = table.getRows();
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            if(cells.size() != 3) {
                return null;
            }
            if(!cells.get(1).getText().equals("指")) {
                return null;
            }
        }
        String ret = "";
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            ret += "【" + row.getTableCells().get(0).getText() + " = " + row.getTableCells().get(2).getText() + "】\n";
        }
        return ret;
    }

    public String leftRight(XWPFTableRow row) {
        String retText = "";
        List<XWPFTableCell> cells = row.getTableCells();
        if(cells.size() % 2 != 0) {
            return null;
        }
        String color = null;
        for (int j = 0; j < cells.size(); j++) {
            XWPFTableCell cell = cells.get(j);
            if(Objects.equals(color, cell.getColor())) {
                return null;
            }
            color = cell.getColor();
        }
        for (int j = 0; j < cells.size(); j+=2) {
            XWPFTableCell cell1 = cells.get(j);
            XWPFTableCell cell2 = cells.get(j+1);
            retText += "【" + cell1.getText() + " = " + cell2.getText() + "】\n";
        }
        return retText;
    }

    public String leftRightTable(XWPFTable table) {
        List<XWPFTableRow> rows = table.getRows();
        String retText = "";
        boolean shortTag = false;
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            if(cells.size() % 2 != 0 || cells.size() > 4) {
                return null;
            }
            if(cells.size() == 2) {
                shortTag = true;
            }
        }
        if(!shortTag) {
            return null;
        }
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            for (int j = 0; j < cells.size(); j+=2) {
                XWPFTableCell cell1 = cells.get(j);
                XWPFTableCell cell2 = cells.get(j+1);
                retText += "【" + cell1.getText() + " = " + cell2.getText() + "】\n";
            }
        }
        return retText;
    }

    public boolean judgeTitle(XWPFTableRow row) {
        List<XWPFTableCell> firstCells = row.getTableCells();
        for (int j = 0; j < firstCells.size(); j++) {
            XWPFTableCell cell = firstCells.get(j);
            String color = cell.getColor();
            if(color == null) {
                return false;
            }
        }
        return true;
    }

    public boolean judgeTitleWithoutColor(XWPFTableRow row) {
        List<XWPFTableCell> cells = row.getTableCells();
        if(cells.size() < 2) {
            return false;
        }
        if(!cells.get(0).getText().equals("")) {
            return false;
        }
        for (int j = 1; j < cells.size(); j++) {
            XWPFTableCell cell = cells.get(j);
            String text = cell.getText();
            if(text.equals("")) {
                return false;
            }
        }
        return true;
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
