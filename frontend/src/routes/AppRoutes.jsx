import { Outlet, Route, Routes } from "react-router-dom";
import Navbar from "../components/Navbar.jsx";
import Sidebar from "../components/Sidebar.jsx";
import AdminDashboard from "../pages/AdminDashboard.jsx";
import AdministrativeRequests from "../pages/AdministrativeRequests.jsx";
import AdminReservations from "../pages/AdminReservations.jsx";
import Home from "../pages/Home.jsx";
import LibrarianDashboard from "../pages/LibrarianDashboard.jsx";
import Library from "../pages/Library.jsx";
import Login from "../pages/Login.jsx";
import ManageBooks from "../pages/ManageBooks.jsx";
import ManageLoans from "../pages/ManageLoans.jsx";
import ManageRequests from "../pages/ManageRequests.jsx";
import ManageRooms from "../pages/ManageRooms.jsx";
import ManageUsers from "../pages/ManageUsers.jsx";
import MyLoans from "../pages/MyLoans.jsx";
import MyRequests from "../pages/MyRequests.jsx";
import MyReservations from "../pages/MyReservations.jsx";
import NotFound from "../pages/NotFound.jsx";
import RoomReservation from "../pages/RoomReservation.jsx";
import StudentDashboard from "../pages/StudentDashboard.jsx";
import Unauthorized from "../pages/Unauthorized.jsx";
import ProtectedRoute from "./ProtectedRoute.jsx";

function DashboardLayout() {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-main">
        <Navbar />
        <main className="page-shell">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      <Route
        element={
          <ProtectedRoute roles={["STUDENT"]}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/student/dashboard" element={<StudentDashboard />} />
        <Route path="/student/library" element={<Library />} />
        <Route path="/student/my-loans" element={<MyLoans />} />
        <Route path="/student/rooms" element={<RoomReservation />} />
        <Route path="/student/my-reservations" element={<MyReservations />} />
        <Route path="/student/requests/new" element={<AdministrativeRequests />} />
        <Route path="/student/requests" element={<MyRequests />} />
      </Route>

      <Route
        element={
          <ProtectedRoute roles={["ADMIN"]}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/admin/requests" element={<ManageRequests />} />
        <Route path="/admin/rooms" element={<ManageRooms />} />
        <Route path="/admin/reservations" element={<AdminReservations />} />
        <Route path="/admin/users" element={<ManageUsers />} />
      </Route>

      <Route
        element={
          <ProtectedRoute roles={["LIBRARIAN"]}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/librarian/dashboard" element={<LibrarianDashboard />} />
        <Route path="/librarian/books" element={<ManageBooks />} />
        <Route path="/librarian/loans" element={<ManageLoans />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
