CREATE DATABASE IF NOT EXISTS student_management_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management_system;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS system_logs;
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;

-- Create tables
-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT true
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User roles table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Students table
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    address TEXT NOT NULL,
    enrollment_date DATE,
    graduation_date DATE,
    major VARCHAR(100),
    current_semester INT,
    gpa DECIMAL(3,2),
    status VARCHAR(20) DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Courses table
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    credits INT NOT NULL,
    max_students INT,
    instructor VARCHAR(100) NOT NULL,
    semester VARCHAR(20),
    academic_year VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scores table
CREATE TABLE scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    grade_letter VARCHAR(2),
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System logs table
CREATE TABLE system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
-- Insert users (password is 'password' encoded with BCrypt)
INSERT INTO users (username, password, full_name, email, enabled) VALUES
('admin', '$2a$10$rXNiQ9uRHjahg9cICXTDjOyq4z3Vvm5P1oKGbTZ0FmMV/o3IGNP1K', 'System Administrator', 'admin@example.com', true),
('teacher1', '$2a$10$rXNiQ9uRHjahg9cICXTDjOyq4z3Vvm5P1oKGbTZ0FmMV/o3IGNP1K', 'John Smith', 'john.smith@example.com', true),
('student1', '$2a$10$rXNiQ9uRHjahg9cICXTDjOyq4z3Vvm5P1oKGbTZ0FmMV/o3IGNP1K', 'Alice Johnson', 'alice.johnson@example.com', true);

-- Insert user roles
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'),
(2, 'TEACHER'),
(3, 'STUDENT');

-- Insert sample students
INSERT INTO students (first_name, last_name, student_id, date_of_birth, gender, email, phone_number, address, enrollment_date, major, current_semester, gpa, status) VALUES
('Alice', 'Johnson', 'STU001', '2000-05-15', 'Female', 'alice.j@example.com', '1234567890', '123 Student St, College Town', '2022-09-01', 'Computer Science', 2, 3.75, 'ACTIVE'),
('Bob', 'Smith', 'STU002', '2001-03-22', 'Male', 'bob.s@example.com', '2345678901', '456 College Ave, University City', '2022-09-01', 'Mathematics', 2, 3.50, 'ACTIVE'),
('Carol', 'Williams', 'STU003', '2000-11-30', 'Female', 'carol.w@example.com', '3456789012', '789 Campus Rd, Academic Village', '2022-09-01', 'Physics', 2, 3.90, 'ACTIVE');

-- Insert sample courses
INSERT INTO courses (course_code, course_name, description, credits, max_students, instructor, semester, academic_year, status) VALUES
('CS101', 'Introduction to Programming', 'Basic concepts of programming using Java', 3, 50, 'John Smith', 'Fall', '2023-2024', 'ACTIVE'),
('MATH201', 'Calculus I', 'Limits, derivatives, and integrals', 4, 40, 'Sarah Brown', 'Fall', '2023-2024', 'ACTIVE'),
('PHY101', 'Physics Mechanics', 'Basic concepts of classical mechanics', 4, 35, 'Michael Wilson', 'Fall', '2023-2024', 'ACTIVE');

-- Insert sample scores
INSERT INTO scores (student_id, course_id, score, grade_letter, comments, created_at) VALUES
(1, 1, 92.5, 'A', 'Excellent work in programming concepts', CURRENT_TIMESTAMP),
(1, 2, 88.0, 'B+', 'Good understanding of calculus', CURRENT_TIMESTAMP),
(2, 1, 85.0, 'B', 'Good progress in programming', CURRENT_TIMESTAMP),
(2, 3, 90.0, 'A-', 'Strong performance in physics', CURRENT_TIMESTAMP),
(3, 2, 95.0, 'A', 'Outstanding mathematical ability', CURRENT_TIMESTAMP),
(3, 3, 87.5, 'B+', 'Good grasp of physics concepts', CURRENT_TIMESTAMP); 