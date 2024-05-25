import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CalendarMethod extends JFrame {
    private JLabel monthLabel;
    private JButton previousButton;
    private JButton nextButton;
    private JPanel calendarPanel;
    private Calendar calendar;
    private List<String> reminders;
    private static final String REMINDERS_FILE = "reminders.txt";

    public CalendarMethod() {
        loadReminders();
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        setTitle("Reminder Calendar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 24));
        previousButton = new JButton("<< Previous");
        nextButton = new JButton("Next >>");
        calendarPanel = new JPanel(new GridBagLayout());
        calendar = new GregorianCalendar();

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(previousButton, BorderLayout.WEST);
        controlPanel.add(monthLabel, BorderLayout.CENTER);
        controlPanel.add(nextButton, BorderLayout.EAST);

        previousButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        JButton viewRemindersButton = new JButton("View Reminders");
        viewRemindersButton.addActionListener(e -> displayReminders());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewRemindersButton);

        add(controlPanel, BorderLayout.NORTH);
        add(calendarPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateCalendar();
        setVisible(true);
    }

    private void updateCalendar() {
        monthLabel.setText(new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        calendarPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        // Add day headers
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            JLabel label = new JLabel(daysOfWeek[i], JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            calendarPanel.add(label, gbc);
        }

        // Fill the days of the month
        int dayOfMonth = 1;
        for (int i = 1; i <= 6; i++) { // 6 rows (weeks) to cover all days in a month
            for (int j = 1; j <= 7; j++) { // 7 columns (days of the week)
                gbc.gridx = j - 1;
                gbc.gridy = i;
                if (i == 1 && j < firstDayOfWeek || dayOfMonth > maxDayOfMonth) {
                    calendarPanel.add(new JLabel(""), gbc);
                } else {
                    JButton button = new JButton(String.valueOf(dayOfMonth));
                    button.setFont(new Font("Arial", Font.PLAIN, 14));
                    // Highlight today's date
                    if (dayOfMonth == today && calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear) {
                        button.setBackground(Color.green); // Cyan background color
                    }
                    button.addActionListener(e -> {
                        JButton source = (JButton) e.getSource();
                        int selectedDay = Integer.parseInt(source.getText());
                        int selectedYear = calendar.get(Calendar.YEAR);
                        int selectedMonth = calendar.get(Calendar.MONTH) + 1;
                        addReminderDialog(selectedYear, selectedMonth, selectedDay);
                    });
                    calendarPanel.add(button, gbc);
                    dayOfMonth++;
                }
            }
        }

        revalidate();
        repaint();
    }

    private void addReminderDialog(int year, int month, int day) {
        JFrame parentFrame = new JFrame();
        TimePickerPanel timePickerPanel = new TimePickerPanel();

        String[] priorities = {"High", "Moderate", "Low"};

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField eventTypeField = new JTextField();
        JComboBox<String> priorityComboBox = new JComboBox<>(priorities);
        JTextArea reminderTextArea = new JTextArea(3, 20);
        JScrollPane scrollPane = new JScrollPane(reminderTextArea);
        JTextField locationField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Event Type:"), gbc);
        gbc.gridx = 1;
        panel.add(eventTypeField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        panel.add(priorityComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Reminder:"), gbc);
        gbc.gridx = 1;
        panel.add(scrollPane, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        panel.add(locationField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1;
        panel.add(timePickerPanel, gbc);

        int result = JOptionPane.showConfirmDialog(parentFrame, panel,
                "Add Reminder - " + year + "-" + month + "-" + day, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String eventType = eventTypeField.getText();
            String priority = (String) priorityComboBox.getSelectedItem();
            String reminderText = reminderTextArea.getText();
            String location = locationField.getText();
            String time = timePickerPanel.getTime();

            if (eventType.isEmpty() || reminderText.isEmpty() || location.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
            dateTimeFormat.setLenient(false);
            try {
                String formattedDate = String.format("%04d-%02d-%02d %s", year, month, day, time);
                Date parsedDate = dateTimeFormat.parse(formattedDate);
                String newReminder = String.format("DateTime: %s, Event Type: %s, Priority: %s, Reminder: %s, Location: %s",
                        dateTimeFormat.format(parsedDate), eventType, priority, reminderText, location);
                reminders.add(newReminder);
                saveReminders();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(parentFrame, "Invalid date/time format. Please enter a valid time!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayReminders() {
        JFrame reminderFrame = new JFrame("View Reminders");
        JPanel reminderPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        for (String reminder : reminders) {
            JTextPane reminderArea = new JTextPane();
            reminderArea.setEditable(false);
            StyledDocument doc = reminderArea.getStyledDocument();

            JCheckBox checkBox = new JCheckBox();
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    reminderArea.setFont(reminderArea.getFont().deriveFont(Font.BOLD));
                } else {
                    reminderArea.setFont(reminderArea.getFont().deriveFont(Font.PLAIN));
                }
            });

            JPanel reminderWithCheckBoxPanel = new JPanel(new BorderLayout());
            reminderWithCheckBoxPanel.add(checkBox, BorderLayout.WEST);
            reminderWithCheckBoxPanel.add(reminderArea, BorderLayout.CENTER);

            String[] parts = reminder.split(", ");
            for (String part : parts) {
                String[] keyValue = part.split(": ");
                if (keyValue.length >= 2) {
                    SimpleAttributeSet bold = new SimpleAttributeSet();
                    StyleConstants.setBold(bold, true);

                    try {
                        switch (keyValue[0]) {
                            case "DateTime":
                                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
                                Date dateTime = dateTimeFormat.parse(keyValue[1]);
                                SimpleDateFormat formattedDate = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.ENGLISH);
                                String formattedDateTime = formattedDate.format(dateTime);
                                doc.insertString(doc.getLength(), "Date and Time: " + formattedDateTime + "\n", bold);
                                break;
                            default:
                                doc.insertString(doc.getLength(), keyValue[0] + ": " + keyValue[1] + "\n", bold);
                                break;
                        }
                    } catch (ParseException | BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                reminders.remove(reminder);
                reminderPanel.remove(reminderWithCheckBoxPanel);
                reminderFrame.revalidate();
                reminderFrame.repaint();
                saveReminders();
                JOptionPane.showMessageDialog(null, "Reminder Deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
            });

            JButton markAsDoneButton = new JButton("Mark as Done");
            markAsDoneButton.addActionListener(e -> {
                reminders.remove(reminder);
                reminderPanel.remove(reminderWithCheckBoxPanel);
                reminderFrame.revalidate();
                reminderFrame.repaint();
                saveReminders();
                JOptionPane.showMessageDialog(null, "Marked as Done!", "Success", JOptionPane.INFORMATION_MESSAGE);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(deleteButton);
            buttonPanel.add(markAsDoneButton);
            buttonPanel.setVisible(false);

            checkBox.addActionListener(e -> {
                buttonPanel.setVisible(checkBox.isSelected());
            });

            reminderWithCheckBoxPanel.add(buttonPanel, BorderLayout.SOUTH);

            gbc.gridx = 0;
            gbc.gridy = reminders.indexOf(reminder);
            reminderPanel.add(reminderWithCheckBoxPanel, gbc);
        }

        JScrollPane scrollPane = new JScrollPane(reminderPanel);
        reminderFrame.add(scrollPane);
        reminderFrame.pack();
        reminderFrame.setLocationRelativeTo(null);
        reminderFrame.setVisible(true);
    }

    private void saveReminders() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(REMINDERS_FILE))) {
            for (String reminder : reminders) {
                bw.write(reminder);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadReminders() {
        reminders = new ArrayList<>();
        File file = new File(REMINDERS_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                reminders.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CalendarMethod::new);
    }

    class TimePickerPanel extends JPanel {
        private JComboBox<String> hourComboBox;
        private JComboBox<String> minuteComboBox;
        private JComboBox<String> amPmComboBox;

        public TimePickerPanel() {
            setLayout(new FlowLayout());
            String[] hours = new String[12];
            for (int i = 0; i < 12; i++) {
                hours[i] = String.format("%02d", i + 1);
            }

            String[] minutes = new String[60];
            for (int i = 0; i < 60; i++) {
                minutes[i] = String.format("%02d", i);
            }

            String[] amPm = {"AM", "PM"};

            hourComboBox = new JComboBox<>(hours);
            minuteComboBox = new JComboBox<>(minutes);
            amPmComboBox = new JComboBox<>(amPm);

            add(hourComboBox);
            add(new JLabel(":"));
            add(minuteComboBox);
            add(amPmComboBox);
        }

        public String getTime() {
            return hourComboBox.getSelectedItem() + ":" + minuteComboBox.getSelectedItem() + " " + amPmComboBox.getSelectedItem();
        }
    }
}
