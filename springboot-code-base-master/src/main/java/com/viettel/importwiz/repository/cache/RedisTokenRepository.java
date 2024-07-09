package com.viettel.importwiz.repository.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class RedisTokenRepository extends RedisRepository {

    @Value("${tokenExpiredTime}")
    private Long tokenExpiredTime;

    @Override
    protected String getPrefix() {
        return "import-wiz:token:";
    }

    @Override
    protected Long getExpiredTime() {
        return tokenExpiredTime;
    }

    @Override
    protected Boolean isKeyEncrypted() {
        return false;
    }
}
