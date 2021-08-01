package controller;

import db.Database;
import model.Notification;
import model.Profile;
import model.User;

import java.sql.SQLException;

public class RequestController
{
    public void accept(long notificationId)
    {
        try
        {
            Notification notification = Database.getDB().loadNotification(notificationId);

            User pendingUser = Database.getDB().loadUser(notification.getOwner());
            User requestedUser = Database.getDB().loadUser(notification.getRequestFrom());
            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.addToFollowers(requestedUser);
            pendingProfile.removeFromRequests(requestedUser);
            requestedProfile.removeFromPending(pendingUser);
            requestedProfile.addToFollowers(pendingUser);

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        } catch (SQLException ignored) {}
    }

    public void goodReject(long notificationId)
    {
        try
        {
            Notification notification = Database.getDB().loadNotification(notificationId);

            User pendingUser = Database.getDB().loadUser(notification.getOwner());
            User requestedUser = Database.getDB().loadUser(notification.getRequestFrom());
            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.removeFromRequests(requestedUser);
            requestedProfile.removeFromPending(pendingUser);

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        } catch (SQLException ignored) {}
    }

    public void badReject(long notificationId)
    {
        try
        {
            Notification notification = Database.getDB().loadNotification(notificationId);

            User pendingUser = Database.getDB().loadUser(notification.getOwner());
            User requestedUser = Database.getDB().loadUser(notification.getRequestFrom());
            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.removeFromRequests(requestedUser);
            requestedProfile.removeFromPending(pendingUser);

            Notification roast = new Notification(requestedUser.getId(), pendingUser.getUsername() + " rejected your follow request.");
            requestedProfile.addToNotifications(roast);

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        } catch (SQLException ignored) {}
    }
}
