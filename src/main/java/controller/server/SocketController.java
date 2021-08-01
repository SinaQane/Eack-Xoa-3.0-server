package controller.server;

import config.Config;
import constants.Constants;
import response.SocketResponseSender;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketController extends Thread
{
    private final Config config;

    public SocketController(Config config)
    {
        this.config = config;
    }

    @Override
    public void run()
    {
        ServerSocket serverSocket = null;
        try
        {
            int port = config.getOptionalProperty(Integer.class, "port").orElse(Constants.DEFAULT_PORT);
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        while (true)
        {
            ClientHandler clientHandler;
            try
            {
                assert serverSocket != null;
                Socket socket = serverSocket.accept();
                clientHandler = new ClientHandler(new SocketResponseSender(socket));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                break;
            }
            clientHandler.start();
        }
    }
}