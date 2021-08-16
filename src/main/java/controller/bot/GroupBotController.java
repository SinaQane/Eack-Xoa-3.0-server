package controller.bot;

import bot.groupbot.GroupBot;
import bot.groupbot.GroupBotForm;
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
import java.util.HashMap;
import java.util.Map;

public class GroupBotController
{
    private Long id = 0L;
    private final Map<Long, GroupBotForm> forms = new HashMap<>();

    static GroupBotController controller;

    private GroupBotController(){}

    public static GroupBotController getController()
    {
        if (controller == null)
        {
            controller = new GroupBotController();
        }
        return controller;
    }

    public void botMake(Bot bot, Long chatId, String input)
    {
        id++;

        String[] inputParts = input.split(" ");
        String[] options = new String[inputParts.length - 2];
        System.arraycopy(inputParts, 2, options, 0, inputParts.length - 2);

        try
        {
            URLClassLoader loader = new URLClassLoader(new URL[]{new URL(bot.getJarURL())});
            Class<?> botObject = loader.loadClass("GroupBot");
            GroupBot groupBot = (GroupBot) botObject.getConstructors()[0].newInstance();
            GroupBotForm form = groupBot.make(id, inputParts[1], options);
            forms.put(id, form);

            String output = form.getPrintedForm();
            returnOutput(bot, chatId, output);
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
    }

    public void botVote(Bot bot, Long chatId, String input)
    {
        try
        {
            URLClassLoader loader = new URLClassLoader(new URL[]{new URL(bot.getJarURL())});
            Class<?> botObject = loader.loadClass("GroupBot");
            GroupBot groupBot = (GroupBot) botObject.getConstructors()[0].newInstance();
            GroupBotForm form = forms.get(Long.parseLong(input.split(" ")[1]));
            groupBot.vote(form, input.split(" ")[2]);
            forms.put(id, form);

            String output = form.getPrintedForm();
            returnOutput(bot, chatId, output);
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
    }

    public void returnOutput(Bot bot, Long chatId, String output)
    {
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
    }
}
