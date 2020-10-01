import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import javax.script.ScriptException;

public class CalculatorTest {

    private static Calculator calculator;

    @BeforeClass
    public static void prepareCalculator() {

//        ScriptEngine engine = Mockito.mock(ScriptEngine.class);
//
//        try {
//
//            Double d = 0.0;
//            Mockito.when(engine.eval("0")).then((Answer<?>)(Object)d);
//
//            d = Double.NaN;
//            Mockito.when(engine.eval("1 / 0")).then((Answer<?>)(Object)d);
//
//            d = 4.0;
//            Mockito.when(engine.eval("2 + 2.0")).then((Answer<?>)(Object)d);
//
//            d = 5.8;
//            Mockito.when(engine.eval("2.5 + 3.3")).then((Answer<?>)(Object)d);
//
//            d = 0.0;
//            Mockito.when(engine.eval("sin(0)")).then((Answer<?>)(Object)d);
//        }
//        catch (ScriptException e) { e.printStackTrace(); }
//
//        calculator = new Calculator(engine);

        calculator = new Calculator();
    }

    @Test(timeout = 1000)
    public void testCalculator() throws ScriptException {

        Assert.assertEquals( 0.0, calculator.calculate("0",0), 0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, calculator.calculate("1 / 0",0), 0);
        Assert.assertEquals( 4.0,calculator.calculate("2 + 2.0",0), 0);
        Assert.assertEquals( 5.8, calculator.calculate("2.5 + 3.3",0), 0);
        Assert.assertEquals( 0.0, calculator.calculate("sin(0)",0), 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCalculatorExceptions() throws ScriptException {

        calculator.calculate(null, 0);
    }
}
