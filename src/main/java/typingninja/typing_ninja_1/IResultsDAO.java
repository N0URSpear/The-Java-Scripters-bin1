package com.example.addressbook;

import java.util.List;

// 成绩 DAO 接口：只围绕 WPM / ACC
public interface IResultsDAO {

    // 建表（幂等，随时可调）
    void ensureTable() throws Exception;

    // 新增一条成绩，返回自增主键 id
    long addResult(int wpm, int acc) throws Exception;

    //最近 N 条（新→旧），用于图表可再反转
    List<Result> getLastN(int n) throws Exception;

    // 全部成绩
    List<Result> getAll() throws Exception;

    // 记录总数
    int count() throws Exception;

    // 清空
    void deleteAll() throws Exception;
}
