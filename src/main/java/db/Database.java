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
        String url = new Config(Constants.CONFIG_ADDRESS).getProperty(String.class, "db_url");
        String username = new Config(Constants.CONFIG_ADDRESS).getProperty(String.class, "db_username");
        String password = new Config(Constants.CONFIG_ADDRESS).getProperty(String.class, "db_password");
        connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        statement.close();
    }

    public boolean userExists(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM `users` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        return res.next();
    }

    public Long maxUserId() throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT MAX(`id`) AS `max_id` FROM `users`");
        ResultSet res = statement.executeQuery();
        long maxId = -1L;
        if (res.next()) {
            maxId = res.getLong("max_id");
        }
        return maxId;
    }

    public User loadUser(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        User user = null;
        while (res.next())
        {
            user = new User(res.getString("username"), res.getString("password"));
            user.setId(id);
            user.setBio(res.getString("bio"));
            user.setName(res.getString("name"));
            user.setEmail(res.getString("email"));
            user.setBirthDate(res.getDate("birth_date"));
            user.setPhoneNumber(res.getString("phone_number"));
        }
        res.close();
        statement.close();
        return user;
    }

    public User saveUser(User user) throws SQLException
    {
        PreparedStatement statement;
        if (userExists(user.getId()))
        {
            statement = connection.prepareStatement("UPDATE `users` SET `username` = ?, `password` = ?, `bio` = ?, `name` = ?, `email` = ?, `birth_date` = ?, `phone_number` = ? WHERE `id` = ?");
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getBio());
            statement.setString(4, user.getName());
            statement.setDate(5, (Date) user.getBirthDate());
            statement.setString(6, user.getPhoneNumber());
            statement.setLong(7, user.getId());
        }
        else
        {
            statement = connection.prepareStatement("INSERT INTO `users` (`username`, `password`, `name`, `email`, `phone_number`, `bio`, `birth_date`) VALUES (?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getName());
            statement.setString(4, user.getPhoneNumber());
            statement.setString(5, user.getBio());
            statement.setDate(6, (Date) user.getBirthDate());
            user.setId(maxUserId());
        }
        statement.executeQuery();
        return loadUser(user.getId());
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
