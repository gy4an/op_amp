import javax.swing.*;
import java.awt.*;

public class DifferentialPanel extends JPanel {

    public DifferentialPanel() {
        setLayout(new GridLayout(1, 2, 10, 10)); // 1 row, 2 columns
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === LEFT: Input Form ===
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        // Fields
        JTextField r1Field = new JTextField();
        JTextField r2Field = new JTextField();
        JTextField r3Field = new JTextField();
        JTextField rfField = new JTextField();
        JTextField v1Field = new JTextField();
        JTextField v2Field = new JTextField();

        // Unit dropdowns
        JComboBox<String> unitR1 = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitR2 = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitR3 = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitRf = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitV1 = new JComboBox<>(new String[]{"V", "mV"});
        JComboBox<String> unitV2 = new JComboBox<>(new String[]{"V", "mV"});

        // Add input rows
        inputPanel.add(createInputRow("R1:", r1Field, unitR1));
        inputPanel.add(createInputRow("R2:", r2Field, unitR2));
        inputPanel.add(createInputRow("R3:", r3Field, unitR3));
        inputPanel.add(createInputRow("Rf:", rfField, unitRf));
        inputPanel.add(createInputRow("V1:", v1Field, unitV1));
        inputPanel.add(createInputRow("V2:", v2Field, unitV2));

        // === Output Section ===
        JButton computeBtn = new JButton("Compute");
        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel outputLabel = new JLabel("Output: ");
        JTextField outputField = new JTextField(20);
        outputField.setEditable(false);
        outputField.setBackground(Color.WHITE);
        outputPanel.add(outputLabel);
        outputPanel.add(outputField);

        // === Compute Action ===
        computeBtn.addActionListener(e -> {
            try {
                double R1 = parseWithUnit(r1Field.getText(), (String) unitR1.getSelectedItem());
                double Rf = parseWithUnit(rfField.getText(), (String) unitRf.getSelectedItem());
                double V1 = parseWithUnit(v1Field.getText(), (String) unitV1.getSelectedItem());
                double V2 = parseWithUnit(v2Field.getText(), (String) unitV2.getSelectedItem());

                if (R1 == 0) {
                    outputField.setText("Error: R1 must not be 0");
                    return;
                }

                double term1 = ((R1 + Rf) / R1) * (Rf / (R1 + Rf)) * V2;
                double term2 = (Rf / R1) * V1;
                double Vo = term1 - term2;

                // Adjust unit display
                if (Math.abs(Vo) < 1) {
                    outputField.setText(String.format("%.2f mV", Vo * 1000));
                } else {
                    outputField.setText(String.format("%.2f V", Vo));
                }

            } catch (NumberFormatException ex) {
                outputField.setText("Invalid input!");
            }
        });

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(computeBtn);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(outputPanel);

        // === RIGHT: Image and Formula ===
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon("images/differential_opamp.png"); // Make sure this image exists
        imageLabel.setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance(400, 200, Image.SCALE_SMOOTH)));

        JLabel formulaLabel = new JLabel("<html><center><b>Formula:</b><br>"
                + "V<sub>o</sub> = ((R1 + Rf)/R1) × (Rf / (R1 + Rf)) × V<sub>2</sub> − (Rf / R1) × V<sub>1</sub></center></html>",
                SwingConstants.CENTER);

        rightPanel.add(imageLabel, BorderLayout.CENTER);
        rightPanel.add(formulaLabel, BorderLayout.NORTH);

        // === Add Panels to Main Panel ===
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
}
