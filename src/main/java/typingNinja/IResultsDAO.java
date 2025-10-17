package typingNinja;

import java.util.List;

public interface IResultsDAO {

    void ensureTable() throws Exception;

    long addResult(int wpm, int acc) throws Exception;

    List<Result> getLastN(int n) throws Exception;

    List<Result> getAll() throws Exception;
    int count() throws Exception;

    void deleteAll() throws Exception;
}
