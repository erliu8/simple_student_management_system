# Student Management System

A comprehensive student management system built with Spring Boot, featuring role-based access control, student information management, course management, and grade tracking.

## Features

- Role-based access control (Admin, Teacher, Student)
- Student management (registration, profile updates, status tracking)
- Course management (creation, assignment, enrollment)
- Grade management (recording, viewing, reports)
- User management (creation, role assignment, access control)
- System activity logging
- Responsive web interface using Bootstrap

## Technologies

- Java 17
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- Thymeleaf
- MySQL 8.0
- Bootstrap 5.3
- Maven

## Prerequisites

- JDK 17 or later
- MySQL 8.0 or later
- Maven 3.6 or later

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/student-management-system.git
   cd student-management-system
   ```

2. Configure MySQL:
   - Create a MySQL database named `student_management_system`
   - Update `src/main/resources/application.properties` with your database credentials

3. Build the project:
   ```bash
   ./mvnw clean install
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

5. Access the application:
   - Open a web browser and navigate to `http://localhost:8080`
   - Default admin credentials:
     - Username: admin
     - Password: password

## Default Users

The system comes with three default users:

1. Administrator
   - Username: admin
   - Password: password
   - Role: ADMIN

2. Teacher
   - Username: teacher1
   - Password: password
   - Role: TEACHER

3. Student
   - Username: student1
   - Password: password
   - Role: STUDENT

## Project Structure

```
src/main/
├── java/com/example/studentmanagementsystem/
│   ├── config/          # Configuration classes
│   ├── controller/      # Web controllers
│   ├── entity/          # JPA entities
│   ├── repository/      # Data repositories
│   ├── service/         # Business logic
│   └── util/           # Utility classes
├── resources/
│   ├── static/         # Static resources (CSS, JS)
│   ├── templates/      # Thymeleaf templates
│   ├── application.properties  # Application configuration
│   ├── schema.sql      # Database schema
│   └── data.sql        # Sample data
```

## Security

- Authentication using Spring Security
- Password encryption using BCrypt
- Role-based access control
- Session management
- CSRF protection

## Database Schema

The system uses the following main tables:
- users: User account information
- user_roles: User role assignments
- students: Student information
- courses: Course information
- scores: Student grades and assessments
- system_logs: System activity tracking

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 