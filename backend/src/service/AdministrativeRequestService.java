package service;

import enums.RequestStatus;
import enums.RequestType;
import enums.UserRole;
import model.AdministrativeRequest;
import repository.AdministrativeRequestRepository;
import repository.StudentRepository;
import security.AuthenticatedUser;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdministrativeRequestService {

    private final AdministrativeRequestRepository requestRepo = new AdministrativeRequestRepository();
    private final StudentRepository               studentRepo  = new StudentRepository();

    // =========================================================================
    // SUBMIT REQUEST  →  POST /api/requests
    // Body: { type, description }
    // =========================================================================
    public AdministrativeRequest submitRequest(AuthenticatedUser authUser,
                                               String typeStr,
                                               String description) throws Exception {

        // --- Role check ---
        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can submit administrative requests.");
        }

        int studentId = authUser.getStudentId();

        // --- Student validity check ---
        if (!studentRepo.isStudentValid(studentId)) {
            throw new Exception("Your account is not valid. You cannot submit requests.");
        }

        // --- Validate type ---
        if (typeStr == null || typeStr.isBlank()) {
            throw new Exception("Request type is required.");
        }

        RequestType type;
        try {
            type = RequestType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid request type. Allowed: SCHOOL_CERTIFICATE, ATTENDANCE_CERTIFICATE, TRANSCRIPT, OTHER.");
        }

        // --- Build and save ---
        AdministrativeRequest req = new AdministrativeRequest();
        req.setStudentId(studentId);
        req.setType(type);
        req.setDescription(description != null ? description.trim() : "");
        req.setStatus(RequestStatus.PENDING);
        req.setSubmissionDate(LocalDate.now());
        req.setRefusalReason(null);

        return requestRepo.save(req);
    }

    // =========================================================================
    // MY REQUESTS  →  GET /api/requests/my-requests
    // =========================================================================
    public List<AdministrativeRequest> myRequests(AuthenticatedUser authUser) throws Exception {

        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can view their requests.");
        }

        return requestRepo.findByStudentId(authUser.getStudentId());
    }

    // =========================================================================
    // ADMIN: LIST ALL REQUESTS  →  GET /api/admin/requests
    // =========================================================================
    public List<AdministrativeRequest> listAllRequests(AuthenticatedUser authUser) throws Exception {

        if (authUser.getRole() != UserRole.ADMIN) {
            throw new Exception("Only admins can view all requests.");
        }

        return requestRepo.findAll();
    }

    // =========================================================================
    // ADMIN: APPROVE REQUEST  →  POST /api/admin/requests/approve
    // Body: { requestId }
    // =========================================================================
    public AdministrativeRequest approveRequest(AuthenticatedUser authUser,
                                                int requestId) throws Exception {

        if (authUser.getRole() != UserRole.ADMIN) {
            throw new Exception("Only admins can approve requests.");
        }

        AdministrativeRequest req = requestRepo.findById(requestId);
        if (req == null) {
            throw new Exception("Request not found.");
        }

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new Exception("Only pending requests can be approved.");
        }

        requestRepo.approve(requestId);
        req.setStatus(RequestStatus.APPROVED);
        req.setRefusalReason(null);
        return req;
    }

    // =========================================================================
    // ADMIN: REJECT REQUEST  →  POST /api/admin/requests/reject
    // Body: { requestId, refusalReason }
    // =========================================================================
    public AdministrativeRequest rejectRequest(AuthenticatedUser authUser,
                                               int requestId,
                                               String refusalReason) throws Exception {

        if (authUser.getRole() != UserRole.ADMIN) {
            throw new Exception("Only admins can reject requests.");
        }

        if (refusalReason == null || refusalReason.isBlank()) {
            throw new Exception("A refusal reason is required when rejecting a request.");
        }

        AdministrativeRequest req = requestRepo.findById(requestId);
        if (req == null) {
            throw new Exception("Request not found.");
        }

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new Exception("Only pending requests can be rejected.");
        }

        requestRepo.reject(requestId, refusalReason.trim());
        req.setStatus(RequestStatus.REJECTED);
        req.setRefusalReason(refusalReason.trim());
        return req;
    }
}
