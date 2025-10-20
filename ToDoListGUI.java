import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ToDoListGUI {
    private JFrame frame;
    private JPanel taskPanel;
    private JTextField taskField;
    private JComboBox<String> categoryBox;
    private JComboBox<String> priorityBox;
    private java.util.List<TaskItem> tasks;
    private final String FILE_NAME = "tasks.txt";

    public ToDoListGUI() {
        tasks = new ArrayList<>();
        frame = new JFrame("🌟 My Pro To-Do List 🌟");
        frame.setSize(550, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(245, 245, 250));

        // --- Header ---
        JLabel header = new JLabel("📝 My Tasks", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setOpaque(true);
        header.setBackground(new Color(33, 150, 243));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        frame.add(header, BorderLayout.NORTH);

        // --- Top panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(new Color(245, 245, 250));

        taskField = new JTextField(15);
        taskField.setFont(new Font("Arial", Font.PLAIN, 16));

        String[] categories = {"Work", "Personal", "Shopping", "Other"};
        categoryBox = new JComboBox<>(categories);

        String[] priorities = {"Low", "Medium", "High"};
        priorityBox = new JComboBox<>(priorities);

        JButton addButton = new JButton("➕ Add Task");
        styleButton(addButton, new Color(76, 175, 80), new Color(56, 142, 60));
        addButton.addActionListener(e -> addTask(taskField.getText().trim(),
                (String) categoryBox.getSelectedItem(),
                (String) priorityBox.getSelectedItem()));

        topPanel.add(taskField);
        topPanel.add(categoryBox);
        topPanel.add(priorityBox);
        topPanel.add(addButton);
        frame.add(topPanel, BorderLayout.PAGE_START);

        // --- Center panel ---
        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setBackground(new Color(245, 245, 250));
        JScrollPane scrollPane = new JScrollPane(taskPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom panel ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(245, 245, 250));
        JButton deleteButton = new JButton("🗑️ Delete Selected");
        styleButton(deleteButton, new Color(244, 67, 54), new Color(211, 47, 47));
        deleteButton.addActionListener(e -> deleteTasks());
        bottomPanel.add(deleteButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // --- Load tasks ---
        loadTasks();

        // --- Save on close ---
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });

        // --- Drag & Drop (ready for future enhancement) ---
        taskPanel.setTransferHandler(new TaskReorderHandler());

        frame.setVisible(true);
    }

    // --- Add task safely ---
    private void addTask(String text, String category, String priority) {
        if (!text.isEmpty()) {
            TaskItem taskItem = new TaskItem(text, category, priority);
            tasks.add(taskItem);
            taskPanel.add(taskItem.getPanel());
            taskPanel.revalidate();
            taskPanel.repaint();
            taskField.setText("");
        }
    }

    // --- Delete selected tasks safely ---
    private void deleteTasks() {
        Iterator<TaskItem> it = tasks.iterator();
        while (it.hasNext()) {
            TaskItem t = it.next();
            if (t.isSelected()) {
                taskPanel.remove(t.getPanel());
                it.remove();
            }
        }
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    // --- Load tasks safely ---
    private void loadTasks() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue; // skip invalid lines
                boolean done = Boolean.parseBoolean(parts[0]);
                String category = parts[1];
                String priority = parts[2];
                String text = parts[3];
                TaskItem taskItem = new TaskItem(text, category, priority);
                taskItem.setDone(done);
                tasks.add(taskItem);
                taskPanel.add(taskItem.getPanel());
            }
        } catch (IOException e) {
            // file may not exist first run
        }
    }

    // --- Save tasks safely ---
    private void saveTasks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (TaskItem t : tasks) {
                bw.write(t.isDone() + "|" + t.getCategory() + "|" + t.getPriority() + "|" + t.getText());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Button styling ---
    private void styleButton(JButton button, Color normal, Color hover) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(normal);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { button.setBackground(hover); }
            public void mouseExited(MouseEvent evt) { button.setBackground(normal); }
        });
    }

    // --- Drag & Drop Handler placeholder ---
    private class TaskReorderHandler extends TransferHandler {
        public boolean canImport(TransferSupport support) { return true; }
    }

    // --- TaskItem inner class ---
    private class TaskItem {
        private JPanel panel;
        private JCheckBox checkBox;
        private JLabel categoryLabel;
        private JLabel priorityLabel;

        public TaskItem(String text, String category, String priority) {
            panel = new JPanel(new BorderLayout());
            panel.setBackground(getCategoryColor(category));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));

            checkBox = new JCheckBox(text);
            checkBox.setBackground(getCategoryColor(category));
            checkBox.setFont(new Font("Arial", Font.PLAIN, 16));
            panel.add(checkBox, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            rightPanel.setBackground(getCategoryColor(category));
            categoryLabel = new JLabel(category);
            categoryLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            categoryLabel.setForeground(Color.WHITE);

            priorityLabel = new JLabel(getPriorityIcon(priority));
            rightPanel.add(categoryLabel);
            rightPanel.add(priorityLabel);
            panel.add(rightPanel, BorderLayout.EAST);

            // Hover effect
            panel.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { panel.setBackground(panel.getBackground().darker()); }
                public void mouseExited(MouseEvent e) { panel.setBackground(getCategoryColor(category)); }
            });

            // Double-click to edit
            checkBox.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String newText = JOptionPane.showInputDialog(frame, "Edit Task:", checkBox.getText());
                        if (newText != null && !newText.trim().isEmpty()) {
                            checkBox.setText(newText.trim());
                        }
                    }
                }
            });
        }

        public JPanel getPanel() { return panel; }
        public boolean isSelected() { return checkBox.isSelected(); }
        public boolean isDone() { return checkBox.isSelected(); }
        public void setDone(boolean done) { checkBox.setSelected(done); }
        public String getText() { return checkBox.getText(); }
        public String getCategory() { return categoryLabel.getText(); }
        public String getPriority() { return priorityLabel.getText(); }

        private Color getCategoryColor(String category) {
            return switch (category) {
                case "Work" -> new Color(33, 150, 243);
                case "Personal" -> new Color(76, 175, 80);
                case "Shopping" -> new Color(255, 152, 0);
                default -> new Color(156, 39, 176);
            };
        }

        private String getPriorityIcon(String priority) {
            return switch (priority) {
                case "High" -> "★";
                case "Medium" -> "☆";
                default -> "✩";
            };
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListGUI::new);
    }
}
