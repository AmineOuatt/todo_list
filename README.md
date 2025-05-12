# Java To-Do List App (MVC)

This repository contains a Java To-Do List application built using the Model-View-Controller (MVC) architecture.

## Recent Updates

The application has been enhanced with the following changes:

### UI Updates
- All windows now open in fullscreen mode for better user experience
- Simplified interface focusing on essential functionality
- More consistent layout across different views

### Database Changes
- Added a `categories` table to store task categories
- Added a foreign key relationship between tasks and categories
- Added a `notes` table with category relationships
- Applied SQL updates can be found in the `db_updates.sql` file

### New Features
- **Notes System**: Added a notes feature allowing users to create, edit, and delete notes
- Notes can be categorized using the same categories as tasks
- Notes view includes filtering by category
- Both notes and tasks use categories for better organization

### Model Changes
- Added a `Category` class to represent task categories
- Added a `Note` class for the notes functionality
- Modified the `Task` model to include a reference to the `Category` class
- Added corresponding DAO classes for database operations

### Controller Changes
- Added controllers for managing categories and notes
- Implemented CRUD operations for notes with category support
- Updated the task controllers to handle categorization

## Setup

1. Ensure you have MySQL installed and running
2. Execute the `setup_database.sql` script to set up your database schema
   - This creates all necessary tables and relationships
   - Adds default categories and a test user
3. Configure the database connection in `src/Model/DatabaseConnection.java`
4. Compile and run the application

## Database Schema

The application uses the following tables:
- `users`: Stores user information
- `tasks`: Stores task information with a reference to categories
- `categories`: Stores category information for tasks and notes
- `notes`: Stores notes with references to users and categories

## Features

- Task management with CRUD operations
- Task categorization
- Notes system with category support
- Pomodoro timer for productivity
- Dashboard with task statistics
- Multiple user support
- Fullscreen user interface
