
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainPanel extends JPanel {

    private static final int MAX_COUNT_FUNCTIONS = 5;

    private boolean mode2D = true;

    private final List<JTextField> chars = new ArrayList<>();
    private final List<JTextField> limits = new ArrayList<>();
    private final List<JTextField> constants = new ArrayList<>();
    private final List<JTextField> functionsYList = new ArrayList<>();
    private final List<JLabel> functionsYLablesList = new ArrayList<>();
    private final List<JTextField> functionsZList = new ArrayList<>();
    private final List<JLabel> functionsZLablesList = new ArrayList<>();
    private final List<JLabel> calculateValuesList = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

    MainPanel() {

        setLayout(null);

        JButton plusFunctionButton = new JButton("+");
        plusFunctionButton.setBounds(0, 0, 45, 45);
        plusFunctionButton.addActionListener(actionEvent -> {

            if((functionsYList.size() < MAX_COUNT_FUNCTIONS) && (functionsYLablesList.size() < MAX_COUNT_FUNCTIONS)) {

                functionsYList.add(new JTextField());
                functionsYLablesList.add(new JLabel("Function " + functionsYLablesList.size() + " Y:"));
                functionsZList.add(new JTextField("0"));
                functionsZLablesList.add(new JLabel("Z:"));
                calculateValuesList.add(new JLabel("Y: Z:"));

                for(int i = 0; i < functionsYList.size(); i++) {

                    JTextField field = functionsYList.get(i);
                    JLabel label = functionsYLablesList.get(i);
                    JLabel calcLabel = calculateValuesList.get(i);
                    JTextField fieldZ = functionsZList.get(i);
                    JLabel labelZ = functionsZLablesList.get(i);

                    field.setBounds(100, ((30 * (i + 1)) + 60), 100, 30);
                    label.setBounds(0, ((30 * (i + 1)) + 60), 100, 30);
                    fieldZ.setBounds(270, ((30 * (i + 1)) + 60), 100, 30);
                    labelZ.setBounds(220, ((30 * (i + 1)) + 60), 50, 30);
                    calcLabel.setBounds(380, ((30 * (i + 1)) + 60), 600, 30);

                    add(label);
                    add(field);
                    add(calcLabel);

                    if(!mode2D) {

                        add(labelZ);
                        add(fieldZ);
                    }
                }

                repaint();
                }
            else JOptionPane.showMessageDialog(null, "Sorry, the maximum number of simultaneously generated graphs is " + MAX_COUNT_FUNCTIONS);
        });
        add(plusFunctionButton);

        JButton minusFunctionButton = new JButton("-");
        minusFunctionButton.setBounds(45, 0, 45, 45);
        minusFunctionButton.addActionListener(actionEvent -> {

            if((functionsYList.size() > 1) && (functionsYLablesList.size() > 1)) {

                remove(functionsYList.remove(functionsYList.size() - 1));
                remove(functionsYLablesList.remove(functionsYLablesList.size() - 1));
                remove(functionsZList.remove(functionsZList.size() - 1));
                remove(functionsZLablesList.remove(functionsZLablesList.size() - 1));
                remove(calculateValuesList.remove(calculateValuesList.size() - 1));

                repaint();
            }
            else JOptionPane.showMessageDialog(null, "Sorry, the minimum number of simultaneously generated graphs is 1");
        });
        add(minusFunctionButton);

        JButton derivativeButton = new JButton("dY/dX");
        derivativeButton.setBounds(95, 0, 75, 35);
        derivativeButton.addActionListener(actionEvent -> {

            functionsYList.forEach(f -> {
                try {
                    f.setText(Calculator.derivativeFunction(f.getText()));
                } catch (ScriptException e) {

                    logger.error("Error in derivative function ", e);
                }
            });

            if(!mode2D) functionsZList.forEach(f -> {

                try {

                    f.setText(Calculator.derivativeFunction(f.getText()));
                }
                catch (ScriptException e) {

                    logger.error("Error in derivative function ", e);
                }
            });
        });
        add(derivativeButton);

        JTextField stepField = new JTextField("0.1");
        JLabel stepLabel = new JLabel("Step:");

        stepField.setBounds(450, 300, 50, 30);
        stepLabel.setBounds(400, 300, 50, 30);

        add(stepField);
        add(stepLabel);

        JTextField calculateField = new JTextField("0");
        calculateField.setVisible(true);
        calculateField.setBounds(330, 300, 50, 30);
        add(calculateField);

        JButton calculateButton = new JButton("Calculate values");
        calculateButton.setBounds(180, 300, 130, 30);
        calculateButton.setVisible(true);
        calculateButton.addActionListener(actionEvent -> {

            logger.info("Calculate value");

            for(int i = 0; i < functionsYList.size(); i++) {

                double x = Double.parseDouble(calculateField.getText());
                double y = Double.NaN;
                double z = Double.NaN;

                try {

                    y = PaintPanel.calculate(functionsYList.get(i).getText(), x);
                    if(!mode2D) z = PaintPanel.calculate(functionsZList.get(i).getText(), x);
                }
                catch (ScriptException e) { logger.error("Calculator script error", e); }

                calculateValuesList.get(i).setText("Y(" + x + ")= " + String.format(Locale.US, "%.2f", y) + "; Z(" + x + ")= " + String.format( Locale.US, "%.2f", z));
            }

            repaint();
        });
        add(calculateButton);

        for(int i = 0; i < 3; i++) {

            JLabel label = new JLabel();
            label.setBounds(750, (270 + (30 * i)), 40, 30);

            JTextField field = new JTextField();
            field.setBounds(770, (270 + (30 * i)), 30,30);

            switch (i) {

                case 0: {

                    label.setText("X:");
                    field.setText("x");
                    break;
                }
                case 1: {

                    label.setText("Y:");
                    field.setText("y");
                    break;
                }
                case 2: {

                    label.setText("Z:");
                    field.setText("z");
                    break;
                }
            }

            chars.add(field);

            add(label);
            add(field);
        }

        JTextField fieldXValue = new JTextField("x");
        fieldXValue.setBounds(770, 270, 30,30);
        add(fieldXValue);

        JButton createGraphicsButton = new JButton("Create graphics");
        createGraphicsButton.setBounds(20, 300, 130, 30);
        createGraphicsButton.addActionListener(actionEvent -> {

            PaintPanel panel;
            Map<String, Double> consts = new HashMap<>();

            logger.info("Creating graphics");

            constants.forEach(f -> {

                String[] in = f.getText().split("=");
                if(in.length > 1) consts.put(in[0], Double.parseDouble(in[1]));
            });

            if(mode2D) panel = new PaintPanel(consts,
                    limits.stream().map(f -> Double.parseDouble(f.getText())).collect(Collectors.toList()),
                    chars.stream().map(JTextComponent::getText).collect(Collectors.toList()),
                    Double.parseDouble(stepField.getText()),
                    functionsYList.stream().map(JTextComponent::getText).collect(Collectors.toList())
            );
            else panel = new PaintPanel(consts,
                    limits.stream().map(f -> Double.parseDouble(f.getText())).collect(Collectors.toList()),
                    chars.stream().map(JTextComponent::getText).collect(Collectors.toList()),
                    Double.parseDouble(stepField.getText()),
                    functionsYList.stream().map(JTextComponent::getText).collect(Collectors.toList()),
                    functionsZList.stream().map(JTextComponent::getText).collect(Collectors.toList())
            );

            new Frame(panel);

            panel.repaint();
        });
        add(createGraphicsButton);

        JButton changeModeButton = new JButton("3D");
        changeModeButton.setBounds(400, 0, 50, 50);
        changeModeButton.addActionListener(actionEvent -> {

            logger.info("Change mode");

            mode2D = !mode2D;

            changeModeButton.setText((changeModeButton.getText().equals("3D")) ? "2D" : "3D");

            if(!mode2D) {

                for (int i = 0; i < functionsZList.size(); i++) {

                    add(functionsZList.get(i));
                    add(functionsZLablesList.get(i));
                }
            }
            else {

                for (int i = 0; i < functionsZList.size(); i++) {

                    remove(functionsZList.get(i));
                    remove(functionsZLablesList.get(i));
                }
            }
            repaint();
        });
        add(changeModeButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setVisible(true);
        exitButton.setBounds(610, 0, 80, 20);
        exitButton.addActionListener(actionEvent -> System.exit(0));
        add(exitButton);

        JButton instructionButton = new JButton("Info");
        instructionButton.setVisible(true);
        instructionButton.setBounds(610, 300, 70, 20);
        instructionButton.addActionListener(actionEvent -> {

            logger.info("View instruction");

            JOptionPane.showMessageDialog(null, "Функции и знаки: \n" +
                "+ - сложение" + "\n" +
                "- - вычитание" + "\n" +
                "* - умножение" + "\n" +
                "/ - деление" + "\n" +
                "% - деление по модулю" + "\n" +
                "pow(a,b) - возведение выражения 'a' в степень, раную выражению 'b' " + "\n" +
                "() - разграничительные скобки" + "\n" +
                "sin(a) - синус выражения 'a'" + "\n" +
                "cos(a) - косинус выражения 'a'" + "\n" +
                "tan(a) - тангенс выражения 'a'" + "\n" +
                "ctg(a) - катангенс выражения 'a'" + "\n" +
                "asin(a) - арксинус выражения 'a'" + "\n" +
                "acos(a) - арккосинус выражения 'a'" + "\n" +
                "atan(a) - арктангенс выражения 'a'" + "\n" +
                "actg(a) - арккатангенс выражения 'a'" + "\n" +
                "(у всех тригонометрических функций выражение исчисляется в радианах)" + "\n" +
                "toRadians(a) - переводит градусное значение 'a' в радианы" + "\n" +
                "abs(a) - модуль выражения 'a'" + "\n" +
                "sqrt(a) - квадратный корень из выражения 'a'" + "\n" +
                "log(a) - натуральный логарифм выражения 'a'" + "\n"  +
                "Константы:" + "\n" +
                "P - число 'Пи' " + "\n" +
                "E - экспонента" + "\n" +
                "Наименования собственных констант должны начинаться с большой буквы" + "\n");
        });
        add(instructionButton);

        for(int i = 0; i < 5; i++) {

            JTextField field = new JTextField();
            field.setBounds(600, (70 + 30 * i), 100, 30);
            add(field);
            constants.add(field);
        }

        for(int i = 0; i < 6; i++) {

            JTextField field = new JTextField(((i % 2) == 0) ? "20" : "-20");
            field.setBounds(800, (70 + 30 * i), 50, 30);

            add(field);
            limits.add(field);
        }

        for(int i = 0; i < 6; i++) {

            JLabel label = new JLabel();
            label.setBounds(750, (70 + 30 * i), 50, 30);

            switch (i) {

                case 0: label.setText("X_MAX"); break;
                case 1: label.setText("X_MIN"); break;
                case 2: label.setText("Y_MAX"); break;
                case 3: label.setText("Y_MIN"); break;
                case 4: label.setText("Z_MAX"); break;
                case 5: label.setText("Z_MIN"); break;
            }

            add(label);
        }
    }

    public void paintComponent(Graphics g) { super.paintComponent(g); }
}
