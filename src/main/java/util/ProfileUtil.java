package util;

import db.Database;
import model.Group;
import model.Profile;
import model.Tweet;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProfileUtil
{
    // Finds a group by its name from a profile
    public Group getGroup(Profile profile, String groupName)
    {
        for (Long groupId : profile.getGroups())
        {
            Group group = null;
            try
            {
                group = Database.getDB().loadGroup(groupId);
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
            assert group != null;
            if (group.getTitle().equals(groupName))
            {
                return group;
            }
        }
        return null;
    }

    // Adds a group to profile's group list if it already doesn't exist
    public void addToGroups(Profile profile, Group group)
    {
        int index = -1;
        for (int i = 0; i < profile.getGroups().size(); i++)
        {
            Group tempGroup = null;
            try
            {
                tempGroup = Database.getDB().loadGroup(profile.getGroups().get(i));
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
            assert tempGroup != null;
            if (tempGroup.getTitle().equals(group.getTitle()))
            {
                index = i;
            }
        }
        if(index != -1)
        {
            profile.removeGroup(index);
        }
        profile.addToGroups(group);
        try
        {
            Database.getDB().saveProfile(profile);
        }
        catch (SQLException throwable)
        {
            throwable.printStackTrace();
        }
    }

    /* A HashMap that links every tweet to 2 variables:
        1. A bit that shows that is this tweet a retweet ("1") or the user's tweet ("0").
        2. A long that shows the time that this tweet was tweeted, in milliseconds. */
    public List<Long[]> getHomePageTweets(Profile profile)
    {
        HashMap<Long[], Long> homePageTweets = new HashMap<>();

        for (Long userTweet : profile.getUserTweets())
        {
            Tweet tweet = null;
            try
            {
                tweet = Database.getDB().loadTweet(userTweet);
            } catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
            assert tweet != null;
            homePageTweets.put(new Long[]{userTweet, 0L}, tweet.getTweetDate().getTime());
        }

        for (Long retweetedTweet : profile.getRetweetedTweets())
        {
            Tweet tweet = null;
            try
            {
                tweet = Database.getDB().loadTweet(retweetedTweet);
            } catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
            assert tweet != null;
            homePageTweets.put(new Long[]{retweetedTweet, 1L}, tweet.getTweetDate().getTime());
        }

        List<Long[]> result = new LinkedList<>();

        for (Map.Entry<Long[], Long> e : Utilities.sortByValue(homePageTweets).entrySet())
        {
            result.add(0, e.getKey());
        }
        return result;
    }
}
