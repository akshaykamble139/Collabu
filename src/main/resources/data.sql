INSERT INTO users (id, username, email, password) VALUES
(1, 'john_doe', 'john@example.com', 'password123'),
(2, 'jane_smith', 'jane@example.com', 'password456'),
(3, 'alice_brown', 'alice@example.com', 'password789');

INSERT INTO repositories (id, name, description, user_id) VALUES
(1, 'my-first-repo', 'This is my first repository.', 1),
(2, 'backend-api', 'Repository for backend API development.', 2),
(3, 'frontend-ui', 'Repository for frontend UI components.', 3),
(4, 'shared-library', 'Repository for shared library utilities.', 1);

INSERT INTO files (id, name, content, repository_id) VALUES
(1, 'README.md', '# My First Repo\nWelcome to my first repo!', 1),
(2, 'app.js', 'console.log("Hello, World!");', 2),
(3, 'index.html', '<!DOCTYPE html><html><head><title>My Project</title></head><body></body></html>', 3),
(4, 'utils.py', 'def greet():\n    return "Hello, Python!"', 4),
(5, 'LICENSE', 'MIT License', 1),
(6, 'server.js', 'const express = require("express");', 2);
