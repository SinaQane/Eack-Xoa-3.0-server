package controller.bot;

import bot.gamebot.GameBot;
import bot.gamebot.GameState;
import controller.ChatController;
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

public class GameBotController
{
    private Long id = 0L;
    private final Map<Long, GameState> games = new HashMap<>();

    static GameBotController controller;

    private GameBotController(){}

    public static GameBotController getController()
    {
        if (controller == null)
        {
            controller = new GameBotController();
        }
        return controller;
    }

    public void botStart(Bot bot, Long userId)
    {
        id++;
        try
        {
            URLClassLoader loader = new URLClassLoader(new URL[]{new URL(bot.getJarURL())});
            Class<?> botObject = loader.loadClass("GameBot");
            GameBot gameBot = (GameBot) botObject.getConstructors()[0].newInstance();
            GameState gameState = gameBot.start(id, userId);
            games.put(id, gameState);

            String output = gameState.getMessage();
            returnOutput(bot, gameState, output);
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
    }

    public void botJoin(Bot bot, Long userId, String input)
    {
        Long gameId = Long.parseLong(input.split(" ")[1]);
        GameState gameState = games.get(gameId);

        try
        {
            URLClassLoader loader = new URLClassLoader(new URL[]{new URL(bot.getJarURL())});
            Class<?> botObject = loader.loadClass("GameBot");
            GameBot gameBot = (GameBot) botObject.getConstructors()[0].newInstance();
            gameState = gameBot.join(gameState, userId);
            games.put(gameId, gameState);

            String output = gameState.getMessage();
            returnOutput(bot, gameState, output);
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
    }

    public void botMove(Bot bot, Long userId, String input)
    {
        Long gameId = Long.parseLong(input.split(" ")[1]);
        GameState gameState = games.get(gameId);

        try
        {
            URLClassLoader loader = new URLClassLoader(new URL[]{new URL(bot.getJarURL())});
            Class<?> botObject = loader.loadClass("GameBot");
            GameBot gameBot = (GameBot) botObject.getConstructors()[0].newInstance();
            gameState = gameBot.move(gameState, userId, input);
            games.put(gameId, gameState);

            String output = gameState.getMessage();
            returnOutput(bot, gameState, output + " " + gameState.getPrintedBoard());
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
    }

    public void returnOutput(Bot bot, GameState gameState, String output)
    {
        ChatController chatController = new ChatController();
        Chat chat1 = chatController.getPv(bot.getUserId(), gameState.getUser1());

        Long[] chatIds;
        if (gameState.getUser2().equals(-1L))
        {
            chatIds = new Long[]{chat1.getId()};
        }
        else
        {
            Chat chat2 = chatController.getPv(bot.getUserId(), gameState.getUser2());
            chatIds = new Long[]{chat1.getId(), chat2.getId()};
        }

        for (Long chatId : chatIds)
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
}
