import javax.swing.*;

public class Frame extends JFrame {

    private final JPanel panel;

    public Frame() {

        panel = new MainPanel();

        setVisible(true);
        setBounds(100, 100, 850, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
    }

    public Frame(PaintPanel panel) {

        this.panel = panel;
        setVisible(true);
        setBounds(30, 30, 820, 820);
        setContentPane(panel);
    }

}
