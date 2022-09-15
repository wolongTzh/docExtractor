package com.tsinghua.demo.present.docx.entity;

import com.alibaba.fastjson.JSONObject;

public class Entity {

    String id;

    String label;

    Integer startOffset;

    Integer endOffset;

    Integer parentIndex;

    String parentId;

    public Entity() {
    }

    public Entity(JSONObject jsonObject) {
        this.startOffset = Integer.parseInt(jsonObject.getString("start_offset"));
        this.endOffset = Integer.parseInt(jsonObject.getString("end_offset"));
        this.label = jsonObject.getString("label");
        this.id = jsonObject.getString("id");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public Integer getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(Integer endOffset) {
        this.endOffset = endOffset;
    }

    public Integer getParentIndex() {
        return parentIndex;
    }

    public void setParentIndex(Integer parentIndex) {
        this.parentIndex = parentIndex;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
