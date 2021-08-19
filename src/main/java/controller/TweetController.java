package controller;

import db.Database;
import event.events.general.SendTweetForm;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TweetController
{
    private static final Logger logger = LogManager.getLogger(TweetController.class);

    public void sendTweet(SendTweetForm form)
    {
        long tweetOwnerId = form.getUserId();
        long upperTweetId = form.getUpperTweet();
        String tweetPicture = form.getPicture();
        String tweetText = form.getTweet();

        Tweet tweet = new Tweet();
        tweet.setOwner(tweetOwnerId);
        tweet.setUpperTweet(upperTweetId);
        tweet.setPicture(tweetPicture);
        tweet.setText(tweetText);
        tweet.setTweetDate(new Date());

        try
        {
            tweet = Database.getDB().saveTweet(tweet);
            Profile profile = Database.getDB().loadProfile(tweetOwnerId);
            profile.addToUserTweets(tweet.getId());
            Database.getDB().saveProfile(profile);
            if (upperTweetId != -1)
            {
                Tweet upperTweet = Database.getDB().loadTweet(upperTweetId);
                upperTweet.addComment(tweet.getId());
                Database.getDB().saveTweet(upperTweet);
                logger.info(String.format("new tweet was just tweeted with id: %s", tweet.getId()));
            }
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while saving a new tweet", e));
        }
    }

    public void upvote(long viewer, long tweetId)
    {
        try
        {
            Profile profile = Database.getDB().loadProfile(viewer);
            Tweet tweet = Database.getDB().loadTweet(tweetId);

            if (profile.getUpvotedTweets().contains(tweetId))
            {
                profile.removeFromUpvotedTweets(tweetId);
                tweet.removeUpvote(viewer);
            }
            else
            {
                profile.removeFromDownvotedTweets(tweetId);
                profile.addToUpvotedTweets(tweetId);
                tweet.removeDownvote(viewer);
                tweet.addUpvote(viewer);
            }

            Database.getDB().saveProfile(profile);
            Database.getDB().saveTweet(tweet);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while upvoting a tweet", e));
        }
    }

    public void downvote(long viewer, long tweetId)
    {
        try
        {
            Profile profile = Database.getDB().loadProfile(viewer);
            Tweet tweet = Database.getDB().loadTweet(tweetId);

            if (profile.getDownvotedTweets().contains(tweetId))
            {
                profile.removeFromDownvotedTweets(tweetId);
                tweet.removeDownvote(viewer);
            }
            else
            {
                profile.removeFromUpvotedTweets(tweetId);
                profile.addToDownvotedTweets(tweetId);
                tweet.removeUpvote(viewer);
                tweet.addDownvote(viewer);
            }

            Database.getDB().saveProfile(profile);
            Database.getDB().saveTweet(tweet);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while downvoting a tweet", e));
        }
    }

    public void retweet(long viewer, long tweetId)
    {
        try
        {
            Profile profile = Database.getDB().loadProfile(viewer);
            Tweet tweet = Database.getDB().loadTweet(tweetId);

            if (profile.getRetweetedTweets().contains(tweetId))
            {
                profile.removeFromRetweetedTweets(tweetId);
                tweet.removeRetweet(viewer);
            }
            else
            {
                profile.addToRetweetedTweets(tweetId);
                tweet.addRetweet(viewer);
            }

            Database.getDB().saveProfile(profile);
            Database.getDB().saveTweet(tweet);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while retweeting a tweet", e));
        }
    }

    public void save(long viewer, long tweetId)
    {
        try
        {
            Profile profile = Database.getDB().loadProfile(viewer);

            if (profile.getSavedTweets().contains(tweetId))
            {
                profile.removeFromSavedTweets(tweetId);
            }
            else
            {
                profile.addToSavedTweets(tweetId);
            }

            Database.getDB().saveProfile(profile);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while bookmarking a tweet", e));
        }
    }

    public void report(long viewer, long tweetId)
    {
        try
        {
            Profile profile = Database.getDB().loadProfile(viewer);
            Tweet tweet = Database.getDB().loadTweet(tweetId);

            if (!profile.getReportedTweets().contains(tweetId))
            {
                profile.addToReportedTweets(tweetId);
                tweet.addReport();

                if (tweet.getReports() > 9)
                {
                    tweet.deleteTweet();
                }

                Database.getDB().saveProfile(profile);
                Database.getDB().saveTweet(tweet);
            }
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while reporting a tweet", e));
        }
    }

    public boolean isValid(User viewer, Tweet tweet) throws SQLException
    {
        User ownerUser = Database.getDB().loadUser(tweet.getOwner());
        Profile ownerProfile = Database.getDB().loadProfile(tweet.getOwner());
        Profile viewerProfile = Database.getDB().loadProfile(viewer.getId());

        if (ownerUser.getId().equals(viewer.getId())) return true;
        if (viewerProfile.getMuted().contains(tweet.getOwner())) return false;
        if (ownerProfile.getBlocked().contains(viewer.getId())) return false;
        if (ownerUser.isDeactivated()) return false;
        if (ownerUser.isDeleted()) return false;
        if (!tweet.isVisible()) return false;

        return (!ownerProfile.isPrivate() || ownerProfile.getFollowers().contains(viewer.getId()));
    }

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
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while getting tweet %s comments", e, tweetId));
            return result;
        }

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
}
