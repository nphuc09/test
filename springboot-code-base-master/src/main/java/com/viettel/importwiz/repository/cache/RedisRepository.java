package com.viettel.importwiz.repository.cache;

import com.viettel.cn.PassTranformerCN;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@Slf4j
public class RedisRepository implements KVRepository<String, Object> {

    @Value("${redisDefaultExpiredTime}")
    private Long expiredTime;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    protected String getPrefix() {
        return "import-wiz:";
    }

    protected Long getExpiredTime() {
        return expiredTime;
    }

    protected Boolean isKeyEncrypted() {
        return false;
    }

    @Override
    public synchronized boolean save(String key, Object value) {
        log.debug("finding key '{}' returns '{}'", key, value);
        try {
            String encryptedKey = Boolean.TRUE.equals(isKeyEncrypted()) ? PassTranformerCN.encrypt(key) : key;
            this.redisTemplate.opsForValue().set(getPrefix() + encryptedKey, value);
            if (getExpiredTime() != null && getExpiredTime() > 0) this.redisTemplate.expire(getPrefix() + encryptedKey, Duration.ofSeconds(getExpiredTime()));
        } catch (Exception e) {
            log.error("Error saving entry. Cause: '{}', message: '{}'", e.getCause(), e.getMessage());
            return false;
        }
        return true;
    }

    public synchronized boolean save(String key, Object value, Long expiredTime) {
        log.debug("finding key '{}' returns '{}'", key, value);
        try {
            String encryptedKey = Boolean.TRUE.equals(isKeyEncrypted()) ? PassTranformerCN.encrypt(key) : key;
            this.redisTemplate.opsForValue().set(getPrefix() + encryptedKey, value);
            if (expiredTime != null && expiredTime > 0) this.redisTemplate.expire(getPrefix() + encryptedKey, Duration.ofSeconds(expiredTime));
        } catch (Exception e) {
            log.error("Error saving entry. Cause: '{}', message: '{}'", e.getCause(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public synchronized Optional<Object> find(String key) {
        Object value = null;
        try {
            String encryptedKey = Boolean.TRUE.equals(isKeyEncrypted()) ? PassTranformerCN.encrypt(key) : key;
            value = this.redisTemplate.opsForValue().get(getPrefix() + encryptedKey);
        } catch (Exception e) {
            log.error(
                    "Error retrieving the entry with key: {}, cause: {}, message: {}",
                    key,
                    e.getCause(),
                    e.getMessage()
            );
        }
        log.debug("finding key '{}' returns '{}'", key, value);
        return value != null ? Optional.of(value) : Optional.empty();
    }

    @Override
    public synchronized boolean delete(String key) {
        log.debug("deleting key '{}'", key);
        try {
            String encryptedKey = Boolean.TRUE.equals(isKeyEncrypted()) ? PassTranformerCN.encrypt(key) : key;
            this.redisTemplate.delete(getPrefix() + encryptedKey);
        } catch (Exception e) {
            log.error("Error deleting entry, cause: '{}', message: '{}'", e.getCause(), e.getMessage());
            return false;
        }
        return true;
    }
}
