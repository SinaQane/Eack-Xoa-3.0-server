package response;

import event.Event;

public interface ResponseSender
{
    Event getEvent();

    void sendResponse(Response response);

    void close();
}