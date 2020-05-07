package com.iwscream.demo.service.AuthenticationServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iwscream.demo.controller.cache.JedisPoolWriper;
import com.iwscream.demo.controller.cache.JedisUtil;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class LoginServiceImpl {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getSessionKey(String code) throws IOException {
        StringBuilder json = new StringBuilder();

        URL url = new URL("https://api.weixin.qq.com/sns/jscode2session");
        URLConnection connection = url.openConnection();

        connection.addRequestProperty("appid","wx1f3a0d2aba49f29f");
        connection.addRequestProperty("secret", "");
        connection.addRequestProperty("js_code", code);
        connection.addRequestProperty("grant_type", "authorization_code");

        connection.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String temp;
        while ((temp = in.readLine()) != null){
            json.append(temp);
        }

        JSONObject jsonTemp = JSONObject.parseObject(json.toString());

        JSONObject object = new JSONObject();
        object.put("openid", jsonTemp.get("openid"));
        object.put("session_key", jsonTemp.get("session_key"));
        object.put("unionid", jsonTemp.get("unionid"));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errcode", jsonTemp.get("errcode"));
        jsonObject.put("data", object);
        jsonObject.put("errmsg", jsonTemp.get("errmsg"));

        setKey(jsonTemp.getString("session_key"), "ok");

        return jsonObject.toJSONString();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String setKey(String key, String value){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPoolWriper jedisPoolWriper = new JedisPoolWriper(jedisPoolConfig,"127.0.0.1",6379);
        JedisUtil util = new JedisUtil();
        util.setJedisPool(jedisPoolWriper);
        Jedis jedis = util.getJedis();
        String status = jedis.set(key,value);
        System.out.println(status);
        jedis.expire("key",60);
        jedis.close();
        return status;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getKey(String key){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPoolWriper jedisPoolWriper = new JedisPoolWriper(jedisPoolConfig,"127.0.0.1",6379);
        JedisUtil util = new JedisUtil();
        util.setJedisPool(jedisPoolWriper);
        Jedis jedis = util.getJedis();
        String value = jedis.get(key);
        System.out.println(jedis.exists(key));
        jedis.close();

        return value;
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost");

        System.out.println(jedis.ping());
        //key
        Set<String> keys = jedis.keys("*");
        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            System.out.println((String) iterator.next());
        }

        System.out.println(jedis.exists("k2"));
        System.out.println(jedis.ttl("k1"));

        //String
        jedis.append("k1", "hello myredis");
        System.out.println(jedis.get("k1"));

        jedis.set("k3", "....redis");

        jedis.mset("str1", "vv1", "str2", "vv2", "str3", "vv3");
        System.out.println(jedis.mget("str1", "str2", "str3"));

        //List
        jedis.lpush("myList", "v1", "v2", "v3", "v4", "v5");
        List<String> list = jedis.lrange("myList", 0, -1);
        for (String element : list) {
            System.out.println(element);
        }

        //set
        jedis.sadd("set1", "hello");
        jedis.sadd("set1", " world!");
        Set<String> set = jedis.smembers("set1");

        for (Iterator iterator = set.iterator(); iterator.hasNext(); ) {
            System.out.println(iterator.next());
        }

        jedis.srem("set1", "hello");
        System.out.println(jedis.smembers("set1").size());

        //hash
        jedis.hset("info", "name", "lili");
        System.out.println(jedis.hget("info", "name"));

        Map<String, String> map = new HashMap<String, String>();
        map.put("email", "lili@Duan.com");
        map.put("age", "16");
        map.put("tel", "88888888");

        jedis.hmset("info", map);
        System.out.println(jedis.hmget("info", "age", "email"));
        String[] s = new String[]{"name", "tel"};
        System.out.println(jedis.hmget("info", s));

        //zset
        jedis.zadd("zset1",60,"v1");
        jedis.zadd("zset1",70,"v2");
        jedis.zadd("zset1",80,"v3");
        jedis.zadd("zset1",90,"v4");

        Set<String> zset=jedis.zrange("zset1",0,-1);

        for (Iterator<String> iterator=zset.iterator();iterator.hasNext();){
            System.out.println(iterator.next());
        }
        jedis.close();
    }
}
