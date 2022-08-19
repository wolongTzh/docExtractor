package com.tsinghua.demo;

import com.spire.doc.Document;
import com.spire.doc.DocumentObject;
import com.spire.doc.Section;
import com.spire.doc.Table;
import com.spire.doc.TableRow;
import com.spire.doc.documents.DocumentObjectType;
import com.spire.doc.documents.Paragraph;

public class DocExtractorSpire {

    public static void main(String[] args) {
        String path = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\test\\wps转换后报告.doc";
        spireParaghDoc(path);
//        spireForTableOfDoc(path);
    }

    //读取段落
    public static void spireParaghDoc(String path) {
        Document doc = new Document();
        doc.loadFromFile(path);
        for (int i = 0; i < doc.getSections().getCount(); i++) {
            Section section = doc.getSections().get(i);
            for (int j = 0; j < section.getParagraphs().getCount(); j++) {
                Paragraph paragraph = section.getParagraphs().get(j);
                System.out.println(paragraph.getText());
            }
        }
    }

    //读取表格
    public static void spireForTableOfDoc(String path) {
        Document doc = new Document(path);
        for (int i = 0; i < doc.getSections().getCount(); i++) {
            Section section = doc.getSections().get(i);
            for (int j = 0; j < section.getBody().getChildObjects().getCount(); j++) {
                DocumentObject obj = section.getBody().getChildObjects().get(j);
                if (obj.getDocumentObjectType() == DocumentObjectType.Table) {
                    Table table = (Table) obj;
                    for (int k = 0; k < table.getRows().getCount(); k++) {
                        TableRow rows = table.getRows().get(k);
                        for (int p = 0; p < rows.getCells().getCount(); p++) {
                            for (int h = 0; h < rows.getCells().get(p).getParagraphs().getCount(); h++) {
                                Paragraph f = rows.getCells().get(p).getParagraphs().get(h);
                                System.out.println(f.getText());
                            }
                        }
                    }
                }
            }
        }
    }
}
