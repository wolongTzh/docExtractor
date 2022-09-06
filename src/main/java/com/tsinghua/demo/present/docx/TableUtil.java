package com.tsinghua.demo.present.docx;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.util.*;

public class TableUtil {

    TextFilterUtil textFilterUtil = new TextFilterUtil();
    Map<Integer, Integer> widthRecorder = new HashMap<>();
    List<String> titleRecorder = new ArrayList<>();

    /**
     * 读取表格格式
     * @param table 传入的表格
     * @param consistancy 该表格是否是和上一个表格连续
     *                    主要是根据两个表格之间是否有其他文本来判断的
     * @return
     */
    public String readTable(XWPFTable table, boolean consistancy) throws IOException {
        if(textFilterUtil.filterChar.size() == 0) {
            textFilterUtil.init();
        }
        // 特殊情况：A 指 B
        String pointSituation = pointSituation(table);
        if(pointSituation != null) {
            return pointSituation + "\n";
        }
        // 特殊情况：左右结构的表格
        String leftRightTable = leftRightTable(table);
        if(leftRightTable != null) {
            return leftRightTable + "\n";
        }
        String retText = "";
        List<XWPFTableRow> rows = table.getRows();
        // 存在表头第一行有明显标题倾向的情况下也算是一个新的表格，不能连续
        if(consistancy) {
            consistancy = !judgeUniqueTitleName(rows.get(0));
        }
        // 不连续的表格要空行
        if(!consistancy) {
            retText += "\n";
        }
        // 用于标识每行是否是标题行（主要是根据上一行是不是标题来判断，当然有可能不全面，所以也可能需要加一些其他考虑情况）
        boolean titlePos = true;
        // 该bool主要是用来标注表格里面嵌套复杂标题的情况
        boolean complicatedTitlePos = false;
        if(!consistancy) {
            widthRecorder = new HashMap<>();
            titleRecorder = new ArrayList<>();
        }
        //遍历表体
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            // 判断该行是否为左右结构
            String leftRight = leftRight(row);
            if(leftRight != null) {
                retText += leftRight;
                continue;
            }
            //读取每一列数据
            List<XWPFTableCell> cells = row.getTableCells();
            // 特殊情况：一行一条信息
            if(cells.size() == 1) {
                if(!textFilterUtil.filterKeyWordTable(cells.get(0).getText())) {
                    continue;
                }
                retText += cells.get(0).getText() + "\n";
                continue;
            }
            Map<Integer, Integer> tempWidth = new HashMap<>();
            List<String> tempTitle = new ArrayList<>();
            int widthAcc = 0;
            int curIndex = 0;
            String fstCol = "";
            // 遍历一行的每一列
            for (int j = 0; j < cells.size(); j++) {
                complicatedTitlePos = false;
                XWPFTableCell cell = cells.get(j);
                int width = cell.getWidth();
                widthAcc += width;
                String text = cell.getText();
                text = textFilterUtil.filterCharacters(text);
                // 属于该行是第一行标题行的情况，要向集合里面添加标题元素
                if(titleRecorder.size() == 0) {
                    tempTitle.add(text);
                    tempWidth.put(j, width);
                }
                // 该行是内容行的情况
                else if(cells.size() == titleRecorder.size() &&  !judgeTitleWithoutColor(row)) {
                    titlePos = false;
                    if(text.equals("")) {
                        continue;
                    }
                    // 第一列信息为主要信息，需要拼到后面每一列
                    if(j == 0) {
                        if(titleRecorder.get(j).equals("")) {
                            fstCol = text;
                        }
                        else {
                            fstCol = titleRecorder.get(j) + ":" + text;
                        }
                    }
                    // 其它列的信息需要和第一列进行拼接
                    else {
                        String tempAdd = "【" + fstCol + "——" + titleRecorder.get(j) + ":" + text + "】\n";
                        if(!textFilterUtil.filterKeyWordTable(tempAdd)) {
                            continue;
                        }
                        retText += tempAdd;
                    }
                }
                // 该行是标题行，但不是第一行标题，需要逐渐填充
                else {
                    // 一些不是标题行的特殊情况
                    if(!judgeTitle(row) && !judgeTitleWithoutColor(row) && !titlePos) {
                        continue;
                    }
                    // 上一行不是标题但是本行是标题，所以也是标题的开始
                    if(!titlePos) {
                        tempTitle.add(text);
                        tempWidth.put(j, width);
                        complicatedTitlePos = true;
                        continue;
                    }
                    // 拼接标题，更新list和map
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
            // 该行是表格中的嵌套表格的标题，也算是该更新list了
            if(!titlePos && complicatedTitlePos) {
                titlePos = true;
            }
            // 过了一行标题后，进行更新
            if(titlePos) {
                widthRecorder = tempWidth;
                titleRecorder = tempTitle;
            }
        }
        return retText + "\n";
    }

    /**
     * 指情况：A 指 B
     * @param table
     * @return
     */
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

    /**
     * 判断是左右结构的行
     * @param row
     * @return
     */
    public String leftRight(XWPFTableRow row) {
        String retText = "";
        List<XWPFTableCell> cells = row.getTableCells();
        // 单元格数量需为2的倍数
        if(cells.size() % 2 != 0) {
            return null;
        }
        String color = null;
        // 单元格背景颜色从左至右一个是黑一个是白
        for (int j = 0; j < cells.size(); j++) {
            XWPFTableCell cell = cells.get(j);
            if(Objects.equals(color, cell.getColor())) {
                return null;
            }
            color = cell.getColor();
        }
        // 返回左右结构的拼接文本
        for (int j = 0; j < cells.size(); j+=2) {
            XWPFTableCell cell1 = cells.get(j);
            XWPFTableCell cell2 = cells.get(j+1);
            String cell1Text = textFilterUtil.filterCharacters(cell1.getText());
            String cell2Text = textFilterUtil.filterCharacters(cell2.getText());
            if(cell1Text.equals("") || cell2Text.equals("")) {
                continue;
            }
            retText += "【" + cell1.getText() + " = " + cell2.getText() + "】\n";
        }
        return retText;
    }

    /**
     * 判断是纯左右结构的表格
     * @param table
     * @return
     */
    public String leftRightTable(XWPFTable table) {
        List<XWPFTableRow> rows = table.getRows();
        // 判断第一行标题行是否是标题，是标题就不是左右结构表格
        if(judgeTitleWithoutColor(rows.get(0))) {
            return null;
        }
        String retText = "";
        boolean shortTag = false;
        // 判断每一行单元格数量，要求都是2的倍数且不能大于4且必须有两个单元格为一行的情况
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
        // 单元格如果有空值，那就不是左右结构表格了
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                if(cell.getText().equals("")) {
                    return null;
                }
            }
        }
        // 判断是左右结构表格，进行赋值
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            for (int j = 0; j < cells.size(); j+=2) {
                XWPFTableCell cell1 = cells.get(j);
                XWPFTableCell cell2 = cells.get(j+1);
                String cell1Text = textFilterUtil.filterCharacters(cell1.getText());
                String cell2Text = textFilterUtil.filterCharacters(cell2.getText());
                retText += "【" + cell1Text + " = " + cell2Text + "】\n";
            }
        }
        return retText;
    }

    /**
     * 判断该行为标题行（一行都是有背景色的情况）
     * @param row
     * @return
     */
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

    /**
     * 判断该行为标题行(不考虑单元格背景颜色)
     * @param row
     * @return
     */
    public boolean judgeTitleWithoutColor(XWPFTableRow row) {
        List<XWPFTableCell> cells = row.getTableCells();
        // 该行只有一个单元格，占了一行，肯定不是标题
        if(cells.size() < 2) {
            return false;
        }
        // 该行第一个单元格是空，是标题行
        if(cells.get(0).getText().equals("")) {
            return true;
        }
        // 该行如果存在有空值的情况，就不是标题行
        for (int j = 1; j < cells.size(); j++) {
            XWPFTableCell cell = cells.get(j);
            String text = cell.getText();
            if(text.equals("")) {
                return false;
            }
        }
        // 判断是否有特殊标题名称，有就是标题行
        return judgeUniqueTitleName(row);
    }

    /**
     * 特殊标题名称判断（为该标题名称时代表是新表格的标题）
     * @param row
     * @return
     */
    public boolean judgeUniqueTitleName(XWPFTableRow row) {
        List<XWPFTableCell> cells = row.getTableCells();
        for (int j = 0; j < cells.size(); j++) {
            XWPFTableCell cell = cells.get(j);
            String text = cell.getText();
            if(textFilterUtil.filterCharacters(text).equals("项目") || textFilterUtil.filterCharacters(text).equals("金额")) {
                return true;
            }
        }
        return false;
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
