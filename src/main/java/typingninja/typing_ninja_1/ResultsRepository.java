package typingninja.typing_ninja_1;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 便捷静态封装：仍提供 ensureTable/saveResult/loadLastN，底层用 DAO */
public final class ResultsRepository {

    private static IResultsDAO dao() {
        Connection c = SqliteConnection.getInstance();
        return new SqliteResultsDAO(c);
    }

    public static void ensureTable() {
        try { dao().ensureTable(); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static void saveResult(int wpm, int acc) {
        try { dao().addResult(wpm, acc); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static record Metrics(List<Integer> wpm, List<Integer> acc) {}

    public static Metrics loadLastN(int n) {
        try {
            var rows = dao().getLastN(n); // 新→旧
            Collections.reverse(rows);    // 变为旧→新，便于图表
            List<Integer> w = new ArrayList<>();
            List<Integer> a = new ArrayList<>();
            for (var r : rows) { w.add(r.wpm()); a.add(r.acc()); }
            return new Metrics(w, a);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResultsRepository() {}
}
