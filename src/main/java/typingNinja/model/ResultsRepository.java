package typingNinja.model;

import typingNinja.model.SqliteConnection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
            var rows = dao().getLastN(n);
            Collections.reverse(rows);
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
