package view;

import config.Config;
import constants.Constants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Objects;

public class AddBotFrame
{
    private final Scene scene;

    public AddBotFrame()
    {
        String path = new Config(Constants.CONFIG).getProperty(String.class, "addBotFrame");
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(path)));
        Parent root = null;
        try
        {
            root = loader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        assert root != null;
        scene = new Scene(root);
    }

    public Scene getScene()
    {
        return scene;
    }
}
