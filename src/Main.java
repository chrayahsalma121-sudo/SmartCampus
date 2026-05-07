import com.sun.net.httpserver.HttpServer;
import controller.AdministrativeRequestController;
import controller.RoomController;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ── Marwa hna ntina ──────────────────────────────────────────
        // Marwa will add her routes here
        // server.createContext("/api/auth",  new AuthController());
        // server.createContext("/api/books", new LibraryController());

        // ── Salma's routes ──────────────────────────────────────────
        server.createContext("/api/rooms",          new RoomController());
        server.createContext("/api/admin/rooms",    new RoomController());
        server.createContext("/api/requests",       new AdministrativeRequestController());
        server.createContext("/api/admin/requests", new AdministrativeRequestController());

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port 8080");
    }
}
