package controller;

import db.Database;
import model.Chat;
import model.Message;
import model.User;

import java.sql.SQLException;

public class ChatController
{
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

    public int getUnseenCount(Chat chat, User user)
    {
        int cnt = 0;
        for (Long messageId : chat.getMessages())
        {
            Message message = null;
            try
            {
                message = Database.getDB().loadMessage(messageId);
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
            assert message != null;
            if (!message.getSeenList().contains(user.getId()))
            {
                cnt++;
            }
        }
        return cnt;
    }
}
