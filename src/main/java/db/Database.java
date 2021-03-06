package db;

import com.google.gson.Gson;
import util.ListUtil;
import model.*;

import java.sql.*;

public class Database
{
    private static Connection connection;

    // Singleton class stuff

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

    // Establish connection to the database
    public void connectToDatabase(String url, String username, String password) throws SQLException
    {
        connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        statement.close();
    }

    // Find out if there exists a row with given id in a table
    public boolean rowExists(String table, long id) throws SQLException
    {
        if (id == -1L)
        {
            return false;
        }

        String query = "";
        switch (table)
        {
            case "users":
                query = "SELECT 1 FROM `users` WHERE `id` = ?";
                break;
            case "profiles":
                query = "SELECT 1 FROM `profiles` WHERE `id` = ?";
                break;
            case "tweets":
                query = "SELECT 1 FROM `tweets` WHERE `id` = ?";
                break;
            case "groups":
                query = "SELECT 1 FROM `groups` WHERE `id` = ?";
                break;
            case "chats":
                query = "SELECT 1 FROM `chats` WHERE `id` = ?";
                break;
            case "messages":
                query = "SELECT 1 FROM `messages` WHERE `id` = ?";
                break;
            case "notifications":
                query = "SELECT 1 FROM `notifications` WHERE `id` = ?";
                break;
        }
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        boolean ans = res.next();
        statement.close();
        res.close();
        return ans;
    }

    // Get the max amount of the auto-generated id in a table
    public Long maxTableId(String table) throws SQLException
    {
        String query = "";
        switch (table)
        {
            case "users":
                query = "SELECT MAX(`id`) AS `max_id` FROM `users`";
                break;
            case "profiles":
                query = "SELECT MAX(`id`) AS `max_id` FROM `profiles`";
                break;
            case "tweets":
                query = "SELECT MAX(`id`) AS `max_id` FROM `tweets`";
                break;
            case "groups":
                query = "SELECT MAX(`id`) AS `max_id` FROM `groups`";
                break;
            case "chats":
                query = "SELECT MAX(`id`) AS `max_id` FROM `chats`";
                break;
            case "messages":
                query = "SELECT MAX(`id`) AS `max_id` FROM `messages`";
                break;
            case "notifications":
                query = "SELECT MAX(`id`) AS `max_id` FROM `notifications`";
                break;
            case "bots":
                query = "SELECT MAX(`id`) AS `max_id` FROM `bots`";
                break;
        }
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet res = statement.executeQuery();
        long maxId = -1L;
        if (res.next()) {
            maxId = res.getLong("max_id");
        }
        statement.close();
        res.close();
        return maxId;
    }

