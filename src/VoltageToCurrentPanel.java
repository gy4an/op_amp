import javax.swing.*;
import java.awt.*;

/**
 * Voltage -> Current (V-to-I) panel.
 *
 * Formula used:
 *   Iout = ± Vin / R
 *   Vout_required = Iout * RL
 *
 * If rails are provided and Vout_required is outside [V-, V+], Vout is clipped
 * and the achievable Iout = Vout_clipped / RL.
 */
public class VoltageToCurrentPanel extends JPanel {

    public VoltageToCurrentPanel() {
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === LEFT PANEL: Inputs ===
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JTextField vinField = new JTextField();
        JTextField rField = new JTextField();
        JTextField rlField = new JTextField();
        JTextField vpField = new JTextField();
        JTextField vmField = new JTextField();

        JComboBox<String> unitVin = new JComboBox<>(new String[]{"V", "mV"});
        JComboBox<String> unitR = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitRL = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});

        JComboBox<String> signBox = new JComboBox<>(new String[]{
                "Positive (Iout = +Vin / R)",
                "Negative (Iout = -Vin / R)"
        });
        signBox.setPreferredSize(new Dimension(220, 25));

        inputPanel.add(createInputRow("Vin:", vinField, unitVin));
        inputPanel.add(createInputRow("Rf:", rField, unitR));
        inputPanel.add(createInputRow("RL:", rlField, unitRL));

        // sign row
        JPanel signPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel signLabel = new JLabel("Sign:");
        signLabel.setPreferredSize(new Dimension(70, 25));
        signPanel.add(signLabel);
        signPanel.add(signBox);
        inputPanel.add(signPanel);

        // rails
        inputPanel.add(createInputRow("V+ (rail):", vpField, new JLabel("V")));
        inputPanel.add(createInputRow("V- (rail):", vmField, new JLabel("V")));

        // === Compute (left-aligned) ===
        JButton computeBtn = new JButton("Compute");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(computeBtn);

        // === Output area ===
        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel outputLabel = new JLabel("Output:");
        JTextField outputField = new JTextField(18);
        outputField.setEditable(false);
        outputField.setBackground(Color.WHITE);
        outputPanel.add(outputLabel);
        outputPanel.add(outputField);

        // Additional text to show required Vout
        JPanel voutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel voutLabel = new JLabel("Vout (req):");
        JTextField voutField = new JTextField(18);
        voutField.setEditable(false);
        voutField.setBackground(Color.WHITE);
        voutPanel.add(voutLabel);
        voutPanel.add(voutField);

        // === Steps / Explanation ===
        JPanel explanationPanel = new JPanel(new BorderLayout());
        JLabel stepsLabel = new JLabel("Steps / Explanation:");
        JTextArea explanationArea = new JTextArea(8, 30);
        explanationArea.setEditable(false);
        explanationArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(explanationArea);
        explanationPanel.add(stepsLabel, BorderLayout.NORTH);
        explanationPanel.add(scrollPane, BorderLayout.CENTER);

        // === Action: compute ===
        computeBtn.addActionListener(e -> {
            try {
                // Parse inputs to base units
                double Vin = parseWithVoltage(vinField.getText().trim(), (String) unitVin.getSelectedItem());
                double R = parseWithResistance(rField.getText().trim(), (String) unitR.getSelectedItem());

                Double RL = rlField.getText().trim().isEmpty() ? null
                        : parseWithResistance(rlField.getText().trim(), (String) unitRL.getSelectedItem());

                // rails: optional
                double Vplus = vpField.getText().trim().isEmpty() ? Double.POSITIVE_INFINITY
                        : Double.parseDouble(vpField.getText().trim());
                double Vminus = vmField.getText().trim().isEmpty() ? Double.NEGATIVE_INFINITY
                        : Double.parseDouble(vmField.getText().trim());

                if (R == 0) {
                    outputField.setText("Error: R must not be 0");
                    explanationArea.setText("Reference resistor R cannot be zero.");
                    return;
                }
                if (RL != null && RL == 0) {
                    outputField.setText("Error: RL must not be 0");
                    explanationArea.setText("Load resistor RL cannot be zero.");
                    return;
                }

                int sign = signBox.getSelectedIndex() == 0 ? +1 : -1;

                // unclipped Iout
                double Iout_unclipped = sign * Vin / R; // A

                // required Vout to produce that current through RL
                double Vout_required = (RL == null) ? Double.NaN : Iout_unclipped * RL;

                // clipping if rails provided
                double Vout_final = Vout_required;
                boolean clipped = false;
                if (RL != null) {
                    if (Vout_required > Vplus) {
                        Vout_final = Vplus;
                        clipped = true;
                    } else if (Vout_required < Vminus) {
                        Vout_final = Vminus;
                        clipped = true;
                    }
                }

                double Iout_final = Iout_unclipped;
                if (clipped && RL != null) {
                    Iout_final = Vout_final / RL;
                }

                // === Display Output ===
                String ioutDisplay;
                if (!clipped) {
                    ioutDisplay = formatCurrent(Iout_final);
                } else {
                    ioutDisplay = String.format("%s (achievable %s, clipped)",
                            formatCurrent(Iout_unclipped), formatCurrent(Iout_final));
                }
                outputField.setText(ioutDisplay);

                if (RL != null) {
                    voutField.setText(formatVoltage(Vout_final));
                } else {
                    voutField.setText("N/A (no RL)");
                }

                // === Steps text ===
                StringBuilder sb = new StringBuilder();
                sb.append("Step 1: Convert inputs to base units\n");
                sb.append(String.format("Vin = %s V\n", formatNumber(Vin)));
                sb.append(String.format("Rf = %s Ω\n", formatNumber(R)));
                if (RL != null) sb.append(String.format("RL = %s Ω\n", formatNumber(RL)));
                sb.append("\nStep 2: Apply formula\n");
                sb.append(String.format("Iout = %sVin / Rf = %s A\n",
                        sign == 1 ? "+" : "-", formatNumber(Iout_unclipped)));
                if (RL != null) {
                    sb.append(String.format("Vout_required = Iout × RL = %s V\n", formatNumber(Vout_required)));
                } else {
                    sb.append("(No RL provided → only computing Iout)\n");
                }

                if (RL != null) {
                    sb.append("\nStep 3: Apply rail limits\n");
                    sb.append(String.format("V+ rail = %s, V- rail = %s\n",
                            Double.isInfinite(Vplus) ? "none" : formatNumber(Vplus),
                            Double.isInfinite(Vminus) ? "none" : formatNumber(Vminus)));

                    if (clipped) {
                        sb.append(String.format("Required Vout (%s V) outside rails → clipped to %s V\n",
                                formatNumber(Vout_required), formatNumber(Vout_final)));
                        sb.append(String.format("Achievable Iout = Vout_clipped / RL = %s A\n",
                                formatNumber(Iout_final)));
                    } else {
                        sb.append("Vout_required is within rails → no clipping.\n");
                    }
                }

                explanationArea.setText(sb.toString());

            } catch (NumberFormatException ex) {
                outputField.setText("Invalid input!");
                voutField.setText("");
                explanationArea.setText("⚠ Please check that all inputs are valid numbers (Vin, R, RL, rails).");
            }
        });

        // assemble left panel
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(buttonPanel);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(outputPanel);
        inputPanel.add(voutPanel);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(explanationPanel);

        // === RIGHT PANEL: Formula + Image ===
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel formulaLabel = new JLabel("<html><b>Formula:</b> Iout = ± Vin / R<br/>Vout_required = Iout × RL</html>");
        formulaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(formulaLabel, BorderLayout.NORTH);

        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon("images/VoltageToCurrent.png");
        if (imageIcon.getIconWidth() > 0) {
            imageLabel.setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance(400, 250, Image.SCALE_SMOOTH)));
        } else {
            imageLabel.setText("<html><center>Image not found.<br>Put images/VoltageToCurrent.png in project</center></html>");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(400, 250));
        }
        rightPanel.add(imageLabel, BorderLayout.CENTER);

        add(inputPanel);
        add(rightPanel);
    }

    // helper: create row with a text field and a unit combo
    private JPanel createInputRow(String labelText, JTextField field, JComboBox<String> unitBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(70, 25));
        field.setPreferredSize(new Dimension(100, 25));
        unitBox.setPreferredSize(new Dimension(70, 25));
        panel.add(label);
        panel.add(field);
        panel.add(unitBox);
        return panel;
    }

    // create row with text field and unit label (like "V")
    private JPanel createInputRow(String labelText, JTextField field, JLabel unitLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(70, 25));
        field.setPreferredSize(new Dimension(100, 25));
        unitLabel.setPreferredSize(new Dimension(30, 25));
        panel.add(label);
        panel.add(field);
        panel.add(unitLabel);
        return panel;
    }

    // parse voltage input (V, mV)
    private double parseWithVoltage(String value, String unit) {
        double base = Double.parseDouble(value);
        return "mV".equals(unit) ? base / 1000.0 : base;
    }

    // parse resistance input (Ω, kΩ, MΩ) to ohms
    private double parseWithResistance(String value, String unit) {
        double base = Double.parseDouble(value);
        switch (unit) {
            case "kΩ": return base * 1_000.0;
            case "MΩ": return base * 1_000_000.0;
            default: return base;
        }
    }

    // formatting helpers
    private String formatNumber(double x) {
        if (Math.abs(x) >= 1.0) return String.format("%.3f", x);
        else if (Math.abs(x) >= 1e-3) return String.format("%.3f", x);
        else return String.format("%.6e", x);
    }

    private String formatCurrent(double i) {
        return (Math.abs(i) < 1.0)
                ? String.format("%.3f mA", i * 1e3)
                : String.format("%.3f A", i);
    }

    private String formatVoltage(double v) {
        return (Math.abs(v) < 1.0)
                ? String.format("%.2f mV", v * 1e3)
                : String.format("%.3f V", v);
    }
}
