package com.viettel.importwiz.repository.cache;

import com.viettel.importwiz.config.AppProps;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.TtlDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.SerializationUtils;
import org.xerial.snappy.Snappy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class RocksDBTokenRepository implements KVRepository<String, Object> {
    @Autowired
    private AppProps props;

    @Value("${tokenExpiredTime}")
    private Long tokenExpiredTime;

    private static final String FILE_NAME = "import-wiz-token-db";
    File baseDir;
    TtlDB db;
    @PostConstruct
        // execute after the application starts.
    void initialize() {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        baseDir = new File(props.rockDir(), FILE_NAME);
        try {
            Files.createDirectories(baseDir.getParentFile().toPath());
            Files.createDirectories(baseDir.getAbsoluteFile().toPath());
            db = TtlDB.open(options, baseDir.getAbsolutePath(), Math.toIntExact(tokenExpiredTime), false);
            db.compactRange();
            log.info("RocksDB initialized");
        } catch(IOException | RocksDBException e) {
            log.error("Error initializing RocksDB. Exception: '{}', message: '{}'", e.getCause(), e.getMessage(), e);
        }
    }
    @PreDestroy
    void onDestroy(){
        if (db!=null){
            db.close();
        }
    }
    @Override
    public synchronized boolean save(String key, Object value) {
        log.debug("saving value '{}' with key '{}'", value, key);
        try {
            byte[] bytes = Snappy.compress(Objects.requireNonNull(SerializationUtils.serialize(value)));
            db.put(key.getBytes(), bytes);
        } catch (RocksDBException | IOException e) {
            log.error("Error saving entry. Cause: '{}', message: '{}'", e.getCause(), e.getMessage());
            return false;
        }
        return true;
    }
    @Override
    public synchronized Optional<Object> find(String key) {
        Object value = null;
        try {
            byte[] bytes = db.get(key.getBytes());
            if (bytes != null) value = SerializationUtils.deserialize(Snappy.uncompress(bytes));
        } catch (RocksDBException | IOException | IllegalArgumentException e) {
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
        log.info("deleting key '{}'", key);
        try {
            db.delete(key.getBytes());
        } catch (RocksDBException e) {
            log.error("Error deleting entry, cause: '{}', message: '{}'", e.getCause(), e.getMessage());
            return false;
        }
        return true;
    }
}
