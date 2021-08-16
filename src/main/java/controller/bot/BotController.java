package controller.bot;

import config.Config;
import constants.Constants;
import db.Database;
import model.*;
import util.ImageUtil;
import util.Validations;

import java.sql.SQLException;

public class BotController
{
    public BotException addBot(String username, String name, String url, int kind)
    {
        User user = new User();
        user.setUsername(username);
        user.setName(name);

        try
        {
            if (Validations.getValidations().usernameIsUnavailable(username))
            {
                return new BotException("name is not available");
            }
            user = Database.getDB().saveUser(user);
            Profile profile = new Profile();
            profile.setPicture(ImageUtil.imageToString(new Config(Constants.CONFIG).getProperty("botPicture")));
            Database.getDB().saveProfile(profile);
            Database.getDB().saveBot(new Bot(kind, user.getId(), url));
        }
        catch (SQLException ignored)
        {
            return new BotException("database error while creating bot");
        }

        return null;
    }

    public void handleCommand(Long userId, Long chatId, String input)
    {
        String[] command = input.split(" ");

        Bot bot = null;
        try
        {
            bot = Database.getDB().loadUserBot(getBotsUserId(chatId));
        } catch (SQLException ignored) {}

        if (bot != null)
        {
            switch (command[0])
            {
                case "/action":
                    PrivateBotController controller = new PrivateBotController();
                    controller.botAction(bot, chatId, input);
                    break;
                case "/start":
                    GameBotController.getController().botStart(bot, userId);
                    break;
                case "/join":
                    GameBotController.getController().botJoin(bot, userId, input);
                    break;
                case "/move":
                    GameBotController.getController().botMove(bot, userId, input);
                    break;
                case "/make":
                case "/vote":
                    break;
            }
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
