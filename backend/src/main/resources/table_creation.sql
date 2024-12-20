CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `role` varchar(255) NOT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `bio` text,
  `location` varchar(100) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `last_login` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
);

CREATE TABLE `repositories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint NOT NULL,
  `visibility` enum('public','private') DEFAULT 'public',
  `forked_from_id` bigint DEFAULT NULL,
  `stars_count` bigint DEFAULT '0',
  `forks_count` bigint DEFAULT '0',
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `default_branch` varchar(255) DEFAULT 'main',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `repositories_ibfk_1` (`user_id`),
  KEY `forked_from_id` (`forked_from_id`),
  KEY `idx_popularity` (`stars_count`,`forks_count`),
  CONSTRAINT `repositories_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `repositories_ibfk_2` FOREIGN KEY (`forked_from_id`) REFERENCES `repositories` (`id`)
);

CREATE TABLE `files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `repository_id` bigint NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  `type` enum('file','directory') DEFAULT 'file',
  `size` bigint NOT NULL DEFAULT '0',
  `last_modified_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `repository_id` (`repository_id`),
  CONSTRAINT `files_ibfk_1` FOREIGN KEY (`repository_id`) REFERENCES `repositories` (`id`)
);

CREATE TABLE `commits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(255) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `repository_id` bigint NOT NULL,
  `branch_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `repository_id` (`repository_id`),
  KEY `branch_id` (`branch_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `commits_ibfk_1` FOREIGN KEY (`repository_id`) REFERENCES `repositories` (`id`),
  CONSTRAINT `commits_ibfk_2` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `commits_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `branches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `repository_id` bigint NOT NULL,
  `is_default` tinyint(1) DEFAULT '0',
  `last_commit_id` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `repository_id` (`repository_id`),
  CONSTRAINT `branches_ibfk_1` FOREIGN KEY (`repository_id`) REFERENCES `repositories` (`id`)
);

CREATE TABLE `pull_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `status` enum('open','closed','merged') DEFAULT 'open',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `repository_id` bigint NOT NULL,
  `source_branch_id` bigint NOT NULL,
  `target_branch_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `merged_at` timestamp NULL DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `repository_id` (`repository_id`),
  KEY `source_branch_id` (`source_branch_id`),
  KEY `target_branch_id` (`target_branch_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `pull_requests_ibfk_1` FOREIGN KEY (`repository_id`) REFERENCES `repositories` (`id`),
  CONSTRAINT `pull_requests_ibfk_2` FOREIGN KEY (`source_branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `pull_requests_ibfk_3` FOREIGN KEY (`target_branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `pull_requests_ibfk_4` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `activity_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `action` varchar(255) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `activity_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `stars` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `repository_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`repository_id`),
  KEY `repository_id` (`repository_id`),
  CONSTRAINT `stars_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `stars_ibfk_2` FOREIGN KEY (`repository_id`) REFERENCES `repositories` (`id`)
)

