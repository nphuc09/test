package com.viettel.importwiz.config;


import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Data
@Accessors(fluent = true)
@Slf4j
public class AppProps {
    @Value("${rock.db.dir}")
    private String rockDir;

    @Value("${cache.db}")
    private String cacheDb;
}
