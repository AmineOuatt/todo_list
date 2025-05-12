-- Database setup script for the To-Do List application

-- Create the database if it doesn't exist (uncomment and replace with your database name)
-- CREATE DATABASE IF NOT EXISTS poo_pr;
-- USE poo_pr;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Create tasks table with category relationship
CREATE TABLE IF NOT EXISTS tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    category_id INT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Create notes table with category relationship
CREATE TABLE IF NOT EXISTS notes (
    note_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    category_id INT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Insert default categories
INSERT IGNORE INTO categories (name) VALUES ('Work');
INSERT IGNORE INTO categories (name) VALUES ('Personal');
INSERT IGNORE INTO categories (name) VALUES ('Study');
INSERT IGNORE INTO categories (name) VALUES ('Health');
INSERT IGNORE INTO categories (name) VALUES ('Other');

-- Insert a test user (change the username and password as needed)
-- Default user: username = test, password = test
INSERT IGNORE INTO users (username, password) VALUES ('test', 'test'); 