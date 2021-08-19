import config.Config;
import constants.Constants;
import controller.server.SocketController;
import db.Database;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.AddBotFrame;

import java.sql.SQLException;

public class Main extends Application
{
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args)
    {
        String url = new Config(Constants.CONFIG).getProperty(String.class, "db_url");
        String username = new Config(Constants.CONFIG).getProperty(String.class, "db_username");
        String password = new Config(Constants.CONFIG).getProperty(String.class, "db_password");
        try
        {
            Database.getDB().connectToDatabase(url, username, password);
        }
        catch (SQLException throwable)
        {
            logger.error(String.format("%s: error while connecting to db", throwable));
        }

        SocketController socketController = new SocketController(new Config(Constants.CONFIG));
        socketController.start();

        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        stage.setTitle(new Config(Constants.CONFIG).getProperty(String.class, "name"));
        stage.setScene(new AddBotFrame().getScene());
        stage.setResizable(false);
        stage.show();
    }
}
