package db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.Config;
import constants.Constants;
import model.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class Database
{
    private static Connection connection;

    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson = gsonBuilder.setPrettyPrinting().create();

    static Database db;

    private Database() {}

    public static Database getDB()
    {
        if (db == null)
        {
            db = new Database();
        }
        return db;
    }

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
            user.setActive(res.getBoolean("is_active"));
            user.setDeleted(res.getBoolean("is_deleted"));
        }
        res.close();
        statement.close();
        return user;
    }

    public User saveUser(User user) throws SQLException
    {
        PreparedStatement statement;
        boolean exists = userExists(user.getId());
        if (exists)
        {
            statement = connection.prepareStatement(
                    "UPDATE `users` SET `username` = ?, `password` = ?, `name` = ?, `email` = ?, `phone_number` = ?, `bio` = ?, `birth_date` = ?, `is_active` = ?, `is_deleted` = ? WHERE `id` = ?");
        }
        else
        {
            statement = connection.prepareStatement(
                    "INSERT INTO `users` (`username`, `password`, `name`, `email`, `phone_number`, `bio`, `birth_date`, `is_active`, `is_deleted`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPassword());
        statement.setString(3, user.getName());
        statement.setString(4, user.getEmail());
        statement.setString(5, user.getPhoneNumber());
        statement.setString(6, user.getBio());
        statement.setDate(7, (Date) user.getBirthDate());
        statement.setBoolean(8, user.isActive());
        statement.setBoolean(9, user.isDeleted());
        if (exists)
        {
            statement.setLong(10, user.getId());
        }
        statement.executeQuery();
        if (!exists)
        {
            user.setId(maxUserId());
        }
        return loadUser(user.getId());
    }

    public boolean profileExists(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM `profiles` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        return res.next();
    }

    public Long maxProfileId() throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT MAX(`id`) AS `max_id` FROM `profiles`");
        ResultSet res = statement.executeQuery();
        long maxId = -1L;
        if (res.next()) {
            maxId = res.getLong("max_id");
        }
        return maxId;
    }

    public Profile loadProfile(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `profiles` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        Profile profile = new Profile();
        while (res.next())
        {
            profile.setId(id);
            profile.setPicture(res.getString("picture"));
            profile.setLastSeen(res.getDate("last_seen"));
            profile.setFollowers(Arrays.asList(gson.fromJson(res.getString("followers"), Long[].class)));
            profile.setFollowings(Arrays.asList(gson.fromJson(res.getString("followings"), Long[].class)));
            profile.setBlocked(Arrays.asList(gson.fromJson(res.getString("blocked"), Long[].class)));
            profile.setMuted(Arrays.asList(gson.fromJson(res.getString("muted"), Long[].class)));
            profile.setRequests(Arrays.asList(gson.fromJson(res.getString("requests"), Long[].class)));
            profile.setPending(Arrays.asList(gson.fromJson(res.getString("pending"), Long[].class)));
            profile.setUserTweets(Arrays.asList(gson.fromJson(res.getString("user_tweets"), Long[].class)));
            profile.setRetweetedTweets(Arrays.asList(gson.fromJson(res.getString("retweeted_tweets"), Long[].class)));
            profile.setUpvotedTweets(Arrays.asList(gson.fromJson(res.getString("upvoted_tweets"), Long[].class)));
            profile.setDownvotedTweets(Arrays.asList(gson.fromJson(res.getString("downvoted_tweets"), Long[].class)));
            profile.setReportedTweets(Arrays.asList(gson.fromJson(res.getString("reported_tweets"), Long[].class)));
            profile.setSavedTweets(Arrays.asList(gson.fromJson(res.getString("saved_tweets"), Long[].class)));
            profile.setNotifications(Arrays.asList(gson.fromJson(res.getString("notifications"), Long[].class)));
            profile.setGroups(Arrays.asList(gson.fromJson(res.getString("groups"), Long[].class)));
            profile.setChats(Arrays.asList(gson.fromJson(res.getString("chats"), Long[].class)));
            profile.setPrivateState(res.getBoolean("private_state"));
            profile.setInfoState(res.getBoolean("info_state"));
            profile.setLastSeenState(res.getInt("last_seen_state"));
        }
        res.close();
        statement.close();
        return null;
    }

    public Profile saveProfile(Profile profile) throws SQLException {
        PreparedStatement statement;
        boolean exists = profileExists(profile.getId());
        if (exists)
        {
            statement = connection.prepareStatement(
                    "UPDATE `profiles` SET `picture` = ?, `last_seen` = ?, `followers` = ?, `followings` = ?, `blocked` = ?, `muted` = ?, `requests` = ?, `pending` = ?, `user_tweets` = ?, `retweeted_tweets` = ?, `upvoted_tweets` = ?, `downvoted_tweets` = ?, `reported_tweets` = ?, `saved_tweets` = ?, `notifications` = ?, `groups` = ?, `chats` = ?, `private_state` = ?, `info_state` = ?, `last_seen_state` = ? WHERE `id` = ?");
        }
        else
        {
            statement = connection.prepareStatement(
                    "INSERT INTO `profiles` (`picture`, `last_seen`, `followers`, `followings`, `blocked`, `muted`, `requests`, `pending`, `user_tweets`, `retweeted_tweets`, `upvoted_tweets`, `downvoted_tweets`, `reported_tweets`, `saved_tweets`, `notifications`, `groups`, `chats`, `private_state`, `info_state`, `last_seen_state`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        statement.setString(1, profile.getPicture());
        statement.setDate(2, (Date) profile.getLastSeen());
        statement.setString(3, new Gson().toJson(profile.getFollowers()));
        statement.setString(4, new Gson().toJson(profile.getFollowings()));
        statement.setString(5, new Gson().toJson(profile.getBlocked()));
        statement.setString(6, new Gson().toJson(profile.getMuted()));
        statement.setString(7, new Gson().toJson(profile.getRequests()));
        statement.setString(8, new Gson().toJson(profile.getPending()));
        statement.setString(9, new Gson().toJson(profile.getUserTweets()));
        statement.setString(10, new Gson().toJson(profile.getRetweetedTweets()));
        statement.setString(11, new Gson().toJson(profile.getUpvotedTweets()));
        statement.setString(12, new Gson().toJson(profile.getDownvotedTweets()));
        statement.setString(13, new Gson().toJson(profile.getReportedTweets()));
        statement.setString(14, new Gson().toJson(profile.getSavedTweets()));
        statement.setString(15, new Gson().toJson(profile.getNotifications()));
        statement.setString(16, new Gson().toJson(profile.getGroups()));
        statement.setString(17, new Gson().toJson(profile.getChats()));
        statement.setBoolean(18, profile.isPrivate());
        statement.setBoolean(19, profile.getInfoState());
        statement.setInt(20, profile.getLastSeenState());
        if (exists)
        {
            statement.setLong(21, profile.getId());
        }
        else
        {
            profile.setId(maxProfileId());
        }
        statement.executeQuery();
        if (!exists)
        {
            profile.setId(maxProfileId());
        }
        return loadProfile(profile.getId());
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
