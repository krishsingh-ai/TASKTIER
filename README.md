# TaskTier — Intelligent Task Prioritization Web Application

> A robust, full-stack Java Web application demonstrating Jakarta EE 10 architecture where tasks are scored, ranked, and tracked by importance and temporal urgency.

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Jakarta EE](https://img.shields.io/badge/Jakarta_EE-10-F09820?style=for-the-badge&logo=eclipse-che&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-JDBC-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-3.9-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![CSS3](https://img.shields.io/badge/Pure_CSS-No_Frameworks-1572B6?style=for-the-badge&logo=css3&logoColor=white)

---

## 1. Problem Statement

Traditional to-do applications present flat, unranked task lists that fail to convey true urgency versus strategic importance, leading to cognitive overload and deadline mismanagement. Users frequently default to low-impact, easy chores while critical, deadline-sensitive milestones slip unnoticed. 

TaskTier solves this dilemma by synthesizing Eisenhower matrix importance weights with non-linear deadline proximity curves into a real-time, mathematically computed priority score (0.0–100.0). By continuously ranking tasks into four distinct urgency tiers (Critical, High, Medium, Low) and providing integrated focus stopwatch tracking, TaskTier gives students and teams a clear, distraction-free roadmap of what matters most today.

---

## 2. Tech Stack

| Component | Technology | Rationale |
|---|---|---|
| **Core Platform** | Java 17 LTS (Jakarta EE 10) | Standard enterprise language leveraging modern features (Records, Switch expressions, Time API, Streams). |
| **Web Container** | Jakarta Servlets 6.0 & JSP 3.1 | Raw HTTP controllers extending `HttpServlet` and dynamic server-rendered views without heavy frameworks. |
| **Database** | SQLite via `org.xerial:sqlite-jdbc` | Self-contained, zero-configuration SQL engine stored locally in `tasktier.db`. |
| **Persistence** | Raw JDBC (100% `PreparedStatement`) | Parameterized queries, explicit transaction boundaries, and manual ResultSet mapping with zero ORMs. |
| **Build Engine** | Apache Maven 3.8+ | Standardized dependency management, WAR assembly, and Jetty 11 embedded development plugin. |
| **Presentation** | Pure CSS (`style.css`) | Linear/Notion-inspired warm paper aesthetic (`#fafaf7`), soft shadows, and native Dark Mode (`#131312`). No Bootstrap, no Tailwind. |
| **Client Logic** | Pure JavaScript (`app.js`) | Native DOM manipulation, async Fetch APIs, modal controllers, and live timers with zero JS frameworks. |

---

## 3. Rubric Mapping Table

This project strictly satisfies every requirement from the Java Web Based Projects rubric using only approved tools:

| Rubric Area | Required Concept | Implementation Class / File Location | Detailed Demonstration & Verification |
|---|---|---|---|
| **1. OOP Implementation** (10 Marks) | **Inheritance** | `com.tasktier.dao.BaseDAO<T, ID>`<br>`com.tasktier.servlet.BaseServlet` | Abstract `BaseDAO` encapsulates reusable CRUD templates inherited by 4 concrete DAOs. `BaseServlet` extends `HttpServlet` providing HTTP method tunneling (`_method=PUT/DELETE`), session extraction, and uniform JSON responses. |
| | **Polymorphism** | `com.tasktier.strategy.PriorityStrategy`<br>`com.tasktier.strategy.WeightedPriorityStrategy`<br>`com.tasktier.strategy.UrgencyOnlyStrategy` | Strategy design pattern allows runtime substitution of prioritization algorithms without restarting the servlet container. |
| | **Encapsulation** | `com.tasktier.model.User`<br>`com.tasktier.model.Task`<br>`com.tasktier.model.Category`<br>`com.tasktier.model.TimeEntry` | All POJO entity state is strictly private with full public getters, setters, constructors, `equals()`, `hashCode()`, and `toString()`. |
| | **Interfaces** | `com.tasktier.strategy.PriorityStrategy`<br>`com.tasktier.dao.Persistable<T, ID>`<br>`com.tasktier.service.Authenticatable` | Clean behavioral contracts defining calculation rules, persistence lifecycles, and user authorization checks. |
| | **Exception Hierarchy** | `com.tasktier.exception.TaskTierException` (base)<br>`com.tasktier.exception.AuthException`<br>`com.tasktier.exception.ValidationException`<br>`com.tasktier.exception.DataAccessException` | Custom checked exception hierarchy where each exception encapsulates HTTP status codes, error causes, and user-facing messages. |
| **2. Collections & Generics** (6 Marks) | **`List<Task>`** | `com.tasktier.dao.TaskDAO`<br>`com.tasktier.service.TaskService` | Type-safe collections storing database query results and filtered task partitions. |
| | **`Map<Integer, User>`** | `com.tasktier.service.AuthService` (`userCache`) | In-memory `HashMap` cache mapping user primary keys to active `User` objects for rapid lookups. |
| | **`Set<String>`** | `com.tasktier.service.TaskService.getUniqueCategoryNames()` | Distinct category extraction across user tasks utilizing Java Streams `Collectors.toSet()`. |
| | **`Optional<User>`** | `com.tasktier.dao.UserDAO.findByUsername()`<br>`com.tasktier.service.AuthService.authenticate()` | Explicit null-safety wrapping database lookups and authentication results. |
| | **`PriorityQueue<Task>`** | `com.tasktier.service.TaskService.getRankedTasks()` | Natural ordering data structure extracting tasks ordered by priority score via `poll()`. |
| | **`Comparator<Task>`** | `com.tasktier.service.TaskService.getRankedTasks()` | Multi-field comparator ordering descending by priority score and tiebreaking by deadline proximity. |
| | **Generics `List<T>`** | `com.tasktier.dao.BaseDAO.findAll()` | Generic method returning type-safe `List<T>`. |
| **3. Multithreading & Concurrency** (4 Marks) | **`ExecutorService` (Fixed Pool 3)** | `com.tasktier.service.PriorityCalculator` | Fixed thread pool of 3 worker threads partitioning and recalculating task priority scores in parallel. |
| | **`synchronized` Method** | `com.tasktier.service.PriorityCalculator.recalculateAll()`<br>`com.tasktier.service.PriorityCalculator.setStrategy()` | Thread-safe critical sections preventing race conditions during batch score recalculations. |
| | **`ScheduledExecutorService`** | `com.tasktier.config.AppInitializer` | Daemon background scheduler running every 60 seconds to detect expired deadlines and transition tasks to `OVERDUE`. |
| | **`ConcurrentHashMap<String, HttpSession>`** | `com.tasktier.config.AppInitializer`<br>`com.tasktier.servlet.AdminServlet` | Thread-safe active HTTP session registry updated via `HttpSessionListener` and inspectable by administrators. |
| **4. Database Operations** (7 Marks) | **`DBConnection` Singleton** | `com.tasktier.util.DBConnection` | Thread-safe singleton managing SQLite JDBC connections with foreign key constraints enabled. |
| | **`BaseDAO<T, ID>`** | `com.tasktier.dao.BaseDAO` | Abstract template class defining `findById`, `findAll`, `insert`, `update`, `delete`. |
| | **`UserDAO`** | `com.tasktier.dao.UserDAO` | Extends `BaseDAO<User, Integer>` with `findByUsername`, `updateLastLogin`, `deactivate`, `reactivate`. |
| | **`TaskDAO`** | `com.tasktier.dao.TaskDAO` | Extends `BaseDAO<Task, Integer>` with `findByUserId`, `updateStatus`, `updatePriorityScore`, `findOverdueTasks`. |
| | **`CategoryDAO`** | `com.tasktier.dao.CategoryDAO` | Extends `BaseDAO<Category, Integer>` with category verification and pre-seeding. |
| | **`TimeEntryDAO`** | `com.tasktier.dao.TimeEntryDAO` | Extends `BaseDAO<TimeEntry, Integer>` with `findByTaskId`, `getTotalMinutesForUser`, `findActiveEntry`. |
| **5. JDBC Connectivity** (8 + 3 Marks) | **`DriverManager`** | `com.tasktier.util.DBConnection` | Direct JDBC driver loading (`Class.forName("org.sqlite.JDBC")`) and URL connection pooling. |
| | **`PreparedStatement`** | All methods in `com.tasktier.dao.*` | 100% parameter-bound queries preventing SQL injection attacks. |
| | **`ResultSet` Mapping** | `BaseDAO.mapRow()` in each concrete DAO | Robust mapping from relational result sets to strongly typed domain POJOs. |
| | **Try-with-resources** | All DAO methods in `com.tasktier.dao.*` | Automatic cleanup and leak-free closure of `Connection`, `PreparedStatement`, and `ResultSet`. |
| | **Automated Migrations** | `com.tasktier.config.AppInitializer`<br>`src/main/resources/db/schema.sql` | `ServletContextListener` executes `schema.sql` on startup to auto-create tables idempotently. |
| **6. Servlets & Web Integration** (7 Marks) | **7+ HTTP Servlets** | `com.tasktier.servlet.*` | Exactly 9 servlets: `LoginServlet`, `SignupServlet`, `LogoutServlet`, `DashboardServlet`, `TaskServlet`, `TimeEntryServlet`, `CategoryServlet`, `AdminServlet`, `ProgressServlet`. |
| | **`AuthFilter`** | `com.tasktier.filter.AuthFilter` | Intercepts protected paths, verifies `HttpSession`, enforces admin authorization, handles AJAX 401s. |
| | **`web.xml` Deployment Descriptor** | `src/main/webapp/WEB-INF/web.xml` | Declares all servlets, filters, lifecycle listeners, session timeouts (30m), and welcome files. |

---

## 4. Architecture Diagram

```
+-----------------------------------------------------------------------------------+
|                                 CLIENT TIER                                       |
|     Browser (Vanilla JS app.js + Pure CSS style.css + SVG Icons + Responsive UI)  |
+-----------------------------------------------------------------------------------+
                                        |  HTTP GET / POST / PUT / DELETE
                                        v
+-----------------------------------------------------------------------------------+
|                           JAKARTA SERVLET WEB LAYER                               |
|   +---------------------------------------------------------------------------+   |
|   |   AuthFilter: Session Authentication & Admin Authorization Interceptor    |   |
|   +---------------------------------------------------------------------------+   |
|                                       |                                           |
|       +-------------------------------+-------------------------------+           |
|       |                               |                               |           |
|  Login / Signup Servlets     Dashboard & Task Servlets       Admin & Progress     |
|  (Auth & Session Setup)      (Task CRUD & Time Logs)         (Console & Metrics)  |
|                                       |                                           |
|                                       v                                           |
|   +---------------------------------------------------------------------------+   |
|   |                       JSP VIEW LAYER (JSTL EL)                            |   |
|   |    index.jsp | signup.jsp | dashboard.jsp | tasks.jsp | progress.jsp |    |   |
|   |    admin.jsp                                                              |   |
|   +---------------------------------------------------------------------------+   |
+-----------------------------------------------------------------------------------+
                                        |
                                        v
+-----------------------------------------------------------------------------------+
|                                BUSINESS SERVICE LAYER                             |
|  +--------------------+   +-----------------------+   +------------------------+  |
|  |    AuthService     |   |      TaskService      |   |   TimeTrackerService   |  |
|  | (PBKDF2 / UserMap) |   | (PriorityQueue / Sort)|   |   (Durations / Logs)   |  |
|  +--------------------+   +-----------------------+   +------------------------+  |
|                                       |                                           |
|                                       v                                           |
|  +-----------------------------------------------------------------------------+  |
|  |           PriorityCalculator (ExecutorService 3 Threads Pool)               |  |
|  |         Strategy Pattern: WeightedPriorityStrategy | UrgencyOnlyStrategy     |  |
|  +-----------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
                                        |
                                        v
+-----------------------------------------------------------------------------------+
|                              DATA ACCESS OBJECT (DAO)                             |
|              BaseDAO<T, ID> Abstract Generic Template Pattern                    |
|        +---------------+---------------+---------------+---------------+          |
|        | UserDAO       | TaskDAO       | CategoryDAO   | TimeEntryDAO  |          |
|        +---------------+---------------+---------------+---------------+          |
|                |               |               |               |                  |
|                +---------------+---------------+---------------+                  |
|                                        v                                          |
|                   DBConnection Singleton (JDBC DriverManager)                     |
+-----------------------------------------------------------------------------------+
                                        |
                                        v
+-----------------------------------------------------------------------------------+
|                             DATABASE PERSISTENCE                                  |
|                  SQLite Database File (tasktier.db via JDBC)                      |
|       Tables: users  <--  tasks  <--  time_entries                                |
|                             ^                                                     |
|                             |                                                     |
|                         categories                                                |
+-----------------------------------------------------------------------------------+
```

---

## 5. Database Schema

The database schema is defined in `src/main/resources/db/schema.sql` and initialized automatically on first startup:

```sql
-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER',     -- 'ADMIN' or 'USER'
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    last_login_at TEXT
);

-- 2. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    color TEXT DEFAULT '#4f46e5'
);

-- 3. Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    category_id INTEGER,
    estimated_hours REAL DEFAULT 0.0,
    deadline TEXT,
    importance TEXT NOT NULL DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'URGENT'
    status TEXT NOT NULL DEFAULT 'PENDING',    -- 'PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'
    priority_score REAL DEFAULT 0.0,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 4. Time Entries Table
CREATE TABLE IF NOT EXISTS time_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    task_id INTEGER NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT,
    duration_minutes INTEGER DEFAULT 0,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
```

### Entity-Relationship Summary:
- **`users` (1) &rarr; (N) `tasks`**: Each task belongs to a registered user (`ON DELETE CASCADE`).
- **`categories` (1) &rarr; (N) `tasks`**: Optional foreign key relationship classifying tasks (`ON DELETE SET NULL`).
- **`tasks` (1) &rarr; (N) `time_entries`**: A task can record multiple active timer intervals (`ON DELETE CASCADE`).
- **`users` (1) &rarr; (N) `time_entries`**: Focus duration records belong to the user who logged them (`ON DELETE CASCADE`).

---

## 6. Prerequisites

Ensure your development environment meets these minimal requirements:
- **Java Development Kit (JDK):** Version 17 LTS or higher (tested on JDK 17, 21, and 25)
  - Verify via `java -version`
- **Apache Maven:** Version 3.8.0 or higher
  - Verify via `mvn -version`
- **Modern Web Browser:** Chrome, Edge, Firefox, or Safari (supports HTML5 date inputs & CSS variables)

---

## 7. Build & Run Instructions

### Option A: Embedded Development Server (Recommended)
This is the fastest way to run and grade the project with zero external setup.

1. Open your terminal in the project root:
   ```bash
   cd "K:\PROJECTS\TASKTIER"
   ```
2. Build the project and package the WAR:
   ```bash
   mvn clean package
   ```
3. Launch the embedded Jetty container:
   ```bash
   mvn jetty:run
   ```
4. Open your browser and navigate to:
   ```
   http://localhost:8080/tasktier/
   ```

### Option B: Standalone Apache Tomcat 10+ Deployment
1. Build the production WAR file:
   ```bash
   mvn clean package
   ```
2. Locate the packaged WAR file at `target/tasktier.war`.
3. Copy `target/tasktier.war` into the `webapps/` folder of your Apache Tomcat 10+ installation.
4. Start Tomcat:
   - **Windows:** `bin\catalina.bat start`
   - **Linux/macOS:** `bin/catalina.sh start`
5. Access the application in your browser:
   ```
   http://localhost:8080/tasktier/
   ```

---

## 8. Default Administrator Credentials

Upon initial launch, `AppInitializer` automatically provisions a pre-configured administrator account:

| Field | Value |
|---|---|
| **Username** | `admin` |
| **Password** | `admin123` |
| **Role** | `ADMIN` |
| **Admin Portal** | `http://localhost:8080/tasktier/admin` |

*(Password is securely hashed in the database using PBKDF2WithHmacSHA256 with a unique 16-byte salt and 100,000 iterations).*

---

## 9. Folder Structure

```
tasktier/
├── pom.xml                               # Maven project configuration & dependencies
├── README.md                             # Comprehensive project documentation
├── .gitignore                            # Git exclusion rules
└── src/main/
    ├── java/com/tasktier/
    │   ├── config/
    │   │   └── AppInitializer.java       # Startup migrations, seeding & scheduled thread
    │   ├── dao/
    │   │   ├── BaseDAO.java              # Abstract generic template DAO
    │   │   ├── CategoryDAO.java          # Category database operations
    │   │   ├── Persistable.java          # Persistence interface
    │   │   ├── TaskDAO.java              # Task database operations
    │   │   ├── TimeEntryDAO.java         # Time entry database operations
    │   │   └── UserDAO.java              # User database operations
    │   ├── exception/
    │   │   ├── AuthException.java        # Authentication failure exception (401)
    │   │   ├── DataAccessException.java  # SQL / DB execution exception (500)
    │   │   ├── TaskTierException.java    # Base custom exception
    │   │   └── ValidationException.java  # Input validation failure exception (400)
    │   ├── filter/
    │   │   └── AuthFilter.java           # Authentication and authorization filter
    │   ├── model/
    │   │   ├── Category.java             # Category domain model
    │   │   ├── Priority.java             # Task importance enumeration
    │   │   ├── Task.java                 # Task domain model (Comparable)
    │   │   ├── TaskStatus.java           # Task lifecycle status enumeration
    │   │   ├── TimeEntry.java            # Time log domain model
    │   │   ├── User.java                 # User account domain model
    │   │   ├── UserRole.java             # User role enumeration (ADMIN / USER)
    │   │   └── UserSummaryDTO.java       # Aggregated admin summary DTO
    │   ├── service/
    │   │   ├── Authenticatable.java      # Authentication service interface
    │   │   ├── AuthService.java          # User login, registration & caching
    │   │   ├── PriorityCalculator.java   # Multithreaded score engine (3 threads)
    │   │   ├── TaskService.java          # PriorityQueue ranking & validation
    │   │   └── TimeTrackerService.java   # Timer duration calculation
    │   ├── servlet/
    │   │   ├── AdminServlet.java         # Admin user management & strategy switch
    │   │   ├── BaseServlet.java          # Abstract servlet with method tunneling
    │   │   ├── CategoryServlet.java      # Category management endpoint
    │   │   ├── DashboardServlet.java     # Main user dashboard controller
    │   │   ├── LoginServlet.java         # User authentication endpoint
    │   │   ├── LogoutServlet.java        # Session termination endpoint
    │   │   ├── ProgressServlet.java      # Visual analytics controller
    │   │   ├── SignupServlet.java        # Registration controller
    │   │   ├── TaskServlet.java          # Task RESTful CRUD controller
    │   │   └── TimeEntryServlet.java     # Stopwatch duration tracking controller
    │   ├── strategy/
    │   │   ├── PriorityStrategy.java     # Polymorphic prioritization interface
    │   │   ├── UrgencyOnlyStrategy.java  # Pure deadline-proximity strategy
    │   │   └── WeightedPriorityStrategy.java # Balanced weighted strategy
    │   └── util/
    │       ├── AppLogger.java            # Centralized Logger wrapper (no System.out)
    │       ├── DateUtil.java             # Modern Java Time API utility
    │       ├── DBConnection.java         # SQLite singleton connection manager
    │       ├── JsonUtil.java             # Google Gson utility & HTTP JSON writer
    │       └── PasswordHasher.java       # PBKDF2WithHmacSHA256 crypto hasher
    ├── resources/
    │   └── db/
    │       └── schema.sql                # SQLite DDL database migration script
    └── webapp/
        ├── WEB-INF/
        │   ├── views/
        │   │   ├── admin.jsp             # Administration & strategy management
        │   │   ├── dashboard.jsp         # Task prioritization board
        │   │   ├── progress.jsp          # Pure CSS analytics breakdown
        │   │   └── tasks.jsp             # Task details & interactive focus stopwatch
        │   └── web.xml                   # Jakarta EE 10 web application descriptor
        ├── index.jsp                     # Sign-in portal
        ├── signup.jsp                    # Account creation portal
        └── static/
            ├── app.js                    # Pure Vanilla JavaScript client logic
            └── style.css                 # Pure CSS stylesheet (Light & Dark mode)
```

---

## 10. Screenshots

| Screen | Description | Preview |
|---|---|---|
| **Sign-In / Authentication** | Elegant card-centered sign-in with dark mode toggle and input validation. | *[Screenshot: Sign-In View]* |
| **Prioritized Dashboard** | Dynamic greeting, 4 tinted metric cards, search/filter bar, and tier-ranked cards. | *[Screenshot: Dashboard View]* |
| **Task Creation & Modal** | Form with side-by-side date/time pickers and AJAX modal to add categories. | *[Screenshot: Task Creation Form]* |
| **Task Details & Timer** | Live JavaScript stopwatch tracking focus sessions and logging time to SQLite. | *[Screenshot: Task Stopwatch Timer]* |
| **Analytics & Progress** | Pure CSS distribution bars by tier and category with total hours computed. | *[Screenshot: Analytics View]* |
| **Admin Console** | System metrics, active session counts, strategy switcher, and user toggles. | *[Screenshot: Administration Panel]* |

---

## 11. Known Limitations

- **In-Memory Session Registry:** The active session registry (`ConcurrentHashMap<String, HttpSession>`) is maintained in-memory and resets upon container restart.
- **Embedded Database Concurrency:** SQLite uses database-level file locks on write operations. While optimal for single-node college deployments, heavy concurrent multi-node enterprise write loads would benefit from migrating `DBConnection` to PostgreSQL or MySQL.
- **Development SSL:** Transport-layer HTTPS is terminated by the host reverse-proxy (e.g. Nginx or production Tomcat); default development runs over standard HTTP on port 8080.

---

## 12. Author & Course Information

- **Project Name:** TaskTier
- **Author / Developer:** DEV_KRISSH
- **Course Title:** Java Web Based Applications / Advanced Java EE
- **Submission Date:** October 2026
- **Architecture Standard:** Jakarta EE 10 / JDK 17 LTS / Raw JDBC / Clean Architecture
