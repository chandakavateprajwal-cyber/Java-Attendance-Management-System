import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;

public class AttendanceSystem {

    static final String ACCOUNT_FILE = "student_accounts.csv";
    static final String ADMIN_USER = "admin";
    static final String ADMIN_PASS = "1234";
    static final String TEACHER_USER = "teacher";
    static final String TEACHER_PASS = "teach123";

    public static void main(String[] args) {
        while (true) {
            String[] options = {"Admin Login", "Teacher Login", "Student Signup", "Student Login", "Exit"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Choose an option",
                    "Attendance Management System",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            switch (choice) {
                case 0 -> adminLogin();
                case 1 -> teacherLogin();
                case 2 -> studentSignup(); // Updated signup
                case 3 -> studentLogin();
                case 4 -> System.exit(0);
                default -> JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    // -------------------- ADMIN SECTION --------------------
    static void adminLogin() {
        String user = JOptionPane.showInputDialog("Enter Admin Username:");
        String pass = JOptionPane.showInputDialog("Enter Admin Password:");

        if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
            JOptionPane.showMessageDialog(null, "Login successful!");
            adminMenu();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid Admin credentials!");
        }
    }

    static void adminMenu() {
        while (true) {
            String[] options = {"Add Student", "View All Students", "Delete Student", "Reset Attendance Files", "Export Student Data", "Back"};
            int choice = JOptionPane.showOptionDialog(null, "Admin Menu", "Admin Control",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            switch (choice) {
                case 0 -> addStudent();
                case 1 -> viewAllStudents();
                case 2 -> deleteStudent();
                case 3 -> resetAttendanceFiles();
                case 4 -> exportStudentData();
                case 5 -> { return; }
                default -> JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    static void addStudent() {
    try (FileWriter fw = new FileWriter(ACCOUNT_FILE, true)) {

        String username = JOptionPane.showInputDialog("Enter Student Username:");
        if (username == null) return;

        String password = JOptionPane.showInputDialog("Enter Student Password:");
        if (password == null) return;

        String branch = JOptionPane.showInputDialog("Enter Branch:");
        if (branch == null) return;

        String sem = JOptionPane.showInputDialog("Enter Semester:");
        if (sem == null) return;

        String roll = JOptionPane.showInputDialog("Enter Roll No:");
        if (roll == null) return;

        // Add to student_accounts.csv
        fw.write(username + "," + password + "," + branch + "," + sem + "," + roll + "\n");

        // Add student to all existing attendance files of same branch & sem
        File folder = new File(".");
        File[] files = folder.listFiles((dir, name) ->
                name.startsWith(branch + sem) && name.endsWith("_Teacher.csv"));

        if (files != null) {
            for (File file : files) {
                addStudentToAttendanceFile(file, roll);
            }
        }

        JOptionPane.showMessageDialog(null, "Student added successfully!");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error adding student!");
    }
}
static void addStudentToAttendanceFile(File file, String roll) {
    try {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) return;

            lines.add(header);
            int dateCount = header.split(",").length - 4;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(roll + ",")) {
                    return; // already exists
                }
                lines.add(line);
            }

            StringBuilder newRow = new StringBuilder(roll);
            for (int i = 0; i < dateCount; i++) {
                newRow.append(",-");
            }
            newRow.append(",").append(dateCount).append(",0,0.00%");

            lines.add(newRow.toString());
        }

        try (FileWriter fw = new FileWriter(file)) {
            for (String l : lines) {
                fw.write(l + "\n");
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


    static void viewAllStudents() {
        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String[] columns = {"Username", "Password", "Branch", "Semester", "Roll No"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            String line;
            while ((line = br.readLine()) != null) {
                model.addRow(line.split(","));
            }
            JTable table = new JTable(model);
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "All Students", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No student data found!");
        }
    }

    static void deleteStudent() {
    try {
        String branchToDelete = JOptionPane.showInputDialog("Enter Branch:");
        if (branchToDelete == null) return;

        String semToDelete = JOptionPane.showInputDialog("Enter Semester:");
        if (semToDelete == null) return;

        String rollToDelete = JOptionPane.showInputDialog("Enter Roll No:");
        if (rollToDelete == null) return;

        // ---------- DELETE FROM student_accounts.csv ----------
        File inputFile = new File(ACCOUNT_FILE);
        File tempFile = new File("temp.csv");
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             FileWriter fw = new FileWriter(tempFile)) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts[2].equalsIgnoreCase(branchToDelete)
                        && parts[3].equalsIgnoreCase(semToDelete)
                        && parts[4].equalsIgnoreCase(rollToDelete)) {
                    found = true;
                    continue; // skip student
                }
                fw.write(line + "\n");
            }
        }

        inputFile.delete();
        tempFile.renameTo(inputFile);

        if (!found) {
            JOptionPane.showMessageDialog(null, "Student not found!");
            return;
        }

        // ---------- DELETE FROM ALL ATTENDANCE FILES ----------
        File folder = new File(".");
        File[] files = folder.listFiles((dir, name) ->
                name.startsWith(branchToDelete + semToDelete)
                        && name.endsWith("_Teacher.csv"));

        if (files != null) {
            for (File file : files) {
                removeStudentFromAttendanceFile(file, rollToDelete);
            }
        }

        JOptionPane.showMessageDialog(null,
                "Student deleted successfully from ALL records!\n" +
                "Branch: " + branchToDelete +
                "\nSemester: " + semToDelete +
                "\nRoll No: " + rollToDelete);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error deleting student!");
        e.printStackTrace();
    }
}

static void removeStudentFromAttendanceFile(File file, String roll) {
    try {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(roll + ",")) {
                    lines.add(line);
                }
            }
        }

        try (FileWriter fw = new FileWriter(file)) {
            for (String l : lines) {
                fw.write(l + "\n");
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
static void resetAttendanceFiles() {
        File folder = new File(".");
        String[] options = {"Delete Specific File", "Delete All Files", "Cancel"};
        int choice = JOptionPane.showOptionDialog(null,
                "Choose an option:",
                "Reset Attendance Files",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) { // Delete specific file
            File[] files = folder.listFiles((dir, name) -> name.endsWith("_Teacher.csv"));
            if (files == null || files.length == 0) {
                JOptionPane.showMessageDialog(null, "No attendance files found!");
                return;
            }

            String[] fileNames = Arrays.stream(files).map(File::getName).toArray(String[]::new);
            String fileToDelete = (String) JOptionPane.showInputDialog(
                    null,
                    "Select a file to delete:",
                    "Delete Specific File",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    fileNames,
                    fileNames[0]);

            if (fileToDelete != null) {
                File file = new File(fileToDelete);
                if (file.delete()) {
                    JOptionPane.showMessageDialog(null, fileToDelete + " deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Error deleting file!");
                }
            }

        } else if (choice == 1) { // Delete all files
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete ALL attendance files?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                for (File file : Objects.requireNonNull(folder.listFiles())) {
                    if (file.getName().endsWith("_Teacher.csv")) {
                        file.delete();
                    }
                }
                JOptionPane.showMessageDialog(null, "All attendance files have been reset!");
            } else {
                JOptionPane.showMessageDialog(null, "Operation cancelled.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Operation cancelled.");
        }
    }

    static void exportStudentData() {
        try {
            File inputFile = new File(ACCOUNT_FILE);
            if (!inputFile.exists()) {
                JOptionPane.showMessageDialog(null, "No student data found!");
                return;
            }
            String exportFileName = "exported_students_" + new Date().getTime() + ".csv";
            try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
                 FileWriter fw = new FileWriter(exportFileName)) {
                String line;
                while ((line = br.readLine()) != null) {
                    fw.write(line + "\n");
                }
            }
            JOptionPane.showMessageDialog(null, "Student data exported successfully to " + exportFileName);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error exporting student data!");
        }
    }

    // -------------------- TEACHER SECTION --------------------
    static void teacherLogin() {
        String username = JOptionPane.showInputDialog("Enter Teacher Username:");
        if (username == null) return;

        String password = JOptionPane.showInputDialog("Enter Teacher Password:");
        if (password == null) return;

        if (TEACHER_USER.equals(username) && TEACHER_PASS.equals(password)) {
            String branch = JOptionPane.showInputDialog("Enter Branch:");
            if (branch == null) return;

            String sem = JOptionPane.showInputDialog("Enter Semester:");
            if (sem == null) return;

            String subject = JOptionPane.showInputDialog("Enter Subject:");
            if (subject == null) return;

            teacherMenu(branch, sem, subject);
        } else {
            JOptionPane.showMessageDialog(null, "Invalid Teacher credentials!");
        }
    }

    static void teacherMenu(String branch, String sem, String subject) {
        while (true) {
            String[] options = {"Take Attendance", "View Attendance", "Back"};
            int choice = JOptionPane.showOptionDialog(null, "Teacher Menu", "Teacher Controls",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            String fileName = branch + "" + sem + "" + subject + "_Teacher.csv";

            switch (choice) {
                case 0 -> takeAttendance(fileName);
                case 1 -> viewAttendance(fileName);
                case 2 -> { return; }
                default -> JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    // -------------------- takeAttendance with totals --------------------
    static void takeAttendance(String fileName) {
        try {
            int totalStudents = Integer.parseInt(JOptionPane.showInputDialog("Enter total students:"));
            String date = JOptionPane.showInputDialog("Enter Date (yyyy-MM-dd):");

            File file = new File(fileName);
            Map<String, List<String>> data = new LinkedHashMap<>();
            List<String> dates = new ArrayList<>();

            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String header = br.readLine();
                    if (header != null) {
                        String[] parts = header.split(",");
                        for (int i = 1; i < parts.length && !parts[i].equalsIgnoreCase("TotalClasses"); i++)
                            dates.add(parts[i]);
                    }
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split(",");
                        List<String> values = new ArrayList<>(Arrays.asList(parts));
                        // remove previous totals if any
                        if (values.size() > dates.size() + 1) {
                            values = values.subList(0, dates.size() + 1);
                        }
                        data.put(parts[0], values);
                    }
                }
            }

            dates.add(date);

            List<String> studentOrder = new ArrayList<>();
            for (int i = 1; i <= totalStudents; i++) studentOrder.add(String.valueOf(i));

            int index = 0;
            List<String> skipped = new ArrayList<>();

            Map<String, String> symbols = new HashMap<>();
            for (String roll : studentOrder) symbols.put(roll, "•");

            while (index < studentOrder.size()) {
                String roll = studentOrder.get(index);
                if (!data.containsKey(roll)) {
                    List<String> values = new ArrayList<>();
                    values.add(roll);
                    for (int j = 0; j < dates.size() - 1; j++) values.add("-");
                    data.put(roll, values);
                }

                List<String> values = data.get(roll);

                Object[] options = {"Present", "Absent", "Skip", "Back", "Front"};
                int choice = JOptionPane.showOptionDialog(null,
                        "Roll No " + roll + " " + symbols.get(roll),
                        "Attendance",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);

                switch (choice) {
                    case 0 -> { values.add("P"); symbols.put(roll, "✔"); index++; }
                    case 1 -> { values.add("A"); symbols.put(roll, "❌"); index++; }
                    case 2 -> { skipped.add(roll); symbols.put(roll, "•"); index++; }
                    case 3 -> { if (index > 0) index--; }
                    case 4 -> { if (index < studentOrder.size() - 1) index++; }
                    default -> index++;
                }
            }

            for (String roll : skipped) {
                List<String> values = data.get(roll);
                Object[] options = {"Present", "Absent"};
                int choice = JOptionPane.showOptionDialog(null,
                        "Roll No " + roll + " (Skipped)",
                        "Attendance",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);
                values.add(choice == 0 ? "P" : "A");
                symbols.put(roll, choice == 0 ? "✔" : "❌");
            }

            for (List<String> values : data.values())
                while (values.size() < dates.size() + 1) values.add("-");

            // Write CSV with totals
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("RollNo");
                for (String d : dates) fw.write("," + d);
                fw.write(",TotalClasses,Present,Percentage\n");

                for (List<String> values : data.values()) {
                    int totalClasses = dates.size();
                    int present = 0;
                    for (int i = 1; i < values.size(); i++) {
                        if (values.get(i).equalsIgnoreCase("P")) present++;
                    }
                    double percentage = totalClasses == 0 ? 0 : (present * 100.0 / totalClasses);
                    fw.write(String.join(",", values) + "," + totalClasses + "," + present + "," + String.format("%.2f%%", percentage) + "\n");
                }
            }

            JOptionPane.showMessageDialog(null, "Attendance saved for " + date);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error taking attendance!");
            e.printStackTrace();
        }
    }

    // -------------------- viewAttendance --------------------
    static void viewAttendance(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                JOptionPane.showMessageDialog(null, "No attendance data found!");
                return;
            }

            String[] header = headerLine.split(",");
            DefaultTableModel model = new DefaultTableModel(header, 0);

            String line;
            while ((line = br.readLine()) != null) {
                model.addRow(line.split(","));
            }

            JTable table = new JTable(model);
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "Attendance Record", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No attendance found!");
        }
    }

    // -------------------- STUDENT SECTION --------------------
    static void studentSignup() {
        String username = JOptionPane.showInputDialog("Enter Username:");
        if (username == null) return;

        String password = JOptionPane.showInputDialog("Enter Password:");
        if (password == null) return;

        String branch = JOptionPane.showInputDialog("Enter Branch:");
        if (branch == null) return;

        String sem = JOptionPane.showInputDialog("Enter Semester:");
        if (sem == null) return;

        String roll = JOptionPane.showInputDialog("Enter Roll No:");
        if (roll == null) return;

        try (FileWriter fw = new FileWriter(ACCOUNT_FILE, true)) {
            fw.write(username + "," + password + "," + branch + "," + sem + "," + roll + "\n");
            JOptionPane.showMessageDialog(null, "Student signed up successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error during signup!");
        }
    }

    static void studentLogin() {
        String username = JOptionPane.showInputDialog("Enter Username:");
        if (username == null) return;

        String password = JOptionPane.showInputDialog("Enter Password:");
        if (password == null) return;

        String subject = JOptionPane.showInputDialog("Enter Subject to View Attendance:");
        if (subject == null) return;

        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String line;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    found = true;
                    String branch = parts[2];
                    String sem = parts[3];
                    String roll = parts[4];
                    showStudentAttendance(branch, sem, subject, roll);
                    break;
                }
            }
            if (!found) JOptionPane.showMessageDialog(null, "Invalid credentials!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error logging in!");
        }
    }

    static void showStudentAttendance(String branch, String sem, String subject, String roll) {
        String fileName = branch + "" + sem + "" + subject + "_Teacher.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String[] header = br.readLine().split(",");
            String line;
            StringBuilder report = new StringBuilder("Attendance Report\n-------------------------\n");
            report.append("Roll No: ").append(roll).append("\n");

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(roll)) {
                    for (int i = 1; i < data.length; i++) {
                        report.append(header[i]).append(": ").append(data[i]).append("\n");
                    }
                    break;
                }
            }

            report.append("-------------------------");
            JOptionPane.showMessageDialog(null, report.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No attendance data found for this subject!");
        }
    }
}