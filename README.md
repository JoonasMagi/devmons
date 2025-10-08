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

#### User Story #3: Team Member Management
- Invite users to projects via email
- Email notifications for invitations
- Accept/decline project invitations
- View pending invitations
- Cancel pending invitations
- Assign roles to team members (Owner, Member, Viewer)
- Role-based access control (RBAC)
- Update member roles
- Remove members from projects
- Enforce at least one owner per project

#### User Story #4: Issue/Ticket Management
- Create issues with unique keys (PROJECT-123)
- Auto-increment issue numbers per project
- Select issue type (Story, Bug, Task, Epic)
- Set priority levels (Low, Medium, High, Critical)
- Assign issues to team members
- Add story points estimates
- Set due dates with overdue detection
- Add multiple labels to issues
- Edit all issue fields inline
- Change issue status via workflow states
- Markdown support in descriptions
- Complete change history tracking
- Activity timeline for all changes
- Reporter automatically set to creator

#### User Story #25: Login and Registration UI
- Login page with form validation
- Registration page with password strength validation
- JWT token management
- Protected routes with auto-redirect
- Responsive design

#### User Story #26: Dashboard and Project Overview UI
- Responsive project cards grid (1/2/3 columns)
- Project creation modal with validation
- Loading skeleton states
- Collapsible sidebar navigation
- User menu with profile, settings, logout
- Keyboard shortcuts (Cmd/Ctrl + B)
- Active route highlighting

#### User Story #27: Kanban Board UI
- Board visualization with workflow columns
- Drag-and-drop cards between columns (@dnd-kit)
- Issue cards with type icons, priority indicators, labels
- Assignee avatars and story points
- Search by title/key
- Filter by assignee, label, type, priority
- Keyboard navigation (/, c)
- Toast notifications for updates
- Optimistic UI updates

#### User Story #28: Issue Detail Modal UI
- Modal opens when clicking issue card
- Full-screen on mobile, centered on desktop
- Close button (X) and Esc key support
- Click outside to close with unsaved changes confirmation
- Smooth open/close animations
- URL updates with issue key (shareable links)
- Editable title with inline auto-save
- Status dropdown (workflow states)
- Priority dropdown with color indicators
- Markdown editor for description with preview/edit toggle
- Story points input
- Due date picker with overdue indicator
- Labels display with color chips
- Reporter display (read-only)
- Activity timeline with change history
- Copy link button
- Delete button with confirmation
- Keyboard shortcuts (e, Esc)
- Toast notifications for all actions

#### User Story #7: Add Comments and Collaborate on Issues
- Add comments to issues with Markdown support
- Edit and delete own comments
- Activity timeline showing comments and system activities
- Filter timeline (all activity / comments only)
- Auto-scroll to latest activity
- @mention autocomplete for team members
- Mention highlighting in rendered comments
- Real-time notifications for mentions
- Notification bell with unread count badge
- Click notification to navigate to issue
- Mark notifications as read
- Real-time updates via WebSocket (STOMP over SockJS)
- Automatic reconnection with heartbeat
- 39 unit tests (CommentService, MentionService, NotificationService)

### 🚧 Planned Features
- Assignee selector with search
- Labels multi-select editor
- Sprint planning and management
- Email notifications for mentions
- File attachments in comments
- Reporting and analytics

## 🛠️ Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access
- **Spring WebSocket** - Real-time updates with STOMP
- **PostgreSQL 15** - Primary database
- **JWT** - Token-based authentication
- **Maven** - Build tool

### Frontend
- **React 18** - UI library
- **TypeScript 5** - Type safety
- **Vite** - Build tool with Rolldown
- **Tailwind CSS v3** - Utility-first CSS framework
- **React Router** - Client-side routing
- **React Query** - Server state management
- **Zustand** - Client state management
- **Axios** - HTTP client
- **@dnd-kit** - Drag-and-drop functionality
- **React Hot Toast** - Toast notifications
- **React Hook Form** - Form management
- **Lucide React** - Icon library
- **Headless UI** - Accessible UI components
- **React Markdown** - Markdown rendering with remark-gfm
- **React DatePicker** - Date picker component
- **SockJS Client** - WebSocket client with fallback
- **@stomp/stompjs** - STOMP protocol for WebSocket

### Testing
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **H2** - In-memory database for tests

## 📋 Prerequisites

### Backend
- Java 17 or higher
- Maven 3.9 or higher
- PostgreSQL 15
- SMTP server (for email functionality)

### Frontend
- Node.js 20 LTS or higher
- npm or yarn

