package controller.server;

import config.Config;
import constants.Constants;
import controller.ListsController;
import controller.RequestController;
import controller.TweetController;
import controller.UserController;
import db.Database;
import event.EventVisitor;
import event.events.authentication.LoginForm;
import event.events.authentication.SignUpForm;
import event.events.general.SendTweetForm;
import event.events.settings.SettingsForm;
import exceptions.DatabaseError;
import exceptions.Unauthenticated;
import exceptions.authentication.LoginFailed;
import exceptions.authentication.SignUpFailed;
import model.Profile;
import model.Tweet;
import model.User;
import response.Response;
import response.ResponseSender;
import response.responses.Pong;
import response.responses.authentication.LoginResponse;
import response.responses.authentication.LogoutResponse;
import response.responses.authentication.OfflineLoginResponse;
import response.responses.authentication.SignUpResponse;
import response.responses.database.*;
import response.responses.general.*;
import util.ImageUtil;
import util.Token;
import util.Validations;

import java.sql.SQLException;
import java.util.List;

public class ClientHandler extends Thread implements EventVisitor
{
    private final Token tokenGenerator = new Token();
    private final ResponseSender responseSender;
    private boolean running;

    private User loggedInUser;
    private String authToken;

    public ClientHandler(ResponseSender responseSender)
    {
        this.responseSender = responseSender;
        running = true;
    }

    @Override
    public void run()
    {
        while (running)
        {
            try
            {
                responseSender.sendResponse(responseSender.getEvent().visit(this));
            }
            catch (Exception ignored) {}
        }
    }

    @SuppressWarnings("unused")
    public void kill()
    {
        running = false;
    }

    @Override
    public Response ping(String ping)
    {
        if (ping.equals("ping")) return new Pong();

        return null;
    }

    @Override
    public Response getChat(long id)
    {
        try
        {
            if (Database.getDB().rowExists("chats", id))
            {
                return new GetChatResponse(Database.getDB().loadChat(id), null);
            }
        } catch (SQLException e)
        {
            return new GetChatResponse(null, new DatabaseError(e.getMessage()));
        }
        return new GetChatResponse(null, new DatabaseError("given chat id doesn't exist"));
    }

    @Override
    public Response getGroup(long id)
    {
        try
        {
            if (Database.getDB().rowExists("groups", id))
            {
                return new GetGroupResponse(Database.getDB().loadGroup(id), null);
            }
        } catch (SQLException e)
        {
            return new GetGroupResponse(null, new DatabaseError(e.getMessage()));
        }
        return new GetGroupResponse(null, new DatabaseError("given group id doesn't exist"));
    }

    @Override
    public Response getMessage(long id)
    {
        try
        {
            if (Database.getDB().rowExists("messages", id))
            {
                return new GetMessageResponse(Database.getDB().loadMessage(id), null);
            }
        } catch (SQLException e)
        {
            return new GetMessageResponse(null, new DatabaseError(e.getMessage()));
        }
        return new GetMessageResponse(null, new DatabaseError("given message id doesn't exist"));
    }

    @Override
    public Response getNotification(long id)
    {
        try
        {
            if (Database.getDB().rowExists("notifications", id))
            {
                return new GetNotificationResponse(Database.getDB().loadNotification(id), null);
            }
        } catch (SQLException e)
        {
            return new GetNotificationResponse(null, new DatabaseError(e.getMessage()));
        }
        return new GetNotificationResponse(null, new DatabaseError("given notification id doesn't exist"));
    }

    @Override
    public Response getTweet(long id)
    {
        try
        {
            if (Database.getDB().rowExists("tweets", id))
            {
                return new GetTweetResponse(Database.getDB().loadTweet(id), null);
            }
        } catch (SQLException e)
        {
            return new GetTweetResponse(null, new DatabaseError(e.getMessage()));
        }
        return new GetTweetResponse(null, new DatabaseError("given tweet id doesn't exist"));
    }

    @Override
    public Response getUser(long id)
    {
        try
        {
            if (Database.getDB().rowExists("users", id) && Database.getDB().rowExists("profiles", id))
            {
                return new GetUserResponse(Database.getDB().loadUser(id), Database.getDB().loadProfile(id), null);
            }
        } catch (SQLException e)
        {
            return new GetUserResponse(null, null, new DatabaseError(e.getMessage()));
        }
        return new GetUserResponse(null, null, new DatabaseError("given user id doesn't exist"));
    }

