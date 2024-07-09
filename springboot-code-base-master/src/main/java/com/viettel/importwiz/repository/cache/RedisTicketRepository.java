package com.viettel.importwiz.repository.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.cn.PassTranformerCN;
import com.viettel.importwiz.security.vsa.UserInformationVsaDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@Slf4j
public class RedisTicketRepository extends RedisRepository {

    @Value("${ticketExpiredTime}")
    private Long ticketExpiredTime;

    @Override
    protected String getPrefix() {
        return "import-wiz:ticket:";
    }

    @Override
    protected Long getExpiredTime() {
        return ticketExpiredTime;
    }

    @Override
    protected Boolean isKeyEncrypted() {
        return false;
    }

    @Override
    public synchronized boolean save(String key, Object value) {
        log.debug("finding key '{}' returns '{}'", key, value);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonValue = objectMapper.writeValueAsString(value).replace(":\"M\",", ":0,"); //replace string object type with long object type
            String encryptedKey = Boolean.TRUE.equals(isKeyEncrypted()) ? PassTranformerCN.encrypt(key) : key;
            this.redisTemplate.opsForValue().set(getPrefix() + encryptedKey, jsonValue);
            this.redisTemplate.expire(getPrefix() + encryptedKey, Duration.ofSeconds(getExpiredTime()));
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
            ObjectMapper objectMapper = new ObjectMapper();
            String encryptedKey = Boolean.TRUE.equals(isKeyEncrypted()) ? PassTranformerCN.encrypt(key) : key;
            String jsonValue = (String) this.redisTemplate.opsForValue().get(getPrefix() + encryptedKey);
            if (jsonValue != null && !jsonValue.isEmpty()) value = objectMapper.readValue(jsonValue, UserInformationVsaDTO.class);
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
}
