package model;

import enums.UserRole;

/**
 * Student — extends User.
 * Maps to the `students` table joined with `users`.
 */
public class Student extends User {

    private int     studentId;
    private String  filiere;
    private boolean valid;

    public Student() {}

    public Student(int userId, String fullName, String email, String password,
                   int studentId, String filiere, boolean valid) {
        super(userId, fullName, email, password, UserRole.STUDENT);
        this.studentId = studentId;
        this.filiere   = filiere;
        this.valid     = valid;
    }

    public int     getStudentId()               { return studentId; }
    public void    setStudentId(int studentId)  { this.studentId = studentId; }

    public String  getFiliere()                 { return filiere; }
    public void    setFiliere(String filiere)   { this.filiere = filiere; }

    public boolean isValid()                    { return valid; }
    public void    setValid(boolean valid)      { this.valid = valid; }
}
