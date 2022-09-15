package com.tsinghua.demo.present.docx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.demo.present.docx.entity.CommonEntity;
import com.tsinghua.demo.present.docx.entity.Entity;
import com.tsinghua.demo.present.docx.entity.ParaData;
import com.tsinghua.demo.present.docx.entity.Relation;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class JsonTransfer {

    static List<String> paraSplitTag = Arrays.asList("《");
    static List<String> endTags = Arrays.asList("】","。");
    static int charNumMaxLimit = 2000;
    static int maxEntity = 4;
    static String commonEntityRelation = "指代";
    static Integer maxId = 0;
    static String basePath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\output\\";

    public static void main(String[] args) throws Exception {

//        singleTransfer();
        batchTransfer();
    }

    public static void singleTransfer() throws IOException {
        String jsonPath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\source\\04：神州高铁.jsonl";
        JSONObject json = readJson(jsonPath);
        String text = json.getString("text");
        List<Integer> titleLs = splitPara(text, paraSplitTag, true);
        List<Integer> sentLs = splitPara(text, endTags, false);
        pointToScale(titleLs, sentLs, text, json, "test\\");
    }

    public static void batchTransfer() throws IOException {

        String basePath = "C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\source\\";
        File file1 = new File(basePath);
        //判断是否有目录
        if(file1.isDirectory()) {
            //获取目录中的所有文件名称
            String[] fileName = file1.list();
            for(String str : fileName) {
                JSONObject json = readJson(basePath + str);
                String text = json.getString("text");
                List<Integer> titleLs = splitPara(text, paraSplitTag, true);
                List<Integer> sentLs = splitPara(text, endTags, false);
                pointToScale(titleLs, sentLs, text, json, str.split("\\.")[0] + "\\");
            }
        }
    }

    public static void pointToScale(List<Integer> titleLs, List<Integer> sentLs, String text, JSONObject mainJson, String dirName) throws IOException {
        JSONArray relations = mainJson.getJSONArray("relations");
        JSONArray entities = mainJson.getJSONArray("entities");
        findMaxId(entities, relations);
        int finalBorderLeft = text.length();
        int finalBorderRight = 0;
        int finalBorderContentLeftIndex = 0;
        int finalBorderContentRightIndex = 0;
        int relationNum = 0;
        List<Relation> relationList = new ArrayList<>();
        List<Entity> entityList = new ArrayList<>();
        int innerCount = 0;
        int start = Integer.MAX_VALUE;
        int end = 0;
        for(int i=0; i<relations.size(); i++) {
            Relation relation = new Relation(relations.getJSONObject(i));
            String fromId = relation.getFromId();
            String toId = relation.getToId();
            relationList.add(relation);
            int contentBorderLeft = -1;
            int contentBorderLeftIndex = 0;
            int contentBorderRight = 0;
            int contentBorderRightIndex = 0;
            int titleBorderLeft = -1;
            int titleBorderLeftIndex = 0;
            int titleBorderRight = 0;
            int titleBorderRightIndex = 0;
            for(int j=0; j<entities.size(); j++) {
                Entity entity = new Entity(entities.getJSONObject(j));
                String id = entity.getId();
                if(fromId.equals(id)) {
                    start = Math.min(start, entity.getStartOffset());
                    end = Math.max(end, entity.getEndOffset());
                    entityList.add(entity);
                }
                if(toId.equals(id)) {
                    start = Math.min(start, entity.getStartOffset());
                    end = Math.max(end, entity.getEndOffset());
                    entityList.add(entity);
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
            int tempFinalBorderLeft = finalBorderLeft;
            if(titleBorderLeft < finalBorderLeft) {
                tempFinalBorderLeft = titleBorderLeft;
            }
            if(titleBorderRight > finalBorderRight) {
                tempFinalBorderRight = titleBorderRight;
            }
            contentBorderRightIndex++;
            contentBorderLeftIndex--;
            boolean scaleOk = false;
            // TODO: 不能毫无考虑的缩，如果缩到把之前的实体给丢了的情况，就不缩了，直接就用原来的scale输出了。如果之前就没实体，那就看一下句子之间能不能走，不能走就不要了，能走就单走。
            if(tempFinalBorderRight - tempFinalBorderLeft > charNumMaxLimit) {
                if(relationNum + 1 < maxEntity) {
                    while(tempFinalBorderRight - tempFinalBorderLeft > charNumMaxLimit && tempFinalBorderRight >= end) {
                        if(tempFinalBorderRight >= end) {
                            tempFinalBorderRight = sentLs.get(--contentBorderRightIndex);
                        }
                    }
                    // 不能把实体给缩没了
                    if(tempFinalBorderRight >= end) {
                        scaleOk = true;
                    }
                    if(!scaleOk) {
                        tempFinalBorderRight = sentLs.get(++contentBorderRightIndex);
                        while(tempFinalBorderRight - tempFinalBorderLeft > charNumMaxLimit && tempFinalBorderLeft <= start) {
                            if(tempFinalBorderLeft <= start) {
                                tempFinalBorderLeft = sentLs.get(++contentBorderLeftIndex);
                            }
                        }
                        if(tempFinalBorderLeft <= start) {
                            scaleOk = true;
                        }
                    }
                    if(scaleOk) {
                        finalBorderLeft = tempFinalBorderLeft;
                        finalBorderRight = tempFinalBorderRight;
//                        relationNum++;
//                        continue;
                    }
                }
                if(!scaleOk) {
                    entityList.remove(entityList.size() - 1);
                    entityList.remove(entityList.size() - 1);
                    relationList.remove(relationList.size() - 1);
                }
                innerCount++;
                generateParaData(relationList, entityList, finalBorderLeft, finalBorderRight, text, basePath + dirName + innerCount + ".json");
                if(innerCount == 8) {
                    System.out.println();
                }
                relationList.clear();
                entityList.clear();
                finalBorderLeft = text.length();
                finalBorderRight = 0;
                relationNum = 0;
                start = Integer.MAX_VALUE;
                end = 0;
            }
            else {
                finalBorderRight = tempFinalBorderRight;
                finalBorderLeft = tempFinalBorderLeft;
                relationNum++;
            }
        }
    }

    public static void generateParaData(List<Relation> relations, List<Entity> paramEntityList, int borderLeft, int borderRight, String text, String outPath) throws IOException {
        if(outPath.equals("C:\\Users\\FEIFEI\\Desktop\\金融知识图谱项目\\output\\test\\23.json")) {
            System.out.println();
        }
        if(relations.size() == 0) {
            return;
        }
        ParaData paraData = new ParaData();
        paraData.setText(text.substring(borderLeft, borderRight));
        List<CommonEntity> commonEntityList = new ArrayList<>();
        paramEntityList = paramEntityList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Entity::getId))), ArrayList::new));
        for(int i=0; i<relations.size(); i++) {
            Relation relation = relations.get(i);
            if(relation.getType().equals(commonEntityRelation)) {
                Entity startEntity = findEntityById(paramEntityList, relation.getFromId());
                Entity endEntity = findEntityById(paramEntityList, relation.getToId());
                if(startEntity.getParentId() == null && endEntity.getParentId() == null) {
                    List<Entity> entityList = new ArrayList<>();
                    CommonEntity commonEntity = new CommonEntity();
                    entityList.add(startEntity);
                    entityList.add(endEntity);
                    commonEntity.setEntityList(entityList);
                    commonEntity.setId(String.valueOf(++maxId));
                    startEntity.setParentIndex(commonEntityList.size());
                    endEntity.setParentIndex(commonEntityList.size());
                    startEntity.setParentId(commonEntity.getId());
                    endEntity.setParentId(commonEntity.getId());
                    commonEntityList.add(commonEntity);
                }
                else if(startEntity.getParentId() != null) {
                    CommonEntity commonEntity = commonEntityList.get(startEntity.getParentIndex());
                    List<Entity> entityList = commonEntity.getEntityList();
                    entityList.add(endEntity);
                    endEntity.setParentId(startEntity.getParentId());
                    endEntity.setParentIndex(startEntity.getParentIndex());
                }
                else if(endEntity.getParentId() != null) {
                    CommonEntity commonEntity = commonEntityList.get(endEntity.getParentIndex());
                    List<Entity> entityList = commonEntity.getEntityList();
                    entityList.add(startEntity);
                    startEntity.setParentId(endEntity.getParentId());
                    startEntity.setParentIndex(endEntity.getParentIndex());
                }
            }
        }
        for(Entity entity : paramEntityList) {
            if(entity.getParentId() == null) {
                List<Entity> entityList = new ArrayList<>();
                CommonEntity commonEntity = new CommonEntity();
                entityList.add(entity);
                commonEntity.setEntityList(entityList);
                commonEntity.setId(String.valueOf(++maxId));
                entity.setParentId(commonEntity.getId());
                commonEntityList.add(commonEntity);
            }
            else {
                CommonEntity commonEntity = commonEntityList.get(entity.getParentIndex());
                entity.setParentId(commonEntity.getId());
            }
            int wordLength = entity.getEndOffset() - entity.getStartOffset();
            entity.setStartOffset(entity.getStartOffset() - borderLeft);
            entity.setEndOffset(entity.getStartOffset() + wordLength);
            String output = paraData.getText().substring(entity.getStartOffset(), entity.getEndOffset());
            System.out.println(output);
        }
        List<Relation> finalRelations = new ArrayList<>();
        for(int i=0; i<relations.size(); i++) {
            Relation relation = relations.get(i);
            if(!relation.getType().equals(commonEntityRelation)) {
                Entity startEntity = findEntityById(paramEntityList, relation.getFromId());
                Entity endEntity = findEntityById(paramEntityList, relation.getToId());
                relation.setFromId(startEntity.getParentId());
                relation.setToId(endEntity.getParentId());
                finalRelations.add(relation);
            }
        }
        paraData.setEntities(commonEntityList);
        paraData.setRelations(finalRelations);
        String output = JSONObject.toJSONString(paraData);
        File file = new File(outPath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(output);
        fileWriter.flush();
        fileWriter.close();
    }

    public static Entity findEntityById(List<Entity> entityList, String id) {
        for(Entity entity : entityList) {
            if(entity.getId().equals(id)) {
                return entity;
            }
        }
        return null;
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

    public static void findMaxId(JSONArray relations, JSONArray entities) {
        for(int i=0; i<relations.size(); i++) {
            JSONObject jsonObject = relations.getJSONObject(i);
            Integer id = Integer.parseInt(jsonObject.getString("id"));
            if(maxId < id) {
                maxId = id;
            }
        }
        for(int i=0; i<entities.size(); i++) {
            JSONObject jsonObject = entities.getJSONObject(i);
            Integer id = Integer.parseInt(jsonObject.getString("id"));
            if(maxId < id) {
                maxId = id;
            }
        }
    }
}
