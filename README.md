# University Management System

A Java Swing desktop application for managing university classes, students, and student-class assignments with PostgreSQL.

## Features

- Add and view classes
- Add students and assign them to classes
- View registered students
- View student-class assignments
- PostgreSQL database connectivity through JDBC

## Requirements

- Java Development Kit (JDK) 8 or later
- PostgreSQL
- PostgreSQL JDBC driver included in `src/postgresql-42.7.4.jar`

## Database setup

Create a PostgreSQL database named `university_management`. The application currently connects using:

```text
Host: localhost
Port: 5432
Database: university_management
Username: username
Password: password
```

Update the connection details in `Main.java` before running the application. The required tables are `classes`, `students`, and `student_classes`. The `data.sql` file is reserved for the database schema and seed data.

## Compile and run

From the project root, run:

```powershell
javac -cp "src/postgresql-42.7.4.jar" -d . Main.java
java -cp ".;src/postgresql-42.7.4.jar" Main
```

On macOS or Linux, use `:` instead of `;` in the Java classpath.

## Project structure

```text
Main.java                         Application source
data.sql                          Database schema and seed-data placeholder
src/postgresql-42.7.4.jar        PostgreSQL JDBC driver
.vscode/tasks.json                VS Code run task
```
