
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

@Slf4j
public class Calculator {

    private static double step = 0.1;

    private static final ScriptEngine calculator = new ScriptEngineManager().getEngineByName("javascript");
    
    private static String valX = "x";

    private static Map<String, Double> constants;

    public Calculator() {

        constants = new HashMap<>();

        prepareCalculator();
    }

    public static double calculate(String function, double x) throws ScriptException {

        if(function == null) {

            log.error("Method calculate", new NullPointerException("String of function is null"));
        }

        function = prepare(function, x);

        Number n = (Number) calculator.eval(function);

        return n.doubleValue();
    }

    public void setConstants(Map<String, Double> consts) {

        if(!constants.equals(consts)) {

            log.info("Be changed constants map");
            constants = consts;
        }
    }

    public static void setStep(double s) {

        if(step != s) {

            log.info("The argument step was changed from " + step + " to " + s);
            step = s;
        }
    }

    public void setXValue(String xValue) {

        if(!valX.equals(xValue)) {

            log.info("Be changed x value");
            valX = xValue;
        }
    }

    public Map<Double, Double> calculate2D(double begin, double end, double minY, double maxY, String function) throws ScriptException { return calculate(function, begin, end, minY, maxY); }

    public Map<Double, Double>[] calculate3D(double begin, double end, double minY, double maxY, double minZ, double maxZ, String functionY, String functionZ) throws ScriptException {

        return new Map[]{ calculate(functionY, begin, end, minY, maxY), calculate(functionZ, begin, end, minZ, maxZ) };
    }

    private Map<Double, Double> calculate(String function, double begin, double end, double min, double max) throws ScriptException {

        Map<Double, Double> ret = new LinkedHashMap<>();

        log.info("Started calculate map values");

        for(double x = begin; x <= end; x += step) {

            String prepareFunction = prepare(function, x);

            Double result;

            try { result = (Double) (calculator.eval(prepareFunction)); }
            catch (Exception e){

                log.error("Error of calculate", e);

                Integer r = (Integer) calculator.eval(prepareFunction);
                result = r.doubleValue();
            }

            if(result == null)
                log.error("Error of calculate", new NullPointerException("Result calculate is null").getCause());
            else if((result >= min) && (result <= max)) ret.put(x, result);
        }

        log.info("Finished calculate values map");

        return ret;
    }

    private static String prepare(String function, double x) {

       for(String s: constants.keySet())
           function = function.replace(s, constants.get(s).toString());

            return  function.replace("e", (Math.E + ""))
                    .replace("P", (Math.PI + "")).replace("E", (Math.E + "")).replace(valX, (x + "")).replace("--", "+")
                    .replace("- -", "+").replace("++", "+").replace("//", "/").replace(" ", "");
    }

    public static String derivativeFunction(String function) throws ScriptException {

        if((function == null) || (function.isEmpty())) {

            log.warn("String from derivative is null or empty");
            throw new NullPointerException("String from derivative is null or empty");
        }

        return primaryProcessing(function);
    }

    private static String primaryProcessing(String function) throws ScriptException {

        int bracketCounter = 0;
        int begin;
        int end = 0;
        char[] func = function.toCharArray();
        StringBuilder answer = new StringBuilder();

        if(!function.contains(valX)) return "0";

        if((!function.contains("+")) && (!function.contains("-"))) {

            if((function.contains("*")) || (function.contains("/"))) return secondaryProcessing(function);
            else return derivative(function);
        }

        for(int i = 0; i < func.length; i++) {

            char c = func[i];

            if(c == '(') bracketCounter++;
            else if(c == ')') bracketCounter--;
            if((bracketCounter == 0) && ((c == '+') || (c == '-') || (i == (func.length - 1)))) {

                begin = (end == 0) ? end : (end + 1);
                end = (i == (func.length - 1)) ? func.length : i;

                String result = function.substring(begin, end);

                if((func[begin] == '+') || (func[begin] == '-')) answer.append(func[begin]);

                if((result.contains("*"))
                        || (result .contains("/")))
                    answer.append(secondaryProcessing(result));
                 else answer.append(derivative(result));

                if((c == '+') || (c == '-')) answer.append(c);
            }
        }

        return answer.toString();
    }

