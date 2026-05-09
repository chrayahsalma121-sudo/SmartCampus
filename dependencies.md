# SmartCampus Dependencies

Read this file after cloning the repository. It explains what must be installed and how to start the database, backend, and frontend.

## Required Tools

- Java JDK 17 or newer with `java` and `javac` available in PATH.
- Node.js 20 or newer with `npm` available in PATH.
- MySQL Server 8.x.
- Windows PowerShell.

## Included Backend Libraries

The backend expects these JAR files inside `backend/lib/`:

- `mysql-connector-j.jar`
- `jjwt.jar`

If they are missing after clone, download them and place them in `backend/lib/` before compiling the backend.

## Database Setup

The backend currently connects with these values in `backend/src/database/DatabaseConnection.java`:

```text
Database: smart_campus
User: root
Password: root
Port: 3306
```

Start MySQL:

```powershell
Start-Service MySQL84
```

If the service name is different, start your MySQL service manually or update the command.

Create and seed the database:

```powershell
cd "C:\path\to\SmartCampus\backend"
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -proot < "sql\schema.sql"
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -proot smart_campus < "sql\seed.sql"
```

Or use the reset script if MySQL is available with the same local configuration:

```powershell
cd "C:\path\to\SmartCampus\backend"
.\reset_db.bat
```

Verify MySQL:

```powershell
Test-NetConnection localhost -Port 3306
```

## Backend Setup

Compile the Java backend:

```powershell
cd "C:\path\to\SmartCampus\backend"
javac -encoding UTF-8 -cp "lib\mysql-connector-j.jar;lib\jjwt.jar" -d "out\production" @(Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName })
```

Run the backend:

```powershell
java -cp "out\production;lib\mysql-connector-j.jar;lib\jjwt.jar" Main
```

Backend URL:

```text
http://localhost:8080
```

## Frontend Setup

Install frontend dependencies:

```powershell
cd "C:\path\to\SmartCampus\frontend"
npm install
```

Run the frontend:

```powershell
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

## Test Accounts

After importing `backend/sql/seed.sql`, these accounts are available:

```text
student@example.com / 1234
admin@example.com / 1234
librarian@example.com / 1234
```

## Quick Start Order

1. Start MySQL.
2. Import `backend/sql/schema.sql` and `backend/sql/seed.sql`.
3. Compile and run the backend.
4. Run `npm install` inside `frontend/`.
5. Run the frontend with `npm run dev`.
6. Open `http://localhost:5173`.
