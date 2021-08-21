package controller.server;

import controller.*;
import db.Database;
import event.EventVisitor;
import event.events.authentication.LoginForm;
import event.events.authentication.SignUpForm;
import event.events.general.SendTweetForm;
import event.events.groups.ManageGroupForm;
import event.events.messages.MessageForm;
import event.events.settings.SettingsForm;
import exceptions.DatabaseError;
import exceptions.Unauthenticated;
import model.*;
import response.Response;
import response.ResponseSender;
import response.responses.Pong;
import response.responses.authentication.LoginResponse;
import response.responses.authentication.LogoutResponse;
import response.responses.authentication.OfflineLoginResponse;
import response.responses.database.*;
import response.responses.explore.ExplorePageResponse;
import response.responses.explore.SearchUserResponse;
import response.responses.general.*;
import response.responses.groups.ManageGroupsResponse;
import response.responses.groups.RefreshGroupsPageResponse;
import response.responses.groups.ViewGroupsPageResponse;
import response.responses.messages.*;
import response.responses.profile.RefreshProfileResponse;
import response.responses.profile.UserInteractionResponse;
import response.responses.profile.ViewProfileResponse;
import response.responses.settings.DeactivationResponse;
import response.responses.settings.DeleteAccountResponse;
import response.responses.settings.SettingsResponse;
import response.responses.timeline.RefreshBookmarksResponse;
import response.responses.timeline.RefreshTimelineResponse;
import response.responses.timeline.ViewBookmarksResponse;
import response.responses.timeline.ViewTimelineResponse;
import response.responses.tweet.ForwardTweetResponse;
import response.responses.tweet.TweetInteractionResponse;

