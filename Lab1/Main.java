import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main extends JFrame {
    private JPanel colorPreview;
    private JSlider cSlider, mSlider, ySlider, kSlider;
    private JTextField cField, mField, yField, kField;
    private JSlider rSlider, gSlider, bSlider;
    private JTextField rField, gField, bField;
    private JSlider hSlider, lSlider, sSlider;
    private JTextField hField, lField, sField;
    private JTextField hexField;
    private JPanel colorPalette;
    private DecimalFormat df = new DecimalFormat("0.00");

    // Храним значения для каждой модели отдельно
    private float[] cmykValues = new float[]{0, 0, 0, 0};
    private int[] rgbValues = new int[]{0, 0, 0};
    private float[] hlsValues = new float[]{0, 0, 0};

    // Флаги для предотвращения циклических обновлений
    private boolean updatingFromCMYK = false;
    private boolean updatingFromRGB = false;
    private boolean updatingFromHLS = false;
    private boolean updatingFromHex = false;

    // Палитра предустановленных цветов - используем LinkedHashMap для сохранения порядка
    private final Map<String, Color> presetColors = new LinkedHashMap<>();

    public Main() {
        initializePresetColors();
        setTitle("Color Models Converter - CMYK, RGB, HLS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initializeComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializePresetColors() {
        // Используем более простые и понятные названия цветов
        presetColors.put("Красный", new Color(255, 0, 0));
        presetColors.put("Зеленый", new Color(0, 255, 0));
        presetColors.put("Синий", new Color(0, 0, 255));
        presetColors.put("Желтый", new Color(255, 255, 0));
        presetColors.put("Голубой", new Color(0, 255, 255));
        presetColors.put("Пурпурный", new Color(255, 0, 255));
        presetColors.put("Черный", new Color(0, 0, 0));
        presetColors.put("Белый", new Color(255, 255, 255));
        presetColors.put("Серый", new Color(128, 128, 128));
        presetColors.put("Оранжевый", new Color(255, 165, 0));
        presetColors.put("Розовый", new Color(255, 192, 203));
        presetColors.put("Темно-серый", new Color(64, 64, 64));
    }

    private void initializeComponents() {
        // Preview Panel
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(300, 100));
        colorPreview.setBackground(getCurrentColor());
        add(colorPreview, BorderLayout.NORTH);

        // Tabbed Panes for Models
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("CMYK", createCMYKPanel());
        tabbedPane.addTab("RGB", createRGBPanel());
        tabbedPane.addTab("HLS", createHLSPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Панель с дополнительными способами выбора цвета
        JPanel controlPanel = new JPanel(new BorderLayout());

        // HEX ввод
        JPanel hexPanel = new JPanel(new FlowLayout());
        hexPanel.add(new JLabel("HEX: #"));
        hexField = new JTextField("000000", 6);
        hexField.addActionListener(e -> updateFromHex());
        hexPanel.add(hexField);
        JButton hexUpdateButton = new JButton("Применить");
        hexUpdateButton.addActionListener(e -> updateFromHex());
        hexPanel.add(hexUpdateButton);

        controlPanel.add(hexPanel, BorderLayout.NORTH);

        // Палитра цветов
        colorPalette = new JPanel(new GridLayout(2, 6, 5, 5));
        colorPalette.setBorder(BorderFactory.createTitledBorder("Палитра цветов"));
        initializeColorPalette();
        controlPanel.add(colorPalette, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.SOUTH);

        // Инициализируем HEX поле
        updateHexField();
    }

    private void initializeColorPalette() {
        // Очищаем палитру перед инициализацией
        colorPalette.removeAll();

        for (Map.Entry<String, Color> entry : presetColors.entrySet()) {
            ColorButton colorButton = new ColorButton(entry.getKey(), entry.getValue());
            colorButton.addActionListener(new ColorButtonListener(entry.getValue()));
            colorPalette.add(colorButton);
        }

        // Перерисовываем палитру
        colorPalette.revalidate();
        colorPalette.repaint();
    }

    // Специальный класс для кнопок цветов
    private class ColorButton extends JButton {
        public ColorButton(String text, Color color) {
            super(text);
            setBackground(color);
            setForeground(getContrastColor(color));
            setOpaque(true);
            setBorderPainted(true);
            setFocusPainted(false);
            setContentAreaFilled(true);
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

            // Устанавливаем фиксированный размер для всех кнопок
            setPreferredSize(new Dimension(80, 30));
            setMinimumSize(new Dimension(80, 30));
            setMaximumSize(new Dimension(80, 30));
        }
    }

    // Отдельный слушатель для каждой кнопки цвета
    private class ColorButtonListener implements ActionListener {
        private final Color color;

        public ColorButtonListener(Color color) {
            this.color = color;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setColorFromPreset(color);
        }
    }

    private Color getContrastColor(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private void setColorFromPreset(Color color) {
        // Устанавливаем все флаги обновления в true для предотвращения циклических обновлений
        updatingFromCMYK = true;
        updatingFromRGB = true;
        updatingFromHLS = true;
        updatingFromHex = true;

        // Устанавливаем цвет через RGB модель
        rgbValues[0] = color.getRed();
        rgbValues[1] = color.getGreen();
        rgbValues[2] = color.getBlue();

        // Обновляем все модели и интерфейс
        updateAllModelsFromRGB();

        // Снимаем флаги обновления
        updatingFromCMYK = false;
        updatingFromRGB = false;
        updatingFromHLS = false;
        updatingFromHex = false;

        // Перерисовываем компоненты
        colorPreview.repaint();
        revalidate();
        repaint();
    }

    private JPanel createCMYKPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        cSlider = new JSlider(0, 100, 0);
        mSlider = new JSlider(0, 100, 0);
        ySlider = new JSlider(0, 100, 0);
        kSlider = new JSlider(0, 100, 0);

        cField = new JTextField("0.00");
        mField = new JTextField("0.00");
        yField = new JTextField("0.00");
        kField = new JTextField("0.00");

        addSliderAndField(panel, "C:", cSlider, cField, "CMYK");
        addSliderAndField(panel, "M:", mSlider, mField, "CMYK");
        addSliderAndField(panel, "Y:", ySlider, yField, "CMYK");
        addSliderAndField(panel, "K:", kSlider, kField, "CMYK");

        return panel;
    }

    private JPanel createRGBPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2));

        rSlider = new JSlider(0, 255, 0);
        gSlider = new JSlider(0, 255, 0);
        bSlider = new JSlider(0, 255, 0);

        rField = new JTextField("0");
        gField = new JTextField("0");
        bField = new JTextField("0");

        addSliderAndField(panel, "R:", rSlider, rField, "RGB");
        addSliderAndField(panel, "G:", gSlider, gField, "RGB");
        addSliderAndField(panel, "B:", bSlider, bField, "RGB");

        return panel;
    }

    private JPanel createHLSPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2));

        hSlider = new JSlider(0, 360, 0);
        lSlider = new JSlider(0, 100, 0);
        sSlider = new JSlider(0, 100, 0);

        hField = new JTextField("0");
        lField = new JTextField("0.00");
        sField = new JTextField("0.00");

        addSliderAndField(panel, "H:", hSlider, hField, "HLS");
        addSliderAndField(panel, "L:", lSlider, lField, "HLS");
        addSliderAndField(panel, "S:", sSlider, sField, "HLS");

        return panel;
    }

    private void addSliderAndField(JPanel panel, String label, JSlider slider, JTextField field, String model) {
        panel.add(new JLabel(label));
        panel.add(slider);
        panel.add(new JLabel("Значение:"));
        panel.add(field);

        // Слушатель для ползунка - обновление в реальном времени
        slider.addChangeListener(e -> {
            updateFieldFromSlider(slider, field, model);
            updateFromModel(model);
        });

        field.addActionListener(e -> {
            try {
                double value = Double.parseDouble(field.getText());
                if (slider == hSlider) {
                    value = Math.max(0, Math.min(360, value));
                    slider.setValue((int) value);
                } else if (slider == rSlider || slider == gSlider || slider == bSlider) {
                    value = Math.max(0, Math.min(255, value));
                    slider.setValue((int) value);
                } else {
                    value = Math.max(0, Math.min(100, value));
                    slider.setValue((int) value);
                }
                updateFromModel(model);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректное число");
            }
        });
    }

    private void updateFieldFromSlider(JSlider slider, JTextField field, String model) {
        if (model.equals("RGB")) {
            field.setText(String.valueOf(slider.getValue()));
        } else if (model.equals("HLS") && slider == hSlider) {
            field.setText(String.valueOf(slider.getValue()));
        } else {
            field.setText(df.format(slider.getValue()));
        }
    }

    private void updateFromModel(String sourceModel) {
        // Проверяем флаги для предотвращения циклических обновлений
        switch (sourceModel) {
            case "CMYK":
                if (updatingFromRGB || updatingFromHLS || updatingFromHex || updatingFromCMYK) return;
                updateFromCMYK();
                break;
            case "RGB":
                if (updatingFromCMYK || updatingFromHLS || updatingFromHex || updatingFromRGB) return;
                updateFromRGB();
                break;
            case "HLS":
                if (updatingFromCMYK || updatingFromRGB || updatingFromHex || updatingFromHLS) return;
                updateFromHLS();
                break;
        }

        // Обновляем HEX поле при любом изменении
        if (!updatingFromHex) {
            updateHexField();
        }

        // Обновляем preview
        colorPreview.setBackground(getCurrentColor());
    }

    private void updateFromCMYK() {
        updatingFromCMYK = true;

        // Сохраняем значения CMYK
        cmykValues[0] = cSlider.getValue() / 100.0f;
        cmykValues[1] = mSlider.getValue() / 100.0f;
        cmykValues[2] = ySlider.getValue() / 100.0f;
        cmykValues[3] = kSlider.getValue() / 100.0f;

        // Ограничиваем значения для избежания выхода за границы
        for (int i = 0; i < 4; i++) {
            cmykValues[i] = Math.max(0, Math.min(1, cmykValues[i]));
        }

        // Преобразуем в RGB
        Color rgbColor = cmykToRgb(cmykValues[0], cmykValues[1], cmykValues[2], cmykValues[3]);
        rgbValues[0] = rgbColor.getRed();
        rgbValues[1] = rgbColor.getGreen();
        rgbValues[2] = rgbColor.getBlue();

        // Преобразуем в HLS
        float[] hls = rgbToHls(rgbValues[0], rgbValues[1], rgbValues[2]);
        hlsValues[0] = hls[0];
        hlsValues[1] = hls[1];
        hlsValues[2] = hls[2];

        // Обновляем поля других моделей
        updateRGBFields();
        updateHLSFields();

        updatingFromCMYK = false;
    }

    private void updateFromRGB() {
        updatingFromRGB = true;

        // Сохраняем значения RGB
        rgbValues[0] = rSlider.getValue();
        rgbValues[1] = gSlider.getValue();
        rgbValues[2] = bSlider.getValue();

        // Ограничиваем значения для избежания выхода за границы
        for (int i = 0; i < 3; i++) {
            rgbValues[i] = Math.max(0, Math.min(255, rgbValues[i]));
        }

        // Преобразуем в CMYK
        float[] cmyk = rgbToCmyk(rgbValues[0], rgbValues[1], rgbValues[2]);
        cmykValues[0] = cmyk[0];
        cmykValues[1] = cmyk[1];
        cmykValues[2] = cmyk[2];
        cmykValues[3] = cmyk[3];

        // Преобразуем в HLS
        float[] hls = rgbToHls(rgbValues[0], rgbValues[1], rgbValues[2]);
        hlsValues[0] = hls[0];
        hlsValues[1] = hls[1];
        hlsValues[2] = hls[2];

        // Обновляем поля других моделей
        updateCMYKFields();
        updateHLSFields();

        updatingFromRGB = false;
    }

    private void updateFromHLS() {
        updatingFromHLS = true;

        // Сохраняем значения HLS
        hlsValues[0] = hSlider.getValue();
        hlsValues[1] = lSlider.getValue() / 100.0f;
        hlsValues[2] = sSlider.getValue() / 100.0f;

        // Ограничиваем значения для избежания выхода за границы
        hlsValues[0] = Math.max(0, Math.min(360, hlsValues[0]));
        hlsValues[1] = Math.max(0, Math.min(1, hlsValues[1]));
        hlsValues[2] = Math.max(0, Math.min(1, hlsValues[2]));

        // Преобразуем в RGB
        Color rgbColor = hlsToRgb(hlsValues[0], hlsValues[1], hlsValues[2]);
        rgbValues[0] = rgbColor.getRed();
        rgbValues[1] = rgbColor.getGreen();
        rgbValues[2] = rgbColor.getBlue();

        // Преобразуем в CMYK
        float[] cmyk = rgbToCmyk(rgbValues[0], rgbValues[1], rgbValues[2]);
        cmykValues[0] = cmyk[0];
        cmykValues[1] = cmyk[1];
        cmykValues[2] = cmyk[2];
        cmykValues[3] = cmyk[3];

        // Обновляем поля других моделей
        updateCMYKFields();
        updateRGBFields();

        updatingFromHLS = false;
    }

    private void updateAllModelsFromRGB() {
        // Преобразуем в CMYK
        float[] cmyk = rgbToCmyk(rgbValues[0], rgbValues[1], rgbValues[2]);
        cmykValues[0] = cmyk[0];
        cmykValues[1] = cmyk[1];
        cmykValues[2] = cmyk[2];
        cmykValues[3] = cmyk[3];

        // Преобразуем в HLS
        float[] hls = rgbToHls(rgbValues[0], rgbValues[1], rgbValues[2]);
        hlsValues[0] = hls[0];
        hlsValues[1] = hls[1];
        hlsValues[2] = hls[2];

        // Обновляем все поля
        updateCMYKFields();
        updateRGBFields();
        updateHLSFields();
        updateHexField();
        colorPreview.setBackground(getCurrentColor());
    }

    private void updateFromHex() {
        String hexText = hexField.getText().trim();
        if (hexText.matches("[0-9A-Fa-f]{6}")) {
            try {
                updatingFromHex = true;
                int r = Integer.parseInt(hexText.substring(0, 2), 16);
                int g = Integer.parseInt(hexText.substring(2, 4), 16);
                int b = Integer.parseInt(hexText.substring(4, 6), 16);

                // Ограничиваем значения для избежания выхода за границы
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                rgbValues[0] = r;
                rgbValues[1] = g;
                rgbValues[2] = b;

                updateAllModelsFromRGB();
                updatingFromHex = false;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Неверный HEX формат");
                updatingFromHex = false;
            }
        } else {
            JOptionPane.showMessageDialog(this, "HEX должен содержать 6 символов (0-9, A-F)");
        }
    }

    private void updateHexField() {
        Color color = getCurrentColor();
        String hex = String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        hexField.setText(hex);
    }

    private void updateCMYKFields() {
        cSlider.setValue((int) (cmykValues[0] * 100));
        mSlider.setValue((int) (cmykValues[1] * 100));
        ySlider.setValue((int) (cmykValues[2] * 100));
        kSlider.setValue((int) (cmykValues[3] * 100));

        cField.setText(df.format(cmykValues[0] * 100));
        mField.setText(df.format(cmykValues[1] * 100));
        yField.setText(df.format(cmykValues[2] * 100));
        kField.setText(df.format(cmykValues[3] * 100));
    }

    private void updateRGBFields() {
        rSlider.setValue(rgbValues[0]);
        gSlider.setValue(rgbValues[1]);
        bSlider.setValue(rgbValues[2]);

        rField.setText(String.valueOf(rgbValues[0]));
        gField.setText(String.valueOf(rgbValues[1]));
        bField.setText(String.valueOf(rgbValues[2]));
    }

    private void updateHLSFields() {
        hSlider.setValue((int) hlsValues[0]);
        lSlider.setValue((int) (hlsValues[1] * 100));
        sSlider.setValue((int) (hlsValues[2] * 100));

        hField.setText(String.valueOf((int) hlsValues[0]));
        lField.setText(df.format(hlsValues[1] * 100));
        sField.setText(df.format(hlsValues[2] * 100));
    }

    private Color getCurrentColor() {
        return new Color(rgbValues[0], rgbValues[1], rgbValues[2]);
    }

    private Color cmykToRgb(float c, float m, float y, float k) {
        // Более точное преобразование CMYK в RGB
        int r = Math.round(255 * (1 - c) * (1 - k));
        int g = Math.round(255 * (1 - m) * (1 - k));
        int b = Math.round(255 * (1 - y) * (1 - k));

        // Ограничиваем значения для избежания выхода за границы
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new Color(r, g, b);
    }

    private float[] rgbToCmyk(int r, int g, int b) {
        // Более точное преобразование RGB в CMYK
        float r0 = r / 255.0f;
        float g0 = g / 255.0f;
        float b0 = b / 255.0f;

        float k = 1 - Math.max(r0, Math.max(g0, b0));

        // Избегаем деления на ноль
        if (k == 1) {
            return new float[]{0, 0, 0, 1};
        }

        float c = (1 - r0 - k) / (1 - k);
        float m = (1 - g0 - k) / (1 - k);
        float y = (1 - b0 - k) / (1 - k);

        // Ограничиваем значения для избежания выхода за границы
        c = Math.max(0, Math.min(1, c));
        m = Math.max(0, Math.min(1, m));
        y = Math.max(0, Math.min(1, y));
        k = Math.max(0, Math.min(1, k));

        return new float[]{c, m, y, k};
    }

    private float[] rgbToHls(int r, int g, int b) {
        float r0 = r / 255.0f;
        float g0 = g / 255.0f;
        float b0 = b / 255.0f;

        float max = Math.max(r0, Math.max(g0, b0));
        float min = Math.min(r0, Math.min(g0, b0));
        float delta = max - min;

        float h = 0, l = (max + min) / 2, s = 0;

        if (delta != 0) {
            s = delta / (1 - Math.abs(2 * l - 1));

            if (max == r0) {
                h = (g0 - b0) / delta;
                if (g0 < b0) h += 6;
            } else if (max == g0) {
                h = (b0 - r0) / delta + 2;
            } else {
                h = (r0 - g0) / delta + 4;
            }
            h *= 60;
        }

        // Нормализуем значения
        h = h % 360;
        if (h < 0) h += 360;
        s = Math.max(0, Math.min(1, s));
        l = Math.max(0, Math.min(1, l));

        return new float[]{h, l, s};
    }

    private Color hlsToRgb(float h, float l, float s) {
        // Нормализуем значения
        h = h % 360;
        if (h < 0) h += 360;
        s = Math.max(0, Math.min(1, s));
        l = Math.max(0, Math.min(1, l));

        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = l - c / 2;

        float r1, g1, b1;

        if (h < 60) {
            r1 = c; g1 = x; b1 = 0;
        } else if (h < 120) {
            r1 = x; g1 = c; b1 = 0;
        } else if (h < 180) {
            r1 = 0; g1 = c; b1 = x;
        } else if (h < 240) {
            r1 = 0; g1 = x; b1 = c;
        } else if (h < 300) {
            r1 = x; g1 = 0; b1 = c;
        } else {
            r1 = c; g1 = 0; b1 = x;
        }

        int r = Math.round((r1 + m) * 255);
        int g = Math.round((g1 + m) * 255);
        int b = Math.round((b1 + m) * 255);

        // Ограничиваем значения для избежания выхода за границы
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new Color(r, g, b);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}