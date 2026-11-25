package Java_lab6;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Лабораторна робота №6, завдання 1, варіант 6.
 * Калькулятор простих відсотків (депозитний калькулятор).
 *
 * Прибуток = Сума × (Ставка / 100) × (Місяці / 12)
 */
public class Task1DepositCalculator extends JFrame {

    private static final long serialVersionUID = 1L;

    // Поля введення (package-private, щоб їх можна було заповнити з тестів)
    final JTextField amountField = new JTextField(12);
    final JTextField rateField = new JTextField(12);
    final JTextField monthsField = new JTextField(12);
    final JButton calculateButton = new JButton("Обчислити дохід");
    final JLabel resultLabel = new JLabel("Введіть дані та натисніть «Обчислити дохід»", SwingConstants.CENTER);

    public Task1DepositCalculator() {
        super("Депозитний калькулятор");

        // Форма: 3 рядки по 2 колонки (підпис + поле) — менеджер GridLayout
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Сума вкладу, грн:"));
        form.add(amountField);
        form.add(new JLabel("Річна ставка, %:"));
        form.add(rateField);
        form.add(new JLabel("Термін, місяців:"));
        form.add(monthsField);

        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 14f));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Обробник натискання кнопки — лямбда-вираз замість анонімного класу ActionListener
        calculateButton.addActionListener(e -> calculate());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(calculateButton);

        // Розміщення частин вікна — менеджер BorderLayout
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        root.add(form, BorderLayout.NORTH);
        root.add(buttonPanel, BorderLayout.CENTER);
        root.add(resultLabel, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(calculateButton); // Enter = натискання кнопки
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(440, 260));
        pack();
        setLocationRelativeTo(null); // вікно по центру екрана
    }

    // Зчитує дані, перевіряє їх і обчислює прибуток
    void calculate() {
        try {
            double amount = parsePositive(amountField.getText(), "Сума вкладу");
            double rate = parsePositive(rateField.getText(), "Річна ставка");
            int months = parsePositiveInt(monthsField.getText(), "Термін");

            if (rate > 100) {
                throw new IllegalArgumentException("Річна ставка не може перевищувати 100%.");
            }
            if (months > 600) {
                throw new IllegalArgumentException("Термін не може перевищувати 600 місяців (50 років).");
            }

            double profit = amount * (rate / 100) * (months / 12.0);
            double total = amount + profit;

            resultLabel.setForeground(new Color(0, 120, 0));
            // HTML у JLabel дозволяє вивести результат у два рядки
            resultLabel.setText(String.format("<html><center>Прибуток: %.2f грн<br>Сума в кінці терміну: %.2f грн</center></html>",
                    profit, total));
        } catch (IllegalArgumentException ex) {
            resultLabel.setForeground(Color.RED);
            resultLabel.setText("Помилка введення");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Перетворює текст на додатне дійсне число; кома замінюється на крапку
    static double parsePositive(String text, String fieldName) {
        String s = text.trim().replace(',', '.');
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» не заповнене.");
        }
        double value;
        try {
            value = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» має містити число, а не «" + text.trim() + "».");
        }
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» має бути додатним числом.");
        }
        return value;
    }

    // Перетворює текст на додатне ціле число
    static int parsePositiveInt(String text, String fieldName) {
        String s = text.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» не заповнене.");
        }
        try {
            int value = Integer.parseInt(s);
            if (value <= 0) {
                throw new IllegalArgumentException("Поле «" + fieldName + "» має бути додатним цілим числом.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» має містити ціле число місяців.");
        }
    }

    public static void main(String[] args) {
        // Створення інтерфейсу в потоці обробки подій (EDT)
        SwingUtilities.invokeLater(() -> new Task1DepositCalculator().setVisible(true));
    }
}
