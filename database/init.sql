-- ============================================================
-- ProfTest — Система тестирования профессиональных навыков
-- Скрипт инициализации базы данных
-- ============================================================

CREATE DATABASE IF NOT EXISTS proftest
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE proftest;

-- 1. Личная информация
CREATE TABLE IF NOT EXISTS Persons (
    PersonID BIGINT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(150) NOT NULL,
    DateOfBirth DATE,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Phone VARCHAR(20),
    Address VARCHAR(255),
    Department VARCHAR(100),
    Position VARCHAR(100),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Пользователи системы
CREATE TABLE IF NOT EXISTS Users (
    UserID BIGINT AUTO_INCREMENT PRIMARY KEY,
    PersonID BIGINT UNIQUE NOT NULL,
    Login VARCHAR(50) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    IsActive BOOLEAN DEFAULT TRUE,
    LastLogin TIMESTAMP NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Роли
CREATE TABLE IF NOT EXISTS Roles (
    RoleID BIGINT AUTO_INCREMENT PRIMARY KEY,
    RoleName VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Связь пользователей и ролей
CREATE TABLE IF NOT EXISTS UserRoles (
    UserID BIGINT NOT NULL,
    RoleID BIGINT NOT NULL,
    PRIMARY KEY (UserID, RoleID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (RoleID) REFERENCES Roles(RoleID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Навыки
CREATE TABLE IF NOT EXISTS Skills (
    SkillID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SkillName VARCHAR(100) NOT NULL,
    Category VARCHAR(80) NOT NULL,
    Description TEXT,
    DifficultyLevel INT CHECK (DifficultyLevel BETWEEN 1 AND 5),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Тесты
CREATE TABLE IF NOT EXISTS Tests (
    TestID BIGINT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(200) NOT NULL,
    Description TEXT,
    CreatorID BIGINT NULL,
    TimeLimitMinutes INT NOT NULL,
    MaxAttempts INT DEFAULT 1,
    PassingScore INT NOT NULL,
    IsActive BOOLEAN DEFAULT TRUE,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CreatorID) REFERENCES Users(UserID)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Общий банк вопросов
CREATE TABLE IF NOT EXISTS Questions (
    QuestionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    Text TEXT NOT NULL,
    QuestionType ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'OPEN') NOT NULL,
    Points INT NOT NULL DEFAULT 1,
    Difficulty INT CHECK (Difficulty BETWEEN 1 AND 5),
    Topic VARCHAR(100),
    CreatedByID BIGINT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CreatedByID) REFERENCES Users(UserID)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Варианты ответов
CREATE TABLE IF NOT EXISTS QuestionOptions (
    OptionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    QuestionID BIGINT NOT NULL,
    OptionText TEXT NOT NULL,
    IsCorrect BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (QuestionID) REFERENCES Questions(QuestionID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Связь Тест — Вопрос
CREATE TABLE IF NOT EXISTS TestQuestions (
    TestID BIGINT NOT NULL,
    QuestionID BIGINT NOT NULL,
    OrderInTest INT NOT NULL DEFAULT 1,
    PRIMARY KEY (TestID, QuestionID),
    FOREIGN KEY (TestID) REFERENCES Tests(TestID) ON DELETE CASCADE,
    FOREIGN KEY (QuestionID) REFERENCES Questions(QuestionID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Связь Тест — Навыки
CREATE TABLE IF NOT EXISTS TestSkills (
    TestID BIGINT NOT NULL,
    SkillID BIGINT NOT NULL,
    PRIMARY KEY (TestID, SkillID),
    FOREIGN KEY (TestID) REFERENCES Tests(TestID) ON DELETE CASCADE,
    FOREIGN KEY (SkillID) REFERENCES Skills(SkillID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Назначения тестов
CREATE TABLE IF NOT EXISTS Assignments (
    AssignmentID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TestID BIGINT NOT NULL,
    UserID BIGINT NOT NULL,
    AssignedByID BIGINT NOT NULL,
    Deadline DATETIME,
    AllowedAttempts INT DEFAULT 1,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (TestID) REFERENCES Tests(TestID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (AssignedByID) REFERENCES Users(UserID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Попытки прохождения
CREATE TABLE IF NOT EXISTS TestAttempts (
    AttemptID BIGINT AUTO_INCREMENT PRIMARY KEY,
    AssignmentID BIGINT NOT NULL,
    UserID BIGINT NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NULL,
    Score DECIMAL(5,2) NULL,
    Status ENUM('IN_PROGRESS','COMPLETED','EXPIRED','CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
    Passed BOOLEAN NULL,
    FOREIGN KEY (AssignmentID) REFERENCES Assignments(AssignmentID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. Ответы пользователей
CREATE TABLE IF NOT EXISTS UserAnswers (
    AnswerID BIGINT AUTO_INCREMENT PRIMARY KEY,
    AttemptID BIGINT NOT NULL,
    QuestionID BIGINT NOT NULL,
    SelectedOptionID BIGINT NULL,
    UserTextAnswer TEXT NULL,
    IsCorrect BOOLEAN NULL,
    PointsEarned INT NULL,
    FOREIGN KEY (AttemptID) REFERENCES TestAttempts(AttemptID) ON DELETE CASCADE,
    FOREIGN KEY (QuestionID) REFERENCES Questions(QuestionID) ON DELETE CASCADE,
    FOREIGN KEY (SelectedOptionID) REFERENCES QuestionOptions(OptionID) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Индексы
CREATE INDEX idx_users_login ON Users(Login);
CREATE INDEX idx_persons_email ON Persons(Email);
CREATE INDEX idx_tests_creator ON Tests(CreatorID);
CREATE INDEX idx_attempts_user ON TestAttempts(UserID);
CREATE INDEX idx_assignments_user ON Assignments(UserID);

-- ============================================================
-- Начальные данные — роли
-- ============================================================
INSERT IGNORE INTO Roles (RoleName) VALUES
    ('ADMIN'),
    ('HR_MANAGER'),
    ('CANDIDATE'),
    ('EMPLOYEE');

-- ПРИМЕЧАНИЕ: учётная запись admin создаётся сервером автоматически при первом запуске.
-- Логин: admin, Пароль: admin123

-- ============================================================
-- Тестовые данные (опционально)
-- ============================================================
-- Раскомментируйте для добавления демонстрационных навыков и вопросов:
/*
INSERT INTO Skills (SkillName, Category, Description, DifficultyLevel) VALUES
    ('Java Core', 'Программирование', 'Основы языка Java', 3),
    ('SQL', 'Базы данных', 'Работа с реляционными БД', 2),
    ('Алгоритмы', 'Программирование', 'Алгоритмы и структуры данных', 4),
    ('Коммуникация', 'Soft Skills', 'Навыки делового общения', 2),
    ('Управление проектами', 'Менеджмент', 'Методологии управления проектами', 3);
*/
