# Java Best Practices in DevMons

This document explains how the DevMons project follows Java best practices and modern development standards.

## Table of Contents
1. [Architecture and Design Patterns](#architecture-and-design-patterns)
2. [Object-Oriented Programming Principles](#object-oriented-programming-principles)
3. [Code Quality and Maintainability](#code-quality-and-maintainability)
4. [Testing Strategy](#testing-strategy)
5. [Exception Handling](#exception-handling)
6. [Dependency Management](#dependency-management)
7. [Security Best Practices](#security-best-practices)

---

## Architecture and Design Patterns

### Layered Architecture
The application follows a **layered architecture** pattern, separating concerns into distinct layers:

```
Controller Layer → Service Layer → Repository Layer → Entity Layer
```

**Benefits:**
- Clear separation of concerns
- Easy to test each layer independently
- Maintainable and scalable codebase
- Follows Single Responsibility Principle

**Example:**
```java
@RestController  // Controller Layer
public class CommentController {
    private final CommentService commentService;  // Delegates to Service Layer
}

@Service  // Service Layer
public class CommentService {
    private final CommentRepository commentRepository;  // Delegates to Repository Layer
}

@Repository  // Repository Layer
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
```

### Repository Pattern
We use **Spring Data JPA** which implements the Repository pattern automatically.

**Benefits:**
- Abstraction over data access logic
- Reduces boilerplate code
- Type-safe queries
- Built-in CRUD operations

**Example:**
```java
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIssueOrderByCreatedAtAsc(Issue issue);
    long countByIssue(Issue issue);
}
```

### Service Layer Pattern
Business logic is encapsulated in **Service classes** annotated with `@Service`.

**Benefits:**
- Centralized business logic
- Reusable across multiple controllers
- Transactional boundaries
- Easy to test

**Example:**
```java
@Service
@RequiredArgsConstructor
public class CommentService {
    @Transactional
    public CommentResponse createComment(Long issueId, CreateCommentRequest request, String username) {
        // Business logic here
    }
}
```

### DTO (Data Transfer Object) Pattern
We use **DTOs** to transfer data between layers and to/from clients.

**Benefits:**
- Decouples internal entities from API contracts
- Prevents over-fetching/under-fetching
- Allows different representations for different use cases
- Security: doesn't expose internal entity structure

**Example:**
```java
@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long issueId;
    private AuthorInfo author;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEdited;
}
```

---

## Object-Oriented Programming Principles

### Encapsulation
- All entity fields are **private**
- Access through **getters/setters** (via Lombok `@Data`)
- Business logic encapsulated in service classes

**Example:**
```java
@Entity
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Private field
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;  // Private field
}
```

### Composition over Inheritance
We favor **composition** over inheritance throughout the codebase.

**Example:**
```java
@Service
@RequiredArgsConstructor  // Constructor injection via composition
public class CommentService {
    private final CommentRepository commentRepository;  // Composition
    private final IssueRepository issueRepository;      // Composition
    private final UserRepository userRepository;        // Composition
}
```

### Dependency Injection
We use **constructor-based dependency injection** (recommended by Spring).

**Benefits:**
- Immutable dependencies (final fields)
- Easy to test (can inject mocks)
- Clear dependencies
- Prevents circular dependencies

**Example:**
```java
@RestController
@RequiredArgsConstructor  // Lombok generates constructor
public class CommentController {
    private final CommentService commentService;  // Injected via constructor
}
```

### Polymorphism
JPA entities use polymorphism through **interfaces and abstract classes**.

**Example:**
```java
public interface JpaRepository<T, ID> extends Repository<T, ID> {
    // Common CRUD operations
}

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Specific comment operations
}
```

---

## Code Quality and Maintainability

### Lombok for Boilerplate Reduction
We use **Lombok** to reduce boilerplate code.

**Annotations used:**
- `@Data` - generates getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` - generates no-args constructor
- `@AllArgsConstructor` - generates all-args constructor
- `@Builder` - implements Builder pattern
- `@RequiredArgsConstructor` - generates constructor for final fields

**Benefits:**
- Less code to maintain
- Fewer bugs (generated code is consistent)
- More readable
- Focus on business logic

### Meaningful Names
- **Classes:** Nouns (e.g., `CommentService`, `CommentRepository`)
- **Methods:** Verbs (e.g., `createComment`, `deleteComment`)
- **Variables:** Descriptive (e.g., `commentResponse`, `issueId`)

### Immutability
- Service dependencies are **final** (immutable after construction)
- DTOs use **@Builder** for immutable object creation
- Method parameters are effectively final

### Code Documentation
- **JavaDoc comments** for public APIs
- **Inline comments** for complex business logic
- **Entity-level comments** explaining domain concepts

**Example:**
```java
/**
 * Create a new comment on an issue.
 * User must have access to the project.
 * 
 * @param issueId Issue ID
 * @param request Comment creation request
 * @param username Current authenticated user
 * @return Created comment
 * @throws IllegalArgumentException if issue not found or user has no access
 */
@Transactional
public CommentResponse createComment(Long issueId, CreateCommentRequest request, String username) {
    // Implementation
}
```

---

## Testing Strategy

### Unit Testing
We use **JUnit 5** and **Mockito** for unit testing.

**Test structure:**
- `@ExtendWith(MockitoExtension.class)` for Mockito support
- `@Mock` for mocked dependencies
- `@InjectMocks` for class under test
- **Arrange-Act-Assert** pattern

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    
    @InjectMocks
    private CommentService commentService;
    
    @Test
    void createComment_Success() {
        // Arrange
        when(commentRepository.save(any())).thenReturn(testComment);
        
        // Act
        CommentResponse result = commentService.createComment(1L, request, "user");
        
        // Assert
        assertNotNull(result);
        assertEquals("Test comment", result.getContent());
    }
}
```

### Test Coverage
- **Service layer:** Comprehensive unit tests
- **Controller layer:** Unit tests with mocked services
- **Edge cases:** Validation failures, permission checks, not found scenarios

### Test Naming Convention
```
methodName_scenario_expectedBehavior
```

**Examples:**
- `createComment_Success`
- `createComment_IssueNotFound`
- `deleteComment_NotAuthorOrOwner`

---

## Exception Handling

### Validation
- **Bean Validation** (`@Valid`, `@NotBlank`, `@NotNull`)
- Input validation at controller level
- Business rule validation at service level

**Example:**
```java
@Data
@Builder
public class CreateCommentRequest {
    @NotBlank(message = "Content is required")
    private String content;
}
```

### Business Logic Exceptions
- Use `IllegalArgumentException` for business rule violations
- Clear, descriptive error messages
- Consistent error handling across services

**Example:**
```java
if (!issue.getProject().isOwner(user) && 
    !projectMemberRepository.existsByProjectAndUser(issue.getProject(), user)) {
    throw new IllegalArgumentException("You do not have access to this issue");
}
```

---

## Dependency Management

### Maven
We use **Maven** for dependency management.

**Benefits:**
- Centralized dependency management
- Transitive dependency resolution
- Consistent builds
- Plugin ecosystem

### Spring Boot Starter Dependencies
We use **Spring Boot Starters** for common functionality:
- `spring-boot-starter-web` - Web applications
- `spring-boot-starter-data-jpa` - JPA/Hibernate
- `spring-boot-starter-security` - Security
- `spring-boot-starter-validation` - Bean Validation

**Benefits:**
- Curated dependencies
- Version compatibility guaranteed
- Minimal configuration

---

## Security Best Practices

### Authentication & Authorization
- **JWT-based authentication**
- **BCrypt password hashing**
- **Role-based access control**
- **Method-level security** in services

**Example:**
```java
// Permission check in service layer
if (!comment.getAuthor().getUsername().equals(username)) {
    throw new IllegalArgumentException("You can only edit your own comments");
}
```

### Input Validation
- **Bean Validation** at controller level
- **Business validation** at service level
- **SQL injection prevention** via JPA/Hibernate

### Sensitive Data
- Passwords are **never stored in plain text**
- JWT tokens have **expiration**
- Database credentials in **environment variables**

---

## Summary

The DevMons project follows Java best practices by:

1. ✅ **Layered Architecture** - Clear separation of concerns
2. ✅ **Design Patterns** - Repository, Service, DTO patterns
3. ✅ **OOP Principles** - Encapsulation, composition, dependency injection
4. ✅ **Code Quality** - Lombok, meaningful names, immutability
5. ✅ **Testing** - Comprehensive unit tests with JUnit 5 and Mockito
6. ✅ **Exception Handling** - Validation and clear error messages
7. ✅ **Dependency Management** - Maven and Spring Boot Starters
8. ✅ **Security** - JWT, BCrypt, input validation, permission checks

These practices ensure the codebase is **maintainable**, **testable**, **secure**, and **scalable**.

