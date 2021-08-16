package controller.bot;

import bot.gamebot.GameState;
import bot.groupbot.GroupBotForm;

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
}
