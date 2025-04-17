package UserService.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);


    public <T> void save(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(getUserServiceCacheKey(key), value, timeout, unit);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(getUserServiceCacheKey(key));
        if (value == null) {
            return null;
        }
        try {
            return type.cast(value);
        } catch (ClassCastException e) {
            logger.warn("ClassCastException: {}", e.getMessage());
            return null; // Or throw an exception
        }
    }

    public void delete(String key) {
        redisTemplate.delete(getUserServiceCacheKey(key));
    }

    public String getUserServiceCacheKey(String key){
        return "userService:" + key;
    }
}