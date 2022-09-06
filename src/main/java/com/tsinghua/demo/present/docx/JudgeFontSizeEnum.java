package com.tsinghua.demo.present.docx;

public enum JudgeFontSizeEnum {

    TITILE_TAG(1, "title"),
    CONTENT_TAG(2, "content"),
    NEED_CHECK_TAG(3, "check"),
    UNKHOWN_TAG(4, "unknown");

    JudgeFontSizeEnum(int num, String name) {
        this.num = num;
        this.name = name;
    }

    int num;

    String name;
}
