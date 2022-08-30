package com.tsinghua.demo.present;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.List;

public class SplitPara {

    public int exportJudgement(XWPFParagraph para1, XWPFParagraph para2, XWPFParagraph para3) {
        int fz1 = getFontSize(para1);
        int fz2 = getFontSize(para2);
        int fz3 = getFontSize(para3);
        if(fz1 == -1 || fz2 == -1 || fz3 == -1) {
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
        return 0;
    }

    public int getFontSize(XWPFParagraph para) {
        List<XWPFRun> runs = para.getRuns();
        for(XWPFRun run : runs) {
            if(run.getFontSize() != -1) {
                return run.getFontSize();
            }
        }
        return -1;
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
}