    // Check if a username/email/phone number exists in the table for Validations class
    public boolean itemAvailable(String column, String item) throws SQLException
    {
        String query = "";
        switch (column)
        {
            case "email":
                query = "SELECT 1 FROM `users` WHERE `email` = ?";
                break;
            case "username":
                query = "SELECT 1 FROM `users` WHERE `username` = ?";
                break;
            case "phone_number":
                query = "SELECT 1 FROM `users` WHERE `phone_number` = ?";
                break;
        }
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, item);
        ResultSet res = statement.executeQuery();
        boolean ans = res.next();
        statement.close();
        res.close();
        return ans;
    }

    public User extractUser(ResultSet res) throws SQLException
    {
        User user = new User();
        while (res.next())
        {
            user.setId(res.getLong("id"));
            user.setUsername(res.getString("username"));
            user.setPassword(res.getString("password"));
            user.setBio(res.getString("bio"));
            user.setName(res.getString("name"));
            user.setEmail(res.getString("email"));
            user.setBirthDate(new java.util.Date(res.getLong("birth_date")));
            user.setPhoneNumber(res.getString("phone_number"));
            user.setActive(res.getBoolean("is_active"));
            user.setDeleted(res.getBoolean("is_deleted"));
        }
        return user;
    }

    public User loadUser(long id) throws SQLException
    {
        if (id == -1)
        {
            return null;
        }

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        User user = extractUser(res);
        statement.close();
        res.close();
        return user;
    }

    public User loadUser(String username) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `username` = ?");
        statement.setString(1, username);
        ResultSet res = statement.executeQuery();
        User user = extractUser(res);
        statement.close();
        res.close();
        return user;
    }

    public User saveUser(User user) throws SQLException
    {
        boolean exists = rowExists("users", user.getId());

        PreparedStatement statement;
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
        statement.setLong(7, user.getBirthDate().getTime());
        statement.setBoolean(8, user.isActive());
        statement.setBoolean(9, user.isDeleted());
        if (exists)
        {
            statement.setLong(10, user.getId());
        }
        statement.executeUpdate();
        statement.close();
        if (!exists)
        {
            user.setId(maxTableId("users"));
        }
        return loadUser(user.getId());
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
            profile.setLastSeen(new java.util.Date(res.getLong("last_seen")));
            profile.setFollowers(ListUtil.JsonToList(res.getString("followers")));
            profile.setFollowings(ListUtil.JsonToList(res.getString("followings")));
            profile.setBlocked(ListUtil.JsonToList(res.getString("blocked")));
            profile.setMuted(ListUtil.JsonToList(res.getString("muted")));
            profile.setRequests(ListUtil.JsonToList(res.getString("requests")));
            profile.setPending(ListUtil.JsonToList(res.getString("pending")));
            profile.setUserTweets(ListUtil.JsonToList(res.getString("user_tweets")));
            profile.setRetweetedTweets(ListUtil.JsonToList(res.getString("retweeted_tweets")));
            profile.setUpvotedTweets(ListUtil.JsonToList(res.getString("upvoted_tweets")));
            profile.setDownvotedTweets(ListUtil.JsonToList(res.getString("downvoted_tweets")));
            profile.setReportedTweets(ListUtil.JsonToList(res.getString("reported_tweets")));
            profile.setSavedTweets(ListUtil.JsonToList(res.getString("saved_tweets")));
            profile.setNotifications(ListUtil.JsonToList(res.getString("notifications")));
            profile.setGroups(ListUtil.JsonToList(res.getString("groups")));
            profile.setChats(ListUtil.JsonToList(res.getString("chats")));
            profile.setPrivateState(res.getBoolean("private_state"));
            profile.setInfoState(res.getBoolean("info_state"));
            profile.setLastSeenState(res.getInt("last_seen_state"));
        }
        statement.close();
        res.close();
        return profile;
    }

    public Profile saveProfile(Profile profile) throws SQLException
    {
        boolean exists = rowExists("profiles", profile.getId());
        PreparedStatement statement;
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
        statement.setLong(2, profile.getLastSeen().getTime());
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
        statement.executeUpdate();
        statement.close();
        if (!exists)
        {
            profile.setId(maxTableId("users"));
        }
        return loadProfile(profile.getId());
    }

    public void updateLastSeen(long id)
    {
        try
        {
            Profile profile = loadProfile(id);
            profile.setLastSeen(new java.util.Date());
            saveProfile(profile);
        } catch (SQLException ignored) {}
    }

    public Tweet loadTweet(long id) throws SQLException
    {
        if (id == -1)
        {
            return null;
        }

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `tweets` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        Tweet tweet = new Tweet();
        while (res.next())
        {
            tweet.setId(id);
            tweet.setOwner(res.getLong("owner"));
            tweet.setUpperTweet(res.getLong("upper_tweet"));
            tweet.setPicture(res.getString("picture"));
            tweet.setVisible(res.getBoolean("visible"));
            tweet.setText(res.getString("text"));
            tweet.setTweetDate(new java.util.Date(res.getLong("tweet_date")));
            tweet.setComments(ListUtil.JsonToList(res.getString("comments")));
            tweet.setUpvotes(ListUtil.JsonToList(res.getString("upvotes")));
            tweet.setDownvotes(ListUtil.JsonToList(res.getString("downvotes")));
            tweet.setRetweets(ListUtil.JsonToList(res.getString("retweets")));
            tweet.setReports(res.getInt("reports"));
        }
        statement.close();
        res.close();
        return tweet;
    }

    public Tweet saveTweet(Tweet tweet) throws SQLException
    {
        boolean exists = rowExists("tweets", tweet.getId());

        PreparedStatement statement;
        if (exists)
        {
            statement = connection.prepareStatement(
                    "UPDATE `tweets` SET `owner` = ?, `upper_tweet` = ?, `picture` = ?, `visible` = ?, `text` = ?, `tweet_date` = ?, `comments` = ?, `upvotes` = ?, `downvotes` = ?, `retweets` = ?, `reports` = ? WHERE `id` = ?");
        }
        else
        {
            statement = connection.prepareStatement(
                    "INSERT INTO `tweets` (`owner`, `upper_tweet`, `picture`, `visible`, `text`, `tweet_date`, `comments`, `upvotes`, `downvotes`, `retweets`, `reports`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        statement.setLong(1, tweet.getOwner());
        statement.setLong(2, tweet.getUpperTweet());
        statement.setString(3, tweet.getPicture());
        statement.setBoolean(4, tweet.isVisible());
        statement.setString(5, tweet.getText());
        statement.setLong(6, tweet.getTweetDate().getTime());
        statement.setString(7, new Gson().toJson(tweet.getComments()));
        statement.setString(8, new Gson().toJson(tweet.getUpvotes()));
        statement.setString(9, new Gson().toJson(tweet.getDownvotes()));
        statement.setString(10, new Gson().toJson(tweet.getRetweets()));
        statement.setInt(11, tweet.getReports());
        if (exists)
        {
            statement.setLong(12, tweet.getId());
        }
        statement.executeUpdate();
        statement.close();
        if (!exists)
        {
            tweet.setId(maxTableId("tweets"));
        }
        return loadTweet(tweet.getId());
    }

    public Group loadGroup(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `groups` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        Group group = new Group();
        while (res.next())
        {
            group.setId(id);
            group.setTitle(res.getString("title"));
            group.setMembers(ListUtil.JsonToList(res.getString("members")));
        }
        statement.close();
        res.close();
        return group;
    }

    public Group saveGroup(Group group) throws SQLException
    {
        boolean exists = rowExists("groups", group.getId());

        PreparedStatement statement;
        if (exists)
        {
            statement = connection.prepareStatement(
                    "UPDATE `groups` SET `title` = ?, `members` = ? WHERE `id` = ?");
        }
        else
        {
            statement = connection.prepareStatement(
                    "INSERT INTO `groups` (`title`, `members`) VALUES (?, ?)");
        }
        statement.setString(1, group.getTitle());
        statement.setString(2, new Gson().toJson(group.getMembers()));
        if (exists)
        {
            statement.setLong(3, group.getId());
        }
        statement.executeUpdate();
        statement.close();
        if (!exists)
        {
            group.setId(maxTableId("groups"));
        }
        return loadGroup(group.getId());
    }

    public Chat loadChat(long id) throws SQLException
    {
        if (id == -1)
        {
            return null;
        }

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `chats` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        Chat chat = new Chat();
        while (res.next())
        {
            chat.setId(id);
            chat.setChatName(res.getString("chat_name"));
            chat.setGroup(res.getBoolean("group"));
            chat.setUsers(ListUtil.JsonToList(res.getString("users")));
            chat.setMessages(ListUtil.JsonToList(res.getString("messages")));
        }
        statement.close();
        res.close();
        return chat;
    }

    public Chat saveChat(Chat chat) throws SQLException
    {
        boolean exists = rowExists("chats", chat.getId());

        PreparedStatement statement;
        if (exists)
        {
            statement = connection.prepareStatement(
                    "UPDATE `chats` SET `chat_name` = ?, `group` = ?, `users` = ?, `messages` = ? WHERE `id` = ?");
        }
        else
        {
            statement = connection.prepareStatement(
                    "INSERT INTO `chats` (`chat_name`, `group`, `users`, `messages`) VALUES (?, ?, ?, ?)");
        }
        statement.setString(1, chat.getChatName());
        statement.setBoolean(2, chat.isGroup());
        statement.setString(3, new Gson().toJson(chat.getUsers()));
        statement.setString(4, new Gson().toJson(chat.getMessages()));
        if (exists)
        {
            statement.setLong(5, chat.getId());
        }
        statement.executeUpdate();
        statement.close();
        if (!exists)
        {
            chat.setId(maxTableId("chats"));
        }
        return loadChat(chat.getId());
    }

    public Message loadMessage(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `messages` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        Message message = new Message();
        while (res.next())
        {
            message.setId(id);
            message.setChatId(res.getLong("chat_id"));
            message.setOwnerId(res.getLong("owner_id"));
            message.setTweetId(res.getLong("tweet_id"));
            message.setIndex(res.getInt("index"));
            message.setText(res.getString("text"));
            message.setPicture(res.getString("picture"));
            message.setMessageDate(res.getLong("message_date_unix"));
            message.setSeenList(ListUtil.JsonToList(res.getString("seen_list")));
            message.setSent(res.getBoolean("sent"));
            message.setReceived(res.getBoolean("received"));
            message.setSeen(res.getBoolean("seen"));
        }
        statement.close();
        res.close();
        return message;
    }

    public Message saveMessage(Message message) throws SQLException
    {
        boolean exists = rowExists("messages", message.getId());

        PreparedStatement statement;
        if (exists)
        {
            statement = connection.prepareStatement(
                    "UPDATE `messages` SET `chat_id` = ?, `owner_id` = ?, `tweet_id` = ?, `index` = ?, `text` = ?, `picture` = ?, `message_date_unix` = ?, `seen_list` = ?, `sent` = ?, `received` = ?, `seen` = ? WHERE `id` = ?");
        }
        else
        {
            statement = connection.prepareStatement(
                    "INSERT INTO `messages` (`chat_id`, `owner_id`, `tweet_id`, `index`, `text`, `picture`, `message_date_unix`, `seen_list`, `sent`, `received`, `seen`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        statement.setLong(1, message.getChatId());
        statement.setLong(2, message.getOwnerId());
        statement.setLong(3, message.getTweetId());
        statement.setInt(4, message.getIndex());
        statement.setString(5, message.getText());
        statement.setString(6, message.getPicture());
        statement.setLong(7, message.getMessageDate());
        statement.setString(8, new Gson().toJson(message.getSeenList()));
        statement.setBoolean(9, message.isSent());
        statement.setBoolean(10, message.isReceived());
        statement.setBoolean(11, message.isSeen());
        if (exists)
        {
            statement.setLong(12, message.getId());
        }
        statement.executeUpdate();
        statement.close();
        if (!exists)
        {
            message.setId(maxTableId("messages"));
        }
        return loadMessage(message.getId());
    }

    public void receivedAllMessages(long userId) throws SQLException
    {
        Profile profile = loadProfile(userId);

        for (Long chatId : profile.getChats())
        {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE `messages` SET `received` = ? WHERE `chat_id` = ? AND `owner_id` <> ?");
            statement.setBoolean(1, true);
            statement.setLong(2, chatId);
            statement.setLong(3, userId);
            statement.executeUpdate();
            statement.close();
        }
    }

    public Notification loadNotification(long id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `notifications` WHERE `id` = ?");
        statement.setLong(1, id);
        ResultSet res = statement.executeQuery();
        Notification notification = new Notification();
        while (res.next())
        {
            notification.setId(id);
            notification.setOwner(res.getLong("owner"));
            notification.setRequestFrom(res.getLong("request_from"));
            notification.setText(res.getString("text"));
        }
        statement.close();
        res.close();
        return notification;
    }

    public Notification saveNotification(Notification notification) throws SQLException
    {
        boolean exists = rowExists("notifications", notification.getId());

        PreparedStatement statement;
        if (exists)
        {
            statement = connection.prepareStatement(
                    "UPDATE `notifications` SET `owner` = ?, `request_from` = ?, `text` = ? WHERE `id` = ?");
        }
        else
        {
            statement = connection.prepareStatement(
                    "INSERT INTO `notifications` (`owner`, `request_from`, `text`) VALUES (?, ?, ?)");
        }
        statement.setLong(1, notification.getOwner());
        statement.setLong(2, notification.getRequestFrom());
        statement.setString(3, notification.getText());
        if (exists)
        {
            statement.setLong(4, notification.getId());
        }
        statement.executeUpdate();
        statement.close();
        if (!exists)
        {
            notification.setId(maxTableId("notifications"));
        }
        return loadNotification(notification.getId());
    }

    public Bot loadUserBot(long userId) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `bots` WHERE `user_id` = ?");
        statement.setLong(1, userId);
        ResultSet res = statement.executeQuery();
        Bot bot = new Bot();
        while (res.next())
        {
            bot.setUserId(userId);
            bot.setId(res.getLong("id"));
            bot.setKind(res.getInt("kind"));
            bot.setJarURL(res.getString("jar_url"));
        }
        statement.close();
        res.close();
        return bot;
    }

    public void saveBot(Bot bot) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO `bots` (`user_id`, `jar_url`, `kind`) VALUES (?, ?, ?)");
        statement.setLong(1, bot.getUserId());
        statement.setString(2, bot.getJarURL());
        statement.setInt(3, bot.getKind());
        statement.executeUpdate();
        statement.close();
    }
}
