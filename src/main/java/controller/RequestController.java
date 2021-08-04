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

            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.addToFollowers(notification.getRequestFrom());
            pendingProfile.removeFromRequests(notification.getRequestFrom());
            requestedProfile.removeFromPending(notification.getOwner());
            requestedProfile.addToFollowers(notification.getOwner());

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        } catch (SQLException ignored) {}
    }

    public void goodReject(long notificationId)
    {
        try
        {
            Notification notification = Database.getDB().loadNotification(notificationId);

            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.removeFromRequests(notification.getRequestFrom());
            requestedProfile.removeFromPending(notification.getOwner());

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

            pendingProfile.removeFromRequests(requestedUser.getId());
            requestedProfile.removeFromPending(pendingUser.getId());

            Notification roast = new Notification(requestedUser.getId(), pendingUser.getUsername() + " rejected your follow request.");
            requestedProfile.addToNotifications(roast.getId());

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        } catch (SQLException ignored) {}
    }
}
