package controller;

import db.Database;
import model.Notification;
import model.Profile;
import model.User;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ListsController
{
    public List<List<Long>> getFollowers(long userId)
    {
        List<Long> followers = new LinkedList<>();

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);

            for (Long follower : profile.getFollowers())
            {
                User followerUser = Database.getDB().loadUser(follower);
                if (!followerUser.isDeleted() && !followerUser.isDeactivated())
                {
                    followers.add(follower);
                }
            }
        }
        catch (SQLException ignored) {}

        return createList(followers);
    }

    public List<List<Long>> getFollowings(long userId)
    {
        List<Long> followings = new LinkedList<>();

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);

            for (Long following : profile.getFollowings())
            {
                User followingUser = Database.getDB().loadUser(following);
                if (!followingUser.isDeleted() && !followingUser.isDeactivated())
                {
                    followings.add(following);
                }
            }
        }
        catch (SQLException ignored) {}

        return createList(followings);
    }

    public List<List<Long>> getBlackList(long userId)
    {
        List<Long> blackList = new LinkedList<>();

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);

            for (Long blocked : profile.getBlocked())
            {
                User blockedUser = Database.getDB().loadUser(blocked);
                if (!blockedUser.isDeleted() && !blockedUser.isDeactivated())
                {
                    blackList.add(blocked);
                }
            }
        } catch (SQLException ignored) {}

        return createList(blackList);
    }

    public List<List<Long>> getNotifications(long userId)
    {
        Profile profile;
        try
        {
            profile = Database.getDB().loadProfile(userId);
        }
        catch (SQLException ignored) {return null;}

        List<Long> notifications = profile.getNotifications();

        try
        {
            for (Long id : profile.getRequests())
            {
                User requestedUser = Database.getDB().loadUser(id);
                Notification request = new Notification(userId, id,  requestedUser.getId() + " wants to follow you.");
                request = Database.getDB().saveNotification(request);
                notifications.add(request.getId());
            }
        }
        catch (SQLException ignored) {return null;}

        return createList(notifications);
    }

    public List<List<Long>> createList(List<Long> items)
    {
        List<List<Long>> result = new LinkedList<>();

        if (items.size() == 0)
        {
            List<Long> temp = new LinkedList<>();
            for (int i = 0; i < 5; i++)
            {
                temp.add(-1L);
            }
            result.add(temp);
        }

        for (int i = 0; i < items.size(); i = i + 5)
        {
            List<Long> temp = new LinkedList<>();
            temp.add(items.get(i));

            for (int j = 1; j < 5; j++)
            {
                if (i + j < items.size())
                {
                    temp.add(items.get(i + j));
                }
                else
                {
                    temp.add(-1L);
                }
            }
            result.add(temp);
        }

        return result;
    }
}
