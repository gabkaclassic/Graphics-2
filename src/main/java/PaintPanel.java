import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;

@Slf4j
public class PaintPanel extends JPanel {

    private static final int OX_2D = 400;
    private static final int OY_2D = 400;
    private static final int OX_3D = 400;
    private static final int OY_3D = 400;
    private static final int OZ_3D = 400;
    private static final double DEGREE_A = 25;
    private static final double DEGREE_B = 25;
    private static final double DEGREE_Y = 0;
    private static final double cosA = Math.cos(Math.toRadians(DEGREE_A));
    private static final double sinA = Math.sin(Math.toRadians(DEGREE_A));
    private static final double cosB = Math.cos(Math.toRadians(DEGREE_B));
    private static final double sinB = Math.sin(Math.toRadians(DEGREE_B));
    private static final double cosY = Math.cos(Math.toRadians(DEGREE_Y));
    private static final double sinY = Math.sin(Math.toRadians(DEGREE_Y));

    private double currentX;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double minZ;
    private double maxZ;
    private double stepX;
    private double stepY;
    private double stepZ;
    private final String symbolX;
    private final String symbolY;
    private final String symbolZ;
    private final List<String> functionsY;
    private List<String> functionsZ;

    private static final Color[] colors = new Color[]{Color.RED, Color.BLUE, Color.MAGENTA, Color.BLACK, Color.GREEN};

    private List<Map<Double, Double>> pointsY;
    private List<Map<Double, Double>> pointsZ;

    private static final Calculator calculator = new Calculator();
    
    @SafeVarargs
    public PaintPanel(Map<String, Double> constants, List<Double> limits, List<String> chars, double step, List<String>...functions) {

        symbolX = chars.get(0);
        symbolY = chars.get(1);
        symbolZ = chars.get(2);

        setLimits(limits);

        calculator.setConstants(constants);
        Calculator.setStep(step);
        calculator.setXValue(symbolX);

        addMouseWheelListener(mouseWheelEvent -> {

            int amount = mouseWheelEvent.getWheelRotation();

            minX -= amount;
            maxX += amount;
            minY -= amount;
            maxY += amount;
            minY -= amount;
            maxY += amount;

            calculateSteps();

            repaint();
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {

                currentX = ((e.getX() - OX_2D) / stepX);

                repaint();
            }
        });

        calculateSteps();

        functionsY = functions[0];
        if(functions.length > 1) functionsZ = functions[1];

        prepareFromRepaint();
    }

    public static double calculate(String function, double x) throws ScriptException {
        
        return  Calculator.calculate(function, x);
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D gr = (Graphics2D)g;

        super.paintComponent(gr);
    
        paintFunctionsTitles(gr);
        
        if(functionsZ == null) {
    
            paintAxis2D(gr);
            paintGraphics2D(gr);
            paintCurrentValues(gr);
        }
        else {
    
            paintAxis3D(gr);
            paintGraphics3D(gr);
        }
    }

    private void paintAxis2D(Graphics2D gr) {
    
    
        gr.setColor(Color.BLACK);
        gr.setFont(new Font("TimesRoman", Font.BOLD, 15));
    
        gr.drawString(symbolX, (OX_2D * 2 - 15), (OY_2D + 15));
    
        gr.drawLine((OX_2D - 1), 0, (OX_2D - 1), OY_2D * 2);
        gr.drawLine(OX_2D, 0, OX_2D, OY_2D * 2);
        gr.drawLine((OX_2D + 1), 0, (OX_2D + 1), OY_2D * 2);
    
        for (int i = 0; i < (OX_2D * 2); i += stepX) gr.drawLine(i, 0, i, (OY_2D * 2));
    
        gr.setColor(Color.GRAY);
    
        gr.drawString(symbolY, (OX_2D + 15), 15);
    
        gr.drawLine(0, (OY_2D - 1), OX_2D * 2, (OY_2D - 1));
        gr.drawLine(0, OY_2D, OX_2D * 2, OY_2D);
        gr.drawLine(0, (OY_2D + 1), OX_2D * 2, (OY_2D + 1));
        
        for (int i = 0; i < (OY_2D * 2); i += stepY) gr.drawLine(0, i, (OX_2D * 5), i);
    }
    
