package com.iwscream.demo.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static com.iwscream.demo.util.SerializeUtil.deserialize;
import static com.iwscream.demo.util.SerializeUtil.serialize;

public class RedisUtil {
    private static final Logger log = LogManager.getLogger(RedisUtil.class);
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private static final int MAX_WAIT = 15 * 1000;
    //超时时间
    private static final int TIMEOUT = 10 * 1000;

    private static JedisPool jedisPool = null;

    /**
     * Jedis实例获取返回码
     */
    public static class JedisStatus{
        /**Jedis实例获取失败*/
        public static final long FAIL_LONG = -5L;
        /**Jedis实例获取失败*/
        public static final int FAIL_INT = -5;
        /**Jedis实例获取失败*/
        public static final String FAIL_STRING = "-5";
    }


    private static void initialPool() {
        PropertyReader propertyReader = new PropertyReader();
        //Redis服务器IP
        String HOST = propertyReader.get("redis.properties","redis.native.host");
        //Redis的端口号
        int PORT = NumberUtils.toInt(propertyReader.get("redis.properties","redis.native.port"), 6379);
        //访问密码
        String AUTH = propertyReader.get("redis.properties","redis.native.password");

        try {
            JedisPoolConfig config = new JedisPoolConfig();
            //最大连接数，如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
            config.setMaxTotal(NumberUtils.toInt(propertyReader.get("redis.properties","redis.native.maxTotal"), 400));
            //最大空闲数，控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
            config.setMaxIdle(NumberUtils.toInt(propertyReader.get("redis.properties","redis.native.maxIdle"), 50));
            //最小空闲数
            config.setMinIdle(NumberUtils.toInt(propertyReader.get("redis.properties","redis.native.minIdle"), 10));
            //是否在从池中取出连接前进行检验，如果检验失败，则从池中去除连接并尝试取出另一个
            config.setTestOnBorrow(true);
            //在return给pool时，是否提前进行validate操作
            config.setTestOnReturn(true);
            //在空闲时检查有效性，默认false
            config.setTestWhileIdle(true);
            //表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；
            //这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
            config.setMinEvictableIdleTimeMillis(30000);
            //表示idle object evitor两次扫描之间要sleep的毫秒数
            config.setTimeBetweenEvictionRunsMillis(60000);
            //表示idle object evitor每次扫描的最多的对象数
            config.setNumTestsPerEvictionRun(1000);
            //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
            config.setMaxWaitMillis(MAX_WAIT);

            if (StringUtils.isNotBlank(AUTH)) {
                jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT, AUTH);
            } else {
                jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT);
            }
        } catch (Exception e) {
            if(jedisPool != null){
                jedisPool.close();
            }
            log.error("初始化Redis连接池失败", e);
        }
    }

    /**
     * 在多线程环境同步初始化
     */
    private static synchronized void poolInit() {
        if (jedisPool == null) {
            initialPool();
        }
    }

    /**
     * 同步获取Jedis实例
     *
     * @return Jedis
     */
    public static Jedis getJedis() {
        if (jedisPool == null) {
            poolInit();
        }

        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
            log.error("同步获取Jedis实例失败" + e.getMessage(), e);
            returnBrokenResource(null);
        }

        return jedis;
    }

    /**
     * 释放jedis资源
     *
     * @param jedis
     */
    @SuppressWarnings("deprecation")
    public static void returnResource(final Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedisPool.returnResource(jedis);
        }
    }

    @SuppressWarnings("deprecation")
    public static void returnBrokenResource(final Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedisPool.returnBrokenResource(jedis);
        }
    }

    /**
     * 设置值
     *
     * @param key
     * @param value
     * @return -5：Jedis实例获取失败<br/>OK：操作成功<br/>null：操作失败
     */
    public static String set(String key, String value) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_STRING;
        }

        String result = null;
        try {
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("设置值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 设置值
     *
     * @param key
     * @param value
     * @param expire 过期时间，单位：秒
     * @return -5：Jedis实例获取失败<br/>OK：操作成功<br/>null：操作失败
     */
    public static String set(String key, String value, int expire) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_STRING;
        }

        String result = null;
        try {
            result = jedis.set(key, value);
            jedis.expire(key, expire);
        } catch (Exception e) {
            log.error("设置值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_STRING;
        }

        String result = null;
        try {
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("获取值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 设置key的过期时间
     *
     * @param key
     * @param seconds -5：Jedis实例获取失败，1：成功，0：失败
     * @return
     */
    public static long expire(String key, int seconds) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_LONG;
        }

        long result = 0;
        try {
            result = jedis.expire(key, seconds);
        } catch (Exception e) {
            log.error(String.format("设置key=%s的过期时间失败：" + e.getMessage(), key), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public static boolean exists(String key) {
        Jedis jedis = getJedis();
        if(jedis == null){
            log.warn("Jedis实例获取为空");
            return false;
        }

        boolean result = false;
        try {
            result = jedis.exists(key);
        } catch (Exception e) {
            log.error(String.format("判断key=%s是否存在失败：" + e.getMessage(), key), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 删除key
     *
     * @param keys
     * @return -5：Jedis实例获取失败，1：成功，0：失败
     */
    public static long del(String... keys) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_LONG;
        }

        long result = JedisStatus.FAIL_LONG;
        try {
            result = jedis.del(keys);
        } catch (Exception e) {
            log.error(String.format("删除key=%s失败：" + e.getMessage(), keys), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * set if not exists，若key已存在，则setnx不做任何操作
     *
     * @param key
     * @param value key已存在，1：key赋值成功
     * @return
     */
    public static long setnx(String key, String value) {
        long result = JedisStatus.FAIL_LONG;

        Jedis jedis = getJedis();
        if(jedis == null){
            return result;
        }

        try {
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            log.error("设置值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * set if not exists，若key已存在，则setnx不做任何操作
     *
     * @param key
     * @param value key已存在，1：key赋值成功
     * @param expire 过期时间，单位：秒
     * @return
     */
    public static long setnx(String key, String value, int expire) {
        long result = JedisStatus.FAIL_LONG;

        Jedis jedis = getJedis();
        if(jedis == null){
            return result;
        }

        try {
            result = jedis.setnx(key, value);
            jedis.expire(key, expire);
        } catch (Exception e) {
            log.error("设置值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 在列表key的头部插入元素
     *
     * @param key
     * @param values -5：Jedis实例获取失败，>0：返回操作成功的条数，0：失败
     * @return
     */
    public static long lpush(String key, String... values) {
        long result = JedisStatus.FAIL_LONG;

        Jedis jedis = getJedis();
        if(jedis == null){
            return result;
        }

        try {
            result = jedis.lpush(key, values);
        } catch (Exception e) {
            log.error("在列表key的头部插入元素失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 在列表key的尾部插入元素
     *
     * @param key
     * @param values -5：Jedis实例获取失败，>0：返回操作成功的条数，0：失败
     * @return
     */
    public static long rpush(String key, String... values) {
        long result = JedisStatus.FAIL_LONG;

        Jedis jedis = getJedis();
        if(jedis == null){
            return result;
        }

        try {
            result = jedis.rpush(key, values);
        } catch (Exception e) {
            log.error("在列表key的尾部插入元素失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 返回存储在key列表的特定元素 
     *
     *
     * @param key
     * @param start 开始索引，索引从0开始，0表示第一个元素，1表示第二个元素
     * @param end 结束索引，-1表示最后一个元素，-2表示倒数第二个元素
     * @return redis client获取失败返回null
     */
    public static List<String> lrange(String key, long start, long end) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return null;
        }

        List<String> result = null;
        try {
            result = jedis.lrange(key, start, end);
        } catch (Exception e) {
            log.error("查询列表元素失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 获取List缓存对象
     *
     * @param key
     * @param start
     * @param end
     * @return List<T> 返回类型
     */
    public static <T> List<T> range(String key, long start, long end, Class<T> clazz){
        List<String> dataList = lrange(key, start, end);
        if(CollectionUtils.isEmpty(dataList)){
            return new ArrayList<T>();
        }

        assert dataList != null;
        return JsonUtil.jA2List(dataList.toString(), clazz);
    }

    /**
     * 获取列表长度
     *
     * @param key -5：Jedis实例获取失败
     * @return
     */
    public static long llen(String key) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_LONG;
        }

        long result = 0;
        try {
            result = jedis.llen(key);
        } catch (Exception e) {
            log.error("获取列表长度失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 移除等于value的元素<br/><br/>
     *        当count>0时，从表头开始查找，移除count个；<br/>
     *        当count=0时，从表头开始查找，移除所有等于value的；<br/>
     *        当count<0时，从表尾开始查找，移除count个
     *
     * @param key
     * @param count
     * @param value
     * @return -5:Jedis实例获取失败
     */
    public static long lrem(String key, long count, String value) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_LONG;
        }

        long result = 0;
        try {
            result = jedis.lrem(key, count, value);
        } catch (Exception e) {
            log.error("获取列表长度失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 对列表进行修剪
     *
     * @param key
     * @param start
     * @param end
     * @return -5：Jedis实例获取失败，OK：命令执行成功
     */
    public static String ltrim(String key, long start, long end) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_STRING;
        }

        String result = "";
        try {
            result = jedis.ltrim(key, start, end);
        } catch (Exception e) {
            log.error("获取列表长度失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 设置对象
     *
     * @param key
     * @param obj
     * @return
     */
    public static <T> String setObject(String key ,T obj){
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_STRING;
        }

        String result = null;
        try {
            byte[] data = serialize(obj);
            result = jedis.set(key.getBytes(), data);
        } catch (Exception e) {
            log.error("设置对象失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 获取对象
     *
     * @param key
     * @return
     */
    public static Object getObject(String key){
        Jedis jedis = getJedis();
        if(jedis == null){
            return null;
        }

        Object result = null;
        try {
            byte[] data = jedis.get(key.getBytes());
            if(data != null && data.length > 0){
                result = deserialize(data, Object.class);
            }
        } catch (Exception e) {
            log.error("获取对象失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 设置List集合（慎用）
     *
     * @param key
     * @param dataList
     * @return
     */
    public synchronized static <T> T setList(String key, List<T> dataList){
        Jedis jedis = getJedis();
        if(jedis == null){
            return null;
        }

        T result = null;
        try {
            List<T> list = getList(key);
            if(CollectionUtils.isNotEmpty(list)){
                dataList.addAll(list);
            }

            if(CollectionUtils.isNotEmpty(dataList)){
                byte[] data = SerializeUtil.serialize(POJOWrapper.builder(dataList));
                jedis.set(key.getBytes(), data);
            }else{//如果list为空,则设置一个空  
                jedis.set(key.getBytes(), "".getBytes());
            }
        } catch (Exception e) {
            log.error("设置List集合失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 获取List集合（慎用）
     *
     * @param key
     * @return
     */
    public static <T> List<T> getList(String key){
        Jedis jedis = getJedis();
        if(jedis == null){
            return null;
        }

        try {
            byte[] data = getJedis().get(key.getBytes());
            if(data != null && data.length > 0){
                return deserialize(data, List.class);
            }
        } catch (Exception e) {
            log.error("获取List集合失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return null;
    }

    /**
     * 缓存Map赋值
     *
     * @param key
     * @param field
     * @param value
     * @return -5：Jedis实例获取失败
     */
    public static long hset(String key, String field, String value) {
        Jedis jedis = getJedis();
        if(jedis == null){
            return JedisStatus.FAIL_LONG;
        }

        long result = 0L;
        try {
            result = jedis.hset(key, field, value);
        } catch (Exception e) {
            log.error("缓存Map赋值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }


    /**
     * 获取缓存的Map值
     *
     * @param key
     * @return
     */
    public static String hget(String key, String field){
        Jedis jedis = getJedis();
        if(jedis == null){
            return null;
        }

        String result = null;
        try {
            result = jedis.hget(key, field);
        } catch (Exception e) {
            log.error("获取缓存的Map值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return result;
    }

    /**
     * 获取map所有的字段和值
     *
     * @param key
     * @return
     */
    public static Map<String, String> hgetAll(String key){
        Map<String, String> map = new HashMap<String, String>();

        Jedis jedis = getJedis();
        if(jedis == null){
            log.warn("Jedis实例获取为空");
            return map;
        }

        try {
            map = jedis.hgetAll(key);
        } catch (Exception e) {
            log.error("获取map所有的字段和值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return map;
    }

    /**
     * 查看哈希表 key 中，指定的field字段是否存在。
     *
     * @param key
     * @param field
     * @return
     */
    public static Boolean hexists(String key, String field){
        Jedis jedis = getJedis();
        if(jedis == null){
            log.warn("Jedis实例获取为空");
            return null;
        }

        try {
            return jedis.hexists(key, field);
        } catch (Exception e) {
            log.error("查看哈希表field字段是否存在失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return null;
    }

    /**
     * 获取所有哈希表中的字段
     *
     * @param key
     * @return
     */
    public static Set<String> hkeys(String key) {
        Set<String> set = new HashSet<String>();
        Jedis jedis = getJedis();
        if(jedis == null){
            log.warn("Jedis实例获取为空");
            return set;
        }

        try {
            return jedis.hkeys(key);
        } catch (Exception e) {
            log.error("获取所有哈希表中的字段失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return null;
    }

    /**
     * 获取所有哈希表中的值
     *
     * @param key
     * @return
     */
    public static List<String> hvals(String key) {
        List<String> list = new ArrayList<String>();
        Jedis jedis = getJedis();
        if(jedis == null){
            log.warn("Jedis实例获取为空");
            return list;
        }

        try {
            return jedis.hvals(key);
        } catch (Exception e) {
            log.error("获取所有哈希表中的值失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return null;
    }

    /**
     * 从哈希表 key 中删除指定的field
     *
     * @param key
     * @param fields
     * @return
     */
    public static long hdel(String key, String... fields){
        Jedis jedis = getJedis();
        if(jedis == null){
            log.warn("Jedis实例获取为空");
            return JedisStatus.FAIL_LONG;
        }

        try {
            return jedis.hdel(key, fields);
        } catch (Exception e) {
            log.error("map删除指定的field失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return 0;
    }

    public static Set<String> keys(String pattern){
        Set<String> keyList = new HashSet<String>();
        Jedis jedis = getJedis();
        if(jedis == null){
            log.warn("Jedis实例获取为空");
            return keyList;
        }

        try {
            keyList = jedis.keys(pattern);
        } catch (Exception e) {
            log.error("操作keys失败：" + e.getMessage(), e);
            returnBrokenResource(jedis);
        } finally {
            returnResource(jedis);
        }

        return keyList;
    }

    public static void main(String[] args) throws IOException {
//        System.out.println(hset("map", "a","3"));
//        System.out.println(hset("map", "b","3"));
//        System.out.println(hset("map", "c","3"));
        Set<String> set = keys("lock*");
        for(String key : set){
            System.out.println(key);
        }
    }
}