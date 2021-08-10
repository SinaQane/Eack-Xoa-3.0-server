package controller;

import db.Database;
import model.Profile;
import model.Tweet;
import util.Utilities;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TimelineController
{
    public List<List<Long[]>> getTimeline(long userId)
    {
        HashMap<Long[], Long> unsortedTimeline = new HashMap<>();
        List<Long[]> allTweets = new LinkedList<>();
        List<Long> users = new LinkedList<>();
        users.add(userId);

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);

            for (Long id : profile.getFollowings())
            {
                if (!profile.getMuted().contains(id))
                {
                    users.add(id);
                }
            }

            UserController controller = new UserController();

            for (Long user : users)
            {
                allTweets.addAll(controller.getHomePageTweets(Database.getDB().loadProfile(user)));
            }

            for (Long[] tweetInfo : allTweets)
            {
                Tweet tempTweet = Database.getDB().loadTweet(tweetInfo[0]);
                unsortedTimeline.put(tweetInfo, tempTweet.getTweetDate().getTime());
            }
        } catch (SQLException ignored) {}

        List<Long[]> timeline = new LinkedList<>();

        for (Map.Entry<Long[], Long> e : Utilities.sortByValue(unsortedTimeline).entrySet())
        {
            timeline.add(0, e.getKey());
        }

        return createList(timeline);
    }

    public List<List<Long[]>> getBookmarks(long userId)
    {
        List<Long[]> bookmarks = new LinkedList<>();

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);

            for (Long id : profile.getSavedTweets())
            {
                bookmarks.add(new Long[]{id, -1L});
            }
        } catch (SQLException ignored) {}

        return createList(bookmarks);
    }

    public List<List<Long[]>> createList(List<Long[]> items)
    {
        List<List<Long[]>> result = new LinkedList<>();

        for (int i = 0; i < items.size(); i = i+3)
        {
            List<Long[]> temp = new LinkedList<>();

            Long[] firstTweet = new Long[2];
            Long[] secondTweet = new Long[2];
            Long[] thirdTweet = new Long[2];

            firstTweet[0] = items.get(i)[0];
            firstTweet[1] = items.get(i)[1];

            if (i + 1 < items.size())
            {
                secondTweet[0] = items.get(i + 1)[0];
                secondTweet[1] = items.get(i + 1)[1];
            }
            else
            {
                secondTweet[0] = secondTweet[1] = -1L;
            }

            if (i + 2 < items.size())
            {
                thirdTweet[0] = items.get(i + 2)[0];
                thirdTweet[1] = items.get(i + 2)[1];
            }
            else
            {
                thirdTweet[0] = thirdTweet[1] = -1L;
            }

            temp.add(firstTweet);
            temp.add(secondTweet);
            temp.add(thirdTweet);

            result.add(temp);
        }

        return result;
    }
}