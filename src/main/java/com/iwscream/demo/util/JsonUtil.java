package com.iwscream.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class JsonUtil {

    /**
     * Returns a json object string parsed by object entity
     *
     * @param o parsed object
     * @return json object string
     *
     * @author iwscream
     * */
    public static String o2j (Object o){
        return JSON.toJSONString(o);
    }

    /**
     * Returns a json object parsed by json object string
     *
     * @param s a string to be parsed to json object
     * @return json object
     *
     * @author iwscream
     * */
    public static JSONObject parse (String s){
        return JSONObject.parseObject(s);
    }

    /**
     * Returns a list parsed by json array string
     *
     * @param <T> the type of elements in the list
     *
     * @param text a json array string to be parsed to List
     * @param clazz the class of T
     * @return a list of elements in the json array string
     *
     * @author iwscream
     * */
    public static <T> List<T> jA2List (String text, Class<T> clazz){
        return JSONObject.parseArray(text, clazz);
    }

    /**
     * Returns a json array string parsed by json object string
     *
     * @param text a json object string to be parsed
     * @return json array string parsed by json object string
     *
     * @author iwscream
     * */
    public static JSONArray json2Array (String text){
        return JSON.parseArray(text);
    }
}
