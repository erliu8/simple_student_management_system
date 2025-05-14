-- 查询学生的所有成绩
CREATE VIEW student_scores AS
SELECT 
    s.student_number,
    s.name as student_name,
    s.class_name,
    c.course_code,
    c.name as course_name,
    sc.score,
    sc.exam_date,
    sc.semester
FROM student s
JOIN score sc ON s.id = sc.student_id
JOIN course c ON c.id = sc.course_id;

-- 查询班级平均分
CREATE VIEW class_average_scores AS
SELECT 
    s.class_name,
    c.name as course_name,
    c.course_code,
    sc.semester,
    AVG(sc.score) as average_score,
    COUNT(DISTINCT s.id) as student_count
FROM student s
JOIN score sc ON s.id = sc.student_id
JOIN course c ON c.id = sc.course_id
GROUP BY s.class_name, c.name, c.course_code, sc.semester;

-- 查询学生排名
DELIMITER //
CREATE PROCEDURE get_student_ranking(IN p_semester VARCHAR(20), IN p_course_code VARCHAR(20))
BEGIN
    SELECT 
        s.student_number,
        s.name as student_name,
        s.class_name,
        c.course_code,
        c.name as course_name,
        sc.score,
        RANK() OVER (ORDER BY sc.score DESC) as ranking
    FROM student s
    JOIN score sc ON s.id = sc.student_id
    JOIN course c ON c.id = sc.course_id
    WHERE sc.semester = p_semester AND c.course_code = p_course_code
    ORDER BY sc.score DESC;
END //
DELIMITER ;

-- 查询学生的GPA
DELIMITER //
CREATE PROCEDURE calculate_student_gpa(IN p_student_number VARCHAR(20))
BEGIN
    SELECT 
        s.student_number,
        s.name as student_name,
        AVG(sc.score) as average_score,
        SUM(c.credits) as total_credits,
        ROUND(SUM(sc.score * c.credits) / SUM(c.credits), 2) as weighted_average
    FROM student s
    JOIN score sc ON s.id = sc.student_id
    JOIN course c ON c.id = sc.course_id
    WHERE s.student_number = p_student_number
    GROUP BY s.student_number, s.name;
END //
DELIMITER ;

-- 查询不及格学生
CREATE VIEW failed_students AS
SELECT 
    s.student_number,
    s.name as student_name,
    s.class_name,
    c.course_code,
    c.name as course_name,
    sc.score,
    sc.exam_date,
    sc.semester
FROM student s
JOIN score sc ON s.id = sc.student_id
JOIN course c ON c.id = sc.course_id
WHERE sc.score < 60;

-- 查询教师课程统计
CREATE VIEW teacher_course_stats AS
SELECT 
    c.teacher_name,
    COUNT(DISTINCT c.id) as course_count,
    COUNT(DISTINCT s.id) as student_count,
    AVG(sc.score) as average_score
FROM course c
LEFT JOIN score sc ON c.id = sc.course_id
LEFT JOIN student s ON s.id = sc.student_id
GROUP BY c.teacher_name; 