## 🚀 Getting Started

### Backend Setup

#### 1. Clone the repository
```bash
git clone https://github.com/JoonasMagi/devmons.git
cd devmons
```

#### 2. Set up PostgreSQL database
```sql
CREATE DATABASE devmons;
CREATE USER devmons WITH PASSWORD 'devmons';
GRANT ALL PRIVILEGES ON DATABASE devmons TO devmons;
```

#### 3. Configure application properties
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

#### 4. Set JWT secret (production)
```bash
export JWT_SECRET=your-secure-secret-key-minimum-256-bits
```

#### 5. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

#### 1. Navigate to frontend directory
```bash
cd frontend
```

#### 2. Install dependencies
```bash
npm install
```

#### 3. Configure environment
```bash
cp .env.example .env
```

Edit `.env` and set your API URL:
```
VITE_API_URL=http://localhost:8080/api
```

#### 4. Start development server
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## 🧪 Running Tests

### Backend Tests
```bash
mvn test
```

### Frontend Tests
```bash
cd frontend
npm run test
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

### Team Management API

#### Invite Member to Project
```http
POST /api/projects/{projectId}/members/invite
Content-Type: application/json
Authorization: Bearer {token}

{
  "email": "user@example.com",
  "role": "MEMBER"
}
```

**Roles:** `OWNER`, `MEMBER`, `VIEWER`

**Note:** Only project owner can invite members.

Response:
```json
{
  "id": 1,
  "projectId": 1,
  "projectName": "My Project",
  "email": "user@example.com",
  "role": "MEMBER",
  "status": "PENDING",
  "invitedByUsername": "johndoe",
  "createdAt": "2025-10-07T21:00:00",
  "expiresAt": "2025-10-14T21:00:00",
  "expired": false
}
```

#### Get Pending Invitations
```http
GET /api/projects/{projectId}/invitations
Authorization: Bearer {token}
```

**Note:** Only project owner can view invitations.

#### Cancel Invitation
```http
DELETE /api/invitations/{invitationId}
Authorization: Bearer {token}
```

**Note:** Only project owner can cancel invitations.

#### Accept Invitation
```http
POST /api/invitations/accept?token={invitationToken}
Authorization: Bearer {token}
```

User's email must match the invitation email.

Response:
```json
{
  "id": 2,
  "userId": 2,
  "username": "janedoe",
  "email": "jane@example.com",
  "fullName": "Jane Doe",
  "role": "MEMBER",
  "joinedAt": "2025-10-07T21:00:00"
}
```

#### Get Project Members
```http
GET /api/projects/{projectId}/members
Authorization: Bearer {token}
```

Returns list of all project members. Any project member can view the list.

#### Update Member Role
```http
PUT /api/projects/{projectId}/members/{memberId}/role
Content-Type: application/json
Authorization: Bearer {token}

{
  "role": "VIEWER"
}
```

**Note:** Only project owner can change roles. At least one owner must remain.

#### Remove Member
```http
DELETE /api/projects/{projectId}/members/{memberId}
Authorization: Bearer {token}
```

**Note:** Only project owner can remove members. Cannot remove the last owner.

### Issue Management API

#### Create Issue
```http
POST /api/projects/{projectId}/issues
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "Implement user login",
  "description": "Add JWT-based authentication",
  "issueTypeId": 1,
  "priority": "HIGH",
  "assigneeId": 2,
  "storyPoints": 5,
  "dueDate": "2025-10-15",
  "labelIds": [1, 2]
}
```

**Priority values:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

Response:
```json
{
  "id": 1,
  "key": "PROJ-1",
  "number": 1,
  "title": "Implement user login",
  "description": "Add JWT-based authentication",
  "projectId": 1,
  "projectName": "My Project",
  "projectKey": "PROJ",
  "issueTypeId": 1,
  "issueTypeName": "Story",
  "issueTypeIcon": "📖",
  "issueTypeColor": "#0052CC",
  "workflowStateId": 1,
  "workflowStateName": "Backlog",
  "workflowStateTerminal": false,
  "priority": "HIGH",
  "reporterId": 1,
  "reporterUsername": "johndoe",
  "reporterFullName": "John Doe",
  "assigneeId": 2,
  "assigneeUsername": "janedoe",
  "assigneeFullName": "Jane Doe",
  "storyPoints": 5,
  "dueDate": "2025-10-15",
  "overdue": false,
  "labels": [
    {
      "id": 1,
      "name": "backend",
      "color": "#FF0000"
    }
  ],
  "createdAt": "2025-10-07T21:00:00",
  "updatedAt": "2025-10-07T21:00:00"
}
```

#### Get Issue by ID
```http
GET /api/issues/{id}
Authorization: Bearer {token}
```

#### Get Issue by Key
```http
GET /api/issues/key/{key}
Authorization: Bearer {token}
```

Example: `GET /api/issues/key/PROJ-123`

#### Get All Project Issues
```http
GET /api/projects/{projectId}/issues
Authorization: Bearer {token}
```

Returns list of all issues for the project, ordered by number descending.

#### Update Issue
```http
PUT /api/issues/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "Updated title",
  "description": "Updated description",
  "issueTypeId": 2,
  "workflowStateId": 3,
  "priority": "CRITICAL",
  "assigneeId": 3,
  "storyPoints": 8,
  "dueDate": "2025-10-20",
  "labelIds": [1, 3]
}
```

**Note:** All fields are optional. Only provided fields will be updated. All changes are tracked in history.

#### Get Issue History
```http
GET /api/issues/{id}/history
Authorization: Bearer {token}
```

Returns activity timeline showing all changes made to the issue.

Response:
```json
[
  {
    "id": 1,
    "fieldName": "status",
    "oldValue": "To Do",
    "newValue": "In Progress",
    "changedByUsername": "johndoe",
    "changedByFullName": "John Doe",
    "changedAt": "2025-10-07T21:30:00"
  }
]
```

### Comments API

#### Create Comment
```http
POST /api/issues/{issueId}/comments
Content-Type: application/json
Authorization: Bearer {token}

