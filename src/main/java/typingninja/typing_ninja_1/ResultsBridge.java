package typingninja.typing_ninja_1;

import java.util.List;
import java.util.Optional;

/** UI 与数据层之间的薄桥接；内部委托给 ResultsRepository */
public final class ResultsBridge {

    /** 供图表直接使用的列表返回体（旧→新） */
    public static record Metrics(List<Integer> wpm, List<Integer> acc) {}

    /** 幂等建表（随时可调，不会重复创建） */
    public static void ensureTable() {
        ResultsRepository.ensureTable();
    }

    /** 写入一次成绩 */
    public static void saveResult(int wpm, int acc) {
        ResultsRepository.saveResult(wpm, acc);
    }

    /** 读取最近 N 条，顺序为“旧→新”（便于图表从左到右） */
    public static Metrics loadLastN(int n) {
        var m = ResultsRepository.loadLastN(n);
        return new Metrics(m.wpm(), m.acc());
    }

    /** 获取最新一条（用于左侧标签）；若暂无数据则返回 empty */
    public static Optional<int[]> getLatest() {
        var m = ResultsRepository.loadLastN(1);
        if (m.wpm().isEmpty() || m.acc().isEmpty()) return Optional.empty();
        int w = m.wpm().get(0);
        int a = m.acc().get(0);
        return Optional.of(new int[]{ w, a }); // [0]=wpm, [1]=acc
    }

    private ResultsBridge() {}
}

