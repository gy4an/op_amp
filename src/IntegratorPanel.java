import javax.swing.*;
import java.awt.*;

public class IntegratorPanel extends JPanel {

    public IntegratorPanel() {
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === LEFT PANEL: Inputs ===
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JTextField rField = new JTextField();
        JTextField cField = new JTextField();
        JTextField vinField = new JTextField();
        JTextField timeField = new JTextField();

        JComboBox<String> unitR = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitC = new JComboBox<>(new String[]{"F", "mF", "μF", "nF"});
        JComboBox<String> unitVin = new JComboBox<>(new String[]{"V", "mV"});
        JComboBox<String> unitTime = new JComboBox<>(new String[]{"s", "ms", "μs"});

        inputPanel.add(createInputRow("R:", rField, unitR));
        inputPanel.add(createInputRow("C:", cField, unitC));
        inputPanel.add(createInputRow("Vin:", vinField, unitVin));
        inputPanel.add(createInputRow("t:", timeField, unitTime));

        // === OUTPUT ===
        JButton computeBtn = new JButton("Compute");

        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel outputLabel = new JLabel("Output: ");
        JTextField outputField = new JTextField(20);
        outputField.setEditable(false);
        outputField.setBackground(Color.WHITE);

        outputPanel.add(outputLabel);
        outputPanel.add(outputField);

        computeBtn.addActionListener(e -> {
            try {
                double R = parseWithUnit(rField.getText(), (String) unitR.getSelectedItem());
                double C = parseWithCapacitance(cField.getText(), (String) unitC.getSelectedItem());
                double Vin = parseWithUnit(vinField.getText(), (String) unitVin.getSelectedItem());

                String tText = timeField.getText().trim();
                double t = tText.isEmpty() ? 0 : parseTime(tText, (String) unitTime.getSelectedItem());

                if (R == 0 || C == 0) {
                    outputField.setText("Error: R and C must not be 0");
                    return;
                }

                // Symbolic output if t == 0
                if (t == 0) {
                    double symbolicVo = -(Vin) / (R * C) * t;
                    if (Math.abs(symbolicVo) < 1) {
                        outputField.setText(String.format("%.2f t mV", symbolicVo * 1000));
                    } else {
                        outputField.setText(String.format("%.2f t V", symbolicVo));
                    }
                } else {
                    // Numeric output
                    double Vo = -(Vin * t) / (R * C);
                    if (Math.abs(Vo) < 1) {
                        outputField.setText(String.format("%.2f mV", Vo * 1000));
                    } else {
                        outputField.setText(String.format("%.2f V", Vo));
                    }
                }

            } catch (NumberFormatException ex) {
                outputField.setText("Invalid input!");
            }
        });

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(computeBtn);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(outputPanel);

        // === RIGHT PANEL: Formula Image ===
        JPanel rightPanel = new JPanel(new BorderLayout());



        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon("images/integrator_opamp.png"); // Make sure this exists
        imageLabel.setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance(400, 200, Image.SCALE_SMOOTH)));

        rightPanel.add(imageLabel, BorderLayout.CENTER);

        add(inputPanel);
        add(rightPanel);
    }

    private JPanel createInputRow(String labelText, JTextField field, JComboBox<String> unitBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(50, 25));
        field.setPreferredSize(new Dimension(100, 25));
        unitBox.setPreferredSize(new Dimension(60, 25));
        panel.add(label);
        panel.add(field);
        panel.add(unitBox);
        return panel;
    }

    private double parseWithUnit(String value, String unit) {
        double base = Double.parseDouble(value);
        return switch (unit) {
            case "kΩ" -> base * 1_000;
            case "MΩ" -> base * 1_000_000;
            case "mV" -> base / 1_000;
            default -> base;
        };
    }

    private double parseWithCapacitance(String value, String unit) {
        double base = Double.parseDouble(value);
        return switch (unit) {
            case "mF" -> base / 1_000;
            case "μF" -> base / 1_000_000;
            case "nF" -> base / 1_000_000_000;
            default -> base;
        };
    }

    private double parseTime(String value, String unit) {
        double base = Double.parseDouble(value);
        return switch (unit) {
            case "ms" -> base / 1_000;
            case "μs" -> base / 1_000_000;
            default -> base;
        };
    }
}
