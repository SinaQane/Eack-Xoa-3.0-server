package controller.bot;

import bot.privatebot.PrivateBot;
import db.Database;
import model.Bot;
import model.Chat;
import model.Message;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.Date;

public class PrivateBotController
{
    public void botAction(Bot bot, Long chatId, String input)
    {
        try
        {
            URLClassLoader loader = new URLClassLoader(new URL[]{new URL(bot.getJarURL())});
            Class<?> botObject = loader.loadClass("PrivateBot");
            PrivateBot privateBot = (PrivateBot) botObject.getConstructors()[0].newInstance();
            String output = privateBot.action(input.split(" ")[1]);

            if (!output.equals(""))
            {
                Message message = new Message();
                message.setText(output);
                message.setChatId(chatId);
                message.setOwnerId(bot.getUserId());
                message.setMessageDate(new Date().getTime());
                try
                {
                    message = Database.getDB().saveMessage(message);
                    Chat chat = Database.getDB().loadChat(chatId);
                    chat.addToMessages(message.getId());
                    Database.getDB().saveChat(chat);
                } catch (SQLException ignored) {}
            }
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
    }
}
