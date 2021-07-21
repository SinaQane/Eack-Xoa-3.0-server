package db;

import config.Config;
import constants.Constants;
import model.*;

import java.sql.*;

public class Database
{
    private static Connection connection;

    public static void connectToDatabase() throws SQLException
    {
        String url = new Config(Constants.CONFIG_ADDRESS).getExistingProperty(String.class, "db_url");
        String username = new Config(Constants.CONFIG_ADDRESS).getExistingProperty(String.class, "db_username");
        String password = new Config(Constants.CONFIG_ADDRESS).getExistingProperty(String.class, "db_password");
        connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        statement.close();
    }

    public User loadUser(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        User user = null;
        while (res.next())
        {
            user = new User(id, res.getString("username"), res.getString("password"));
            user.setName(res.getString("name"));
            user.setEmail(res.getString("email"));
            user.setPhoneNumber(res.getString("phone_number"));
            user.setBio(res.getString("bio"));
            user.setBirthDate(res.getDate("birth_date"));
        }
        res.close();
        statement.close();
        return user;
    }

    public Profile loadProfile(long id)
    {
        return null;
    }

    public Tweet loadTweet(long id)
    {
        return null;
    }

    public Group loadGroup(long id)
    {
        return null;
    }

    public Chat loadChat(long id)
    {
        return null;
    }

    public Message loadMessage(long id)
    {
        return null;
    }

    public Notification loadNotification(long id)
    {
        return null;
    }
}