    private static String secondaryProcessing(String function) throws ScriptException {

        int bracketCounter = 0;
        int begin;
        int end = 0;
        char[] func = function.toCharArray();
        List<String> intervals = new ArrayList<>();

        for(int i = 0; i < func.length; i++) {

            char c = func[i];

            if(c == '(') bracketCounter++;
            else if(c == ')') bracketCounter--;
            if((bracketCounter == 0) && ((c == '*') || (c == '/') || (i == (func.length - 1)))) {

                begin = (end == 0) ? end : (end + 1);
                end = (i == (func.length - 1)) ? func.length : i;

                if((func[begin] == '*') || (func[begin] == '/')) intervals.add(String.valueOf(func[begin]));

                intervals.add(function.substring(begin, end));

                if((c == '*') || (c == '/')) intervals.add(String.valueOf(c));
            }
        }

        StringBuilder sb;

        while(intervals.size() > 1) {

            sb = new StringBuilder();

            if((intervals.get(0).contains(valX)) && (intervals.get(2).contains(valX))) {

                if(intervals.get(1).equals("*"))
                    sb.append('(').append(derivative(intervals.get(0))).append(") * (").append(intervals.get(2)).append(") + (")
                            .append(derivative(intervals.get(2))).append(") * (").append(intervals.get(0)).append(')');

                else sb.append("((").append(derivative(intervals.get(0))).append(") * (").append(intervals.get(2)).append(") - (")
                            .append(derivative(intervals.get(2))).append(") * (").append(intervals.get(0)).append(")) / pow((").append(intervals.get(2)).append("), 2)");

            }
            else if((!intervals.get(0).contains(valX)) && (!intervals.get(2).contains(valX)))
                sb.append(calculate((intervals.get(0) + intervals.get(1) + intervals.get(2)), 0));
            else {

                if(intervals.get(0).contains(valX)) sb.append('(')
                        .append(derivative(intervals.get(0)))
                        .append(')')
                        .append(intervals.get(1))
                        .append(intervals.get(2));
                else sb.append('(')
                        .append(derivative(intervals.get(2)))
                        .append(')')
                        .append(intervals.get(1))
                        .append(intervals.get(0));
            }

            intervals.set(0, sb.toString());
            intervals.remove(1);
            intervals.remove(1);
        }

        if(intervals.get(0).equals(function)) return derivative(function);

        return intervals.get(0);
    }

    private static String derivative(String function) throws ScriptException {

        char[] func = function.toCharArray();
        StringBuilder answer = new StringBuilder();

        if(!function.contains(valX)) return "0";

        for(int i = 0; i < func.length; i++) {

            char c = func[i];

            if((c == 'p') && (i < (func.length - 3)) && (function.substring(i, (i + 3)).equals("pow"))) {

                int indComma;
                int j = bracketCounter((i + 4), func);
                boolean beforeComma = false;

                indComma = function.indexOf(',');

                if((indComma != (-1)) && (function.substring(indComma).contains(valX))) beforeComma = true;

                if(beforeComma) {

                    if(function.substring((i + 4), indComma).equals("E"))
                        answer.append("pow((")
                            .append(derivative(function.substring((indComma + 1), (j - 1))))
                            .append(") * E, ")
                            .append(function, indComma, (j - 1)).append(')');

                    else {

                        answer.append(function, i, (j - 1))
                                .append(") * ")
                                .append("(log(")
                                .append(function, (i + 4), indComma)
                                .append(") + (")
                                .append(function, (i + 4), indComma)
                                .append(") / (")
                                .append(function, (indComma + 1), (j - 1))
                                .append("))");
                    }
                }
                else {

                    String s = function.substring((indComma + 1), (j - 1));

                    answer.append("pow((")
                            .append(function, (i + 4), indComma)
                            .append(')')
                            .append(" *")
                            .append(s)
                            .append(", ")
                            .append(s)
                            .append(" - 1")
                            .append(')');
                }
                i = j;
            }
            else if((c == 's') && (i < (func.length - 3)) && (function.substring(i, (i + 3)).equals("sin"))) {

                int j = bracketCounter((i + 4), func);

                answer.append('(')
                        .append(derivativeFunction(function.substring((i + 4),  (j - 1))))
                        .append(") * cos(")
                        .append(function, (i + 4), (j));

                i = j;
            }
            else if((c == 'c') && (i < (func.length - 3)) && (function.substring(i, (i + 3)).equals("cos"))) {

                int j = bracketCounter((i + 4), func);

                answer.append("(")
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") * -sin(")
                        .append(function, (i + 4), (j));

                i = j;
            }
            else if((c == 't') && (i < (func.length - 3)) && (function.substring(i, (i + 3)).equals("tan"))) {

                int j = bracketCounter((i + 4), func);

                answer.append('(')
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") / pow(cos(")
                        .append(function, (i + 4), (j - 1))
                        .append("), 2)");

                i = j;
            }
            else if((c == 'c') && (i < (func.length - 3)) && (function.substring(i, (i + 3)).equals("ctg"))) {

                int j = bracketCounter((i + 4), func);

                answer.append("-1 * (")
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") / pow(sin(")
                        .append(function, (i + 4), (j - 1))
                        .append("), 2)");

