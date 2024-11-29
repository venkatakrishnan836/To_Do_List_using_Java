import java.io.*;
import java.text.*;
import java.util.*;


public class AdvancedToDoListApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, List<Task>> userTasks = new HashMap<>();
    private static final String USER_FILE = "users.dat";
    private static final Map<String, User> users = new HashMap<>();
    private static User currentUser = null;

    // User Class
    static class User implements Serializable {
        private String username;
        private String passwordHash;

        public User(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
        }

        public String getUsername() {
            return username;
        }

        public String getPasswordHash() {
            return passwordHash;
        }
    }

    // Task Class
    static class Task implements Serializable {
        private int id;
        private String description;
        private boolean isCompleted;
        private String category;
        private String priority;
        private Date dueDate;
        private List<Task> subtasks = new ArrayList<>();
        private String recurrence;

        public Task(int id, String description, String category, String priority, Date dueDate, String recurrence) {
            this.id = id;
            this.description = description;
            this.isCompleted = false;
            this.category = category;
            this.priority = priority;
            this.dueDate = dueDate;
            this.recurrence = recurrence;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public String getCategory() {
            return category;
        }

        public String getPriority() {
            return priority;
        }

        public Date getDueDate() {
            return dueDate;
        }

        public String getRecurrence() {
            return recurrence;
        }

        public List<Task> getSubtasks() {
            return subtasks;
        }

        public void markCompleted() {
            this.isCompleted = true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(id)
                .append(", Description: ").append(description)
                .append(", Category: ").append(category)
                .append(", Priority: ").append(priority)
                .append(", Due Date: ").append(formatDate(dueDate))
                .append(", Recurrence: ").append(recurrence)
                .append(", Completed: ").append(isCompleted ? "Yes" : "No");

            if (!subtasks.isEmpty()) {
                sb.append("\n  Subtasks:");
                for (Task subtask : subtasks) {
                    sb.append("\n    - ").append(subtask.description);
                }
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        loadUsers();
        loginMenu();
        loadTasks(currentUser.getUsername());

        while (true) {
            System.out.println("\nAdvanced To-Do List Application");
            System.out.println("1. Add Task");
            System.out.println("2. Remove Task");
            System.out.println("3. Update Task");
            System.out.println("4. Mark Task as Completed");
            System.out.println("5. Add Subtask");
            System.out.println("6. Search Tasks");
            System.out.println("7. Sort Tasks");
            System.out.println("8. Display Tasks");
            System.out.println("9. Save and Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: addTask(); break;
                case 2: removeTask(); break;
                case 3: updateTask(); break;
                case 4: markTaskCompleted(); break;
                case 5: addSubtask(); break;
                case 6: searchTasks(); break;
                case 7: sortTasks(); break;
                case 8: displayTasks(); break;
                case 9:
                    saveTasks(currentUser.getUsername());
                    saveUsers();
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void loginMenu() {
        while (currentUser == null) {
            System.out.println("\n1. Log In");
            System.out.println("2. Sign Up");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: logIn(); break;
                case 2: signUp(); break;
                case 3: System.exit(0);
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void signUp() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
            return;
        }
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        String passwordHash = hashPassword(password);
        User newUser = new User(username, passwordHash);
        users.put(username, newUser);
        System.out.println("Sign up successful!");
        saveUsers();
    }

    private static void logIn() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = users.get(username);
        if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
            currentUser = user;
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    @SuppressWarnings("unchecked")
private static void loadUsers() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_FILE))) {
        users.putAll((Map<String, User>) ois.readObject());
    } catch (FileNotFoundException e) {
        System.out.println("No user data found.");
    } catch (IOException | ClassNotFoundException e) {
        System.out.println("Error loading users: " + e.getMessage());
    }
}

    private static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadTasks(String username) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(username + "_tasks.dat"))) {
            userTasks.put(username, (List<Task>) ois.readObject());
        } catch (FileNotFoundException e) {
            userTasks.put(username, new ArrayList<>());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }

    private static void saveTasks(String username) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(username + "_tasks.dat"))) {
            oos.writeObject(userTasks.get(username));
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    private static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    private static void addTask() {
        System.out.print("Enter task description: ");
        String description = scanner.nextLine();
        System.out.print("Enter task category: ");
        String category = scanner.nextLine();
        System.out.print("Enter task priority (Low, Medium, High): ");
        String priority = scanner.nextLine();
        System.out.print("Enter task due date (yyyy-MM-dd): ");
        Date dueDate = parseDate(scanner.nextLine());
        System.out.print("Enter recurrence (None, Daily, Weekly): ");
        String recurrence = scanner.nextLine();

        if (dueDate != null) {
            int id = userTasks.get(currentUser.getUsername()).size() + 1;
            Task task = new Task(id, description, category, priority, dueDate, recurrence);
            userTasks.get(currentUser.getUsername()).add(task);
            System.out.println("Task added successfully!");
        }
    }

    private static void removeTask() {
        System.out.print("Enter task ID to remove: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        List<Task> tasks = userTasks.get(currentUser.getUsername());
        if (tasks.removeIf(task -> task.getId() == id)) {
            System.out.println("Task removed successfully!");
        } else {
            System.out.println("Task ID not found.");
        }
    }

    private static void updateTask() {
        System.out.print("Enter task ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        List<Task> tasks = userTasks.get(currentUser.getUsername());
        for (Task task : tasks) {
            if (task.getId() == id) {
                System.out.print("Enter new description: ");
                task.description = scanner.nextLine();
                System.out.println("Task updated successfully!");
                return;
            }
        }
        System.out.println("Task ID not found.");
    }

    private static void markTaskCompleted() {
        System.out.print("Enter task ID to mark as completed: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        List<Task> tasks = userTasks.get(currentUser.getUsername());
        for (Task task : tasks) {
            if (task.getId() == id) {
                task.markCompleted();
                System.out.println("Task marked as completed!");
                return;
            }
        }
        System.out.println("Task ID not found.");
    }

    private static void displayTasks() {
        List<Task> tasks = userTasks.get(currentUser.getUsername());
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private static Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Invalid date format! Use yyyy-MM-dd.");
            return null;
        }
    }
    private static void addSubtask() {
        System.out.print("Enter parent task ID to add a subtask: ");
        int parentId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
    
        List<Task> tasks = userTasks.get(currentUser.getUsername());
        for (Task task : tasks) {
            if (task.getId() == parentId) {
                System.out.print("Enter subtask description: ");
                String description = scanner.nextLine();
                int subtaskId = task.getSubtasks().size() + 1;
                Task subtask = new Task(subtaskId, description, task.getCategory(), task.getPriority(), null, "None");
                task.getSubtasks().add(subtask);
                System.out.println("Subtask added successfully!");
                return;
            }
        }
        System.out.println("Parent task ID not found.");
    }
    
    private static void searchTasks() {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine();
    
        List<Task> tasks = userTasks.get(currentUser.getUsername());
        boolean found = false;
        for (Task task : tasks) {
            if (task.getDescription().contains(keyword)) {
                System.out.println(task);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No tasks found matching the keyword.");
        }
    }
    
    private static void sortTasks() {
        System.out.println("Sort by: 1. Due Date  2. Priority");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
    
        List<Task> tasks = userTasks.get(currentUser.getUsername());
        switch (choice) {
            case 1:
                tasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
                System.out.println("Tasks sorted by due date.");
                break;
            case 2:
                tasks.sort(Comparator.comparing(Task::getPriority));
                System.out.println("Tasks sorted by priority.");
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
}
