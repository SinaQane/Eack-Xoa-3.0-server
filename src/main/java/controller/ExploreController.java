package controller;

import db.Database;
import model.Tweet;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ExploreController
{
    private static final Logger logger = LogManager.getLogger(ExploreController.class);

    public List<Long> getRandomTweets(long userId)
    {
        List<Tweet> tweets = new LinkedList<>();
        List<Long> result = new LinkedList<>();

        try
        {
            Long allTweetsCount = Database.getDB().maxTableId("tweets");
            TweetController controller = new TweetController();
            User viewer = Database.getDB().loadUser(userId);

            if (allTweetsCount < 20L)
            {
                for (int i = 1; i < allTweetsCount; i++)
                {
                    Tweet tweet = Database.getDB().loadTweet(i);
                    if (controller.isValid(viewer, tweet))
                    {
                        tweets.add(tweet);
                    }
                }
            }
            else
            {
                int i = 1;

                while (tweets.size() < 2 && allTweetsCount >= i * 20L)
                {
                    for (int j = (i - 1) * 20; j < Math.min(i * 20L, allTweetsCount); j++)
                    {
                        if (j >= 1)
                        {
                            Tweet tweet = Database.getDB().loadTweet(j);
                            if (controller.isValid(viewer, tweet))
                            {
                                tweets.add(tweet);
                            }
                        }
                    }

                    i++;
                }
            }
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while generating random tweets", e));
        }

        if (tweets.size() == 0)
        {
            result.add(-1L);
            result.add(-1L);
            return result;
        }

        if (tweets.size() == 1)
        {
            result.add(tweets.get(0).getId());
            result.add(-1L);
            return result;
        }

        Random random = new Random();
        int num1 = random.nextInt(tweets.size());
        int num2 = random.nextInt(tweets.size());
        while (num1 == num2)
        {
            num2 = random.nextInt(tweets.size());
        }

        result.add(tweets.get(num1).getId());
        result.add(tweets.get(num2).getId());

        return result;
    }

    public List<List<Long>> search(String searched)
    {
        List<List<Long>> result = new LinkedList<>();
        List<User> allUsers = new LinkedList<>();
        List<User> users = new LinkedList<>();

        try
        {
            Long allUsersCount = Database.getDB().maxTableId("users");

            for (int i = 1; i < allUsersCount + 1; i++)
            {
                User user = Database.getDB().loadUser(i);
                allUsers.add(user);
            }
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while searching for %s", e, searched));
        }

        for (User user : allUsers)
        {
            if (user.getUsername().contains(searched) || user.getName().contains(searched))
            {
                if (!user.isDeactivated() && !user.isDeleted())
                {
                    users.add(user);
                }
            }
        }

        if (users.size() == 0)
        {
            List<Long> temp = new LinkedList<>();
            for (int i = 0; i < 4; i++)
            {
                temp.add(-1L);
            }
            result.add(temp);
        }

        for (int i = 0; i < users.size(); i = i+4)
        {
            List<Long> temp = new LinkedList<>();
            temp.add(users.get(i).getId());

            for (int j = 1; j < 4; j++)
            {
                if (i + j < users.size())
                {
                    temp.add(users.get(i + j).getId());
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
