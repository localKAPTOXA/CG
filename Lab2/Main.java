import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class Main extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private JLabel originalImageLabel;
    private JLabel processedImageLabel;
    private JComboBox<String> operationComboBox;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> morphComboBox;
    private JComboBox<String> structElementComboBox;
    private JSlider kernelSizeSlider;
    private JSlider sigmaSlider;
    private JLabel kernelSizeLabel;
    private JLabel sigmaLabel;
    private JLabel kernelSizeTitleLabel;
    private JLabel sigmaTitleLabel;

    public Main() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Обработка изображений - Вариант 17");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель для изображений
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        originalImageLabel = new JLabel("Исходное изображение", SwingConstants.CENTER);
        processedImageLabel = new JLabel("Обработанное изображение", SwingConstants.CENTER);

        originalImageLabel.setPreferredSize(new Dimension(400, 400));
        processedImageLabel.setPreferredSize(new Dimension(400, 400));
        originalImageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        processedImageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        imagePanel.add(originalImageLabel);
        imagePanel.add(processedImageLabel);

        // Панель управления
        JPanel controlPanel = new JPanel(new GridLayout(7, 2, 5, 5));

        // Кнопка загрузки изображения
        JButton loadButton = new JButton("Загрузить изображение");
        loadButton.addActionListener(new LoadImageListener());

        // Выбор типа операции
        controlPanel.add(new JLabel("Тип операции:"));
        operationComboBox = new JComboBox<>(new String[]{
                "Низкочастотный фильтр", "Морфологическая обработка"
        });
        operationComboBox.addActionListener(new OperationTypeListener());
        controlPanel.add(operationComboBox);

        // Выбор фильтра (изначально видим)
        controlPanel.add(new JLabel("Низкочастотный фильтр:"));
        filterComboBox = new JComboBox<>(new String[]{
                "Гауссовский", "Усредняющий", "Медианный"
        });
        filterComboBox.addActionListener(new FilterTypeListener());
        controlPanel.add(filterComboBox);

        // Выбор морфологической операции (изначально скрыт)
        controlPanel.add(new JLabel("Морфологическая операция:"));
        morphComboBox = new JComboBox<>(new String[]{
                "Эрозия", "Дилатация", "Открытие", "Закрытие"
        });
        controlPanel.add(morphComboBox);
        morphComboBox.setVisible(false);
        controlPanel.getComponent(4).setVisible(false); // Скрываем и label

        // Выбор структурирующего элемента (изначально скрыт)
        controlPanel.add(new JLabel("Структурный элемент:"));
        structElementComboBox = new JComboBox<>(new String[]{
                "Квадрат 3x3", "Квадрат 5x5", "Круг", "Крест"
        });
        controlPanel.add(structElementComboBox);
        structElementComboBox.setVisible(false);
        controlPanel.getComponent(6).setVisible(false); // Скрываем и label

        // Слайдер для размера ядра
        kernelSizeTitleLabel = new JLabel("Размер ядра:");
        controlPanel.add(kernelSizeTitleLabel);
        JPanel kernelPanel = new JPanel(new BorderLayout());
        kernelSizeSlider = new JSlider(3, 15, 3);
        kernelSizeSlider.setMajorTickSpacing(2);
        kernelSizeSlider.setPaintTicks(true);
        kernelSizeLabel = new JLabel("3x3");
        kernelSizeSlider.addChangeListener(new KernelSizeListener());
        kernelPanel.add(kernelSizeSlider, BorderLayout.CENTER);
        kernelPanel.add(kernelSizeLabel, BorderLayout.EAST);
        controlPanel.add(kernelPanel);

        // Слайдер для sigma (для Гауссовского фильтра)
        sigmaTitleLabel = new JLabel("Sigma (Гаусс):");
        controlPanel.add(sigmaTitleLabel);
        JPanel sigmaPanel = new JPanel(new BorderLayout());
        sigmaSlider = new JSlider(1, 50, 10);
        sigmaSlider.setMajorTickSpacing(10);
        sigmaSlider.setPaintTicks(true);
        sigmaLabel = new JLabel("1.0");
        sigmaSlider.addChangeListener(new SigmaListener());
        sigmaPanel.add(sigmaSlider, BorderLayout.CENTER);
        sigmaPanel.add(sigmaLabel, BorderLayout.EAST);
        controlPanel.add(sigmaPanel);

        // Кнопка применения фильтров
        JButton applyButton = new JButton("Применить обработку");
        applyButton.addActionListener(new ApplyFilterListener());

        // Кнопка сохранения
        JButton saveButton = new JButton("Сохранить результат");
        saveButton.addActionListener(new SaveImageListener());

        // Основная компоновка
        add(imagePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loadButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(saveButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Изначально скрываем слайдер sigma (пока не выбран Гауссовский фильтр)
        updateSigmaVisibility();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class OperationTypeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedOperation = (String) operationComboBox.getSelectedItem();
            boolean isFilter = "Низкочастотный фильтр".equals(selectedOperation);

            // Показываем/скрываем элементы управления в зависимости от выбора
            filterComboBox.setVisible(isFilter);
            morphComboBox.setVisible(!isFilter);
            structElementComboBox.setVisible(!isFilter);

            // Показываем/скрываем соответствующие labels
            Container parent = filterComboBox.getParent();
            for (Component comp : parent.getComponents()) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if ("Низкочастотный фильтр:".equals(label.getText())) {
                        label.setVisible(isFilter);
                    } else if ("Морфологическая операция:".equals(label.getText())) {
                        label.setVisible(!isFilter);
                    } else if ("Структурный элемент:".equals(label.getText())) {
                        label.setVisible(!isFilter);
                    }
                }
            }

            // Показываем/скрываем слайдеры в зависимости от типа операции
            kernelSizeSlider.setVisible(isFilter);
            kernelSizeLabel.setVisible(isFilter);
            kernelSizeTitleLabel.setVisible(isFilter);

            // Для морфологических операций скрываем sigma
            if (!isFilter) {
                sigmaSlider.setVisible(false);
                sigmaLabel.setVisible(false);
                sigmaTitleLabel.setVisible(false);
            } else {
                // Для фильтров обновляем видимость sigma
                updateSigmaVisibility();
            }

            pack(); // Пересчитываем размер окна
        }
    }

    private class FilterTypeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateSigmaVisibility();
        }
    }

    private void updateSigmaVisibility() {
        String selectedFilter = (String) filterComboBox.getSelectedItem();
        boolean showSigma = "Гауссовский".equals(selectedFilter);

        sigmaSlider.setVisible(showSigma);
        sigmaLabel.setVisible(showSigma);
        sigmaTitleLabel.setVisible(showSigma);

        pack(); // Пересчитываем размер окна
    }

    private class LoadImageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Изображения", "jpg", "jpeg", "png", "bmp", "gif"));

            int result = fileChooser.showOpenDialog(Main.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    originalImage = ImageIO.read(file);
                    processedImage = null;
                    displayImage(originalImage, originalImageLabel);
                    processedImageLabel.setIcon(null);
                    processedImageLabel.setText("Обработанное изображение");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Main.this,
                            "Ошибка загрузки изображения: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class ApplyFilterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (originalImage == null) {
                JOptionPane.showMessageDialog(Main.this,
                        "Сначала загрузите изображение",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String selectedOperation = (String) operationComboBox.getSelectedItem();

            if ("Низкочастотный фильтр".equals(selectedOperation)) {
                // Применяем только низкочастотный фильтр
                String selectedFilter = (String) filterComboBox.getSelectedItem();
                processedImage = applyLowPassFilter(originalImage, selectedFilter);
            } else {
                // Применяем только морфологическую операцию
                String selectedMorph = (String) morphComboBox.getSelectedItem();
                String selectedStruct = (String) structElementComboBox.getSelectedItem();
                processedImage = applyMorphologicalOperation(originalImage, selectedMorph, selectedStruct);
            }

            displayImage(processedImage, processedImageLabel);
        }
    }

    private class SaveImageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (processedImage == null) {
                JOptionPane.showMessageDialog(Main.this,
                        "Нет обработанного изображения для сохранения",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "PNG изображения", "png"));

            int result = fileChooser.showSaveDialog(Main.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getParentFile(), file.getName() + ".png");
                    }
                    ImageIO.write(processedImage, "png", file);
                    JOptionPane.showMessageDialog(Main.this,
                            "Изображение успешно сохранено",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Main.this,
                            "Ошибка сохранения: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class KernelSizeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int size = kernelSizeSlider.getValue();
            // Гауссовский фильтр требует нечетный размер ядра
            if (size % 2 == 0) {
                size++;
                kernelSizeSlider.setValue(size);
            }
            kernelSizeLabel.setText(size + "x" + size);
        }
    }

    private class SigmaListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            double sigma = sigmaSlider.getValue() / 10.0;
            sigmaLabel.setText(String.format("%.1f", sigma));
        }
    }

    private void displayImage(BufferedImage image, JLabel label) {
        if (image != null) {
            int displayWidth = Math.min(400, image.getWidth());
            int displayHeight = Math.min(400, image.getHeight());

            ImageIcon icon = new ImageIcon(image.getScaledInstance(
                    displayWidth, displayHeight, Image.SCALE_SMOOTH));
            label.setIcon(icon);
            label.setText("");
        }
    }

    // Реализация низкочастотных фильтров
    private BufferedImage applyLowPassFilter(BufferedImage image, String filterType) {
        int kernelSize = kernelSizeSlider.getValue();

        switch (filterType) {
            case "Гауссовский":
                return applyGaussianFilter(image, kernelSize);
            case "Усредняющий":
                return applyAverageFilter(image, kernelSize);
            case "Медианный":
                return applyMedianFilter(image, kernelSize);
            default:
                return image;
        }
    }

    private BufferedImage applyGaussianFilter(BufferedImage image, int kernelSize) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        double sigma = sigmaSlider.getValue() / 10.0;

        // Создание гауссовского ядра
        double[][] kernel = createGaussianKernel(kernelSize, sigma);

        applyConvolution(image, result, kernel);
        return result;
    }

    private double[][] createGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        double sum = 0;
        int center = size / 2;

        for (int x = -center; x <= center; x++) {
            for (int y = -center; y <= center; y++) {
                double value = (1.0 / (2 * Math.PI * sigma * sigma)) *
                        Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                kernel[x + center][y + center] = value;
                sum += value;
            }
        }

        // Нормализация
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }

        return kernel;
    }

    private BufferedImage applyAverageFilter(BufferedImage image, int kernelSize) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        double[][] kernel = new double[kernelSize][kernelSize];
        double value = 1.0 / (kernelSize * kernelSize);

        for (int i = 0; i < kernelSize; i++) {
            for (int j = 0; j < kernelSize; j++) {
                kernel[i][j] = value;
            }
        }

        applyConvolution(image, result, kernel);
        return result;
    }

    private BufferedImage applyMedianFilter(BufferedImage image, int kernelSize) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        int radius = kernelSize / 2;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int[] redValues = new int[kernelSize * kernelSize];
                int[] greenValues = new int[kernelSize * kernelSize];
                int[] blueValues = new int[kernelSize * kernelSize];
                int count = 0;

                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int px = Math.min(Math.max(x + kx, 0), image.getWidth() - 1);
                        int py = Math.min(Math.max(y + ky, 0), image.getHeight() - 1);

                        int rgb = image.getRGB(px, py);
                        redValues[count] = (rgb >> 16) & 0xFF;
                        greenValues[count] = (rgb >> 8) & 0xFF;
                        blueValues[count] = rgb & 0xFF;
                        count++;
                    }
                }

                Arrays.sort(redValues, 0, count);
                Arrays.sort(greenValues, 0, count);
                Arrays.sort(blueValues, 0, count);

                int medianIndex = count / 2;
                int medianRGB = (redValues[medianIndex] << 16) |
                        (greenValues[medianIndex] << 8) |
                        blueValues[medianIndex];

                result.setRGB(x, y, medianRGB);
            }
        }

        return result;
    }

    private void applyConvolution(BufferedImage src, BufferedImage dst, double[][] kernel) {
        int width = src.getWidth();
        int height = src.getHeight();
        int kernelSize = kernel.length;
        int radius = kernelSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double red = 0, green = 0, blue = 0;

                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int px = Math.min(Math.max(x + kx, 0), width - 1);
                        int py = Math.min(Math.max(y + ky, 0), height - 1);

                        int rgb = src.getRGB(px, py);
                        double weight = kernel[ky + radius][kx + radius];

                        red += ((rgb >> 16) & 0xFF) * weight;
                        green += ((rgb >> 8) & 0xFF) * weight;
                        blue += (rgb & 0xFF) * weight;
                    }
                }

                int newRGB = ((Math.min(Math.max((int) red, 0), 255) << 16) |
                        (Math.min(Math.max((int) green, 0), 255) << 8) |
                        Math.min(Math.max((int) blue, 0), 255));

                dst.setRGB(x, y, newRGB);
            }
        }
    }

    // Реализация морфологических операций
    private BufferedImage applyMorphologicalOperation(BufferedImage image, String operation, String structElement) {
        // Преобразуем в полутоновое для морфологических операций
        BufferedImage grayImage = convertToGrayScale(image);

        switch (operation) {
            case "Эрозия":
                return erosion(grayImage, structElement);
            case "Дилатация":
                return dilation(grayImage, structElement);
            case "Открытие":
                BufferedImage eroded = erosion(grayImage, structElement);
                return dilation(eroded, structElement);
            case "Закрытие":
                BufferedImage dilated = dilation(grayImage, structElement);
                return erosion(dilated, structElement);
            default:
                return grayImage;
        }
    }

    private BufferedImage convertToGrayScale(BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = gray.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage erosion(BufferedImage image, String structElement) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        boolean[][] kernel = createStructuringElement(structElement);
        int size = kernel.length;
        int radius = size / 2;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int minValue = 255;

                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        if (kernel[ky + radius][kx + radius]) {
                            int px = Math.min(Math.max(x + kx, 0), image.getWidth() - 1);
                            int py = Math.min(Math.max(y + ky, 0), image.getHeight() - 1);

                            int gray = image.getRGB(px, py) & 0xFF;
                            minValue = Math.min(minValue, gray);
                        }
                    }
                }

                int newRGB = (minValue << 16) | (minValue << 8) | minValue;
                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }

    private BufferedImage dilation(BufferedImage image, String structElement) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        boolean[][] kernel = createStructuringElement(structElement);
        int size = kernel.length;
        int radius = size / 2;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int maxValue = 0;

                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        if (kernel[ky + radius][kx + radius]) {
                            int px = Math.min(Math.max(x + kx, 0), image.getWidth() - 1);
                            int py = Math.min(Math.max(y + ky, 0), image.getHeight() - 1);

                            int gray = image.getRGB(px, py) & 0xFF;
                            maxValue = Math.max(maxValue, gray);
                        }
                    }
                }

                int newRGB = (maxValue << 16) | (maxValue << 8) | maxValue;
                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }

    private boolean[][] createStructuringElement(String type) {
        switch (type) {
            case "Квадрат 3x3":
                return new boolean[][]{
                        {true, true, true},
                        {true, true, true},
                        {true, true, true}
                };
            case "Квадрат 5x5":
                boolean[][] square5x5 = new boolean[5][5];
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        square5x5[i][j] = true;
                    }
                }
                return square5x5;
            case "Круг":
                // Круг 5x5 (диск)
                return new boolean[][]{
                        {false, true,  true,  true,  false},
                        {true,  true,  true,  true,  true},
                        {true,  true,  true,  true,  true},
                        {true,  true,  true,  true,  true},
                        {false, true,  true,  true,  false}
                };
            case "Крест":
                // Крест 5x5 (плюс)
                return new boolean[][]{
                        {false, false, true,  false, false},
                        {false, false, true,  false, false},
                        {true,  true,  true,  true,  true},
                        {false, false, true,  false, false},
                        {false, false, true,  false, false}
                };
            default:
                return new boolean[3][3]; // Квадрат 3x3 по умолчанию
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main();
        });
    }
}