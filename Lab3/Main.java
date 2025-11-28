import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private DrawingPanel drawingPanel;
    private JComboBox<String> algorithmComboBox;
    private JTextField x1Field, y1Field, x2Field, y2Field, radiusField;
    private JButton drawButton;
    private JLabel timeLabel;
    private JCheckBox gridCheckBox, axesCheckBox, coordinatesCheckBox;
    private JSlider scaleSlider;

    private static final int GRID_SIZE = 20;
    private double scale = 1.0;

    public Main() {
        setTitle("Алгоритмы растеризации - Современный интерфейс");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        initializeComponents();
        setupEventListeners();

        pack();
        setLocationRelativeTo(null);
        setSize(1100, 750);
    }

    private void initializeComponents() {
        // Главная панель управления
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(250, 250, 250));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Заголовок
        JLabel titleLabel = new JLabel("Параметры построения");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(60, 60, 60));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        controlPanel.add(titleLabel, gbc);

        // Выбор алгоритма
        gbc.gridy++; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Алгоритм:"), gbc);

        gbc.gridx = 1;
        algorithmComboBox = new JComboBox<>(new String[]{
                "Пошаговый алгоритм",
                "Алгоритм ЦДА",
                "Алгоритм Брезенхема (отрезок)",
                "Алгоритм Брезенхема (окружность)"
        });
        algorithmComboBox.setBackground(Color.WHITE);
        controlPanel.add(algorithmComboBox, gbc);

        // Поля ввода координат
        gbc.gridx = 0; gbc.gridy++;
        controlPanel.add(new JLabel("X1:"), gbc);
        gbc.gridx = 1;
        x1Field = createStyledTextField("10");
        controlPanel.add(x1Field, gbc);

        gbc.gridx = 0; gbc.gridy++;
        controlPanel.add(new JLabel("Y1:"), gbc);
        gbc.gridx = 1;
        y1Field = createStyledTextField("10");
        controlPanel.add(y1Field, gbc);

        gbc.gridx = 0; gbc.gridy++;
        controlPanel.add(new JLabel("X2:"), gbc);
        gbc.gridx = 1;
        x2Field = createStyledTextField("50");
        controlPanel.add(x2Field, gbc);

        gbc.gridx = 0; gbc.gridy++;
        controlPanel.add(new JLabel("Y2:"), gbc);
        gbc.gridx = 1;
        y2Field = createStyledTextField("50");
        controlPanel.add(y2Field, gbc);

        gbc.gridx = 0; gbc.gridy++;
        controlPanel.add(new JLabel("Радиус:"), gbc);
        gbc.gridx = 1;
        radiusField = createStyledTextField("30");
        controlPanel.add(radiusField, gbc);

        // Кнопка построения
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        drawButton = new JButton("Построить фигуру");
        styleButton(drawButton);
        controlPanel.add(drawButton, gbc);

        // Время выполнения
        gbc.gridy++;
        timeLabel = new JLabel("Время: -");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(80, 80, 80));
        controlPanel.add(timeLabel, gbc);

        // Панель настроек отображения
        JPanel displayPanel = new JPanel(new GridBagLayout());
        displayPanel.setBackground(new Color(250, 250, 250));
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.gridwidth = 2;

        JLabel displayTitle = new JLabel("Настройки отображения");
        displayTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        displayTitle.setForeground(new Color(60, 60, 60));
        gbc2.gridy = 0;
        displayPanel.add(displayTitle, gbc2);

        gbc2.gridy++;
        gridCheckBox = createStyledCheckBox("Сетка", true);
        displayPanel.add(gridCheckBox, gbc2);

        gbc2.gridy++;
        axesCheckBox = createStyledCheckBox("Оси координат", true);
        displayPanel.add(axesCheckBox, gbc2);

        gbc2.gridy++;
        coordinatesCheckBox = createStyledCheckBox("Подписи координат", true);
        displayPanel.add(coordinatesCheckBox, gbc2);

        gbc2.gridy++;
        displayPanel.add(new JLabel("Масштаб:"), gbc2);

        gbc2.gridy++;
        scaleSlider = new JSlider(50, 200, 100);
        scaleSlider.setBackground(new Color(250, 250, 250));
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setMinorTickSpacing(25);
        displayPanel.add(scaleSlider, gbc2);

        // Основная панель управления
        JPanel mainControlPanel = new JPanel(new BorderLayout(0, 15));
        mainControlPanel.setBackground(new Color(240, 240, 240));
        mainControlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainControlPanel.add(controlPanel, BorderLayout.NORTH);
        mainControlPanel.add(displayPanel, BorderLayout.CENTER);

        // Панель рисования
        drawingPanel = new DrawingPanel();
        JScrollPane scrollPane = new JScrollPane(drawingPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Размещение компонентов
        add(mainControlPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        updateRadiusFieldVisibility();
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 230));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Добавляем эффект при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 110, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 230));
            }
        });
    }

    private JCheckBox createStyledCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text, selected);
        checkBox.setBackground(new Color(250, 250, 250));
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        checkBox.setFocusPainted(false);
        return checkBox;
    }

    private void setupEventListeners() {
        drawButton.addActionListener(e -> drawFigure());
        algorithmComboBox.addActionListener(e -> updateRadiusFieldVisibility());

        gridCheckBox.addActionListener(e -> drawingPanel.repaint());
        axesCheckBox.addActionListener(e -> drawingPanel.repaint());
        coordinatesCheckBox.addActionListener(e -> drawingPanel.repaint());

        scaleSlider.addChangeListener(e -> {
            scale = scaleSlider.getValue() / 100.0;
            drawingPanel.repaint();
        });
    }

    private void updateRadiusFieldVisibility() {
        String selected = (String) algorithmComboBox.getSelectedItem();
        boolean isCircle = selected.equals("Алгоритм Брезенхема (окружность)");
        radiusField.setEnabled(isCircle);
        x2Field.setEnabled(!isCircle);
        y2Field.setEnabled(!isCircle);
    }

    private void drawFigure() {
        try {
            int x1 = Integer.parseInt(x1Field.getText());
            int y1 = Integer.parseInt(y1Field.getText());
            int x2 = Integer.parseInt(x2Field.getText());
            int y2 = Integer.parseInt(y2Field.getText());
            int radius = Integer.parseInt(radiusField.getText());

            String algorithm = (String) algorithmComboBox.getSelectedItem();

            long startTime = System.nanoTime();

            switch (algorithm) {
                case "Пошаговый алгоритм":
                    drawingPanel.setPoints(stepByStepLine(x1, y1, x2, y2));
                    break;
                case "Алгоритм ЦДА":
                    drawingPanel.setPoints(ddaLine(x1, y1, x2, y2));
                    break;
                case "Алгоритм Брезенхема (отрезок)":
                    drawingPanel.setPoints(bresenhamLine(x1, y1, x2, y2));
                    break;
                case "Алгоритм Брезенхема (окружность)":
                    drawingPanel.setPoints(bresenhamCircle(x1, y1, radius));
                    break;
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            timeLabel.setText(String.format("Время: %d нс", duration));

            drawingPanel.repaint();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Введите корректные числа", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Реализации алгоритмов
    private List<Point> stepByStepLine(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;

        while (true) {
            points.add(new Point(x, y));
            if (x == x2 && y == y2) break;

            int err2 = 2 * err;

            if (err2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (err2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return points;
    }

    private List<Point> ddaLine(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();
        points.add(new Point(x1, y1));

        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        double xIncrement = dx / (double) steps;
        double yIncrement = dy / (double) steps;

        double x = x1;
        double y = y1;

        for (int i = 0; i < steps; i++) {
            x += xIncrement;
            y += yIncrement;
            points.add(new Point((int) Math.round(x), (int) Math.round(y)));
        }

        return points;
    }

    private List<Point> bresenhamLine(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;

        while (true) {
            points.add(new Point(x, y));
            if (x == x2 && y == y2) break;

            int err2 = 2 * err;
            if (err2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (err2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return points;
    }

    private List<Point> bresenhamCircle(int xc, int yc, int r) {
        List<Point> points = new ArrayList<>();

        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        addCirclePoints(points, xc, yc, x, y);

        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            addCirclePoints(points, xc, yc, x, y);
        }

        return points;
    }

    private void addCirclePoints(List<Point> points, int xc, int yc, int x, int y) {
        points.add(new Point(xc + x, yc + y));
        points.add(new Point(xc - x, yc + y));
        points.add(new Point(xc + x, yc - y));
        points.add(new Point(xc - x, yc - y));
        points.add(new Point(xc + y, yc + x));
        points.add(new Point(xc - y, yc + x));
        points.add(new Point(xc + y, yc - x));
        points.add(new Point(xc - y, yc - x));
    }

    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private class DrawingPanel extends JPanel {
        private List<Point> points = new ArrayList<>();

        public DrawingPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
        }

        public void setPoints(List<Point> points) {
            this.points = points;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;

            // Сохраняем текущую трансформацию
            AffineTransform originalTransform = g2d.getTransform();

            // Применяем масштаб
            g2d.scale(scale, scale);

            if (gridCheckBox.isSelected()) {
                drawGrid(g2d, width, height, centerX, centerY);
            }
            if (axesCheckBox.isSelected()) {
                drawAxes(g2d, width, height, centerX, centerY);
            }
            if (coordinatesCheckBox.isSelected()) {
                drawCoordinates(g2d, width, height, centerX, centerY);
            }

            drawPoints(g2d, centerX, centerY);

            // Восстанавливаем трансформацию
            g2d.setTransform(originalTransform);
        }

        private void drawGrid(Graphics2D g2d, int width, int height, int centerX, int centerY) {
            g2d.setColor(new Color(230, 230, 230));

            // Вертикальные линии
            for (int x = centerX % GRID_SIZE; x < width; x += GRID_SIZE) {
                g2d.drawLine(x, 0, x, height);
            }
            // Горизонтальные линии
            for (int y = centerY % GRID_SIZE; y < height; y += GRID_SIZE) {
                g2d.drawLine(0, y, width, y);
            }
        }

        private void drawAxes(Graphics2D g2d, int width, int height, int centerX, int centerY) {
            g2d.setColor(new Color(60, 60, 60));
            g2d.setStroke(new BasicStroke(2));

            // Ось X
            g2d.drawLine(0, centerY, width, centerY);
            // Ось Y
            g2d.drawLine(centerX, 0, centerX, height);

            // Стрелки
            g2d.fillPolygon(new int[]{width - 10, width - 10, width}, new int[]{centerY - 5, centerY + 5, centerY}, 3);
            g2d.fillPolygon(new int[]{centerX - 5, centerX + 5, centerX}, new int[]{10, 10, 0}, 3);
        }

        private void drawCoordinates(Graphics2D g2d, int width, int height, int centerX, int centerY) {
            g2d.setColor(new Color(80, 80, 80));
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            // Подписи по оси X
            for (int x = centerX + GRID_SIZE; x < width; x += GRID_SIZE) {
                int value = (x - centerX) / GRID_SIZE;
                g2d.drawString(String.valueOf(value), x - 5, centerY + 15);
                g2d.drawString(String.valueOf(-value), centerX - (x - centerX) - 5, centerY + 15);
            }

            // Подписи по оси Y
            for (int y = centerY + GRID_SIZE; y < height; y += GRID_SIZE) {
                int value = (y - centerY) / GRID_SIZE;
                g2d.drawString(String.valueOf(-value), centerX + 5, y + 5);
                g2d.drawString(String.valueOf(value), centerX + 5, centerY - (y - centerY) + 5);
            }

            // Подписи осей
            g2d.drawString("X", width - 15, centerY - 10);
            g2d.drawString("Y", centerX + 10, 15);
        }

        private void drawPoints(Graphics2D g2d, int centerX, int centerY) {
            g2d.setColor(new Color(220, 60, 60));

            for (Point p : points) {
                int x = centerX + p.x * GRID_SIZE;
                int y = centerY - p.y * GRID_SIZE; // инвертируем Y для правильного отображения

                g2d.fillOval(x - 3, y - 3, 6, 6);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Main().setVisible(true);
        });
    }
}