import config.Config;
import constants.Constants;
import controller.server.SocketController;
import db.Database;

import java.sql.SQLException;

public class Main
{
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
            throwable.printStackTrace();
        }

        SocketController socketController = new SocketController(new Config(Constants.CONFIG));
        socketController.start();
    }
}
