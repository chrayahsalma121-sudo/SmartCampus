import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import controller.AdministrativeRequestController;
import controller.AdminUserController;
import controller.AuthController;
import controller.LibraryController;
import controller.RoomController;
import util.CorsFilter;
import util.ResponseUtil;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ── Marwa's routes ───────────────────────────────────────────────────
        createApiContext(server, "/api/auth",      new AuthController());
        createApiContext(server, "/api/books",     new LibraryController());
        createApiContext(server, "/api/librarian", new LibraryController());

        // ── Salma's routes ───────────────────────────────────────────────────
        createApiContext(server, "/api/rooms",          new RoomController());
        createApiContext(server, "/api/admin/rooms",    new RoomController());
        createApiContext(server, "/api/admin/reservations", new RoomController());
        createApiContext(server, "/api/requests",       new AdministrativeRequestController());
        createApiContext(server, "/api/admin/requests", new AdministrativeRequestController());
        createApiContext(server, "/api/admin/users",    new AdminUserController());

        createApiContext(server, "/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("GET".equals(exchange.getRequestMethod()) && (path.equals("/") || path.equals("/api"))) {
                ResponseUtil.sendSuccess(exchange, 200, "SmartCampus backend is running", "{}");
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found.");
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println("SmartCampus server started on http://localhost:8080");
    }

    private static void createApiContext(HttpServer server, String path, HttpHandler handler) {
        HttpContext context = server.createContext(path, handler);
        context.getFilters().add(new CorsFilter());
    }
}
