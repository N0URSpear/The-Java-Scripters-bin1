package com.example.addressbook;

import java.util.List;
import java.util.Optional;

/**
 * Thin wrapper around ResultsRepository for UI layers.
 * - 不直接持有 JDBC / SqliteConnection
 * - 统一从 ResultsRepository（-> SqliteResultsDAO -> Lesson 表）取数
 * - 返回给 UI 的类型保持 wpm()/acc() 接口一致
 */
public final class ResultsBridge {

    /** UI 层使用的返回体（旧 → 新） */
    public static record Metrics(List<Integer> wpm, List<Integer> acc) {}

    private ResultsBridge() {}

    /** 确保数据表存在（转发） */
    public static void ensureTable() {
        ResultsRepository.ensureTable();
    }

    /** 写入一条结果（转发） */
    public static void saveResult(int wpm, int acc) {
        ResultsRepository.saveResult(wpm, acc);
    }

    /** 最近 N 条，返回顺序：旧 → 新（转发并保留 UI 期望的结构） */
    public static Metrics loadLastN(int n) {
        var r = ResultsRepository.loadLastN(n);   // Repository 已处理四舍五入 / 用户过滤 / 排序
        return new Metrics(r.wpm(), r.acc());
    }

    /** 取全部（便捷方法）：内部用极大 LIMIT 实现 */
    public static Metrics loadAll() {
        return loadLastN(Integer.MAX_VALUE);
    }

    /** 最新一条（给 CongratulationsScene 用） */
    public static Optional<int[]> getLatest() {
        var r = ResultsRepository.loadLastN(1);
        if (r.wpm().isEmpty()) return Optional.empty();
        int w = r.wpm().get(0);
        int a = r.acc().get(0);
        return Optional.of(new int[]{ w, a });
    }
}
