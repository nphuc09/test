package com.viettel.importwiz.repository.cache;

import com.viettel.importwiz.config.AppProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TicketRepository implements KVRepository<String, Object> {

    @Autowired
    private AppProps props;

    @Autowired
    RedisTicketRepository redisTicketRepository;

    @Autowired
    RocksDBTicketRepository rocksDBTicketRepository;

    private KVRepository<String, Object> getRepository() {
        if (props.cacheDb().equalsIgnoreCase("redis"))
            return redisTicketRepository;
        else
            return rocksDBTicketRepository;
    }

    @Override
    public synchronized boolean save(String key, Object value) {
        return this.getRepository().save(key, value);
    }

    @Override
    public synchronized Optional<Object> find(String key) {
        return this.getRepository().find(key);
    }

    @Override
    public synchronized boolean delete(String key) {
        return this.getRepository().delete(key);
    }
}
