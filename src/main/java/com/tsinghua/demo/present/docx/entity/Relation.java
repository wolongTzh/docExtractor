package com.tsinghua.demo.present.docx.entity;

import com.alibaba.fastjson.JSONObject;

public class Relation {

    String id;

    String fromId;

    String toId;

    String type;

    public Relation() {
    }

    public Relation(JSONObject jsonObject) {
        this.fromId = jsonObject.getString("from_id");
        this.toId = jsonObject.getString("to_id");
        this.type = jsonObject.getString("type");
        this.id = jsonObject.getString("id");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
