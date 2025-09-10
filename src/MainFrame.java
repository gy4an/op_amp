import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Op-Amp Circuits Application");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Inverting", new InvertingPanel());
        tabbedPane.addTab("Non-Inverting", new NonInvertingPanel());
        tabbedPane.addTab("Differential", new DifferentialPanel());
        tabbedPane.addTab("Integrator", new IntegratorPanel());
        tabbedPane.addTab("Current to Voltage", new CurrentToVoltagePanel());
        tabbedPane.addTab("Voltage to Current", new VoltageToCurrentPanel());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