    @Override
    public Response login(LoginForm form)
    {
        String username = form.getUsername();
        String password = form.getPassword();
        try
        {
            User user = Database.getDB().loadUser(username);
            if (user.getPassword().equals(password))
            {
                loggedInUser = user;
                authToken = tokenGenerator.newToken();
                Database.getDB().updateLastSeen(loggedInUser.getId());
                return new LoginResponse(loggedInUser, authToken, null);
            }
            else
            {
                return new LoginResponse(null, "", new LoginFailed("wrong password"));
            }
        } catch (SQLException ignored) {}
        return new LoginResponse(null, "", new LoginFailed("user doesn't exist"));
    }

    @Override
    public Response offlineLogin(long id)
    {
        try
        {
            loggedInUser = Database.getDB().loadUser(id);
            authToken = tokenGenerator.newToken();
            Database.getDB().updateLastSeen(id);
            return new OfflineLoginResponse(loggedInUser, authToken);
        } catch (SQLException ignored) {}
        return new OfflineLoginResponse(null, "");
    }

    @Override
    public Response signUp(SignUpForm form)
    {
        String phoneNumber = form.getPhoneNumber();
        String username = form.getUsername();
        String password = form.getPassword();
        String email = form.getEmail();
        String name = form.getName();

        if (name.equals(""))
        {
            return new SignUpResponse(null, "", new SignUpFailed("name cannot be empty"));
        }
        if (Validations.getValidations().usernameIsInvalid(username))
        {
            return new SignUpResponse(null, "", new SignUpFailed("please enter a valid username"));
        }
        if (Validations.getValidations().usernameIsUnavailable(username))
        {
            return new SignUpResponse(null, "", new SignUpFailed("there is already an account with this username"));
        }
        if (Validations.getValidations().emailIsInvalid(email))
        {
            return new SignUpResponse(null, "", new SignUpFailed("please enter a valid email"));
        }
        if (Validations.getValidations().emailIsUnavailable(email))
        {
            return new SignUpResponse(null, "", new SignUpFailed("there is already an account with this email"));
        }
        if (Validations.getValidations().phoneNumberIsInvalid(phoneNumber))
        {
            return new SignUpResponse(null, "", new SignUpFailed("please enter a valid phone number"));
        }
        if (Validations.getValidations().phoneNumberIsUnavailable(phoneNumber))
        {
            return new SignUpResponse(null, "", new SignUpFailed("there is already an account with this phone number"));
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setPhoneNumber(phoneNumber);
        user.setPhoneNumber(form.getPhoneNumber());
        user.setBirthDate(form.getBirthDate());
        user.setEmail(form.getEmail());

        String picture = form.getPicture();
        if (picture.equals(""))
        {
            picture = ImageUtil.imageToBytes(new Config(Constants.CONFIG).getProperty("profilePicture"));
        }

        Profile profile = new Profile();
        profile.setPicture(picture);

        try
        {
            user = Database.getDB().saveUser(user);
            Database.getDB().saveProfile(profile);

            authToken = tokenGenerator.newToken();

            return new SignUpResponse(user, authToken, null);
        } catch (SQLException ignored) {}

        return new SignUpResponse(null, "", new SignUpFailed("signup failed for some internal reason"));
    }

    @Override
    public Response logout(long id, String token)
    {
        if (!authToken.equals(token))
        {
            return new LogoutResponse(null, new Unauthenticated());
        }

        loggedInUser = null;
        authToken = "";

        return new LogoutResponse(null, null);
    }

    @Override
    public Response sendTweet(SendTweetForm form)
    {
        if (!authToken.equals(form.getAuthToken()))
        {
            return new SendTweetResponse(new Unauthenticated());
        }

        long tweetOwnerId = form.getUserId();
        long upperTweetId = form.getUpperTweet();
        String tweetPicture = form.getPicture();
        String tweetText = form.getTweet();

        Tweet tweet = new Tweet();
        tweet.setOwner(tweetOwnerId);
        tweet.setUpperTweet(upperTweetId);
        tweet.setPicture(tweetPicture);
        tweet.setText(tweetText);

        try
        {
            tweet = Database.getDB().saveTweet(tweet);
            Tweet upperTweet = Database.getDB().loadTweet(upperTweetId);
            upperTweet.addComment(tweet);
            Database.getDB().saveTweet(upperTweet);
        } catch (SQLException ignored) {}

        return new SendTweetResponse(null);
    }

    @Override
    public Response viewList(String list, long userId)
    {
        List<List<Long>> items = getList(list, userId);

        if (items == null) return new ViewListResponse("", null, null);

        return new ViewListResponse(list, loggedInUser, items);

    }

    @Override
    public Response refreshList(String list, long userId)
    {
        List<List<Long>> items = getList(list, userId);

        if (items == null) return new ViewListResponse("", null, null);

        return new ViewListResponse(list, loggedInUser, items);
    }

    public List<List<Long>> getList(String list, long userId)
    {
        ListsController controller = new ListsController();
        List<List<Long>> items = null;

        switch (list)
        {
            case "followers":
                items = controller.getFollowers(userId);
                break;
            case "followings":
                items = controller.getFollowings(userId);
                break;
            case "blacklist":
                items = controller.getBlackList(userId);
                break;
            case "notifications":
                items = controller.getNotifications(userId);
                break;
        }

        if (items == null) return null;

        try
        {
            loggedInUser = Database.getDB().loadUser(loggedInUser.getId());
        } catch (SQLException ignored) {}

        return items;
    }

    @Override
    public Response viewTweet(long tweetId)
    {
        TweetController controller = new TweetController();
        List<List<Long>> comments = controller.getComments(loggedInUser.getId(), tweetId);

        try
        {
            Tweet tweet = Database.getDB().loadTweet(tweetId);
            return new ViewTweetResponse(tweet, comments);
        } catch (SQLException ignored) {}

        return new ViewTweetResponse(null, null);
    }

    @Override
    public Response refreshTweet(long tweetId)
    {
        TweetController controller = new TweetController();
        List<List<Long>> comments = controller.getComments(loggedInUser.getId(), tweetId);

        try
        {
            Tweet tweet = Database.getDB().loadTweet(tweetId);
            return new ViewTweetResponse(tweet, comments);
        } catch (SQLException ignored) {}

        return new ViewTweetResponse(null, null);
    }

    @Override
    public Response viewUser(long userId)
    {
        UserController controller = new UserController();
        List<List<Long[]>> tweets = controller.getTweets(loggedInUser.getId(), userId);

        try
        {
            User user = Database.getDB().loadUser(userId);
            return new ViewUserResponse(user, tweets);
        } catch (SQLException ignored) {}

        return new ViewUserResponse(null, null);
    }

    @Override
    public Response refreshUser(long userId)
    {
        UserController controller = new UserController();
        List<List<Long[]>> tweets = controller.getTweets(loggedInUser.getId(), userId);

        try
        {
            User user = Database.getDB().loadUser(userId);
            return new ViewUserResponse(user, tweets);
        } catch (SQLException ignored) {}

        return new RefreshUserResponse(null, null);
    }

    @Override
    public Response requestReaction(String reaction, long notificationId, String token)
    {
        if (!authToken.equals(token))
        {
            return new RequestReactionResponse(new Unauthenticated());
        }

        RequestController controller = new RequestController();

        switch (reaction)
        {
            case "accept":
                controller.accept(notificationId);
                break;
            case "good reject":
                controller.goodReject(notificationId);
                break;
            case "bad reject":
                controller.badReject(notificationId);
                break;
        }

        return new RequestReactionResponse(null);
    }

    @Override
    public Response settings(SettingsForm settingsForm, Long aLong, String s, boolean b)
    {
        return null;
    }

    @Override
    public Response deleteAccount(long l, String s, boolean b)
    {
        return null;
    }

    @Override
    public Response deactivate(long l, String s, boolean b)
    {
        return null;
    }

    @Override
    public Response viewProfile(long l)
    {
        return null;
    }

    @Override
    public Response refreshProfile(long l)
    {
        return null;
    }

    @Override
    public Response userInteraction(String s, long l, long l1, String s1) {
        return null;
    }

    @Override
    public Response explore(long l) {
        return null;
    }

    @Override
    public Response searchUser(long l, String s) {
        return null;
    }

    @Override
    public Response viewTimeline(long l) {
        return null;
    }

    @Override
    public Response refreshTimeline(long l) {
        return null;
    }

    @Override
    public Response viewBookmarks(long l) {
        return null;
    }

    @Override
    public Response refreshBookmarks(long l) {
        return null;
    }

    @Override
    public Response tweetInteraction(String s, long l, long l1, String s1) {
        return null;
    }

    @Override
    public Response forwardTweet(String s, String s1, long l, String s2) {
        return null;
    }
}