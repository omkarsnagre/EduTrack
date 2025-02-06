-- Create the database
CREATE DATABASE StudentGradesDB;

-- Use the database
USE StudentGradesDB;

-- Create the grades table
CREATE TABLE grades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    grade DOUBLE NOT NULL
);
