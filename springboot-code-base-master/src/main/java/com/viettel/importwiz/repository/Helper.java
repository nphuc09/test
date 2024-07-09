package com.viettel.importwiz.repository;

import com.viettel.importwiz.entity.*;
import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;
import io.vavr.Function2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.viettel.importwiz.util.CommonUtil.errSupply;

@Slf4j
class Helper {
    private Helper() {}

    static <A> Function<Throwable, Optional<A>> emptyOption() {
        return errSupply(Optional.empty());
    }

    /**
     * @param source    DataSour
     * @param sql       SQL query to execute
     * @param rowMapper A function to extract from a {@link ResultSet}
     */
    static <T> List<T> select(DataSource source, String sql, Function<ResultSet, Optional<T>> rowMapper) {
        return Try.withResources(source::getConnection).of(conn ->
                Try.withResources(conn::createStatement).of(stm ->
                        Try.withResources(() -> stm.executeQuery(sql)).of(rs -> extractRs(rs, rowMapper)))
        ).flatMap(tries -> tries.flatMap(t -> t)).getOrElseGet(errSupply(Collections.emptyList()));
    }

    /**
     * Select from datasource, use {@link PreparedStatement} to set query params
     *
     * @param source    DataSource
     * @param sql       SQL query to execute
     * @param c         A function which fills out the place holders in SQL command
     * @param rowMapper A function to extract from a {@link ResultSet}
     */
    static <T> List<T> select(DataSource source,
                              String sql,
                              CheckedConsumer<PreparedStatement> c,
                              Function<ResultSet, Optional<T>> rowMapper) {
        return tryExecutePrep(source, sql, c,
                prepStm -> Try.withResources(prepStm::executeQuery).of(rs -> extractRs(rs, rowMapper))
        ).flatMap(t -> t).getOrElseGet(errSupply(Collections.emptyList()));
    }

    static <T> Either<Throwable, List<T>> selectEither(DataSource source,
                                                       String sql,
                                                       CheckedConsumer<PreparedStatement> c,
                                                       Function<ResultSet, Optional<T>> rowMapper) {
        return tryExecutePrep(source, sql, c,
                prepStm -> Try.withResources(prepStm::executeQuery).of(rs -> extractRs(rs, rowMapper))
        ).flatMap(t -> t).toEither();
    }

    /**
     * Sequentially execute a list of sql queries using a single connection
     */
    static <T> List<T> selectBatch(DataSource source,
                                   List<String> sqls,
                                   Function<ResultSet, Optional<T>> extractFn) {
        return
                Try.withResources(source::getConnection).of(conn -> sqls.stream()
                        .flatMap(sql -> executeQueryFn(extractFn).apply(conn, sql).stream())
                        .collect(Collectors.toList())
                ).getOrElseGet(errSupply(Collections.emptyList()));
    }

    /**
     * Extract rows from a {@link ResultSet}. Caller of this fn is responsible for closing {@link ResultSet}
     */
    static <T> List<T> extractRs(ResultSet rs, Function<ResultSet, Optional<T>> rowMapper) {
        return Try.of(() -> {
            List<T> output = new ArrayList<>();
            while (rs.next()) {
                rowMapper.apply(rs).ifPresent(output::add);
            }
            return output;
        }).getOrElseGet(errSupply(Collections.emptyList()));
    }

    /**
     * @return A {@link Function2} which execute sql query
     */
    static <T> Function2<Connection, String, List<T>> executeQueryFn(Function<ResultSet, Optional<T>> rowMapper) {
        return (conn, sql) ->
                Try.withResources(conn::createStatement).of(stm ->
                        Try.withResources(() -> stm.executeQuery(sql)).of(rs -> extractRs(rs, rowMapper)))
                        .transform(tries -> tries.flatMap(t -> t))
                        .getOrElseGet(errSupply(Collections.emptyList()));
    }

