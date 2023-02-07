package com.tsinghua.demo;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class MyPDFTextStripper extends PDFTextStripper {

    public MyPDFTextStripper() throws IOException {
        super();
    }

    public List<List<TextPosition>> myGetCharactersByArticle() {
        return getCharactersByArticle();
    }
}
