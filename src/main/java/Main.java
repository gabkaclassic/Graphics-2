@lombok.extern.slf4j.Slf4j
public class Main {
    
    private Main(){}

    public static void main(String[] args) {

        new Main().start();
    }

    private void start() {

        log.warn("PROGRAM START");

        new Frame();

        log.warn("PROGRAM FINISH");
    }
}