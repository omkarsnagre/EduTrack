package com.studentGradeTracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/StudentGradesDB";
    private static final String DB_USER = "root"; // Change to your MySQL username
    private static final String DB_PASSWORD = "root"; // Change to your MySQL password

    public static Connection getConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver NOT found!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("❌ Failed to connect to MySQL database!");
            e.printStackTrace();
            return null;
        }
    }
}
