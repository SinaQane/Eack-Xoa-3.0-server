package controller;

import db.Database;
import model.Chat;
import model.Message;
import model.Profile;

import java.sql.SQLException;
import java.util.*;

public class ChatController
{
    public List<Long> getChatroom(Long chatId)
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
                    message.addToSeen(messageId);
                }
            }
        } catch (SQLException ignored) {}

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
        } catch (SQLException ignored) {}

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
                    temp.add(new Long[]{sortedChatsList.get(i + j), getUnseenCount(sortedChatsList.get(i + j), userId)}
                    );
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
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
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
                if (!message.getSeenList().contains(userId))
                {
                    cnt++;
                }
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
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

        } catch (SQLException ignored) {}

        return pv;
    }
}
