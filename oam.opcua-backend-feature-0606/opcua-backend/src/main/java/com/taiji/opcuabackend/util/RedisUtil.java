package com.taiji.opcuabackend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


@Component
public class RedisUtil {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 向Redis中存值，永久有效
     */
    public String set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            jedis.close();
        }
    }

    /**
     * 向Redis中存值，有效时长为{seconds}秒
     */
    public String set(String key, String value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.set(key, value, SetParams.setParams().ex(seconds));
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            jedis.close();
        }
    }

    /**
     * 根据传入Key获取指定Value
     */
    public String get(String key) {
        Jedis jedis = null;
        String value;
        try {
            jedis = jedisPool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            jedis.close();
        }
        return value;
    }

    /**
     * 根据传入Keys获取指定Values
     */
    public List<String> mget(String... keys) {
        Jedis jedis = null;
        List<String> values;
        try {
            jedis = jedisPool.getResource();
            values = jedis.mget(keys);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
        return values;
    }

    /**
     * 模糊查询根据PATTERN获取指定Keys
     */
    public Set<String> keys(String pattern) {
        Jedis jedis = null;
        Set<String> values;
        try {
            jedis = jedisPool.getResource();
            values = jedis.keys(pattern);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
        return values;
    }

    /**
     * 校验Keys值是否存在
     */
    public Long exists(String... keys) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(keys);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
    }

    /**
     * 校验Key值是否存在
     */
    public Boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            jedis.close();
        }
    }

    /**
     * 删除指定Key-Value
     */
    public Long del(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            jedis.close();
        }
    }

    /**
     * 分布式锁
     * @param key
     * @param value
     * @param time 锁的超时时间，单位：秒
     *
     * @return 获取锁成功返回"OK"，失败返回null
     */
    public String getDistributedLock(String key,String value,int time){
        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();

            ret = jedis.set(key, value, new SetParams().nx().ex(time));
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
    }
    /**
     * 根据 list的 key
     * 返回 list的 长度
     *
     * @param key
     * @return
     */
    public long getListLength(String key) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            return jedis.llen(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            jedis.close();
        }
    }

    /**
     * 在 list 指定位置 插入值
     * 覆盖原有的值
     *
     * @param key list的key
     * @param index 指定位置
     * @param value
     * @return
     */
    public String setList(String key, int index, String value) {

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            return jedis.lset(key, index, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
    }

    /**
     * List头部追加记录
     * 将一个或多个值 value 插入到列表 key 的表头
     * 如果list不存在，则创建list 并进行push 操作
     *
     * @param key
     * @param value
     * @return
     */
    public long lPush(String key,String... value){

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            long resultStatus = jedis.lpush(key,value);
            return resultStatus;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            jedis.close();
        }
    }

    /**
     * List尾部追加记录
     * 将一个或多个值 value 插入到列表 key 的表尾(最右边)
     * 如果list不存在，一个空列表会被创建并执行 RPUSH 操作
     *
     * @param key
     * @param value
     * @return
     */
    public long rPush(String key,String... value){

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            long resultStatus = jedis.rpush(key,value);
            return resultStatus;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            jedis.close();
        }
    }

    /**
     * 清空List数据
     * ltrim 让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
     * start和end为0时，即清空list
     *
     * @param key
     * @return
     */
    public String clear(String key){

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            String result = jedis.ltrim(key,0,0);
            jedis.lpop(key);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
    }

    /**
     * 添加一个set
     * @param key
     * @param value
     * @return
     */
    public long addSet(String key,String... value){

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            long resultStatus = jedis.sadd(key,value);
            return resultStatus;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取一个set
     * @param key
     * @return
     */
    public Set getSet(String key){

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            Set resultSet = jedis.smembers(key);
            return resultSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
    }
    /**
     * Set删除元素
     * @param key
     * @return
     */
    public long delSet(String key,String... values){

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            long resultStatus = jedis.srem(key,values);
            return resultStatus;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            jedis.close();
        }
    }

    /**
     * Set是否存在元素value
     * @param key
     * @return
     */
    public Boolean isExistSet(String key,String values){

        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();
            Boolean resultStatus = jedis.sismember(key,values);
            return resultStatus;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
    }

}