{
  "content": "This is a comment with @username mention"
}
```

Response:
```json
{
  "id": 1,
  "issueId": 1,
  "content": "This is a comment with @username mention",
  "authorId": 1,
  "authorUsername": "johndoe",
  "authorFullName": "John Doe",
  "createdAt": "2025-10-08T10:00:00",
  "updatedAt": "2025-10-08T10:00:00",
  "edited": false
}
```

#### Get Issue Comments
```http
GET /api/issues/{issueId}/comments
Authorization: Bearer {token}
```

Returns list of all comments for the issue, ordered by creation date.

#### Update Comment
```http
PUT /api/comments/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "content": "Updated comment content"
}
```

**Note:** Only comment author can update their own comments.

#### Delete Comment
```http
DELETE /api/comments/{id}
Authorization: Bearer {token}
```

**Note:** Only comment author can delete their own comments.

### Notifications API

#### Get User Notifications
```http
GET /api/notifications
Authorization: Bearer {token}
```

Returns list of all notifications for the authenticated user, ordered by creation date descending.

Response:
```json
[
  {
    "id": 1,
    "type": "MENTION",
    "message": "johndoe mentioned you in PROJ-123",
    "issueId": 1,
    "issueKey": "PROJ-123",
    "read": false,
    "createdAt": "2025-10-08T10:00:00"
  }
]
```

#### Get Unread Count
```http
GET /api/notifications/unread-count
Authorization: Bearer {token}
```

Response:
```json
{
  "count": 5
}
```

#### Mark Notification as Read
```http
POST /api/notifications/{id}/read
Authorization: Bearer {token}
```

#### Mark All Notifications as Read
```http
POST /api/notifications/read-all
Authorization: Bearer {token}
```

### WebSocket API

#### Connect to WebSocket
```javascript
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  Authorization: `Bearer ${token}`
}, () => {
  // Subscribe to issue updates
  stompClient.subscribe('/topic/issues/123', (message) => {
    const data = JSON.parse(message.body);
    console.log('Received:', data);
  });

  // Subscribe to user notifications
  stompClient.subscribe('/topic/users/johndoe/notifications', (message) => {
    const data = JSON.parse(message.body);
    console.log('Notification:', data);
  });
});
```

**Message Types:**
- `COMMENT_ADDED` - New comment added to issue
- `COMMENT_UPDATED` - Comment edited
- `COMMENT_DELETED` - Comment deleted
- `ISSUE_UPDATED` - Issue fields updated
- `NOTIFICATION_CREATED` - New notification for user

## 🔒 Security Features

- **Password Hashing**: Bcrypt with salt
- **JWT Tokens**: 8-hour expiration
- **Account Lockout**: 5 failed attempts → 15-minute lockout
- **Email Verification**: Required before login
- **Token Expiration**: 24 hours for verification/reset, 7 days for invitations
- **Input Validation**: Comprehensive validation on all endpoints
- **Role-Based Access Control**: Owner, Member, and Viewer roles with enforced permissions

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