    private void paintAxis3D(Graphics2D gr) {
    
        gr.setColor(Color.BLACK);
        gr.setFont(new Font("TimesRoman", Font.BOLD, 15));
    
        gr.drawString(symbolX, (int)(OX_3D + (OX_3D * 2 * Math.cos(Math.toRadians(DEGREE_A + 10))) - 300),
                (int)(OY_3D + (OX_3D * 2 * Math.sin(Math.toRadians(DEGREE_A + 10)))) - 270);
    
        gr.drawLine((int)(OX_3D - (OX_3D * 2 * cosA)),
                (int)(OY_3D - (OX_3D * 2 * sinA) - 1),
                (int)(OX_3D + (OX_3D * 2 * cosA)),
                (int)(OY_3D + (OX_3D * 2 * sinA)) - 1);
        gr.drawLine((int)(OX_3D - (OX_3D * 2 * cosA)),
                (int)(OY_3D - (OX_3D * 2 * sinA)),
                (int)(OX_3D + (OX_3D * 2 * cosA)),
                (int)(OY_3D + (OX_3D * 2 * sinA)));
        gr.drawLine((int)(OX_3D - (OX_3D * 2 * cosA)),
                (int)(OY_3D - (OX_3D * 2 * sinA) + 1),
                (int)(OX_3D + (OX_3D * 2 * cosA)),
                (int)(OY_3D + (OX_3D * 2 * sinA)) + 1);
    
        gr.setColor(Color.GRAY);
    
        gr.drawString(symbolY, 590, 20);  //КОСТЫЛЬ!!!
    
        gr.drawLine((int)(OX_3D - (OY_2D * 2 * sinB) - 1),
                (int)(OY_3D + (OY_2D * 2 * cosB)),
                (int)(OX_3D + (OY_2D * 2 * sinB) - 1),
                (int)(OY_3D - (OY_2D * 2 * cosB)));
        gr.drawLine((int)(OX_3D - (OY_2D * 2 * sinB)),
                (int)(OY_3D + (OY_2D * 2 * cosB)),
                (int)(OX_3D + (OY_2D * 2 * sinB)),
                (int)(OY_3D - (OY_2D * 2 * cosB)));
        gr.drawLine((int)(OX_3D - (OY_2D * 2 * sinB) + 1),
                (int)(OY_3D + (OY_2D * 2 * cosB)),
                (int)(OX_3D + (OY_2D * 2 * sinB) + 1),
                (int)(OY_3D - (OY_2D * 2 * cosB)));
    
        gr.setColor(new Color(65, 25, 0));
    
        gr.drawString(symbolZ, (int)(OX_3D - (OZ_3D * 2 * sinY) + 10), 20);
    
        gr.drawLine((int)(OX_3D - (OZ_3D * 2 * sinY) - 1),
                0,
                (int)(OX_3D + (OZ_3D * 2 * sinY - 1)),
                OZ_3D * 2);
        gr.drawLine((int)(OX_3D - (OZ_3D * 2 * sinY) - 1),
                0,
                (int)(OX_3D + (OZ_3D * 2 * sinY - 1)),
                OZ_3D * 2);
        gr.drawLine((int)(OX_3D - (OZ_3D * 2 * sinY) - 1),
                0,
                (int)(OX_3D + (OZ_3D * 2 * sinY - 1)),
                OZ_3D * 2);
    }
    
    private void paintGraphics2D(Graphics2D gr) {
        
        int color = 0;
        
        for(Map<Double, Double> map: pointsY) {
    
            gr.setColor(colors[color++]);
            
            Double[] arrX = map.keySet().toArray(new Double[0]);
            Double[] arrY = map.values().toArray(new Double[0]);
    
            for(int i = 0; i < arrX.length - 1 && i < arrY.length - 1; i++) {
    
                gr.drawLine((int) (OX_2D + arrX[i] * stepX), (int) (OY_2D - arrY[i] * stepY),
                        (int) (OX_2D + arrX[i + 1] * stepX), (int) (OY_2D - arrY[i + 1] * stepY));
            }
        }
    }
    
