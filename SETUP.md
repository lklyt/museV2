# MUSE Social Network - Setup Guide

## Project Structure

```
muse/
├── pom.xml                          # Maven configuration
├── .gitignore                       # Git ignore file
├── SETUP.md                         # This setup guide
│
├── src/main/
│   ├── java/com/muse/
│   │   ├── MuseApp.java            # Main JavaFX application entry point
│   │   │
│   │   ├── models/                 # Data models
│   │   │   ├── User.java
│   │   │   ├── Post.java
│   │   │   ├── Comment.java
│   │   │   └── Community.java
│   │   │
│   │   ├── config/                 # Configuration
│   │   │   └── DatabaseConfig.java # Database connection pool setup
│   │   │
│   │   ├── dao/                    # Data Access Layer
│   │   │   ├── UserDAO.java          # Interface
│   │   │   ├── UserDAOImpl.java       # Implementation
│   │   │   ├── PostDAO.java          # Interface
│   │   │   ├── PostDAOImpl.java       # Implementation
│   │   │   ├── CommentDAO.java       # Interface
│   │   │   ├── CommentDAOImpl.java    # Implementation
│   │   │   ├── CommunityDAO.java     # Interface
│   │   │   └── CommunityDAOImpl.java  # Implementation
│   │   │
│   │   ├── service/               # Business Logic Layer
│   │   │   ├── UserService.java     # User operations and authentication
│   │   │   ├── PostService.java     # Post operations
│   │   │   ├── CommentService.java  # Comment operations
│   │   │   └── CommunityService.java # Community operations
│   │   │
│   │   ├── ui/
│   │   │   ├── controllers/       # JavaFX Controllers
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   └── DashboardController.java
│   │   │   │
│   │   │   └── views/             # FXML files (in resources)
│   │   │
│   │   └── util/                  # Utilities
│   │       └── SessionManager.java # User session management
│   │
│   └── resources/
│       ├── views/                 # FXML layout files
│       │   ├── login.fxml
│       │   ├── register.fxml
│       │   └── dashboard.fxml
│       ├── schema.sql             # Database schema
│       └── logback.xml            # Logging configuration
│
└── src/test/java/                 # Unit tests
```

## Prerequisites

1. **Java 21 or higher**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Git**

## Setup Instructions

### 1. Install Dependencies

Ensure you have Java 21+, Maven, and MySQL installed:

```bash
java -version
mvn -version
mysql --version
```

### 2. Set Up MySQL Database

1. Start your MySQL server
2. Open MySQL client:
   ```bash
   mysql -u root -p
   ```
3. Create the database and tables:
   ```bash
   source src/main/resources/schema.sql
   ```

### 3. Configure Database Connection

Edit `src/main/java/com/muse/config/DatabaseConfig.java`:

```java
private static final String JDBC_URL = "jdbc:mysql://localhost:3306/muse_db";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password"; // Change this
```

### 4. Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn javafx:run

# Or run the JAR directly
java -jar target/muse-app-1.0.0.jar
```

## Architecture Overview

### Layered Architecture

- **UI Layer** (JavaFX Controllers & Views)
  - Handles user interface and user interactions
  - Controllers respond to user events
  - FXML files define UI layout

- **Service Layer**
  - Contains business logic
  - Validates data
  - Coordinates between UI and DAO layers
  - Examples: UserService, PostService, CommentService, CommunityService

- **DAO Layer** (Data Access Objects)
  - Handles all database operations
  - Abstract database complexity from upper layers
  - Implements CRUD operations for each entity
  - Uses prepared statements to prevent SQL injection

- **Config Layer**
  - Database connection pool management (HikariCP)
  - Connection pooling for better performance

- **Model Layer**
  - Plain Java Objects (POJOs)
  - Represent domain entities
  - No business logic

### Key Technologies

- **JavaFX 21**: Modern UI framework for Java
- **MySQL 8.0**: Relational database
- **HikariCP**: High-performance database connection pool
- **SLF4J + Logback**: Logging framework
- **Maven**: Build and dependency management

## Database Schema

### Tables

1. **users** - User accounts
2. **communities** - Community groups
3. **community_members** - Junction table for community membership
4. **posts** - User posts in communities
5. **comments** - Comments on posts
6. **post_likes** - Track post likes
7. **comment_likes** - Track comment likes

## Features to Implement

### Core Features (Already Structured)
- ✅ User authentication (signup/login)
- ✅ Create/view/edit posts
- ✅ Add comments to posts
- ✅ Create/manage communities
- ✅ Join/leave communities
- ✅ Like posts and comments

### Future Enhancements
- Direct messaging
- User profiles with avatars
- Notifications
- Post/comment search
- User follow system
- Post reactions (emojis)
- Media uploads (images/videos)
- Admin dashboard

## Development Notes

### Adding a New Feature

1. Create Model class in `models/` (if needed)
2. Create DAO interface and implementation in `dao/`
3. Create Service class in `service/`
4. Create Controller in `ui/controllers/`
5. Create/update FXML in `resources/views/`
6. Add database tables to `schema.sql`

### Error Handling

- All DAO methods throw `Exception` for error handling at service layer
- Services perform validation before calling DAO
- Controllers display user-friendly error messages

### Logging

Logs are written to:
- Console (for development)
- File: `logs/muse.log` (for production)

Adjust log levels in `logback.xml`

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Verify JDBC_URL, USERNAME, PASSWORD in DatabaseConfig.java
- Check firewall settings

### JavaFX Issues
- Ensure Java 17+ is installed
- Check Maven dependencies in pom.xml

### Build Errors
- Run `mvn clean` to clear cached builds
- Verify all dependencies download correctly

## Running Tests

(To be implemented)

```bash
mvn test
```

## Git Workflow

```bash
git init
git add .
git commit -m "Initial project structure"
git remote add origin <your-repo-url>
git push -u origin main
```

---

For questions or issues, check the code comments or refer to the JavaDoc in source files.
