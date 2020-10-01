
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Calculator {

    private static double step = 0.1;

    private static final ScriptEngine calculator = new ScriptEngineManager().getEngineByName("javascript");
    private static final Logger logger = LoggerFactory.getLogger(Calculator.class);

    private static String valX = "x";

    private static Map<String, Double> constants;

    public Calculator() {

        constants = new HashMap<>();

        prepareCalculator();
    }

//    public Calculator(ScriptEngine calc) {
//
//        this();
//        calculator = calc;
//    }

    public static double calculate(String function, double x) throws ScriptException {

        if(function == null) {

            logger.error("Method calculate", new NullPointerException("String of function is null"));
        }

        function = prepare(function, x);

        Number n = (Number) calculator.eval(function);

        return n.doubleValue();
    }

    public void setConstants(Map<String, Double> consts) {

        if(!constants.equals(consts)) {

            logger.info("Be changed constants map");
            constants = consts;
        }
    }

    public static void setStep(double s) {

        if(step != s) {

            logger.info("The argument step was changed from " + step + " to " + s);
            step = s;
        }
    }

    public void setXValue(String xValue) {

        if(!valX.equals(xValue)) {

            logger.info("Be changed x value");
            valX = xValue;
        }
    }

    public Map<Double, Double> calculate2D(double begin, double end, double minY, double maxY, String function) throws ScriptException { return calculate(function, begin, end, minY, maxY); }

    public Map[] calculate3D(double begin, double end, double minY, double maxY, double minZ, double maxZ, String functionY, String functionZ) throws ScriptException {

        return new Map[]{ calculate(functionY, begin, end, minY, maxY), calculate(functionZ, begin, end, minZ, maxZ) };
    }

    private Map<Double, Double> calculate(String function, double begin, double end, double min, double max) throws ScriptException {

        Map<Double, Double> ret = new LinkedHashMap<>();

        logger.info("Started calculate map values");

        for(double x = begin; x <= end; x += step) {

            String prepareFunction = prepare(function, x);

            Double result;

            try { result = (Double) (calculator.eval(prepareFunction)); }
            catch (Exception e){

                logger.error("Error of calculate", e);

                Integer r = (Integer) calculator.eval(prepareFunction);
                result = r.doubleValue();
            }

            if(result == null)
                logger.error("Error of calculate", new NullPointerException("Result calculate is null").getCause());
            else if((result >= min) && (result <= max)) ret.put(x, result);
        }

        logger.info("Finished calculate values map");

        return ret;
    }

    private static String prepare(String function, double x) {

       for(String s: constants.keySet())
           function = function.replace(s, constants.get(s).toString());

            return  function.replace("e", (Math.E + ""))
                    .replace("P", (Math.PI + "")).replace("E", (Math.E + "")).replace(valX, (x + "")).replace("--", "+").replace("- -", "+").replace("++", "+")
                    .replace("//", "/").replace(" ", "");
    }

    private void prepareCalculator() {

        try {

            logger.info("Start load functions");

            for (String function : new String[]{"sin", "cos", "tan", "abs", "sqrt", "acos", "asin", "atan", "log", "toRadians", "toDegrees", "ceil", "floor", "round"})
                calculator.eval("function " + function + "(x) { return Java.type('java.lang.Math')." + function + "(x); }");

            calculator.eval("function ctg(x) { return 1 / Java.type('java.lang.Math')." + "tan" + "(x); }");
            calculator.eval("function actg(x) { return 1 / Java.type('java.lang.Math')." + "atan" + "(x); }");
            calculator.eval("function pow(x, y) { return Java.type('java.lang.Math').pow(x, y); }");
        }
        catch (ScriptException e) { logger.error("Error of load functions in calculator {}", e); }
    }
}