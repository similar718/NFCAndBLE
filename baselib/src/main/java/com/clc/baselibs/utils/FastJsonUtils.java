package com.clc.baselibs.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FastJsonUtils {
    public static final <T> List<T> parseArray(String json, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            list = JSON.parseArray(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static final <T> T parseObject(String json, Class<T> clazz) {
        T t = null;
        try {
            t = JSON.parseObject(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }
    public static Map<String, Object> jsonToMap(String jsonStr){
        Map<String, Object> map = null;
        try {
            map = JSON.parseObject(
                    jsonStr, new TypeReference<Map<String, Object>>() {
                    });
        }catch (Exception e){
            Log.e("","",e);
        }
        return map;
    }

    public static String mapToJSON(Map<String, Object> map){
        String json="";
        try {
            json=JSON.toJSONString(map);
        } catch (Exception e) {
            Log.e("","",e);
        }
        return json;
    }
    public static String objToJson(Object obj){
        String json="";
        try {
            json=JSON.toJSONString(obj);
        } catch (Exception e) {
            Log.e("","",e);
        }
        return json;
    }
}
