# MUSE Social Network - Setup Guide

## Project Structure

```
muse/
├── pom.xml
├── dependency-reduced-pom.xml
├── SETUP.md
├── .gitignore
│
└── src/
    ├── main/
    │   ├── java/com/muse/
    │   │   ├── MuseApp.java
    │   │   ├── config/
    │   │   │   ├── DatabaseConfig.java
    │   │   │   └── ImageCacheConfig.java
    │   │   ├── dao/ (Data Access Objects)
    │   │   │   ├── ClothingItemDAO.java
    │   │   │   ├── ClothingItemDAOImpl.java
    │   │   │   ├── CommentDAO.java
    │   │   │   ├── CommentDAOImpl.java
    │   │   │   ├── CommunityDAO.java
    │   │   │   ├── CommunityDAOImpl.java
    │   │   │   ├── PostDAO.java
    │   │   │   ├── PostDAOImpl.java
    │   │   │   ├── UserDAO.java
    │   │   │   └── UserDAOImpl.java
    │   │   ├── models/ (Data Models)
    │   │   │   ├── ClothingCategory.java
    │   │   │   ├── ClothingItem.java
    │   │   │   ├── Comment.java
    │   │   │   ├── Community.java
    │   │   │   ├── ImageMetadata.java
    │   │   │   ├── Post.java
    │   │   │   ├── SearchResult.java
    │   │   │   ├── SearchType.java
    │   │   │   ├── Searchable.java
    │   │   │   └── User.java
    │   │   ├── service/ (Logic)
    │   │   │   ├── ClothingItemService.java
    │   │   │   ├── CommentService.java
    │   │   │   ├── CommunityService.java
    │   │   │   ├── PostService.java
    │   │   │   ├── SearchBar.java
    │   │   │   ├── SearchService.java
    │   │   │   └── UserService.java
    │   │   ├── ui/
    │   │   │   └── controllers/
    │   │   │       ├── DashboardController.java
    │   │   │       ├── LoginController.java
    │   │   │       └── RegisterController.java
    │   │   └── util/ (Utilities)
    │   │       ├── CacheIndex.java
    │   │       ├── ImageCacheManager.java
    │   │       └── SessionManager.java
    │   │
    │   └── resources/
    │       ├── logback.xml
    │       ├── schema.sql
    │       └── views/
    │           ├── login.fxml
    │           ├── register.fxml
    │           └── dashboard.fxml
    │
    └── test/
        └── java/ (Test files)
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
  - Plain Java Objects
  - Represent domain entities

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
- ✅ Search function for 

### Future Enhancements
- Direct messaging
- User profiles with avatars
- Notifications
- Comment search
- User follow system
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
- Ensure Java 21+ is installed
- Check Maven dependencies in pom.xml

### Build Errors
- Run `mvn clean` to clear cached builds
- Verify all dependencies download correctly

