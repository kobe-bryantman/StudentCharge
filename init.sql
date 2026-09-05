-- ============================================================
-- 在线考试系统 数据库初始化脚本
-- 数据库：MySQL 8.0
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `online_exam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `online_exam`;

-- ============================================================
-- 1. user表（统一用户表，role区分教师/学生）
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL COMMENT '角色（teacher/student）',
    `teacher_no` VARCHAR(30) DEFAULT NULL COMMENT '工号（教师用）',
    `student_no` VARCHAR(30) DEFAULT NULL COMMENT '学号（学生用）',
    `college` VARCHAR(100) DEFAULT NULL COMMENT '所在学院',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. course表（课程）
-- ============================================================
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
    `teacher_id` BIGINT NOT NULL COMMENT '授课教师ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ============================================================
-- 3. student_course表（学生选课关联）
-- ============================================================
DROP TABLE IF EXISTS `student_course`;
CREATE TABLE `student_course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课关联表';

-- ============================================================
-- 4. question表（考题）
-- ============================================================
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_id` BIGINT NOT NULL COMMENT '所属课程ID',
    `question_text` TEXT NOT NULL COMMENT '题干',
    `option_a` VARCHAR(500) DEFAULT NULL COMMENT '选项A',
    `option_b` VARCHAR(500) DEFAULT NULL COMMENT '选项B',
    `option_c` VARCHAR(500) DEFAULT NULL COMMENT '选项C',
    `option_d` VARCHAR(500) DEFAULT NULL COMMENT '选项D',
    `correct_answer` VARCHAR(10) NOT NULL COMMENT '正确答案（A/B/C/D，多选如AB）',
    `question_type` VARCHAR(20) NOT NULL DEFAULT 'single' COMMENT '题型（single单选/multiple多选）',
    `score` INT NOT NULL DEFAULT 5 COMMENT '分值',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- ============================================================
-- 5. exam_record表（考试记录）
-- ============================================================
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `total_score` INT DEFAULT 0 COMMENT '总得分',
    `exam_time` DATETIME DEFAULT NULL COMMENT '考试时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'unstarted' COMMENT '状态（unstarted/ongoing/submitted）',
    `exam_count` INT DEFAULT 1 COMMENT '第几次考试',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- ============================================================
-- 6. exam_answer表（答题明细）
-- ============================================================
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exam_record_id` BIGINT NOT NULL COMMENT '考试记录ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `student_answer` VARCHAR(50) DEFAULT NULL COMMENT '学生答案',
    `is_correct` TINYINT DEFAULT 0 COMMENT '是否正确（0否1是）',
    `score` INT DEFAULT 0 COMMENT '本题得分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_exam_record_id` (`exam_record_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题明细表';

-- ============================================================
-- 测试数据
-- ============================================================

-- 教师用户
INSERT INTO `user` (`username`, `password`, `name`, `gender`, `phone`, `role`, `teacher_no`, `college`) VALUES
('teacher01', '123456', '张老师', '女', '13800000001', 'teacher', 'T001', '计算机学院'),
('teacher02', '123456', '李老师', '男', '13800000002', 'teacher', 'T002', '计算机学院');

-- 学生用户
INSERT INTO `user` (`username`, `password`, `name`, `gender`, `phone`, `role`, `student_no`, `college`) VALUES
('student01', '123456', '小明', '男', '13900000001', 'student', 'S001', '计算机学院'),
('student02', '123456', '小红', '女', '13900000002', 'student', 'S002', '计算机学院'),
('student03', '123456', '小刚', '男', '13900000003', 'student', 'S003', '计算机学院');

-- 课程（teacher_id 对应上面的教师）
INSERT INTO `course` (`course_name`, `description`, `teacher_id`) VALUES
('Java程序设计', 'Java语言基础与面向对象程序设计，涵盖语法、集合、异常等核心知识。', 1),
('数据结构', '常见数据结构与算法，包括线性表、栈、队列、树、图等。', 2);

-- 学生选课（3个学生都选了2门课）
INSERT INTO `student_course` (`student_id`, `course_id`) VALUES
(3, 1), (4, 1), (5, 1),
(3, 2), (4, 2), (5, 2);

-- Java程序设计（course_id=1）的5道单选题
INSERT INTO `question` (`course_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_type`, `score`) VALUES
(1, 'Java中用于定义类的关键字是？', 'class', 'Class', 'interface', 'struct', 'A', 'single', 10),
(1, '下列哪个不是Java的基本数据类型？', 'int', 'String', 'boolean', 'char', 'B', 'single', 10),
(1, 'Java中创建对象使用的关键字是？', 'new', 'create', 'instance', 'object', 'A', 'single', 10),
(1, '下列关于Java方法重载的描述，正确的是？', '方法名相同参数列表不同', '方法名不同参数列表相同', '返回值类型相同即可', '访问修饰符必须相同', 'A', 'single', 10),
(1, 'Java中处理异常的关键字不包括？', 'try', 'catch', 'finally', 'thrownew', 'D', 'single', 10);

-- 数据结构（course_id=2）的5道单选题
INSERT INTO `question` (`course_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_type`, `score`) VALUES
(2, '链表中删除一个已知节点，时间复杂度为？', 'O(1)', 'O(n)', 'O(logn)', 'O(n²)', 'A', 'single', 10),
(2, '栈的特点是？', '先进先出', '后进先出', '随机存取', '双向进出', 'B', 'single', 10),
(2, '队列的特点是？', '先进先出', '后进先出', '随机存取', '只能插入', 'A', 'single', 10),
(2, '在含有n个节点的二叉查找树中查找的平均时间复杂度约为？', 'O(n)', 'O(logn)', 'O(n²)', 'O(1)', 'B', 'single', 10),
(2, '下列哪种排序算法的平均时间复杂度为O(nlogn)？', '冒泡排序', '选择排序', '快速排序', '插入排序', 'C', 'single', 10);
