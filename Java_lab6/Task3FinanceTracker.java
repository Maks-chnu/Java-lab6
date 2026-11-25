package Java_lab6;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Лабораторна робота №6, завдання 3, варіант 6.
 * Особистий фінансовий трекер (доходи / витрати).
 */
public class Task3FinanceTracker extends JFrame {

    private static final long serialVersionUID = 1L;

    static final String INCOME = "Дохід";
    static final String EXPENSE = "Витрата";
    static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

    // Модель даних таблиці (патерн MVC: модель окремо від відображення JTable)
    final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Дата", "Тип", "Категорія", "Сума, грн", "Коментар"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // редагування лише через форму, щоб дані завжди були коректні
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 3 ? Double.class : String.class; // сума сортується як число
        }
    };
    final JTable table = new JTable(model);
    final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    // Поля форми додавання
    final JTextField dateField = new JTextField(LocalDate.now().format(DATE), 9);
    final JComboBox<String> typeBox = new JComboBox<>(new String[] {INCOME, EXPENSE});
    final JComboBox<String> categoryBox = new JComboBox<>(new String[] {
            "Зарплата", "Стипендія", "Підробіток", "Продукти", "Транспорт", "Комунальні", "Розваги", "Інше"});
    final JTextField amountField = new JTextField(8);
    final JTextField commentField = new JTextField(15);

    final JButton addButton = new JButton("Додати");
    final JButton deleteButton = new JButton("Видалити вибраний");

    // Пошук і фільтрація
    final JTextField searchField = new JTextField(15);
    final JComboBox<String> filterBox = new JComboBox<>(new String[] {"Усі", INCOME, EXPENSE});

    // Баланс під таблицею
    final JLabel balanceLabel = new JLabel();

    public Task3FinanceTracker() {
        super("Особистий фінансовий трекер");

        setJMenuBar(createMenuBar());

        // ===== Таблиця =====
        table.setRowSorter(sorter); // клік по заголовку — сортування
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        // Власний рендерер: сума завжди з двома знаками після коми, вирівнювання праворуч
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : String.format("%.2f", (Double) value));
            }
        };
        moneyRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(moneyRenderer);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(700, 260));

        // ===== Панель пошуку над таблицею =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Пошук:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Тип:"));
        searchPanel.add(filterBox);
        searchField.addActionListener(e -> applyFilter());       // Enter у полі пошуку
        filterBox.addActionListener(e -> applyFilter());
        JButton resetFilter = new JButton("Скинути фільтр");
        resetFilter.addActionListener(e -> {
            searchField.setText("");
            filterBox.setSelectedIndex(0);
            applyFilter();
        });
        searchPanel.add(resetFilter);

        // ===== Форма додавання під таблицею (GridBagLayout) =====
        categoryBox.setEditable(true); // можна ввести власну категорію
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Новий запис"));
        String[] labels = {"Дата (дд.мм.рррр)", "Тип", "Категорія", "Сума, грн", "Коментар"};
        javax.swing.JComponent[] fields = {dateField, typeBox, categoryBox, amountField, commentField};
        for (int i = 0; i < labels.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i;
            c.gridy = 0;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(2, 4, 2, 4);
            form.add(new JLabel(labels[i]), c);

            c = new GridBagConstraints();
            c.gridx = i;
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = (i == 4) ? 1 : 0; // коментар розтягується
            c.insets = new Insets(2, 4, 4, 4);
            form.add(fields[i], c);
        }

        addButton.addActionListener(e -> addFromForm());
        deleteButton.addActionListener(e -> deleteSelected());
        amountField.addActionListener(e -> addFromForm());
        commentField.addActionListener(e -> addFromForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(deleteButton);

        balanceLabel.setFont(balanceLabel.getFont().deriveFont(Font.BOLD, 15f));
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        balanceLabel.setOpaque(true);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(form, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.WEST);
        bottom.add(balanceLabel, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(searchPanel, BorderLayout.NORTH);
        root.add(tableScroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        updateBalance();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    // ===================== Меню =====================

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem demo = new JMenuItem("Завантажити приклад");
        demo.addActionListener(e -> loadDemoData());
        JMenuItem clear = new JMenuItem("Очистити таблицю");
        clear.addActionListener(e -> clearTable(true));
        JMenuItem stats = new JMenuItem("Статистика");
        stats.addActionListener(e -> showStatistics());
        JMenuItem export = new JMenuItem("Вивантажити статистику у файл...");
        export.addActionListener(e -> exportStatistics());
        JMenuItem exit = new JMenuItem("Вихід");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        exit.addActionListener(e -> dispose());

        fileMenu.add(demo);
        fileMenu.add(clear);
        fileMenu.addSeparator();
        fileMenu.add(stats);
        fileMenu.add(export);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        JMenu helpMenu = new JMenu("Довідка");
        JMenuItem about = new JMenuItem("Про програму");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Особистий фінансовий трекер\n"
                        + "Лабораторна робота №6, варіант 6\n\n"
                        + "Автор: Іванійчук Максим, група 241б\n"
                        + "ЧНУ імені Юрія Федьковича, 2026",
                "Про програму", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(about);

        bar.add(fileMenu);
        bar.add(helpMenu);
        return bar;
    }

    // ===================== Робота з даними =====================

    // Зчитує форму, перевіряє дані та додає рядок у таблицю
    void addFromForm() {
        try {
            LocalDate date = parseDate(dateField.getText());
            String type = (String) typeBox.getSelectedItem();
            Object categoryValue = categoryBox.getSelectedItem();
            String category = categoryValue == null ? "" : categoryValue.toString().trim();
            if (category.isEmpty()) {
                throw new IllegalArgumentException("Вкажіть категорію.");
            }
            double amount = parseAmount(amountField.getText());
            String comment = commentField.getText().trim();

            addRecord(date, type, category, amount, comment);

            amountField.setText("");
            commentField.setText("");
            amountField.requestFocusInWindow();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Помилка введення", JOptionPane.ERROR_MESSAGE);
        }
    }

    void addRecord(LocalDate date, String type, String category, double amount, String comment) {
        model.addRow(new Object[] {date.format(DATE), type, category, amount, comment});
        updateBalance();
    }

    void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Спочатку виберіть рядок у таблиці.",
                    "Видалення", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Індекс у таблиці (view) може відрізнятися від індексу в моделі через сортування/фільтр
        int modelRow = table.convertRowIndexToModel(viewRow);
        model.removeRow(modelRow);
        updateBalance();
    }

    void clearTable(boolean ask) {
        if (ask && model.getRowCount() > 0) {
            int answer = JOptionPane.showConfirmDialog(this, "Видалити всі записи?",
                    "Очищення таблиці", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        model.setRowCount(0);
        updateBalance();
    }

    // Фільтр за типом і текстом пошуку (у будь-якій колонці)
    void applyFilter() {
        String type = (String) filterBox.getSelectedItem();
        String text = searchField.getText().trim().toLowerCase();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (!"Усі".equals(type) && !type.equals(entry.getStringValue(1))) {
                    return false;
                }
                if (text.isEmpty()) {
                    return true;
                }
                for (int i = 0; i < entry.getValueCount(); i++) {
                    if (entry.getStringValue(i).toLowerCase().contains(text)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    double total(String type) {
        double sum = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (type.equals(model.getValueAt(i, 1))) {
                sum += (Double) model.getValueAt(i, 3);
            }
        }
        return sum;
    }

    // Баланс = доходи − витрати; колір мітки залежить від знака
    void updateBalance() {
        double income = total(INCOME);
        double expense = total(EXPENSE);
        double balance = income - expense;
        balanceLabel.setText(String.format("Доходи: %.2f грн    Витрати: %.2f грн    Баланс: %.2f грн",
                income, expense, balance));
        if (balance > 0) {
            balanceLabel.setBackground(new Color(220, 245, 220));
            balanceLabel.setForeground(new Color(0, 110, 0));
        } else if (balance < 0) {
            balanceLabel.setBackground(new Color(250, 220, 220));
            balanceLabel.setForeground(new Color(170, 0, 0));
        } else {
            balanceLabel.setBackground(new Color(235, 235, 235));
            balanceLabel.setForeground(Color.DARK_GRAY);
        }
    }

    // Текст статистики: підсумки та розподіл за категоріями
    String buildStatistics() {
        Map<String, Double> incomeByCategory = new TreeMap<>();
        Map<String, Double> expenseByCategory = new TreeMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String type = (String) model.getValueAt(i, 1);
            String category = (String) model.getValueAt(i, 2);
            double amount = (Double) model.getValueAt(i, 3);
            Map<String, Double> target = INCOME.equals(type) ? incomeByCategory : expenseByCategory;
            target.merge(category, amount, Double::sum);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Кількість записів: ").append(model.getRowCount()).append('\n');
        sb.append(String.format("Усього доходів:  %.2f грн%n", total(INCOME)));
        sb.append(String.format("Усього витрат:   %.2f грн%n", total(EXPENSE)));
        sb.append(String.format("Баланс:          %.2f грн%n", total(INCOME) - total(EXPENSE)));
        sb.append("\nДоходи за категоріями:\n");
        appendMap(sb, incomeByCategory);
        sb.append("\nВитрати за категоріями:\n");
        appendMap(sb, expenseByCategory);
        return sb.toString();
    }

    // Показує статистику моноширинним шрифтом, щоб колонки чисел були рівні
    void showStatistics() {
        JTextArea area = new JTextArea(buildStatistics());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setOpaque(false);
        JOptionPane.showMessageDialog(this, area, "Статистика", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void appendMap(StringBuilder sb, Map<String, Double> map) {
        if (map.isEmpty()) {
            sb.append("  немає\n");
        }
        for (Map.Entry<String, Double> e : map.entrySet()) {
            sb.append(String.format("  %-12s %10.2f грн%n", e.getKey(), e.getValue()));
        }
    }

    void exportStatistics() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("statistics.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            writeReport(file);
            JOptionPane.showMessageDialog(this, "Статистику збережено у файл:\n" + file.getAbsolutePath(),
                    "Вивантаження", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Не вдалося зберегти файл: " + ex.getMessage(),
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Записує статистику та всі записи у текстовий файл
    void writeReport(File file) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            w.write(buildStatistics());
            w.newLine();
            w.write("Записи:");
            w.newLine();
            for (int i = 0; i < model.getRowCount(); i++) {
                w.write(String.format("%s; %s; %s; %.2f; %s", model.getValueAt(i, 0), model.getValueAt(i, 1),
                        model.getValueAt(i, 2), (Double) model.getValueAt(i, 3), model.getValueAt(i, 4)));
                w.newLine();
            }
        }
    }

    void loadDemoData() {
        clearTable(false);
        addRecord(LocalDate.of(2026, 11, 1), INCOME, "Стипендія", 2000, "Академічна стипендія");
        addRecord(LocalDate.of(2026, 11, 3), EXPENSE, "Транспорт", 300, "Проїзний на місяць");
        addRecord(LocalDate.of(2026, 11, 5), EXPENSE, "Продукти", 850.50, "Супермаркет");
        addRecord(LocalDate.of(2026, 11, 10), INCOME, "Підробіток", 3500, "Фриланс-проєкт");
        addRecord(LocalDate.of(2026, 11, 12), EXPENSE, "Комунальні", 1200, "Гуртожиток");
        addRecord(LocalDate.of(2026, 11, 15), EXPENSE, "Розваги", 450, "Кіно з друзями");
        addRecord(LocalDate.of(2026, 11, 20), EXPENSE, "Продукти", 620, "Ринок");
    }

    // ===================== Перевірка введення =====================

    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Дата має бути у форматі дд.мм.рррр і бути реальною, отримано: «"
                    + text.trim() + "».");
        }
    }

    static double parseAmount(String text) {
        String s = text.trim().replace(',', '.');
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Вкажіть суму.");
        }
        try {
            double value = Double.parseDouble(s);
            if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                throw new IllegalArgumentException("Сума має бути додатним числом.");
            }
            return Math.round(value * 100) / 100.0; // округлення до копійок
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Сума має бути числом, отримано: «" + text.trim() + "».");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Task3FinanceTracker app = new Task3FinanceTracker();
            app.loadDemoData(); // щоб одразу було видно, як працює програма
            app.setVisible(true);
        });
    }
}
