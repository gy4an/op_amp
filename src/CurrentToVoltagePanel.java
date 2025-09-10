import javax.swing.*;
import java.awt.*;

public class CurrentToVoltagePanel extends JPanel {

    public CurrentToVoltagePanel() {
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === LEFT PANEL: Inputs ===
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JTextField iinField = new JTextField();
        JTextField rfField = new JTextField();
        JTextField vpField = new JTextField();
        JTextField vmField = new JTextField();

        JComboBox<String> unitIin = new JComboBox<>(new String[]{"A", "mA", "μA"});
        JComboBox<String> unitRf = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> signBox = new JComboBox<>(new String[]{
                "Inverting (Vo = -Iin × Rf)",
                "Non-inverting (Vo = +Iin × Rf)"
        });

        inputPanel.add(createInputRow("Iin:", iinField, unitIin));
        inputPanel.add(createInputRow("Rf:", rfField, unitRf));

        JPanel signPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel signLabel = new JLabel("Sign:");
        signLabel.setPreferredSize(new Dimension(70, 25));
        signBox.setPreferredSize(new Dimension(220, 25));
        signPanel.add(signLabel);
        signPanel.add(signBox);
        inputPanel.add(signPanel);

        inputPanel.add(createInputRow("V+ (rail):", vpField, new JLabel("V")));
        inputPanel.add(createInputRow("V- (rail):", vmField, new JLabel("V")));

        // === Compute Button ===
        // === Compute Button ===
        JButton computeBtn = new JButton("Compute");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // LEFT aligned
        buttonPanel.add(computeBtn);


        // === OUTPUT ===
        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel outputLabel = new JLabel("Output: ");
        JTextField outputField = new JTextField(20);
        outputField.setEditable(false);
        outputField.setBackground(Color.WHITE);

        outputPanel.add(outputLabel);
        outputPanel.add(outputField);

        // === Steps / Explanation ===
        JPanel explanationPanel = new JPanel(new BorderLayout());
        JLabel stepsLabel = new JLabel("Steps / Explanation:");
        JTextArea explanationArea = new JTextArea(8, 30);
        explanationArea.setEditable(false);
        explanationArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(explanationArea);

        explanationPanel.add(stepsLabel, BorderLayout.NORTH);
        explanationPanel.add(scrollPane, BorderLayout.CENTER);

        // === Button Action ===
        computeBtn.addActionListener(e -> {
            try {
                // Parse inputs
                double Iin = parseWithCurrent(iinField.getText(), (String) unitIin.getSelectedItem());
                double Rf = parseWithResistance(rfField.getText(), (String) unitRf.getSelectedItem());
                // Default to ±infinity if empty (no saturation)
                double Vplus = vpField.getText().isEmpty() ? Double.POSITIVE_INFINITY : Double.parseDouble(vpField.getText());
                double Vminus = vmField.getText().isEmpty() ? Double.NEGATIVE_INFINITY : Double.parseDouble(vmField.getText());

                String signChoice = (String) signBox.getSelectedItem();
                double Vo;

                // Apply sign convention
                if (signChoice.startsWith("Inverting")) {
                    Vo = -Iin * Rf;
                } else {
                    Vo = Iin * Rf;
                }

                // Save unclipped value
                double unclippedVo = Vo;

                // Apply rail clipping
                if (Vo > Vplus) {
                    Vo = Vplus;
                } else if (Vo < Vminus) {
                    Vo = Vminus;
                }

                // Format output
                String resultText;
                if (Math.abs(Vo) < 1) {
                    resultText = String.format("%.2f mV", Vo * 1000);
                } else {
                    resultText = String.format("%.2f V", Vo);
                }

                outputField.setText(resultText);

                // Steps / Explanation
                StringBuilder steps = new StringBuilder();
                steps.append("Step 1: Convert inputs to base units\n");
                steps.append(String.format("Iin = %.6f A\n", Iin));
                steps.append(String.format("Rf = %.2f Ω\n", Rf));

                steps.append("\nStep 2: Apply formula\n");
                steps.append(String.format("Vo = %sIin × Rf\n",
                        signChoice.startsWith("Inverting") ? "-" : "+"));
                steps.append(String.format("Vo = %.6f × %.2f = %.3f V (before clipping)\n",
                        (signChoice.startsWith("Inverting") ? -Iin : Iin), Rf, unclippedVo));

                steps.append("\nStep 3: Apply rail limits\n");
                steps.append(String.format("V+ rail = %.2f V, V- rail = %.2f V\n", Vplus, Vminus));
                if (unclippedVo != Vo) {
                    steps.append(String.format("Unclipped Vo = %.3f V → Clipped to %.3f V\n", unclippedVo, Vo));
                } else {
                    steps.append("No clipping applied.\n");
                }

                explanationArea.setText(steps.toString());

            } catch (NumberFormatException ex) {
                outputField.setText("Invalid input!");
                explanationArea.setText("⚠ Please check that all inputs are valid numbers.");
            }
        });

        // Add everything to the left panel
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(buttonPanel); // Centered Compute button
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(outputPanel);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(explanationPanel);

        // === RIGHT PANEL: Formula + Circuit Image ===
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel formulaPanel = new JPanel();
        JLabel formulaLabel = new JLabel("Formula: Vo = ± Iin × Rf (depends on convention)");
        formulaPanel.add(formulaLabel);

        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon("images/CurrentToVoltage.png");
        imageLabel.setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance(400, 250, Image.SCALE_SMOOTH)));

        rightPanel.add(formulaPanel, BorderLayout.NORTH);
        rightPanel.add(imageLabel, BorderLayout.CENTER);

        add(inputPanel);
        add(rightPanel);
    }

    private JPanel createInputRow(String labelText, JTextField field, JComboBox<String> unitBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(70, 25));
        field.setPreferredSize(new Dimension(100, 25));
        unitBox.setPreferredSize(new Dimension(80, 25));
        panel.add(label);
        panel.add(field);
        panel.add(unitBox);
        return panel;
    }

    private JPanel createInputRow(String labelText, JTextField field, JLabel unitLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(70, 25));
        field.setPreferredSize(new Dimension(100, 25));
        unitLabel.setPreferredSize(new Dimension(40, 25));
        panel.add(label);
        panel.add(field);
        panel.add(unitLabel);
        return panel;
    }

    private double parseWithResistance(String value, String unit) {
        double base = Double.parseDouble(value);
        return switch (unit) {
            case "kΩ" -> base * 1_000;
            case "MΩ" -> base * 1_000_000;
            default -> base;
        };
    }

    private double parseWithCurrent(String value, String unit) {
        double base = Double.parseDouble(value);
        return switch (unit) {
            case "mA" -> base / 1_000;
            case "μA" -> base / 1_000_000;
            default -> base;
        };
    }
}
