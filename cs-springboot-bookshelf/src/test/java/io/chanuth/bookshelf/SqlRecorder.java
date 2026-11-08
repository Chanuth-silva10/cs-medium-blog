package io.chanuth.bookshelf;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.hibernate.resource.jdbc.spi.StatementInspector;

/** Hibernate hands every SQL statement to this class before running it, so tests can see the exact SQL. */
public class SqlRecorder implements StatementInspector {

    private static final List<String> STATEMENTS = new CopyOnWriteArrayList<>();

    @Override
    public String inspect(String sql) {
        STATEMENTS.add(sql.toLowerCase());
        return sql;
    }

    public static void clear() {
        STATEMENTS.clear();
    }

    public static List<String> statements() {
        return List.copyOf(STATEMENTS);
    }

    public static long count(String prefix) {
        return STATEMENTS.stream().filter((s) -> s.startsWith(prefix)).count();
    }
}
