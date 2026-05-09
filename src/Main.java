import com.sun.net.httpserver.HttpServer;
import controller.AdministrativeRequestController;
import controller.AuthController;
import controller.LibraryController;
import controller.RoomController;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ── Marwa's routes ───────────────────────────────────────────────────
        server.createContext("/api/auth",      new AuthController());
        server.createContext("/api/books",     new LibraryController());
        server.createContext("/api/librarian", new LibraryController());

        // ── Salma's routes ───────────────────────────────────────────────────
        server.createContext("/api/rooms",          new RoomController());
        server.createContext("/api/admin/rooms",    new RoomController());
        server.createContext("/api/requests",       new AdministrativeRequestController());
        server.createContext("/api/admin/requests", new AdministrativeRequestController());
        server.createContext("/api/admin/users",    new controller.AdminUserController());

        server.setExecutor(null);
        server.start();

        System.out.println("SmartCampus server started on http://localhost:8080");
    }
}
