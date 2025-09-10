import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Op-Amp Circuits Graphical Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Inverting", createPanel("Inverting Amplifier"));
        tabbedPane.addTab("Non-Inverting", createPanel("Non-Inverting Amplifier"));
        tabbedPane.addTab("Differential", createPanel("Differential Amplifier"));
        tabbedPane.addTab("Integrator", createPanel("Integrator Circuit"));
        tabbedPane.addTab("Current to Voltage", createPanel("Current to Voltage Converter"));
        tabbedPane.addTab("Voltage to Current", createPanel("Voltage to Current Converter"));

        add(tabbedPane);
    }

    private JPanel createPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("This is the " + title + " panel", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);

        // You can add input fields, graphs, and formulas here for each topic.
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}