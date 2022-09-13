package com.tsinghua.demo.present.docx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonTransfer {

    static List<String> paraSplitTag = Arrays.asList("《《");
    static List<String> endTags = Arrays.asList("】】","。");
    static int charNumMaxLimit = 2000;
    static int maxEntity = 4;

    public static void main(String[] args) throws Exception {

        String jsonPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\admin.jsonl";
        JSONObject json = readJson(jsonPath);
        String text = json.getString("text");
        List<Integer> titleLs = splitPara(text, paraSplitTag, true);
        List<Integer> sentLs = splitPara(text, endTags, false);
        pointToScale(titleLs, sentLs, text, json);
    }

    public static void pointToScale(List<Integer> titleLs, List<Integer> sentLs, String text, JSONObject mainJson) {
        JSONArray relations = mainJson.getJSONArray("relations");
        JSONArray entities = mainJson.getJSONArray("entities");
        int finalBorderLeft = text.length();
        int finalBorderRight = 0;
        int finalBorderContentLeftIndex = 0;
        int finalBorderContentRightIndex = 0;
        int entityNum = 0;
        for(int i=0; i<relations.size(); i++) {
            JSONObject relation = relations.getJSONObject(i);
            String fromId = relation.getString("from_id");
            String toId = relation.getString("to_id");
            int start = 0;
            int end = 0;
            int contentBorderLeft = -1;
            int contentBorderLeftIndex = 0;
            int contentBorderRight = 0;
            int contentBorderRightIndex = 0;
            int titleBorderLeft = -1;
            int titleBorderLeftIndex = 0;
            int titleBorderRight = 0;
            int titleBorderRightIndex = 0;
            for(int j=0; j<entities.size(); j++) {
                JSONObject entity = entities.getJSONObject(j);
                String id = entity.getString("id");
                if(fromId.equals(id)) {
                    start = Integer.parseInt(entity.getString("start_offset"));
                }
                if(toId.equals(id)) {
                    end = Integer.parseInt(entity.getString("end_offset"));
                    break;
                }
            }
            for(int j=0; j<titleLs.size(); j++) {
                int titlePos = titleLs.get(j);
                if(titlePos > start && titleBorderLeft == -1) {
                    titleBorderLeft = titleLs.get(j-1);
                    titleBorderLeftIndex = j - 1;
                }
                if(titlePos >= end) {
                    titleBorderRight = titlePos;
                    titleBorderRightIndex = j;
                    break;
                }
            }
            for(int j=0; j<sentLs.size(); j++) {
                int contentPos = sentLs.get(j);
                if(contentPos > start && contentBorderLeft == -1) {
                    contentBorderLeft = sentLs.get(j-1);
                    contentBorderLeftIndex = j - 1;
                }
                if(contentPos >= end) {
                    contentBorderRight = contentPos;
                    contentBorderRightIndex = j;
                    break;
                }
            }
            int tempFinalBorderRight = finalBorderRight;
            if(titleBorderLeft < finalBorderLeft) {
                finalBorderLeft = titleBorderLeft;
            }
            if(titleBorderRight > finalBorderRight) {
                tempFinalBorderRight = titleBorderRight;
            }
            contentBorderRightIndex++;
            titleBorderRightIndex++;
            // TODO: 不能毫无考虑的缩，如果缩到把之前的实体给丢了的情况，就不缩了，直接就用原来的scale输出了。如果之前就没实体，那就看一下句子之间能不能走，不能走就不要了，能走就单走。
            if(tempFinalBorderRight - finalBorderLeft > charNumMaxLimit) {
                if(entityNum >= maxEntity) {
                    while(tempFinalBorderRight - finalBorderLeft > charNumMaxLimit && tempFinalBorderRight >= end) {
                        tempFinalBorderRight = sentLs.get(--contentBorderRightIndex);
                    }
                    // 不能把实体给缩没了
                    if(tempFinalBorderRight >= end) {
                        finalBorderRight = tempFinalBorderRight;
                    }
                }
                finalBorderLeft = text.length();
                finalBorderRight = 0;
            }
            else {
                finalBorderRight = tempFinalBorderRight;
                entityNum++;
            }
        }
    }

    public static JSONObject readJson(String jsonPath) throws IOException {
        File file = new File(jsonPath);
        Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
        BufferedReader br = new BufferedReader(reader);
        StringBuffer sb = new StringBuffer();
        String s = null;
        while((s = br.readLine()) != null){
            sb.append(s);
        }
        reader.close();
        JSONObject json = JSON.parseObject(sb.toString());
        return json;
    }
    
    public static List<Integer> splitPara(String text, List<String> find, boolean start) {
        boolean judgeStart = false;
        List<Integer> paraStartPos = new ArrayList<>();
        if(!start) {
            paraStartPos.add(0);
        }
        int sizeRecord;
        for(int i=0; i<text.length(); i++) {
            for(int k=0; k<find.size(); k++) {
                String ls = find.get(k);
                sizeRecord = ls.length();
                for(int j=0; j<ls.length(); j++) {
                    if(i+j < text.length() && text.charAt(i+j) == ls.charAt(j)) {
                        judgeStart = true;
                    }
                    else {
                        judgeStart = false;
                        break;
                    }
                }
                if(judgeStart) {
                    if(start) {
                        paraStartPos.add(i);
                    }
                    else if(i+sizeRecord < text.length()) {
                        if(i+sizeRecord - paraStartPos.get(paraStartPos.size() - 1) < 3) {
                            paraStartPos.set(paraStartPos.size()-1, i+sizeRecord);
                        }
                        else {
                            paraStartPos.add(i+sizeRecord);
                        }
                    }
                    break;
                }
            }
        }
        return paraStartPos;
    }
}