    private void paintGraphics3D(Graphics2D gr) {
    
        for(int index = 0; index < pointsY.size(); index++) {
    
            gr.setColor(colors[index]);
            
            Double[] arrX = pointsY.get(index).keySet().toArray(new Double[0]);
            Double[] arrY = pointsY.get(index).values().toArray(new Double[0]);
            Double[] arrZ = pointsZ.get(index).values().toArray(new Double[0]);
    
            for (int j = 0; j < (arrX.length - 1)
                    && j < (arrY.length - 1)
                    && j < (arrZ.length - 1); j++) {
        
                gr.drawLine((int) (OX_3D + (stepX * arrX[j] * cosA + stepY * arrY[j] * sinB)),
                        (int) (OY_3D + (stepX * arrX[j] * sinA - stepY * arrY[j] * cosB - stepZ * arrZ[j] * cosY)),
                        (int) (OX_3D + (stepX * arrX[j + 1] * cosA + stepY * arrY[j + 1] * sinB)),
                        (int) (OY_3D + (stepX * arrX[j + 1] * sinA - stepY * arrY[j + 1] * cosB - stepZ * arrZ[j + 1] * cosY)));
            }
        }
    }
    
    private void paintFunctionsTitles(Graphics2D gr) {
        
        for(int index = 0; index < pointsY.size(); index++) {
    
            gr.setColor(colors[index]);
            gr.setFont(new Font("TimesRoman", Font.BOLD, 15));
            gr.drawString(("Function " + index), (OX_2D * 2 - 100), OY_2D * 2 - 200 + (index * 20));
        }
    }
    
    private void paintCurrentValues(Graphics2D gr) {
    
        gr.clearRect(0, 630, 70, 150);
    
        gr.setColor(Color.BLACK);
        gr.drawString(("X=" + currentX), 0, 650);
    
        for (int i = 0; i < functionsY.size(); i++) {
        
            gr.setColor(colors[i]);
        
            double y = Double.NaN;
        
            try {
                
                y = calculate(functionsY.get(i), currentX);
            }
            catch (ScriptException e) {
                
                log.error("Calculator script error", e);
            }
        
            gr.drawString(("Y= " + String.format(Locale.US, "%.2f", y)), 0, 670 + (30 * i));
        
        }
    }
    
    private void prepareFromRepaint() {

        pointsY = new ArrayList<>();
        
        if(functionsZ == null) {

            try {
    
                for(String s : functionsY)
                    pointsY.add(calculator.calculate2D(minX, maxX, minY, maxY, s));
            }
            catch(ScriptException e) {
                
                log.error("Calculator script error", e);
            }

        }
        else {

            pointsZ = new ArrayList<>();

                try {

                    for (int i = 0; i < functionsY.size(); i++) {

                            Map<Double, Double>[] arr = calculator.calculate3D(minX, maxX, minY, maxY, minZ, maxZ, functionsY.get(i), functionsZ.get(i));
                            pointsY.add(arr[0]);
                            pointsZ.add(arr[1]);
                        }
                    }
                    catch (ScriptException e) {
                    
                    log.error("Calculator script error", e);
                }
            }
    }

    private void setLimits(List<Double>limits) {

        maxX = limits.get(0);
        minX = limits.get(1);
        maxY = limits.get(2);
        minY = limits.get(3);
        maxZ = limits.get(4);
        minZ = limits.get(5);
    }

    private void calculateSteps() {

        stepX = (OX_2D * 2) / Math.abs(maxX - minX);
        stepY = (OY_2D * 2) / Math.abs(maxY - minY);
        stepZ = (OZ_3D * 2) / Math.abs(maxZ - minZ);
    }
}