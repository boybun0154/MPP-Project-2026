import config.AppConfig;
import server.AppServer;

public class Main {
    public static void main(String[] args) {
        String mode = AppConfig.getAppMode();

        if ("SERVER".equals(mode)) {
            AppServer.start();
        } else {
            // TODO: Code the CLI mode
            System.out.println("...");
        }
    }
}