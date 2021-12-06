import databasemanager.Manager;
import javafx.application.Application;
import javafx.stage.Stage;
import ui.HomeScreen;

public class Main extends Application {
    private String DB_URL = "jdbc:mysql://localhost:3306/abd_al-muttalib_201904158";
    private String USERNAME = "root";
    private String PASSWORD = "";

    @Override
    public void start(Stage stg) {
        Manager mgr = new Manager(DB_URL, USERNAME, PASSWORD);

        stg = new HomeScreen(stg, "Classic Car Manager", HomeScreen.FULL_SCREEN, mgr).getStage();
        stg.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}