                i = j;
            }
            else if((c == 'l') && (i < (func.length - 3)) && (function.substring(i, (i + 3)).equals("log"))) {

                int j = bracketCounter((i + 4), func);

                answer.append(" 1 / (")
                        .append(function, (i + 4), (j - 1))
                        .append(')');

                i = j;
            }
            else if((c == 'a') && (i < (func.length - 4)) && (function.substring(i, (i + 4)).equals("asin"))) {

                int j = bracketCounter((i + 4), func);

                answer.append('(')
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") / sqrt(1 - pow(")
                        .append(function, (i + 4), (j - 1))
                        .append("), 2)");

                i = j;
            }
            else if((c == 'a') && (i < (func.length - 4)) && (function.substring(i, (i + 4)).equals("acos"))) {

                int j = bracketCounter((i + 4), func);

                answer.append("-1 * (")
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") / sqrt(1 - pow((")
                        .append(function, (i + 4), (j - 1))
                        .append("), 2))");

                i = j;
            }
            else if((c == 'a') && (i < (func.length - 4)) && (function.substring(i, (i + 4)).equals("atan"))) {

                int j = bracketCounter((i + 4), func);

                answer.append('(')
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") / (1 + pow((")
                        .append(function, (i + 4), (j - 1))
                        .append("), 2))");

                i = j;
            }
            else if((c == 'a') && (i < (func.length - 4)) && (function.substring(i, (i + 4)).equals("actg"))) {

                int j = bracketCounter((i + 4), func);

                answer.append("-1 * (")
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") / (1 + pow((")
                        .append("), 2))");

                i = j;
            }
            else if((c == 's') && (i < (func.length - 4)) && (function.substring(i, (i + 4)).equals("sqrt"))) {

                int j = bracketCounter((i + 4), func);

                answer.append("(")
                        .append(derivativeFunction(function.substring((i + 4), j)))
                        .append(") / (2 * (1 + pow((")
                        .append(function, (i + 4), (j - 1))
                        .append("), 2)))");

                i = j;
            }
            else if((c == 'a') && (i < (func.length - 3)) && (function.substring(i, (i + 3)).equals("abs"))) {

                int j = bracketCounter((i + 4), func);

                answer.append("(")
                        .append(function, (i + 3), j)
                        .append(") / abs(")
                        .append(function, (i + 3), (j - 1))
                        .append(')');

                i = j;
            }
            else if(c == valX.toCharArray()[0]) {

                StringBuilder sb = new StringBuilder();
                int j = i;

                for(; j < (i + valX.length()); j++) sb.append(func[j]);

                if(valX.equals(sb.toString())) {

                    answer.append('1');
                    i = j;
                }
            }
            else {

                try {

                    Integer.parseInt(String.valueOf(c));
                    answer.append(0);
                }
                catch(Exception e) {

                    if(c == '.') answer.append(0);
                    else answer.append(c);
                }
            }
        }

        String s = answer.toString();

        while(s.contains("00")) s.replace("00", "0");

        return s;
    }

    private static int bracketCounter(int j, char[] func) {

        int bracketCounter = 1;

        for(; j < func.length; j++) {

            if(bracketCounter == 0) break;

            if(func[j] == '(') bracketCounter++;
            else if(func[j] == ')') bracketCounter--;
        }

        checkBracketCounter(bracketCounter);

        return j;
    }

    private static void checkBracketCounter(int c) {

        if(c > 0) {

            log.warn("Incorrectly placed brackets");
            throw new IllegalArgumentException("Incorrectly placed brackets");
        }
    }

    private void prepareCalculator() {

        try {

            log.info("Start load functions");

            for (String function : new String[]{"sin", "cos", "tan", "abs", "sqrt", "acos", "asin", "atan", "log", "toRadians", "toDegrees", "ceil", "floor", "round"})
                calculator.eval("function " + function + "(x) { return Java.type('java.lang.Math')." + function + "(x); }");

            calculator.eval("function ctg(x) { return 1 / Java.type('java.lang.Math')." + "tan" + "(x); }");
            calculator.eval("function actg(x) { return 1 / Java.type('java.lang.Math')." + "atan" + "(x); }");
            calculator.eval("function pow(x, y) { return Java.type('java.lang.Math').pow(x, y); }");
            calculator.eval("function log(x, y) { return Java.type('java.lang.Math').log(y) / Java.type('java.lang.Math').log(x); }");
        }
        catch (ScriptException e) { log.error("Error of load functions in calculator {}", e); }
    }
}