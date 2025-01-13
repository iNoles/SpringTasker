# SpringTasker - Spring Boot & Kotlin 🚀

SpringTasker is a task management application built using Spring Boot and Kotlin. It supports both session-based authentication with CSRF protection for form-based logins and JWT-based authentication for API access. The application leverages H2 Database for persistence and follows clean, modular design principles for efficient task management.

## Features 🌟

### User Authentication & Authorization
- Session-based login/logout for UI users
- JWT-based authentication for API clients

### Managing Tasks
- Create, edit, and delete tasks
- Mark tasks as completed
- Set priority levels (Low, Medium, High)

### Security Enhancements
- CSRF protection for form logins
- Stateless JWT-based authentication for APIs

## Technologies Used 🛠️

- **Kotlin**: Primary language
- **Spring Boot**: Framework for rapid development
- **Spring Security**: Authentication and authorization
- **JWT (JSON Web Token)**: Secure API authentication
- **H2 Database**: Lightweight, embedded database
- **Thymeleaf**: Server-side template engine for UI
- **Gradle**: Build tool for dependency management

## Authentication Methods 🔐

| Endpoint                | Authentication Method           | Notes                                   |
|-------------------------|--------------------------------|-----------------------------------------|
| `/login`, `/register`   | Session-based with CSRF protection | For UI users                           |
| `/api/auth/*`           | JWT-based Authentication       | For API clients                        |
| `/logout`               | Invalidates session and JWT   | Supports both methods                  |

## Getting Started 💡

### Prerequisites

- JDK 17 or higher
- Gradle installed
- Your favorite IDE (IntelliJ IDEA recommended)

### Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/iNoles/SpringTasker.git
   cd SpringTasker
   ```
2. Build the project:
   ```bash
   ./gradlew build
   ```
3. Run the application:

```bash
  ./gradlew bootRun
```

4. Open your browser and navigate to:
   ```arduino
   http://localhost:8080
   ```
## API Endpoints 📡

### Authentication

| Method | Endpoint              | Description               |
|--------|-----------------------|---------------------------|
| POST   | `/api/auth/login`      | Login and receive JWT token |
| POST   | `/api/auth/register`   | Register a new user        |
| POST   | `/api/auth/logout`     | Invalidate JWT token       |

### Task Management

| Method   | Endpoint                   | Description             |
|----------|----------------------------|-------------------------|
| GET      | `/api/tasks/`             | Get all tasks by current user |
| POST     | `/api/tasks/add`          | Add a new task          |
| PUT      | `/api/tasks/{id}`         | Update a task           |
| DELETE   | `/api/tasks/{id}`         | Delete a task           |

## Roadmap 🛤️

Here’s what’s coming next:

1. Deployment
   - Deploy the project on a public site for easy access
2. Database Integration
   - Upgrade from H2 to a production-grade database (PostgreSQL or MySQL)

## Contributions 🤝

This project is a work in progress, and contributions are welcome! Feel free to:

- Submit issues for bugs or feature suggestions
- Open pull requests to contribute directly
