package model;

import enums.RequestStatus;
import enums.RequestType;
import java.time.LocalDate;

public class AdministrativeRequest {

    private int requestId;
    private int studentId;
    private String studentName;
    private RequestType type;
    private String description;
    private RequestStatus status;
    private LocalDate submissionDate;
    private String refusalReason;

    public AdministrativeRequest() {}

    public AdministrativeRequest(int requestId, int studentId, RequestType type,
                                   String description, RequestStatus status,
                                   LocalDate submissionDate, String refusalReason) {
        this.requestId      = requestId;
        this.studentId      = studentId;
        this.type           = type;
        this.description    = description;
        this.status         = status;
        this.submissionDate = submissionDate;
        this.refusalReason  = refusalReason;
    }

    public AdministrativeRequest(int requestId, int studentId, String studentName, RequestType type,
                                  String description, RequestStatus status,
                                  LocalDate submissionDate, String refusalReason) {
        this(requestId, studentId, type, description, status, submissionDate, refusalReason);
        this.studentName = studentName;
    }

    public int getRequestId()                   { return requestId; }
    public void setRequestId(int requestId)     { this.requestId = requestId; }

    public int getStudentId()               { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName()                    { return studentName; }
    public void setStudentName(String studentName)    { this.studentName = studentName; }

    public RequestType getType()                { return type; }
    public void setType(RequestType type)       { this.type = type; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public RequestStatus getStatus()                { return status; }
    public void setStatus(RequestStatus status)     { this.status = status; }

    public LocalDate getSubmissionDate()                        { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate)     { this.submissionDate = submissionDate; }

    public String getRefusalReason()                    { return refusalReason; }
    public void setRefusalReason(String refusalReason)  { this.refusalReason = refusalReason; }
}
