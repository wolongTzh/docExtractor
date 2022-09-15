package com.tsinghua.demo.present.docx.entity;

import java.util.List;

public class CommonEntity {

    String id;

    List<Entity> entityList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Entity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<Entity> entityList) {
        this.entityList = entityList;
    }
}
