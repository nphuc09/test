package com.viettel.importwiz.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Stream;


@Slf4j
@Service
public class CommonUtil {

    public static void logDebugStackTrace(Throwable err, Logger logger) {
        logger.warn("{}: {}", err.getClass().getName(), err.getLocalizedMessage());
        logger.error(err.getLocalizedMessage(), err);
        Stream.of(err.getStackTrace()).forEach(x -> logger.debug("\tat {}", x));
    }

    /**
     * Provide a function which return provided value and perform some logging
     * This is mostly use for error cases to supply an alternative value
     */
    public static <A> Function<Throwable, A> errSupply(A value) {
        return err -> {
            logDebugStackTrace(err, log);
            return value;
        };
    }
}
