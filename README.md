### Project: Collabu - A Code Collaboration Platform

#### **Description:**
The Code Collaboration Platform is a portfolio project designed to simulate a GitHub-like experience. It allows users to create repositories, manage files, simulate version control, and collaborate on code through features such as pull requests, inline comments, and merge functionalities. The platform combines robust backend services with a modern, user-friendly interface, offering real-time collaboration and analytics.

---

### **Project Components**

1. **User Management**:
   - User authentication and authorization (JWT-based).
   - Role-based access control (Admin, Contributor, Viewer).

2. **Repository Management**:
   - Create, edit, delete repositories.
   - File and directory structure management.
   - File upload and code editing with syntax highlighting.

3. **Version Control Simulation**:
   - Commit history tracking.
   - Branch creation and merging.
   - File comparison (diff view).

4. **Collaboration Tools**:
   - Pull requests and merge requests.
   - Inline comments for code review.
   - Real-time collaboration using WebSockets.

5. **Search and Filters**:
   - Repository and user search.
   - Filter repositories by programming language or activity.

6. **Analytics and Insights**:
   - Activity logs and contributor statistics.
   - Repository insights (commits, contributors, and more).

7. **Deployment**:
   - Hosting repositories and serving files.
   - Scalability using cloud services.

---

### **Scope**

- **Core Features**: Basic repository creation, file uploads, and user authentication.
- **Extended Features**: Version control simulation, pull requests, and analytics.
- **Real-time Features**: WebSocket-based collaboration for pull requests and comments.
- **Long-term Expansion**: Integration with external tools (e.g., CI/CD pipelines), API access for external clients, and mobile-friendly interfaces.

---

### **Technologies**

#### **Frontend**:
- **ReactJS**: For building the user interface.
- **Redux**: State management for handling user sessions and repository data.
- **ACE Editor or Monaco Editor**: To provide online code editing with syntax highlighting.
- **Material-UI or Tailwind CSS**: For responsive and aesthetic design.

#### **Backend**:
- **Spring Boot**: REST API development for managing backend services.
- **Hibernate**: ORM for database interactions.
- **Spring Security**: JWT-based authentication and role-based authorization.
- **WebSockets**: Enabling real-time collaboration features.

#### **Database**:
- **MySQL/PostgreSQL**: For storing user, repository, and version data.
- **Elasticsearch (Optional)**: To implement advanced search functionalities.

#### **File Storage**:
- **AWS S3 or Local Storage**: For storing repository files and data.

#### **Deployment**:
- **Docker**: To containerize the application.
- **AWS/GCP/Heroku**: For cloud-based deployment and scalability.

---

#### **Current Version:** v0.12

Repositories page enhanced and added new UI to add description and select public or private type. New individual Repository Page created for each repository

#### v0.11

Profile page UI further updated and added buttons for logout and delete account. Common header added so that it can be used to navigate this application.

#### v0.10

Profile page added, API endpoints for user modified and integrated, can change the user details and upload profile picture now

#### v0.09

Repositories and error pages added, API endpoints for them modified and integrated, navigation added after authenticating the user

#### v0.08

Mockito, JUnit test cases added for all methods in service classes

#### v0.07

Remaining service classes added and API endpoints added for all table entities in new controllers.

#### v0.06

Database tables modified and new tables added to enhance the application's storage, scope and functionality. Entity classes, DTOs, DAO interfaces, main service classes updated.

#### v0.05

Login, register, home and profile pages added in react with responsive UIs. Also tested the API integration by registering and logging in with new user credentials.

#### v0.04

Project structure changed now, two separate folders for front end and back end. Added basic files to test login using axios to call API from front end. Also added CorsConfig file for back end to ignore the cors error.

#### v0.03

Validation and exception handling added for APIs. Spring security used to add basic security for authentication and authorization to the application. JWT used for stateless authentication to call the APIs

#### v0.02

Tables created in database and entities classes, repositories, services and controllers added in the project. Swagger ui added for testing API endpoints.


#### v0.01

Initialized the backend project with Spring Boot and configured the database by creating a new user and database and tested the jdbc connection by running the application.
