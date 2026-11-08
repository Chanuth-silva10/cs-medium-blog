package io.chanuth.httpclient;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A tiny fake of the DevCraft Task API, built on the HTTP server that ships with the JDK. Using a
 * real server (not a mock of the client) means the tests exercise real HTTP: headers, JSON, status
 * codes, timeouts.
 */
public class TaskApiStub {

    public static final String EMAIL = "svc@devcraft.io";
    public static final String PASSWORD = "secret";

    private static final String WORK_CATEGORY = "{\"_id\":\"c1\",\"name\":\"Work\",\"color\":\"#2aa4e0\"}";

    private final HttpServer server;
    private final AtomicInteger logins = new AtomicInteger();
    private final AtomicInteger requests = new AtomicInteger();
    private final Set<String> validTokens = ConcurrentHashMap.newKeySet();
    private volatile boolean rejectLogins;
    private volatile String lastBody = "";
    private volatile String lastRawPath = "";

    public TaskApiStub() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        server.createContext("/", this::handle);
        // One thread per request, so a deliberately slow answer never blocks the next test.
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }

    /** Back to a clean state before each test. */
    public void reset() {
        logins.set(0);
        requests.set(0);
        rejectLogins = false;
        lastBody = "";
        lastRawPath = "";
    }

    /** Makes every token we handed out stop working, as if they all expired. */
    public void revokeTokens() {
        validTokens.clear();
    }

    public void rejectLogins(boolean reject) {
        this.rejectLogins = reject;
    }

    public int loginCount() {
        return logins.get();
    }

    public int requestCount() {
        return requests.get();
    }

    public String lastBody() {
        return lastBody;
    }

    public String lastRawPath() {
        return lastRawPath;
    }

    private void handle(HttpExchange exchange) throws IOException {
        requests.incrementAndGet();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        lastRawPath = exchange.getRequestURI().getRawPath();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        if (method.equals("POST") && path.equals("/auth/login")) {
            login(exchange, body);
            return;
        }

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ") || !validTokens.contains(auth.substring(7))) {
            reply(exchange, 401, "{\"message\":\"Invalid or missing token\"}");
            return;
        }
        lastBody = body;

        if (method.equals("GET") && path.equals("/tasks/me")) {
            reply(exchange, 200, "{\"data\":[" + task("t1", WORK_CATEGORY) + "]}");
        } else if (method.equals("GET") && path.equals("/tasks/slow")) {
            sleep(1500);
            reply(exchange, 200, "{\"data\":" + task("slow", WORK_CATEGORY) + "}");
        } else if (method.equals("GET") && path.equals("/tasks/boom")) {
            reply(exchange, 500, "{\"message\":\"Internal Server Error\"}");
        } else if (method.equals("GET") && path.equals("/tasks/html")) {
            reply(exchange, 503, "<html>Service Unavailable</html>");
        } else if (method.equals("GET") && path.equals("/tasks/missing")) {
            reply(exchange, 404, "{\"message\":\"Task with id \\\"missing\\\" not found.\"}");
        } else if (method.equals("GET") && path.startsWith("/tasks/")) {
            reply(exchange, 200, "{\"data\":" + task(path.substring(7), WORK_CATEGORY) + "}");
        } else if (method.equals("POST") && path.equals("/tasks")) {
            if (!body.contains("\"title\"") || !body.contains("\"category\"")) {
                reply(exchange, 422, "{\"message\":\"The fields title and category are required\"}");
            } else {
                // Like the real API, a freshly created task carries only the category id.
                reply(exchange, 201, "{\"data\":" + task("t2", "\"c1\"") + "}");
            }
        } else if (method.equals("PATCH") && path.startsWith("/tasks/")) {
            reply(exchange, 200, "{\"data\":" + task(path.substring(7), WORK_CATEGORY).replace("\"TODO\"", "\"DONE\"") + "}");
        } else if (method.equals("DELETE") && path.equals("/tasks/ok")) {
            reply(exchange, 200, "{\"message\":\"Task deleted successfully.\"}");
        } else if (method.equals("DELETE") && path.startsWith("/tasks/")) {
            reply(exchange, 403, "{\"message\":\"Forbidden: your role cannot do this\"}");
        } else if (method.equals("GET") && path.equals("/categories")) {
            reply(exchange, 200, "{\"data\":[" + WORK_CATEGORY + "]}");
        } else if (method.equals("POST") && path.equals("/categories")) {
            reply(exchange, 201, "{\"data\":{\"_id\":\"c2\",\"name\":\"Home\",\"color\":\"#2aa4e0\"}}");
        } else {
            reply(exchange, 404, "{\"message\":\"No such route\"}");
        }
    }

    private void login(HttpExchange exchange, String body) throws IOException {
        logins.incrementAndGet();
        boolean ok = !rejectLogins
                && body.contains("\"email\":\"" + EMAIL + "\"")
                && body.contains("\"password\":\"" + PASSWORD + "\"");
        if (!ok) {
            reply(exchange, 400, "{\"message\":\"Email or password is incorrect\"}");
            return;
        }
        String token = "token-" + logins.get() + "-" + System.nanoTime();
        validTokens.add(token);
        reply(exchange, 200, "{\"token\":\"" + token + "\",\"expiresIn\":3600}");
    }

    private static String task(String id, String category) {
        return "{\"_id\":\"" + id + "\",\"title\":\"Write docs\",\"description\":null,\"status\":\"TODO\","
                + "\"priority\":\"MEDIUM\",\"category\":" + category + ",\"dueDate\":null,\"overdue\":false,"
                + "\"createdAt\":\"2026-11-01T10:00:00.000Z\",\"updatedAt\":\"2026-11-01T10:00:00.000Z\",\"__v\":0}";
    }

    private static void reply(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", json.startsWith("{") ? "application/json" : "text/html");
        // A fresh connection per request keeps tests independent: the timeout test cancels a request
        // mid-flight, and a reused connection could otherwise break the test that runs after it.
        exchange.getResponseHeaders().set("Connection", "close");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
