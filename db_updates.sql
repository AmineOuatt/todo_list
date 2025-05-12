-- 1. Create the 'categories' table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Add a 'category_id' column to the 'tasks' table (nullable)
ALTER TABLE tasks
ADD COLUMN IF NOT EXISTS category_id INT NULL;

-- 3. Create a foreign key relationship from tasks to categories
ALTER TABLE tasks
ADD CONSTRAINT fk_task_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ON DELETE SET NULL;

-- 4. Insert some default categories
INSERT IGNORE INTO categories (name) VALUES ('Work');
INSERT IGNORE INTO categories (name) VALUES ('Personal');
INSERT IGNORE INTO categories (name) VALUES ('Study');
INSERT IGNORE INTO categories (name) VALUES ('Health');
INSERT IGNORE INTO categories (name) VALUES ('Other');

-- 5. Create the 'notes' table with category relationship
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

-- Add the subtasks table
CREATE TABLE IF NOT EXISTS subtasks (
    subtask_id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_subtasks_task_id ON subtasks(task_id); 