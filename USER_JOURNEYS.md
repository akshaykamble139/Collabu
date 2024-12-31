# Collabu User Journeys (Core Features)

This document outlines comprehensive user journeys for core functionalities of Collabu, a GitHub-clone application. It covers user registration, repository management, file handling, and branch management.

---

## 1. User Registration and Authentication

### User Registration
**Steps:**
1. **Frontend:**
   - User fills out the registration form (username, email, password).
   - Form validation checks:
     - Password strength (minimum 8 characters, special symbols).
     - Unique username and email.
     - Proper email format.
   - Sends a POST request to `/api/auth/register`.
2. **Backend:**
   - Validates inputs:
     - Checks for duplicate username/email in the `users` table.
     - Ensures password policy compliance.
   - Returns errors for:
     - Duplicate data or weak password.
   - Hashes the password securely (e.g., Bcrypt, Argon2).
   - Inserts user into the `users` table with `is_active = true`.
   - Sends an optional verification email with a token.
3. **Response:**
   - Success message with user ID returned.
   - If errors, returns appropriate error messages.

---

### User Login
**Steps:**
1. **Frontend:**
   - User enters email and password.
   - Sends POST request to `/api/auth/login`.
2. **Backend:**
   - Validates:
     - Checks if user exists in `users` table.
     - Compares hashed password.
   - Handles errors:
     - Returns "Invalid credentials" if incorrect.
     - If inactive, sends reactivation email.
   - On success:
     - Generates JWT token.
     - Updates `last_login` timestamp.
3. **Response:**
   - JWT token and user profile data.

---

## 2. Repository Management

### Repository Creation
**Steps:**
1. **Frontend:**
   - User clicks "New Repository."
   - Fills in name, description, and visibility (public/private).
   - Sends POST request to `/api/repositories/create`.
2. **Backend:**
   - Validates:
     - Unique repository name under the user.
     - Ensures name is alphanumeric.
   - Inserts repository into the `repositories` table.
   - Creates a `main` branch in `branches` table.
   - Logs activity in `activity_logs`.
3. **Response:**
   - Returns repository ID and details.

---

### Repository Deletion
**Steps:**
1. **Frontend:**
   - User selects "Delete" and confirms.
   - Sends DELETE request to `/api/repositories/{id}`.
2. **Backend:**
   - Marks repository `is_deleted = true` (soft delete).
   - Cascades to `files`, `branches`.
   - Updates logs.
3. **Response:**
   - Returns deletion success.

---

## 3. File Management

### File Upload
**Steps:**
1. **Frontend:**
   - User selects and uploads file.
   - Sends POST to `/api/files/upload`.
2. **Backend:**
   - Validates:
     - Checks for max size and allowed types.
   - Uploads to S3 (`/repository-id/branch/file`).
   - Adds entry to `files` table.
   - Logs upload.
3. **Response:**
   - Returns file URL and metadata.

---

### File Versioning
**Steps:**
1. **Frontend:**
   - User modifies a file and clicks "Save."
   - Sends POST request to `/api/files/version`.
2. **Backend:**
   - Saves new version to S3.
   - Updates `file_versions` table.
   - Creates commit entry.
3. **Response:**
   - Returns version ID and commit hash.

---

## 4. Branch Management

### Create a Branch
**Steps:**
1. **Frontend:**
   - User clicks "Create Branch."
   - Enters branch name.
   - Sends POST to `/api/branches/create`.
2. **Backend:**
   - Duplicates latest commit.
   - Inserts into `branches`.
   - Associates branch with the latest file state.
3. **Response:**
   - Returns branch ID.

---

### Merge Branches
**Steps:**
1. **Frontend:**
   - User selects source and target branches.
   - Clicks "Merge."
   - Sends POST to `/api/branches/merge`.
2. **Backend:**
   - Checks for merge conflicts.
   - Merges or returns conflict data.
   - Updates files, creates merge commit.
3. **Response:**
   - Merge success or conflict details.

---

## 5. Pull Requests

### Create Pull Request
**Steps:**
1. **Frontend:**
   - User clicks "New Pull Request."
   - Selects source and target branches.
   - Submits request.
   - Sends POST to `/api/pull-requests/create`.
2. **Backend:**
   - Validates:
     - Ensures target branch exists.
   - Inserts into `pull_requests`.
   - Logs action.
3. **Response:**
   - PR details and ID returned.

---

### Merge Pull Request
**Steps:**
1. **Frontend:**
   - User clicks "Merge" on PR.
   - Sends POST to `/api/pull-requests/merge`.
2. **Backend:**
   - Merges or returns conflicts.
   - Updates `commits`, `files`, and `branches`.
3. **Response:**
   - Returns merge success or conflict status.
   
---

## 6. User Profile Management

### View Profile
**Steps:**
1. **Frontend:**
   - User navigates to profile page.
   - Sends GET request to `/api/users/profile`.
2. **Backend:**
   - Retrieves:
     - User info, bio, repositories.
3. **Response:**
   - Profile data returned.

---

### Edit Profile
**Steps:**
1. **Frontend:**
   - User edits bio or uploads picture.
   - Sends PUT to `/api/users/update-profile`.
2. **Backend:**
   - Updates `users` table.
3. **Response:**
   - Returns updated profile.

---

## 7. Star Management

### Star Repository
**Steps:**
1. **Frontend:**
   - User stars a repository.
   - Sends POST to `/api/repositories/star`.
2. **Backend:**
   - Adds to `stars` table.
3. **Response:**
   - Updated star count.

---

## 8. Notifications

### Subscribe to Notifications
**Steps:**
1. **Frontend:**
   - User enables notifications.
   - Sends POST to `/api/notifications/subscribe`.
2. **Backend:**
   - Adds to `notifications` table.
3. **Response:**
   - Subscription confirmed.

---

## 9. Advanced Search

### Search Repositories
**Steps:**
1. **Frontend:**
   - User searches repositories.
   - Sends GET to `/api/search/repositories`.
2. **Backend:**
   - Queries repositories.
3. **Response:**
   - Results returned.

