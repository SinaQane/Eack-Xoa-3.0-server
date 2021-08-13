package view;

import controller.bot.PrivateBotController;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.BotException;

import java.util.Locale;

public class AddBotFrameFXML
{
    public Text errorText;
    public Button addButton;
    public TextField urlTextField;
    public TextField nameTextField;
    public TextField kindTextField;

    public void refresh()
    {
        urlTextField.clear();
        nameTextField.clear();
        kindTextField.clear();
    }

    public void setError(String text)
    {
        errorText.setVisible(true);
        errorText.setFill(Color.RED);
        errorText.setText(text);
    }

    public void setFinished()
    {
        errorText.setVisible(true);
        errorText.setFill(Color.GREEN);
        errorText.setText("bot was added successfully");
    }

    public void add()
    {
        String url = urlTextField.getText();
        String kind = kindTextField.getText();
        String name = nameTextField.getText();

        switch (kind)
        {
            case "1":
                PrivateBotController controller = new PrivateBotController();
                BotException err = controller.addBot(name.toLowerCase(Locale.ROOT) + "_bot", name, url);
                if (err != null)
                {
                    setError(err.getMessage());
                }
                else
                {
                    setFinished();
                }
                refresh();
                break;
            case "2":
            case "3":
                break;
            default:
                setError("please enter a valid kind number (1, 2 or 3)");
                refresh();
        }
    }
}
