package utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JedisPoolUtils {

    private static JedisPool jedisPool = null;

    static {
        //加载配置文件
        InputStream resourceAsStream = JedisPoolUtils.class.getClassLoader().getResourceAsStream("redis.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //获得pool对象
        //创建pool的配置信息
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(Integer.parseInt(properties.get("redis.mzxIdl").toString()));
        jedisPoolConfig.setMinIdle(Integer.parseInt(properties.get("redis.minIdle").toString()));
        jedisPoolConfig.setMaxTotal(Integer.parseInt(properties.get("redis.maxTotal").toString()));
        jedisPool = new JedisPool(jedisPoolConfig, (String) properties.get("redis.url"), Integer.parseInt(properties.get("redis.port").toString()));
    }

    //获得jedis资源
    public static Jedis getJedis() {

        return jedisPool.getResource();
    }

    public static void main(String[] args) {
        Jedis jedis = getJedis();
        System.out.println(jedis.get("xxx"));
    }
}