import java.sql.SQLException;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ClientHandler extends Thread implements EventVisitor
{
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

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
        if (ping.equals("ping"))
        {
            logger.info("application pinged with some client");
            return new Pong();
        }

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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while getting chat with id: %s", e, id));
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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while getting group with id: %s", e, id));
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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while getting message with id: %s", e, id));
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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while getting notification with id: %s", e, id));
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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while getting tweet with id: %s", e, id));
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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while getting user with id: %s", e, id));
            return new GetUserResponse(null, null, new DatabaseError(e.getMessage()));
        }
        return new GetUserResponse(null, null, new DatabaseError("given user id doesn't exist"));
    }

    @Override
    public Response login(LoginForm form)
    {
        AuthController controller = new AuthController();
        LoginResponse response = controller.login(form);
        if (response.getErr() == null)
        {
            loggedInUser = response.getUser();
            authToken = response.getAuthToken();
        }
        return response;
    }

    @Override
    public Response offlineLogin(long id)
    {
        AuthController controller = new AuthController();
        OfflineLoginResponse response = controller.offlineLogin(id);
        if (response.getUser() != null)
        {
            loggedInUser = response.getUser();
            authToken = response.getAuthToken();
        }
        return response;
    }

    @Override
    public Response signUp(SignUpForm form)
    {
        AuthController controller = new AuthController();
        return controller.signUp(form);
    }

    @Override
    public Response logout(long id, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new LogoutResponse(null, new Unauthenticated());
        }

        // loggedInUser = null;
        authToken = "";

        logger.debug(String.format("user %s logged out", id));
        return new LogoutResponse(null, null);
    }

    @Override
    public Response sendTweet(SendTweetForm form)
    {
        if (!authToken.equals(form.getAuthToken()))
        {
            logger.warn(String.format("unauthenticated token: %s", form.getAuthToken()));
            return new SendTweetResponse(new Unauthenticated());
        }

        TweetController controller = new TweetController();
        controller.sendTweet(form);

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

        if (items == null) return new RefreshListResponse("", null, null);

        return new RefreshListResponse(list, loggedInUser, items);
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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading tweet with id: %s", e, tweetId));
        }

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
            return new RefreshTweetResponse(tweet, comments);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading tweet with id: %s", e, tweetId));
        }

        return new RefreshTweetResponse(null, null);
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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading user with id: %s", e, userId));
        }

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
            return new RefreshUserResponse(user, tweets);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading user with id: %s", e, userId));
        }

        return new RefreshUserResponse(null, null);
    }

    @Override
    public Response requestReaction(String reaction, long notificationId, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new RequestReactionResponse(new Unauthenticated());
        }

        RequestController controller = new RequestController();

        switch (reaction)
        {
            case "accept":
                logger.debug(String.format("follow request with id %s was just accepted", notificationId));
                controller.accept(notificationId);
                break;
            case "good reject":
                logger.debug(String.format("follow request with id %s was just rejected", notificationId));
                controller.goodReject(notificationId);
                break;
            case "bad reject":
                logger.debug(String.format("follow request with id %s was just roasted", notificationId));
                controller.badReject(notificationId);
                break;
        }

        return new RequestReactionResponse(null);
    }

    @Override
    public Response refreshLastSeen(Long userId)
    {
        if (loggedInUser != null && loggedInUser.getId().equals(userId))
        {
            Database.getDB().updateLastSeen(userId);
        }

        return new RefreshLastSeenResponse();
    }

    @Override
    public Response settings(SettingsForm form, Long userId, String token, boolean online)
    {
        if (online)
        {
            if (!authToken.equals(token))
            {
                logger.warn(String.format("unauthenticated token: %s", token));
                return new SettingsResponse(true, null, new Unauthenticated());
            }
        }

        SettingsController controller = new SettingsController();
        controller.editUser(form, userId);

        try
        {
            logger.fatal(String.format("user %s changed their account settings", userId));
            loggedInUser = Database.getDB().loadUser(loggedInUser.getId());
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while changing user settings with id: %s", e, userId));
        }

        return new SettingsResponse(online, null, null);
    }

    @Override
    public Response deleteAccount(long userId, String token, boolean online)
    {
        if (online)
        {
            if (!authToken.equals(token))
            {
                logger.warn(String.format("unauthenticated token: %s", token));
                return new DeleteAccountResponse(true, new Unauthenticated());
            }
        }

        SettingsController controller = new SettingsController();
        controller.deleteAccount(userId);

        loggedInUser = null;
        authToken = "";

        logger.fatal(String.format("user %s just deleted their account :(((", userId));
        return new DeleteAccountResponse(online, null);
    }

    @Override
    public Response deactivate(long userId, String token, boolean online)
    {
        if (online)
        {
            if (!authToken.equals(token))
            {
                logger.warn(String.format("unauthenticated token: %s", token));
                return new DeactivationResponse(true, new Unauthenticated());
            }
        }

        SettingsController controller = new SettingsController();
        controller.deactivate(userId);

        loggedInUser = null;
        authToken = "";

        logger.fatal(String.format("user %s just deactivated their account :(", userId));
        return new DeactivationResponse(online, null);
    }

    @Override
    public Response viewProfile(long userId)
    {
        UserController controller = new UserController();
        List<List<Long[]>> tweets = controller.getTweets(loggedInUser.getId(), userId);

        try
        {
            User user = Database.getDB().loadUser(userId);
            return new ViewProfileResponse(user, tweets);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading user with id: %s", e, userId));
        }

        return new ViewProfileResponse(null, null);
    }

    @Override
    public Response refreshProfile(long userId)
    {
        UserController controller = new UserController();
        List<List<Long[]>> tweets = controller.getTweets(loggedInUser.getId(), userId);

        try
        {
            User user = Database.getDB().loadUser(userId);
            return new RefreshProfileResponse(user, tweets);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading user with id: %s", e, userId));
        }

        return new RefreshProfileResponse(null, null);
    }

    @Override
    public Response explore(long userId)
    {
        ExploreController controller = new ExploreController();
        return new ExplorePageResponse(controller.getRandomTweets(userId));
    }

    @Override
    public Response searchUser(long userId, String searched)
    {
        ExploreController controller = new ExploreController();
        return new SearchUserResponse(controller.search(searched));
    }

    @Override
    public Response viewTimeline(long userId)
    {
        TimelineController controller = new TimelineController();
        return new ViewTimelineResponse(controller.getTimeline(userId));
    }

    @Override
    public Response refreshTimeline(long userId)
    {
        TimelineController controller = new TimelineController();
        return new RefreshTimelineResponse(controller.getTimeline(userId));
    }

    @Override
    public Response viewBookmarks(long userId)
    {
        TimelineController controller = new TimelineController();
        return new ViewBookmarksResponse(controller.getBookmarks(userId));
    }

    @Override
    public Response refreshBookmarks(long userId)
    {
        TimelineController controller = new TimelineController();
        return new RefreshBookmarksResponse(controller.getBookmarks(userId));
    }

    @Override
    public Response viewGroupsPage(Long userId)
    {
        GroupController controller = new GroupController();
        return new ViewGroupsPageResponse(controller.getGroups(userId));
    }

    @Override
    public Response refreshGroupsPage(Long userId)
    {
        GroupController controller = new GroupController();
        return new RefreshGroupsPageResponse(controller.getGroups(userId));
    }

    @Override
    public Response manageGroup(ManageGroupForm form, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new ManageGroupsResponse(new Unauthenticated());
        }

        GroupController controller = new GroupController();
        controller.manageGroup(form, loggedInUser.getId());

        return new ManageGroupsResponse(null);
    }

    @Override
    public Response receivedAllMessages(Long userId, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new ReceivedAllMessagesResponse(new Unauthenticated());
        }

        try
        {
            Database.getDB().receivedAllMessages(userId);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while setting all %s messages received", e, userId));
        }

        return new ReceivedAllMessagesResponse(null);
    }

    @Override
    public Response viewChatroom(Long chatId)
    {
        ChatController controller = new ChatController();
        return new ViewChatroomResponse(controller.getChatroom(loggedInUser.getId(), chatId), chatId);
    }

    @Override
    public Response refreshChatroom(Long chatId)
    {
        ChatController controller = new ChatController();
        return new RefreshChatroomResponse(controller.getChatroom(loggedInUser.getId(), chatId), chatId);
    }

    @Override
    public Response viewMessagesPage(Long userId)
    {
        ChatController controller = new ChatController();
        return new ViewMessagesPageResponse(controller.getMessagesList(userId));
    }

    @Override
    public Response refreshMessagesPage(Long userId)
    {
        ChatController controller = new ChatController();
        return new RefreshMessagesPageResponse(controller.getMessagesList(userId));
    }

    @Override
    public Response sendMessage(MessageForm form, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new SendMessageResponse(new Unauthenticated());
        }

        MessageController controller = new MessageController();
        controller.sendMessage(form);

        return new SendMessageResponse(null);
    }

    @Override
    public Response editMessage(MessageForm form, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new EditMessageResponse(new Unauthenticated());
        }

        try
        {
            Message message = Database.getDB().loadMessage(form.getId());
            message.edit(form.getText());
            Database.getDB().saveMessage(message);
            logger.debug(String.format("message %s was just edited", form.getId()));
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while editing message with id: %s", e, form.getId()));
        }

        return new EditMessageResponse(null);
    }

    @Override
    public Response deleteMessage(long messageId, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new DeleteMessageResponse(new Unauthenticated());
        }

        try
        {
            Message message = Database.getDB().loadMessage(messageId);
            message.delete();
            Database.getDB().saveMessage(message);
            logger.debug(String.format("message %s was just deleted", messageId));
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while deleting message with id: %s", e, messageId));
        }

        return new DeleteMessageResponse(null);
    }

    @Override
    public Response sendCachedMessages(List<Message> messages, String token)
    {
        MessageController controller = new MessageController();
        controller.sendCachedMessages(messages);

        return new SendCachedMessagesResponse(null);
    }

    @Override
    public Response newChat(String username, String chatName, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new NewChatResponse(new Unauthenticated(), null);
        }

        ChatController controller = new ChatController();
        return controller.newChat(loggedInUser, username, chatName);
    }

    @Override
    public Response addMember(Long chatId, String username, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new AddMemberResponse(new Unauthenticated());
        }

        ChatController controller = new ChatController();
        controller.addMember(chatId, username);

        return new AddMemberResponse(null);
    }

    @Override
    public Response leaveGroup(Long chatId, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new LeaveGroupResponse(new Unauthenticated());
        }

        ChatController controller = new ChatController();
        controller.leaveGroup(loggedInUser, chatId);

        return new LeaveGroupResponse(null);
    }

    @Override
    public Response userInteraction(String interaction, long userId, long otherUserId, String token)
    {
        if (!authToken.equals(token) || !loggedInUser.getId().equals(userId))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new UserInteractionResponse(new Unauthenticated());
        }

        UserController controller = new UserController();

        switch (interaction)
        {
            case "change":
                logger.debug(String.format("user %s changed follow status for user %s", userId, otherUserId));
                controller.changeFollowStatus(userId, otherUserId);
                break;
            case "block":
                logger.warn(String.format("user %s blocked user %s", userId, otherUserId));
                controller.block(userId, otherUserId);
                break;
            case "mute":
                logger.debug(String.format("user %s muted user %s", userId, otherUserId));
                controller.mute(userId, otherUserId);
                break;
        }

        return new UserInteractionResponse(null);
    }

    @Override
    public Response tweetInteraction(String interaction, long userId, long tweetId, String token)
    {
        if (!authToken.equals(token) || !loggedInUser.getId().equals(userId))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new TweetInteractionResponse(new Unauthenticated());
        }

        TweetController controller = new TweetController();

        switch (interaction)
        {
            case "upvote":
                logger.info(String.format("user %s upvoted tweet %s", userId, tweetId));
                controller.upvote(userId, tweetId);
                break;
            case "downvote":
                logger.info(String.format("user %s downvoted tweet %s", userId, tweetId));
                controller.downvote(userId, tweetId);
                break;
            case "retweet":
                logger.info(String.format("user %s retweeted tweet %s", userId, tweetId));
                controller.retweet(userId, tweetId);
                break;
            case "save":
                logger.info(String.format("user %s saved tweet %s", userId, tweetId));
                controller.save(userId, tweetId);
                break;
            case "report":
                logger.warn(String.format("user %s reported tweet %s", userId, tweetId));
                controller.report(userId, tweetId);
                break;
        }

        return new TweetInteractionResponse(null);
    }

    @Override
    public Response forwardTweet(String usernames, String groupNames, long tweetId, String token)
    {
        if (!authToken.equals(token))
        {
            logger.warn(String.format("unauthenticated token: %s", token));
            return new ForwardTweetResponse(null, new Unauthenticated());
        }

        MessageController controller = new MessageController();
        try
        {
            controller.forwardTweet(loggedInUser.getId(), tweetId, usernames, groupNames);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while forwarding tweet %s", e, tweetId));
        }

        logger.debug(String.format("tweet with id %s was forwarded to a bunch of people", tweetId));
        return new ForwardTweetResponse(null, null);
    }
}
