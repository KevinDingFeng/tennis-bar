package com.shenghesun.invite.sso.redis.realization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

@SuppressWarnings("unchecked")
public class CustomRedisCache<K, V> implements Cache<K, V> {

    private static final String REDIS_SHIRO_CACHE = "weiyou-shiro-cache:";
    private String cacheKey;
    private RedisTemplate<K, V> redisTemplate;
    private long globExpire = 30;

    @SuppressWarnings("rawtypes")
    public CustomRedisCache(String name, RedisTemplate client) {
        this.cacheKey = REDIS_SHIRO_CACHE + name + ":";
        this.redisTemplate = client;
    }

    @Override
    public V get(K key) throws CacheException {
        redisTemplate.boundValueOps(getCacheKey(key)).expire(globExpire, TimeUnit.MINUTES);
        //System.out.println("Get key = " + getCacheKey(key) + " ; value = " + redisTemplate.boundValueOps(getCacheKey(key)).get());
        return redisTemplate.boundValueOps(getCacheKey(key)).get();
    }

    @Override
    public V put(K key, V value) throws CacheException {
        V old = get(key);
//        System.out.println("put key = " + getCacheKey(key) + " ; value = " + value);
        redisTemplate.boundValueOps(getCacheKey(key)).set(value);
        return old;
    }

    @Override
    public V remove(K key) throws CacheException {
//    	System.out.println("removed key = " + getCacheKey(key));
        V old = get(key);
        redisTemplate.delete(getCacheKey(key));
        return old;
    }

    @Override
    public void clear() throws CacheException {

//    	System.out.println("clear ");
        redisTemplate.delete(keys());
    }

    @Override
    public int size() {
        return keys().size();
    }

    @Override
    public Set<K> keys() {
        return redisTemplate.keys(getCacheKey("*"));
    }

    @Override
    public Collection<V> values() {
        Set<K> set = keys();
        List<V> list = new ArrayList<>();
        for (K s : set) {
            list.add(get(s));
        }
        return list;
    }

    private K getCacheKey(Object k) {
        return (K) (this.cacheKey + k);
    }
}
