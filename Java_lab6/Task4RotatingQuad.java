package Java_lab6;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Лабораторна робота №6, завдання 4 (додаткове), варіант 6.
 * Чотирикутник, що обертається в площині фрейму навколо свого центру ваги.
 */
public class Task4RotatingQuad extends JFrame {

    private static final long serialVersionUID = 1L;

    // Вершини довільного (неправильного) опуклого чотирикутника
    static final double[] XS = {-90, 70, 110, -40};
    static final double[] YS = {-60, -80, 50, 70};

    final RotationPanel panel = new RotationPanel();
    final Timer timer;

    public Task4RotatingQuad() {
        super("Обертання чотирикутника навколо центру ваги");

        // Таймер Swing викликає обробник у потоці EDT кожні 30 мс (~33 кадри за секунду)
        timer = new Timer(30, e -> {
            panel.angle += Math.toRadians(2); // поворот на 2° за кадр
            panel.repaint();                  // запит на перемальовування -> paintComponent()
        });

        JButton toggle = new JButton("Пауза");
        toggle.addActionListener(e -> {
            if (timer.isRunning()) {
                timer.stop();
                toggle.setText("Старт");
            } else {
                timer.start();
                toggle.setText("Пауза");
            }
        });
        JPanel bottom = new JPanel();
        bottom.add(toggle);

        add(panel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Центр ваги (центроїд) плоского многокутника за формулами:
     * A  = 1/2 · Σ (x_i·y_{i+1} − x_{i+1}·y_i)
     * Cx = 1/(6A) · Σ (x_i + x_{i+1})(x_i·y_{i+1} − x_{i+1}·y_i)
     * Cy = 1/(6A) · Σ (y_i + y_{i+1})(x_i·y_{i+1} − x_{i+1}·y_i)
     */
    static Point2D centroid(double[] xs, double[] ys) {
        double area = 0;
        double cx = 0;
        double cy = 0;
        int n = xs.length;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double cross = xs[i] * ys[j] - xs[j] * ys[i];
            area += cross;
            cx += (xs[i] + xs[j]) * cross;
            cy += (ys[i] + ys[j]) * cross;
        }
        area /= 2;
        return new Point2D.Double(cx / (6 * area), cy / (6 * area));
    }

    static class RotationPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        double angle = 0;
        private final Path2D shape = new Path2D.Double();
        private final Point2D center = centroid(XS, YS);

        RotationPanel() {
            setPreferredSize(new Dimension(420, 420));
            setBackground(Color.WHITE);
            shape.moveTo(XS[0], YS[0]);
            for (int i = 1; i < XS.length; i++) {
                shape.lineTo(XS[i], YS[i]);
            }
            shape.closePath();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // очищення фону
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1) центр ваги фігури переносимо в центр панелі
            g2.translate(getWidth() / 2.0 - center.getX(), getHeight() / 2.0 - center.getY());
            // 2) поворот навколо центру ваги
            g2.transform(AffineTransform.getRotateInstance(angle, center.getX(), center.getY()));

            g2.setColor(new Color(120, 170, 230));
            g2.fill(shape);
            g2.setColor(new Color(20, 60, 140));
            g2.setStroke(new BasicStroke(3));
            g2.draw(shape);

            // Позначка центру ваги (нерухома точка обертання)
            g2.setColor(Color.RED);
            g2.fill(new Ellipse2D.Double(center.getX() - 5, center.getY() - 5, 10, 10));
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Task4RotatingQuad frame = new Task4RotatingQuad();
            frame.setVisible(true);
            frame.timer.start();
        });
    }
}
