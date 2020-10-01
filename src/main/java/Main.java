import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private Main(){}

    public static void main(String[] args) { new Main().start(); }

    private void start() {

        logger.info("PROGRAM START");

        new Frame();

        logger.info("PROGRAM FINISH");
    }
}