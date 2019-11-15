package com.iwscream.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonOp {

    /**
     *
     * this method parse object entity to json object string
     * @param o parsed object
     * @return json object string
     *
     * */
    public static String o2j (Object o){
        new JSONObject();
        return JSON.toJSONString(o);
    }
}
