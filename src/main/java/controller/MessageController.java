package controller;

import controller.bot.BotController;
import db.Database;
import event.events.messages.MessageForm;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MessageController
{
    private static final Logger logger = LogManager.getLogger(MessageController.class);

    public void forwardTweet(long loggedInUserId, long tweetId, String usernames, String groupNames) throws SQLException
    {
        User loggedInUser = Database.getDB().loadUser(loggedInUserId);

        GroupController groupController = new GroupController();
        ChatController chatController = new ChatController();
        List<Long> usersId = new LinkedList<>();

        String[] usernamesArray = usernames.split(" ");
        String[] groupNamesArray = groupNames.split(" ");

        for (String username : usernamesArray)
        {
            if (!username.equals(""))
            {
                try
                {
                    User user = Database.getDB().loadUser(username);
                    if (!usersId.contains(user.getId()) && !user.getId().equals(loggedInUser.getId()))
                    {
                        usersId.add(user.getId());
                    }
                }
                catch (SQLException ignored)
                {
                    logger.error(String.format("database error while loading user: %s", username));
                }
            }
        }

        for (String groupName : groupNamesArray)
        {
            if (!groupName.equals(""))
            {
                Group group = groupController.getGroupByName(loggedInUser.getId(), groupName);
                for (Long userId : group.getMembers())
                {
                    if (!usersId.contains(userId) && !userId.equals(loggedInUser.getId()))
                    {
                        usersId.add(userId);
                    }
                }
            }
        }

        Tweet tweet = null;
        try
        {
            tweet = Database.getDB().loadTweet(tweetId);
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while loading tweet: %s", tweetId));
        }

        for (Long userId : usersId)
        {
            Chat pv = chatController.getPv(loggedInUser.getId(), userId);
            if (pv != null)
            {
                Message message = new Message(pv, loggedInUser, Objects.requireNonNull(tweet));
                message.setSent(true);
                try
                {
                    message = Database.getDB().saveMessage(message);
                    pv.addToMessages(message.getId());
                    Database.getDB().saveChat(pv);
                }
                catch (SQLException ignored)
                {
                    logger.error(String.format("database error while forwarding tweet: %s", tweetId));
                }
            }
        }
    }

    public void sendMessage(MessageForm form)
    {
        Message message = new Message();
        message.setText(form.getText());
        message.setChatId(form.getChatId());
        message.setOwnerId(form.getOwnerId());
        message.setPicture(form.getBase64Picture());
        message.setMessageDate(form.getMessageDate().equals(-1L) ? new Date().getTime() : form.getMessageDate());
        message.setSent(true);

        try
        {
            message = Database.getDB().saveMessage(message);
            Chat chat = Database.getDB().loadChat(message.getChatId());
            chat.addToMessages(message.getId());
            Database.getDB().saveChat(chat);

            BotController controller = new BotController();
            if (form.getText().startsWith("/") && controller.hasBot(form.getChatId()))
            {
                controller.handleCommand(form.getOwnerId(), form.getChatId(), form.getText());
            }

            logger.info(String.format("new message was just sent with id: %s", message.getId()));
        }
        catch (SQLException ignored)
        {
            logger.error("database error while saving new message");
        }
    }

    public void sendCachedMessages(List<Message> messages)
    {
        for (Message message : messages)
        {
            try
            {
                message.setMessageDate(message.getMessageDate() == -1L ? new Date().getTime() : message.getMessageDate());
                message.setSent(true);
                message = Database.getDB().saveMessage(message);
                Chat chat = Database.getDB().loadChat(message.getChatId());
                chat.addToMessages(message.getId());
                Database.getDB().saveChat(chat);
            } catch (SQLException ignored)
            {
                logger.error("database error while saving received offline message");
            }
        }
    }
}
