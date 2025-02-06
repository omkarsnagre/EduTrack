package com.studentGradeTracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import java.util.Scanner;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

public class StudentGradeTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Unable to connect to the database.");
                return;
            }

            System.out.println("Connected to MySQL Database!");
            createTableIfNotExists(conn);

            while (true) {
                System.out.println("\n MENU:");
                System.out.println("1. Add Student Grade");
                System.out.println("2. View All Grades (Sorted)");
                System.out.println("3. Update Student Grade");
                System.out.println("4. Delete Student Record");
                System.out.println("5. Search Student");
                System.out.println("6. Generate Student Report");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addStudentGrade(conn, scanner);
                        break;
                    case 2:
                        viewAllGrades(conn);
                        break;
                    case 3:
                        updateStudentGrade(conn, scanner);
                        break;
                    case 4:
                        deleteStudentRecord(conn, scanner);
                        break;
                    case 5:
                        searchStudent(conn, scanner);
                        break;
                    case 6:
                        generateStudentReport(conn);
                        break;
                    case 7:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }

        scanner.close();
    }

    private static void createTableIfNotExists(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS grades (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_name VARCHAR(100) NOT NULL, " +
                "grade DOUBLE NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }
    private static void addStudentGrade(Connection conn, Scanner scanner) {
        while (true) {
            System.out.print("\nEnter Student Name: ");
            String studentName = scanner.nextLine().trim();

            System.out.print("Enter Grade (0-100): ");
            while (!scanner.hasNextDouble()) {
                System.out.println("Invalid input! Please enter a valid number.");
                scanner.next();
            }
            double grade = scanner.nextDouble();
            scanner.nextLine();

            if (grade < 0 || grade > 100) {
                System.out.println("Invalid grade! Must be between 0 and 100.");
                continue;
            }

            String sql = "INSERT INTO grades (student_name, grade) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studentName);
                pstmt.setDouble(2, grade);
                pstmt.executeUpdate();
                System.out.println("Grade added successfully!\n");
            } catch (SQLException e) {
                System.out.println("Error adding grade: " + e.getMessage());
            }

            while (true) {
                System.out.println("1. Add New");
                System.out.println("2. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();
                if (choice.equals("1")) break;
                if (choice.equals("2")) return;
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }



    private static void viewAllGrades(Connection conn) {
        String sql = "SELECT * FROM grades";  
        String statsSql = "SELECT MAX(grade) AS highest, MIN(grade) AS lowest, AVG(grade) AS average FROM grades";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nStudent Grades:");
            System.out.println("-------------------------------------------------");

            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Grade: %.2f\n",
                        rs.getInt("id"), rs.getString("student_name"), rs.getDouble("grade"));
            }

            System.out.println("-------------------------------------------------");
            try (Statement statsStmt = conn.createStatement();
                 ResultSet statsRs = statsStmt.executeQuery(statsSql)) {
                if (statsRs.next()) {
                    System.out.printf("Highest Grade: %.2f\n Lowest Grade: %.2f\n Average Grade: %.2f\n",
                            statsRs.getDouble("highest"), statsRs.getDouble("lowest"), statsRs.getDouble("average"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving grades: " + e.getMessage());
        }
    }



    private static void updateStudentGrade(Connection conn, Scanner scanner) {
        System.out.print("Enter Student ID to update: ");
        int id = scanner.nextInt();
        System.out.print("Enter new grade (0-100): ");
        double newGrade = scanner.nextDouble();
        scanner.nextLine();

        String sql = "UPDATE grades SET grade = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newGrade);
            pstmt.setInt(2, id);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(" Grade updated successfully!");
            } else {
                System.out.println("Student ID not found.");
            }
        } catch (SQLException e) {
            System.out.println(" Error updating grade: " + e.getMessage());
        }
    }

    private static void deleteStudentRecord(Connection conn, Scanner scanner) {
        System.out.print("Enter Student ID to delete: ");
        while (!scanner.hasNextInt()) {
            System.out.println(" Invalid input! Please enter a valid Student ID.");
            scanner.next();
        }
        int id = scanner.nextInt();
        scanner.nextLine();

        String sql = "DELETE FROM grades WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println(" Student record deleted successfully!");
            } else {
                System.out.println(" Student ID not found.");
            }
        } catch (SQLException e) {
            System.out.println(" Error deleting record: " + e.getMessage());
        }
    }

    private static void searchStudent(Connection conn, Scanner scanner) {
        System.out.print("Enter Student Name or ID: ");
        String searchInput = scanner.nextLine().trim();
        String sql;

        if (searchInput.matches("\\d+")) { // Check if input is a number (ID search)
            sql = "SELECT * FROM grades WHERE id = " + searchInput;
        } else { // Search by name
            sql = "SELECT * FROM grades WHERE student_name LIKE '%" + searchInput + "%'";
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Name: %s | Grade: %.2f\n",
                        rs.getInt("id"), rs.getString("student_name"), rs.getDouble("grade"));
            }
            if (!found) {
                System.out.println(" No student found.");
            }
        } catch (SQLException e) {
            System.out.println(" Error searching student: " + e.getMessage());
        }
    }




    private static void generateStudentReport(Connection conn) {
        String sql = "SELECT * FROM grades"; 
        String statsSql = "SELECT MAX(grade) AS highest, MIN(grade) AS lowest, AVG(grade) AS average FROM grades";

        try {
            System.out.println("\n Generating Student Report...");

            
            Document document = new Document();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "Student_Report_" + timestamp + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph(" Student Grades Report\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

           
            System.out.println("\n Student Grades:");
            System.out.println("-------------------------------------------------");

    
            PdfPTable table = new PdfPTable(3); 
            table.setWidthPercentage(100);
            table.addCell("ID");
            table.addCell("Student Name");
            table.addCell("Grade");

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("student_name");
                    double grade = rs.getDouble("grade");

                   
                    System.out.printf("ID: %d | Name: %s | Grade: %.2f\n", id, name, grade);

              
                    table.addCell(String.valueOf(id));
                    table.addCell(name);
                    table.addCell(String.format("%.2f", grade));
                }
            }

            System.out.println("-------------------------------------------------");
            document.add(table);

          
            try (Statement statsStmt = conn.createStatement();
                 ResultSet statsRs = statsStmt.executeQuery(statsSql)) {
                if (statsRs.next()) {
                    double highest = statsRs.getDouble("highest");
                    double lowest = statsRs.getDouble("lowest");
                    double average = statsRs.getDouble("average");

                   
                    System.out.printf(" Highest Grade: %.2f\n Lowest Grade: %.2f\n Average Grade: %.2f\n", highest, lowest, average);

                   
                    document.add(new Paragraph("\n-------------------------------------------------"));
                    document.add(new Paragraph(String.format(
                            " Highest Grade: %.2f\n Lowest Grade: %.2f\n Average Grade: %.2f",
                            highest, lowest, average
                    )));
                }
            }

            document.close();
            System.out.println(" Report saved successfully as '" + fileName + "'!");

        } catch (Exception e) {
            System.out.println(" Error generating PDF report: " + e.getMessage());
        }
    }

}
