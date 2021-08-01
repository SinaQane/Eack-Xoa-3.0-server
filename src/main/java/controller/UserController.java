package controller;

import db.Database;
import model.Group;
import model.Profile;
import model.Tweet;
import model.User;
import util.Utilities;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserController
{
    public List<List<Long[]>> getTweets(long viewerId, long userId)
    {
        List<List<Long[]>> result = new LinkedList<>();
        List<Long[]> homePageTweets = new LinkedList<>();
        List<Long[]> tempTweets;

        try
        {
            User viewer = Database.getDB().loadUser(viewerId);

            Profile profile = Database.getDB().loadProfile(userId);
            tempTweets = getHomePageTweets(profile);

            TweetController controller = new TweetController();

            for (Long[] tweetId : tempTweets)
            {
                Tweet tweet = Database.getDB().loadTweet(tweetId[0]);

                if (controller.isValid(viewer, tweet))
                {
                    homePageTweets.add(tweetId);
                }
            }
        } catch (SQLException ignored) {}

        for (int i = 0; i < homePageTweets.size(); i = i + 2)
        {
            List<Long[]> temp = new LinkedList<>();

            Long[] firstTweet;
            Long[] secondTweet;

            firstTweet = homePageTweets.get(i);

            if (i + 1 != homePageTweets.size())
            {
                secondTweet = homePageTweets.get(i + 1);
            }
            else
            {
                secondTweet = new Long[]{-1L, -1L};
            }

            temp.add(firstTweet);
            temp.add(secondTweet);

            result.add(temp);
        }

        return result;
    }

    /* A HashMap that links every tweet to 2 variables:
        1. A bit that shows that is this tweet a retweet ("userId") or the user's tweet ("-1L").
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
            homePageTweets.put(new Long[]{userTweet, -1L}, tweet.getTweetDate().getTime());
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
            homePageTweets.put(new Long[]{retweetedTweet, profile.getId()}, tweet.getTweetDate().getTime());
        }

        List<Long[]> result = new LinkedList<>();

        for (Map.Entry<Long[], Long> e : Utilities.sortByValue(homePageTweets).entrySet())
        {
            result.add(0, e.getKey());
        }

        return result;
    }
}