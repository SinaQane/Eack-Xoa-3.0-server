package controller.bot;

import config.Config;
import constants.Constants;
import db.Database;
import model.Bot;
import model.BotException;
import model.Profile;
import model.User;
import util.ImageUtil;
import util.Validations;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;

public class PrivateBotController
{
    public BotException addBot(String username, String name, String url)
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
            Database.getDB().saveBot(new Bot(1, user.getId(), url));
        }
        catch (SQLException ignored)
        {
            return new BotException("database error while creating bot");
        }

        return null;
    }

    public String botAction(Bot bot, String input)
    {
        try
        {
            URLClassLoader loader = new URLClassLoader(new URL[]{new URL(bot.getJarURL())});
            Class<?> botObject = loader.loadClass("PrivateBot");
            PrivateBot privateBot = (PrivateBot) botObject.getConstructors()[0].newInstance();
            return privateBot.action(input.split(" ")[1]);
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
        return "";
    }
}
