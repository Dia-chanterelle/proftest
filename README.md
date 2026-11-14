# ProfTest — Система тестирования профессиональных навыков

## Описание

Клиент-серверное приложение для проведения тестирования.
Реализовано на Java 17 с использованием JavaFX (GUI), Hibernate (ORM), MySQL (БД),
многопоточного TCP-сервера и JSON-протокола обмена через сокеты.

---

## Архитектура

```
proftest/
├── common/      — общие классы: DTO, протокол (Request/Response, RequestType, ResponseStatus)
├── server/      — многопоточный сервер, Hibernate entities, DAO, сервисы
├── client/      — JavaFX клиент, FXML интерфейс, CSS стили
└── database/    — SQL скрипт инициализации БД
```


---

## Требования

- Java 17+ (JDK 17)
- Maven 3.8+
- MySQL 8.0+

---

## Установка и запуск

### 1. Создать базу данных

```bash
mysql -u root -p < database/init.sql
```

### 2. Настроить сервер

Отредактируйте `server/src/main/resources/server.properties`:
```properties
server.port=12345
db.url=jdbc:mysql://localhost:3306/proftest?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
db.user=root
db.password=ваш_пароль
```

### 3. Собрать проект

```bash
mvn clean package -DskipTests
```

### 4. Запустить сервер

```bash
java -jar server/target/proftest-server-jar-with-dependencies.jar
```

Или с параметрами командной строки:
```bash
java -jar server/target/proftest-server-jar-with-dependencies.jar --port 12345 --db-password secret
```

### 5. Запустить клиент

```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml \
     -jar client/target/proftest-client-jar-with-dependencies.jar
```

---

## Учётные данные по умолчанию

Администратор создаётся автоматически при первом запуске сервера:
- **Логин:** `admin`
- **Пароль:** `admin123`

---

## Роли пользователей

| Роль        | Возможности                                              |
|-------------|----------------------------------------------------------|
| ADMIN       | Полный доступ: управление пользователями, ролями, системой |
| HR_MANAGER  | Создание тестов, вопросов, назначение, отчёты            |
| CANDIDATE   | Прохождение тестов, просмотр своих результатов            |

---

## Варианты использования (Use Cases — 12+)

1. Авторизация в системе
2. Регистрация нового пользователя
3. Управление учётными записями
4. Управление ролями пользователей
5. Управление навыками
6. Создание и редактирование тестов
7. Управление банком вопросов
8. Формирование теста из банка вопросов
9. Назначение теста пользователю
10. Прохождение назначенного теста
11. Автоматическая проверка ответов
12. Просмотр результатов (кандидат)
13. Просмотр отчётов по тестам (HR/Admin)
14. Смена пароля

---

## Паттерны проектирования

1. **Singleton** — `HibernateUtil`, `ServerConfig`, `SessionManager`, `ServerConnection`, `ClientConfig`
2. **Strategy** — подсчёт баллов: `ScoringStrategy` → `SingleChoiceScoringStrategy`, `MultipleChoiceScoringStrategy`, `OpenAnswerScoringStrategy`
3. **Factory Method** — `ScoringStrategyFactory.getStrategy(QuestionType)`

---

## Иерархия классов (8+)

1. `BaseEntity` (abstract) ← `Person`, `User`, `Role`, `Skill`, `Question`, `Test`, `Assignment`, `TestAttempt`, `UserAnswer`, `QuestionOption`
2. `BaseDao<T>` (interface) ← `AbstractDao<T>` (abstract) ← `UserDao`, `PersonDao`, `RoleDao`, `SkillDao`, `QuestionDao`, `TestDao`, `AssignmentDao`, `TestAttemptDao`, `UserAnswerDao`
3. `BaseService` (abstract) ← `AuthService`, `UserService`, `SkillService`, `TestService`, `AssignmentService`, `TestingService`
4. `BaseController` (abstract) ← `LoginController`, `RegisterController`, `MainController`, все остальные контроллеры
5. `ScoringStrategy` (interface) ← `SingleChoiceScoringStrategy`, `MultipleChoiceScoringStrategy`, `OpenAnswerScoringStrategy`

---

## Технологии

- **Java 17** — основной язык
- **JavaFX 17** — GUI (FXML + CSS)
- **Hibernate 6** — ORM для работы с БД
- **MySQL 8** — СУБД
- **Gson** — сериализация/десериализация JSON
- **SLF4J + Logback** — логирование
- **TCP Sockets + BufferedReader/PrintWriter** — сетевой обмен
- **Maven** — сборка проекта
