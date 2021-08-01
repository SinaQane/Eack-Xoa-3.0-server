package controller;

import db.Database;
import model.Profile;
import model.Tweet;
import model.User;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class TweetController
{
    public List<List<Long>> getComments(long viewerId, long tweetId)
    {
        List<List<Long>> result = new LinkedList<>();
        List<Long> comments = new LinkedList<>();

        try
        {
            User viewer = Database.getDB().loadUser(viewerId);
            Tweet tweet = Database.getDB().loadTweet(tweetId);

            List<Long> tempComments = tweet.getComments();

            for (Long commentId : tempComments)
            {
                Tweet comment = Database.getDB().loadTweet(commentId);
                if (isValid(viewer, comment))
                {
                    comments.add(commentId);
                }
            }
        } catch (SQLException ignored) {return null;}

        if (comments.size() == 0)
        {
            List<Long> temp = new LinkedList<>();
            temp.add(-1L);
            temp.add(-1L);
            result.add(temp);
        }

        for (int i = 0; i < comments.size(); i = i+2)
        {
            List<Long> temp = new LinkedList<>();
            temp.add(comments.get(i));

            if (i + 1 < comments.size())
            {
                temp.add(comments.get(i + 1));
            }
            else
            {
                temp.add(-1L);
            }
            result.add(temp);
        }

        return result;
    }

    public boolean isValid(User viewer, Tweet tweet) throws SQLException
    {
        User ownerUser = Database.getDB().loadUser(tweet.getOwner());
        Profile ownerProfile = Database.getDB().loadProfile(tweet.getOwner());

        if (ownerUser.isDeleted())
        {
            return false;
        }
        if (ownerUser.isDeactivated())
        {
            return false;
        }
        if (!tweet.isVisible())
        {
            return false;
        }
        if (ownerProfile.getBlocked().contains(viewer.getId()))
        {
            return false;
        }

        return (!ownerProfile.isPrivate() || ownerProfile.getFollowers().contains(viewer.getId()));
    }
}
