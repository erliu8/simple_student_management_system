-- Drop existing tables if they exist
DROP TABLE IF EXISTS system_logs;
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS enrollments;

-- Create tables
-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    id_card VARCHAR(18) NOT NULL UNIQUE,
    phone VARCHAR(11),
    enabled BOOLEAN DEFAULT true
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User roles table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Students table
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    date_of_birth DATE NOT NULL,
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
CREATE TABLE IF NOT EXISTS courses (
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
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Enrollments table
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    grade VARCHAR(2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE (student_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert initial users
INSERT INTO users (username, password, full_name, email, id_card, phone, enabled) VALUES
('admin', '123456', 'System Admin', 'admin@example.com', 'ADMIN001', '13800138000', true),
('teacher1', '123456', 'Teacher One', 'teacher1@example.com', 'TEACHER001', '13900139000', true),
('student1', '123456', 'Student One', 'student1@example.com', 'STUDENT001', '13700137000', true);

-- Insert user roles
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_TEACHER'),
(3, 'ROLE_STUDENT');

-- Insert sample students
INSERT INTO students (student_id, first_name, last_name, gender, date_of_birth, email, phone_number, address, enrollment_date, major, current_semester, status) VALUES
('S2023001', 'John', 'Doe', 'Male', '2000-01-15', 'john.doe@example.com', '13511112222', '123 College St, City', '2023-09-01', 'Computer Science', 1, 'ACTIVE'),
('S2023002', 'Jane', 'Smith', 'Female', '2001-05-20', 'jane.smith@example.com', '13522223333', '456 University Ave, Town', '2023-09-01', 'Business', 1, 'ACTIVE'),
('S2023003', 'David', 'Johnson', 'Male', '2000-11-10', 'david.j@example.com', '13533334444', '789 Campus Rd, City', '2023-09-01', 'Engineering', 1, 'ACTIVE');

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

-- Insert sample enrollments
INSERT INTO enrollments (student_id, course_id, enrollment_date, grade, status) VALUES
(1, 1, '2023-09-01', 'A', 'ACTIVE'),
(1, 2, '2023-09-01', 'B+', 'ACTIVE'),
(2, 1, '2023-09-01', 'B', 'ACTIVE'),
(2, 3, '2023-09-01', 'A-', 'ACTIVE'),
(3, 2, '2023-09-01', 'A', 'ACTIVE'),
(3, 3, '2023-09-01', 'B+', 'ACTIVE');

-- Insert sample system logs
INSERT INTO system_logs (user_id, action, description, entity_type, entity_id) VALUES
(1, 'LOGIN', 'User logged in', 'USER', 1),
(2, 'CREATE_COURSE', 'Created new course CS101', 'COURSE', 1),
(3, 'ENROLL_COURSE', 'Enrolled in CS101', 'ENROLLMENT', 1); 