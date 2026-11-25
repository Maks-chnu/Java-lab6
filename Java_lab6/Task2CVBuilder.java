package Java_lab6;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Лабораторна робота №6, завдання 2, варіант 6.
 * Конструктор резюме (CV Builder).
 */
public class Task2CVBuilder extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ()-]{10,18}$");

    // Персональні дані
    final JTextField nameField = new JTextField(25);
    final JTextField emailField = new JTextField(25);
    final JTextField phoneField = new JTextField(25);
    final JTextField positionField = new JTextField(25);

    // Мови — прапорці (можна обрати кілька)
    final JCheckBox[] languageBoxes = {
            new JCheckBox("Українська"), new JCheckBox("Англійська"),
            new JCheckBox("Німецька"), new JCheckBox("Польська")
    };

    // Освіта — перемикачі, об'єднані в групу (можна обрати лише один)
    final JRadioButton bachelorRadio = new JRadioButton("Бакалавр");
    final JRadioButton masterRadio = new JRadioButton("Магістр");
    final ButtonGroup educationGroup = new ButtonGroup();

    // Досвід роботи — багаторядкове поле в області прокрутки
    final JTextArea experienceArea = new JTextArea(6, 30);

    final JButton buildButton = new JButton("Сформувати резюме");
    final JButton resetButton = new JButton("Очистити");

    public Task2CVBuilder() {
        super("Конструктор резюме (CV Builder)");

        educationGroup.add(bachelorRadio);
        educationGroup.add(masterRadio);

        experienceArea.setLineWrap(true);       // перенесення довгих рядків
        experienceArea.setWrapStyleWord(true);  // перенесення по словах

        // ===== Центральна форма — менеджер GridBagLayout =====
        JPanel form = new JPanel(new GridBagLayout());
        int row = 0;
        row = addRow(form, row, "ПІБ*:", nameField);
        row = addRow(form, row, "Email*:", emailField);
        row = addRow(form, row, "Телефон:", phoneField);
        row = addRow(form, row, "Бажана посада:", positionField);

        // Мови: 2×2 прапорці — GridLayout
        JPanel languagesPanel = new JPanel(new GridLayout(2, 2));
        for (JCheckBox box : languageBoxes) {
            languagesPanel.add(box);
        }
        languagesPanel.setBorder(BorderFactory.createTitledBorder("Володіння мовами"));
        row = addWide(form, row, languagesPanel);

        // Освіта: перемикачі в один рядок — FlowLayout
        JPanel educationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        educationPanel.add(bachelorRadio);
        educationPanel.add(masterRadio);
        educationPanel.setBorder(BorderFactory.createTitledBorder("Рівень освіти*"));
        row = addWide(form, row, educationPanel);

        JScrollPane experienceScroll = new JScrollPane(experienceArea);
        experienceScroll.setBorder(BorderFactory.createTitledBorder("Досвід роботи"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;                     // область досвіду забирає вільне місце по вертикалі
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 4, 4, 4);
        form.add(experienceScroll, c);

        // ===== Кнопки внизу =====
        buildButton.addActionListener(e -> buildResume());
        resetButton.addActionListener(e -> resetForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(resetButton);
        buttons.add(buildButton);

        JLabel hint = new JLabel("* — обов'язкові поля");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC));

        JPanel south = new JPanel(new BorderLayout());
        south.add(hint, BorderLayout.WEST);
        south.add(buttons, BorderLayout.EAST);

        // ===== Компонування вікна — BorderLayout =====
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(form, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 560));
        pack();
        setLocationRelativeTo(null);
    }

    // Додає рядок «підпис + поле» у форму GridBagLayout
    private static int addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 8);
        form.add(new JLabel(label), c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = row;
        c.weightx = 1;                         // поле розтягується по ширині
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 4);
        form.add(field, c);
        return row + 1;
    }

    // Додає компонент на всю ширину форми (дві колонки)
    private static int addWide(JPanel form, int row, JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 4, 4, 4);
        form.add(component, c);
        return row + 1;
    }

    // Перевіряє дані та формує текст резюме; при помилках — повертає null і показує повідомлення
    String validateAndFormat() {
        List<String> errors = new ArrayList<>();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String position = positionField.getText().trim();

        if (name.isEmpty()) {
            errors.add("Вкажіть ПІБ.");
        }
        if (email.isEmpty()) {
            errors.add("Вкажіть Email.");
        } else if (!EMAIL.matcher(email).matches()) {
            errors.add("Email має некоректний формат.");
        }
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            errors.add("Телефон має некоректний формат.");
        }
        if (educationGroup.getSelection() == null) {
            errors.add("Оберіть рівень освіти.");
        }
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, String.join("\n", errors),
                    "Заповніть форму", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        List<String> languages = new ArrayList<>();
        for (JCheckBox box : languageBoxes) {
            if (box.isSelected()) {
                languages.add(box.getText());
            }
        }
        String education = bachelorRadio.isSelected() ? "Бакалавр" : "Магістр";
        String experience = experienceArea.getText().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("РЕЗЮМЕ\n");
        sb.append("========================================\n");
        sb.append(name.toUpperCase()).append('\n');
        if (!position.isEmpty()) {
            sb.append("Бажана посада: ").append(position).append('\n');
        }
        sb.append('\n');
        sb.append("КОНТАКТИ\n");
        sb.append("  Email:   ").append(email).append('\n');
        sb.append("  Телефон: ").append(phone.isEmpty() ? "не вказано" : phone).append('\n');
        sb.append('\n');
        sb.append("ОСВІТА\n");
        sb.append("  ").append(education).append('\n');
        sb.append('\n');
        sb.append("МОВИ\n");
        sb.append("  ").append(languages.isEmpty() ? "не вказано" : String.join(", ", languages)).append('\n');
        sb.append('\n');
        sb.append("ДОСВІД РОБОТИ\n");
        if (experience.isEmpty()) {
            sb.append("  не вказано\n");
        } else {
            for (String line : experience.split("\\R")) {
                sb.append("  ").append(line).append('\n');
            }
        }
        return sb.toString();
    }

    // Обробник кнопки «Сформувати резюме»: показує резюме в окремому модальному вікні
    void buildResume() {
        String text = validateAndFormat();
        if (text == null) {
            return;
        }
        createResumeDialog(text).setVisible(true);
    }

    JDialog createResumeDialog(String text) {
        JDialog dialog = new JDialog(this, "Перегляд резюме", true); // true — модальне вікно

        JTextArea view = new JTextArea(text);
        view.setEditable(false);
        view.setLineWrap(true);       // довгі рядки переносяться, горизонтальної прокрутки немає
        view.setWrapStyleWord(true);
        view.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        view.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton close = new JButton("Закрити");
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(close);

        dialog.add(new JScrollPane(view), BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    // Кнопка «Очистити»: скидає всі поля форми
    void resetForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        positionField.setText("");
        for (JCheckBox box : languageBoxes) {
            box.setSelected(false);
        }
        educationGroup.clearSelection();
        experienceArea.setText("");
        nameField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Task2CVBuilder().setVisible(true));
    }
}
