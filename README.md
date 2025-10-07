# DevMons - Project Management System

DevMons is a comprehensive project management system similar to Jira/Trello, built with Java Spring Boot and React.

## 🎯 Features

### ✅ Implemented

#### User Story #1: User Registration and Authentication
- Secure user registration with email verification
- Login with JWT token-based authentication (8-hour expiration)
- Password reset functionality
- Account lockout after 5 failed login attempts
- Bcrypt password hashing
- Password requirements: 12+ characters, mixed case, numbers, special characters

#### User Story #2: Project Creation and Management
- Create projects with unique keys (2-10 uppercase letters)
- Default board creation automatically
- Default workflow states (Backlog → To Do → In Progress → Review → Testing → Done)
- Default issue types (Story, Bug, Task, Epic)
- Update project name and description
- Archive and restore projects
- Create custom labels with colors
- Project owner permissions and access control

### 🚧 Planned Features
- Issue/ticket tracking
- Kanban board visualization
- Sprint planning and management
- Team member invitations
- Notifications
- Reporting and analytics

## 🛠️ Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access
- **PostgreSQL 15** - Primary database
- **JWT** - Token-based authentication
- **Maven** - Build tool

### Testing
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **H2** - In-memory database for tests

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.9 or higher
- PostgreSQL 15
- SMTP server (for email functionality)

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/JoonasMagi/devmons.git
cd devmons
```

### 2. Set up PostgreSQL database
```sql
CREATE DATABASE devmons;
CREATE USER devmons WITH PASSWORD 'devmons';
GRANT ALL PRIVILEGES ON DATABASE devmons TO devmons;
```

### 3. Configure application properties
Update `src/main/resources/application.yml` with your database and email settings:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/devmons
    username: devmons
    password: your_password

  mail:
    host: your_smtp_host
    port: 587
    username: your_email
    password: your_password
```

### 4. Set JWT secret (production)
```bash
export JWT_SECRET=your-secure-secret-key-minimum-256-bits
```

### 5. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🧪 Running Tests

```bash
mvn test
```

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}
```

#### Verify Email
```http
GET /api/auth/verify?token={verification_token}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "SecurePass123!"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe"
}
```

#### Request Password Reset
```http
POST /api/auth/password-reset/request
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Confirm Password Reset
```http
POST /api/auth/password-reset/confirm
Content-Type: application/json

{
  "token": "reset_token",
  "newPassword": "NewSecurePass123!"
}
```

### Project Endpoints

**Note:** All project endpoints require authentication. Include JWT token in Authorization header:
```
Authorization: Bearer {your_jwt_token}
```

#### Create Project
```http
POST /api/projects
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "My Project",
  "key": "MYPROJ",
  "description": "Project description"
}
```

Response:
```json
{
  "id": 1,
  "name": "My Project",
  "key": "MYPROJ",
  "description": "Project description",
  "ownerId": 1,
  "ownerUsername": "johndoe",
  "createdAt": "2025-10-07T21:00:00",
  "archived": false,
  "memberCount": 1
}
```

#### Get All Projects
```http
GET /api/projects
Authorization: Bearer {token}
```

Returns list of all projects where user is owner or member.

#### Get Project by ID
```http
GET /api/projects/{id}
Authorization: Bearer {token}
```

#### Update Project
```http
PUT /api/projects/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "Updated Project Name",
  "description": "Updated description"
}
```

**Note:** Only project owner can update project.

#### Archive Project
```http
POST /api/projects/{id}/archive
Authorization: Bearer {token}
```

**Note:** Only project owner can archive project.

#### Restore Project
```http
POST /api/projects/{id}/restore
Authorization: Bearer {token}
```

**Note:** Only project owner can restore project.

#### Create Label
```http
POST /api/projects/{id}/labels
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "urgent",
  "color": "#FF0000"
}
```

**Note:** Only project owner can create labels.

#### Get Project Labels
```http
GET /api/projects/{id}/labels
Authorization: Bearer {token}
```

## 🔒 Security Features

- **Password Hashing**: Bcrypt with salt
- **JWT Tokens**: 8-hour expiration
- **Account Lockout**: 5 failed attempts → 15-minute lockout
- **Email Verification**: Required before login
- **Token Expiration**: 24 hours for verification and reset tokens
- **Input Validation**: Comprehensive validation on all endpoints

## 📁 Project Structure

```
devmons/
├── src/
│   ├── main/
│   │   ├── java/com/devmons/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Exception handlers
│   │   │   ├── repository/      # Data repositories
│   │   │   ├── security/        # Security components
│   │   │   ├── service/         # Business logic
│   │   │   └── DevmonsApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/com/devmons/
│       │   └── service/         # Service tests
│       └── resources/
│           └── application-test.yml
├── docs/                        # Documentation
├── pom.xml
└── README.md
```

## 📝 Development Guidelines

- Follow OOP principles and clean code practices
- Write unit tests for all business logic
- Use feature branches for development
- Squash commits before merging to main
- Document all public APIs

## 📄 License

This project is developed as part of a university course assignment.

## 👥 Author

Joonas Mägi

