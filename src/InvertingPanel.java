import javax.swing.*;
import java.awt.*;

public class InvertingPanel extends JPanel {

    public InvertingPanel() {
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JTextField r1Field = new JTextField();
        JTextField rfField = new JTextField();
        JTextField viField = new JTextField();

        JComboBox<String> unitR1 = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitRf = new JComboBox<>(new String[]{"Ω", "kΩ", "MΩ"});
        JComboBox<String> unitVi = new JComboBox<>(new String[]{"V", "mV"});

        inputPanel.add(createInputRow("R1:", r1Field, unitR1));
        inputPanel.add(createInputRow("Rf:", rfField, unitRf));
        inputPanel.add(createInputRow("Vi:", viField, unitVi));

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
                double R1 = parseWithUnit(r1Field.getText(), (String) unitR1.getSelectedItem());
                double Rf = parseWithUnit(rfField.getText(), (String) unitRf.getSelectedItem());
                double Vi = parseWithUnit(viField.getText(), (String) unitVi.getSelectedItem());

                if (R1 == 0) {
                    outputField.setText("Error: R1 must not be 0");
                    return;
                }

                double Vo = -(Rf / R1) * Vi;

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

        JPanel rightPanel = new JPanel(new BorderLayout());

        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon("images/inverting_opamp.png"); // Make sure this image exists
        imageLabel.setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance(400, 200, Image.SCALE_SMOOTH)));

        JLabel formulaLabel = new JLabel("<html><center><b>Formula:</b><br>"
                + "V<sub>o</sub> = -(Rf / R1) × V<sub>i</sub></center></html>", SwingConstants.CENTER);

        rightPanel.add(formulaLabel, BorderLayout.NORTH);
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
}
