package response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import event.Event;
import json.Deserializer;
import json.Serializer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class SocketResponseSender implements ResponseSender
{
    private final PrintStream printStream;
    private final Scanner scanner;
    private final Socket socket;
    private final Gson gson;

    public SocketResponseSender(Socket socket) throws IOException
    {
        this.printStream = new PrintStream(socket.getOutputStream());
        this.scanner = new Scanner(socket.getInputStream());
        this.socket = socket;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Event.class, new Deserializer<>())
                .registerTypeAdapter(Response.class, new Serializer<>())
                .create();
    }

    @Override
    public Event getEvent()
    {
        String line = "null";
        if (scanner.hasNext()) line = scanner.nextLine();
        return gson.fromJson(line, Event.class);
    }

    @Override
    public void sendResponse(Response response)
    {
        printStream.println(gson.toJson(response, Response.class));
    }

    @Override
    public void close()
    {
        try
        {
            printStream.close();
            scanner.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}