    /**
     * Each sql query is executed by wrapping in a {@link CompletableFuture}
     */
    static <T> List<T> selectAsync(DataSource source,
                                   List<String> sqls,
                                   Function<ResultSet, Optional<T>> rowMapper) {
        if (sqls.size() == 1) {
            String sql = sqls.stream().reduce("", (acc, a) -> a);
            return select(source, sql, rowMapper);
        }

        Function2<Connection, String, CompletableFuture<Stream<T>>> supplyAsync = (conn, sql) ->
                CompletableFuture.supplyAsync(() -> executeQueryFn(rowMapper).apply(conn, sql).stream());

        return Try.withResources(source::getConnection).of(conn -> sqls.stream()
                .map(sql -> supplyAsync.apply(conn, sql))
                .collect(Collectors.toList()).stream()
                .flatMap(CompletableFuture::join)
                .collect(Collectors.toList())
        ).getOrElseGet(errSupply(Collections.emptyList()));
    }

    /**
     * Execute SQL command, apply for INSERT/UPDATE/DELETE
     *
     * @param source {@link DataSource}
     * @param sql    SQL command
     * @param c      A function which fills out the place holders in SQL command
     * @return See {@link PreparedStatement#executeUpdate()}
     */
    static int execute(DataSource source, String sql, CheckedConsumer<PreparedStatement> c) {
        return tryExecutePrep(source, sql, c, PreparedStatement::executeUpdate)
                .getOrElseGet(errSupply(0));
    }

    /**
     * Execute a given SQL in batch, apply for INSERT/UPDATE/DELETE
     *
     * @param source {@link DataSource}
     * @param sql    SQL command
     * @param c      A function which fills out the place holders in SQL command
     * @return See {@link PreparedStatement#executeBatch()}
     */
    static int[] executeBatch(DataSource source, String sql, CheckedConsumer<PreparedStatement> c) {
        return tryExecutePrep(source, sql, c, PreparedStatement::executeBatch)
                .getOrElseGet(errSupply(new int[]{}));
    }

    /**
     * Try to execute a {@link PreparedStatement}
     */
    static <T> Try<T> tryExecutePrep(DataSource source,
                                     String sql,
                                     CheckedConsumer<PreparedStatement> c,
                                     CheckedFunction1<PreparedStatement, T> executor) {
        return
                Try.withResources(source::getConnection).of(conn ->
                        Try.withResources(() -> conn.prepareStatement(sql)).of(prepStm -> {
                            c.accept(prepStm);
                            return executor.apply(prepStm);
                        })
                ).flatMap(t -> t);
    }

    static Try<Integer> tryExecute(DataSource source, String sql) {
        return
                Try.withResources(source::getConnection).of(conn ->
                        Try.withResources(conn::createStatement).of(stm -> stm.executeUpdate(sql))
                ).flatMap(t -> t);
    }


    static <A> A getOrNull(String str, Function<String, A> parser) {
        return Try.of(() -> parser.apply(str)).getOrNull();
    }

    static Double getDoubleOrNull(String s) {
        return getOrNull(s, Double::parseDouble);
    }

    static Integer getIntegerOrNull(String i) {
        return getOrNull(i, Integer::parseInt);
    }

    static Boolean getBooleanOrNull(String s) {
        return getOrNull(s, str -> Objects.nonNull(s) ? Boolean.parseBoolean(str) : null);
    }

    static Function<ResultSet, Optional<String>> extractStringCol(String col) {
        return rs -> Try.of(() -> Optional.of(rs.getString(col))).getOrElseGet(emptyOption());
    }

    static Function<ResultSet, Optional<String>> extractListSchema =
        rs -> Try.of(() -> Optional.of(rs.getString("table_schema"))).getOrElseGet(emptyOption());

    static Function<ResultSet, Optional<String>> extractListTable =
        rs -> Try.of(() -> Optional.of(rs.getString("table_name"))).getOrElseGet(emptyOption());

    public static Function<ResultSet, Optional<Long>> extractTotals =
        rs -> Try.of(() -> {
            Long totals = rs.getLong("totals");
            return Optional.of(totals);
        }).getOrElseGet(emptyOption());
}
