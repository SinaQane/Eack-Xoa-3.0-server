package controller.bot;

import db.Database;
import model.Bot;
import model.Chat;
import model.Message;
import model.User;

import java.sql.SQLException;
import java.util.Date;

public class BotController
{
    public void handleCommand(Long chatId, String input)
    {
        String[] command = input.split(" ");
        String output = "";

        long botsUserId = -1L;
        Bot bot = null;
        try
        {
            botsUserId = getBotsUserId(chatId);
            bot = Database.getDB().loadUserBot(botsUserId);
        } catch (SQLException ignored) {}

        if (bot != null)
        {
            switch (command[0])
            {
                case "/action":
                    PrivateBotController controller = new PrivateBotController();
                    output = controller.botAction(bot, input);
                    break;
                case "/start":
                case "/join":
                case "/move":
                    break;
            }
        }

        if (!output.equals(""))
        {
            Message message = new Message();
            message.setText(output);
            message.setChatId(chatId);
            message.setOwnerId(botsUserId);
            message.setMessageDate(new Date().getTime());
            try
            {
                message = Database.getDB().saveMessage(message);
                Chat chat = Database.getDB().loadChat(chatId);
                chat.addToMessages(message.getId());
                Database.getDB().saveChat(chat);
            } catch (SQLException ignored) {}
        }
    }

    public long getBotsUserId(long chatId) throws SQLException
    {
        Chat chat = Database.getDB().loadChat(chatId);
        for (Long id : chat.getUsers())
        {
            User user = Database.getDB().loadUser(id);
            if (user.getUsername().endsWith("_bot"))
            {
                return user.getId();
            }
        }
        return -1L;
    }

    public boolean hasBot(long chatId)
    {
        long botsUserId = -1L;

        try
        {
            botsUserId = getBotsUserId(chatId);
        } catch (SQLException ignored) {}

        return botsUserId != -1L;
    }
}
