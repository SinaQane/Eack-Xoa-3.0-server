package controller;

import db.Database;
import exceptions.messages.ChatCreationFailed;
import model.Chat;
import model.Message;
import model.Profile;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import response.responses.messages.NewChatResponse;

import java.sql.SQLException;
import java.util.*;

public class ChatController
{
    private static final Logger logger = LogManager.getLogger(ChatController.class);

    public List<Long> getChatroom(Long userId, Long chatId)
    {
        List<Long> messages = new LinkedList<>();
        try
        {
            Chat chat = Database.getDB().loadChat(chatId);

            for (Long messageId : chat.getMessages())
            {
                Message message = Database.getDB().loadMessage(messageId);
                if (message.getMessageDate() < new Date().getTime())
                {
                    if (!message.getSeenList().contains(userId) && !message.getOwnerId().equals(userId))
                    {
                        message.addToSeen(userId);
                        message.setSeen(true);
                        Database.getDB().saveMessage(message);
                    }
                    messages.add(messageId);
                }
            }
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while getting chatroom %s", chatId));
        }

        return messages;
    }

    public List<List<Long[]>> getMessagesList(long userId)
    {
        List<List<Long[]>> result = new LinkedList<>();
        HashMap<Long, Long> chatsMap = new HashMap<>();

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);

            for (Long id : profile.getChats())
            {
                Chat chat = Database.getDB().loadChat(id);
                chatsMap.put(- getLastMessageTime(chat), id);
            }
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while getting messages list for user %s", userId));
        }

        TreeMap<Long, Long> sortedChatsMap = new TreeMap<>(chatsMap);
        List<Long> sortedChatsList = new LinkedList<>();

        for (Map.Entry<Long, Long> e : sortedChatsMap.entrySet())
        {
            sortedChatsList.add(e.getValue());
        }

        for (int i = 0; i < sortedChatsList.size(); i = i+7)
        {
            List<Long[]> temp = new LinkedList<>();
            temp.add(new Long[]{sortedChatsList.get(i), getUnseenCount(sortedChatsList.get(i), userId)});

            for (int j = 1; j < 7; j++)
            {
                if (i + j < sortedChatsList.size())
                {
                    temp.add(new Long[]{sortedChatsList.get(i + j), getUnseenCount(sortedChatsList.get(i + j), userId)});
                }
                else
                {
                    temp.add(new Long[]{-1L, -1L});
                }
            }
            result.add(temp);
        }

        return result;
    }

    public long getLastMessageTime(Chat chat)
    {
        if (chat.getMessages().size() != 0)
        {
            long messageId = chat.getMessages().get(chat.getMessages().size() - 1);

            Message message = null;
            try
            {
                message = Database.getDB().loadMessage(messageId);
            }
            catch (SQLException ignored)
            {
                logger.error(String.format("database error while getting message with id %s", messageId));
            }

            if (message ==  null)
            {
                return 0;
            }
            return message.getMessageDate();
        }
        return 0;
    }

    public long getUnseenCount(Long chatId, Long userId)
    {
        Chat chat = null;
        try
        {
            chat = Database.getDB().loadChat(chatId);
        } catch (SQLException ignored) {}

        long cnt = 0;
        for (Long messageId : Objects.requireNonNull(chat).getMessages())
        {
            try
            {
                Message message = Database.getDB().loadMessage(messageId);
                if (!message.getSeenList().contains(userId) && !message.getOwnerId().equals(userId))
                {
                    if (message.getMessageDate() < new Date().getTime())
                    {
                        cnt++;
                    }
                }
            }
            catch (SQLException ignored)
            {
                logger.error("database error in counting unseen messages");
            }
        }
        return cnt;
    }

    public Chat getPv(Long userId1, Long userId2)
    {
        Chat pv = null;

        try
        {
            Profile profile1 = Database.getDB().loadProfile(userId1);
            Profile profile2 = Database.getDB().loadProfile(userId2);

            for (Long chatId : profile1.getChats())
            {
                if (profile2.getChats().contains(chatId))
                {
                    Chat chat = Database.getDB().loadChat(chatId);
                    if (!chat.isGroup())
                    {
                        pv = chat;
                        break;
                    }
                }
            }

        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while finding pv with users %s, %s", userId1, userId2));
        }
        return pv;
    }

    public NewChatResponse newChat(User loggedInUser, String username, String chatName)
    {
        if (username.equals(""))
        {
            try
            {
                Profile profile = Database.getDB().loadProfile(loggedInUser.getId());
                Chat chat = new Chat(loggedInUser, chatName);
                chat = Database.getDB().saveChat(chat);
                profile.addToChats(chat.getId());
                Database.getDB().saveProfile(profile);
                logger.info(String.format("new chat was created with id: %s", chat.getId()));
            }
            catch (SQLException ignored)
            {
                logger.error("database error while saving new created chat");
                return new NewChatResponse(null, new ChatCreationFailed("failed to load database"));
            }
        }
        else if (chatName.equals(""))
        {
            try
            {
                User otherUser = Database.getDB().loadUser(username);
                Profile loggedInUserProfile = Database.getDB().loadProfile(loggedInUser.getId());
                Profile otherUserProfile = Database.getDB().loadProfile(otherUser.getId());
                if (!otherUserProfile.getBlocked().contains(loggedInUserProfile.getId()) && !otherUser.isDeleted() && !otherUser.isDeactivated()
                        && (otherUserProfile.getFollowers().contains(loggedInUserProfile.getId()) || otherUserProfile.getFollowings().contains(loggedInUserProfile.getId())))
                {
                    Chat chat = new Chat(loggedInUser, otherUser);
                    chat = Database.getDB().saveChat(chat);
                    loggedInUserProfile.addToChats(chat.getId());
                    otherUserProfile.addToChats(chat.getId());
                    Database.getDB().saveProfile(loggedInUserProfile);
                    Database.getDB().saveProfile(otherUserProfile);
                    logger.info(String.format("new pv was created with id: %s", chat.getId()));
                }
            }
            catch (SQLException ignored)
            {
                logger.error("database error while saving new created chat");
                return new NewChatResponse(null, new ChatCreationFailed("failed to load database"));
            }
        }
        return new NewChatResponse(null, null);
    }

    public void addMember(Long chatId, String username)
    {
        try
        {
            Profile profile = Database.getDB().loadProfile(Database.getDB().loadUser(username).getId());
            Chat chat = Database.getDB().loadChat(chatId);
            chat.getUsers().add(profile.getId());
            profile.getChats().add(chatId);
            Database.getDB().saveProfile(profile);
            Database.getDB().saveChat(chat);
            logger.debug(String.format("user with username %s was added to chat %s", username, chatId));
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while adding user with username %s to chat %s", username, chatId));
        }
    }

    public void leaveGroup(User loggedInUser, Long chatId)
    {
        try
        {
            Profile profile = Database.getDB().loadProfile(loggedInUser.getId());
            Chat chat = Database.getDB().loadChat(chatId);
            chat.getUsers().remove(profile.getId());
            profile.getChats().remove(chatId);
            Database.getDB().saveProfile(profile);
            Database.getDB().saveChat(chat);
            logger.debug(String.format("user %s left group %s", profile.getId(), chat.getId()));
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while leaving group %s", chatId));
        }
    }
}
