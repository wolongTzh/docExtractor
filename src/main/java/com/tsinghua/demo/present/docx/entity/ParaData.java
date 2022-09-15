package com.tsinghua.demo.present.docx.entity;

import java.util.List;

public class ParaData {

    String text;

    List<CommonEntity> entities;

    List<Relation> relations;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<CommonEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<CommonEntity> entities) {
        this.entities = entities;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
}
