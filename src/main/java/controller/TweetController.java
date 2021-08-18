package controller;

import db.Database;
import model.*;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class TweetController
{
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
        } catch (SQLException ignored) {}
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
        } catch (SQLException ignored) {}
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
        } catch (SQLException ignored) {}
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
        } catch (SQLException ignored) {}
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
        } catch (SQLException ignored) {}
    }

    public void forward(long loggedInUserId, long tweetId, String usernames, String groupNames) throws SQLException
    {
        User loggedInUser = Database.getDB().loadUser(loggedInUserId);

        GroupController groupController = new GroupController();
        ChatController chatController = new ChatController();
        List<Long> usersId = new LinkedList<>();

        String[] usernamesArray = usernames.split(" ");
        String[] groupNamesArray = groupNames.split(" ");

        for (String username : usernamesArray)
        {
            if (!username.equals(""))
            {
                try
                {
                    User user = Database.getDB().loadUser(username);
                    if (!usersId.contains(user.getId()) && !user.getId().equals(loggedInUser.getId()))
                    {
                        usersId.add(user.getId());
                    }
                } catch (SQLException ignored) {}
            }
        }

        for (String groupName : groupNamesArray)
        {
            if (!groupName.equals(""))
            {
                Group group = groupController.getGroupByName(loggedInUser.getId(), groupName);
                for (Long userId : group.getMembers())
                {
                    if (!usersId.contains(userId) && !userId.equals(loggedInUser.getId()))
                    {
                        usersId.add(userId);
                    }
                }
            }
        }

        Tweet tweet = null;
        try
        {
            tweet = Database.getDB().loadTweet(tweetId);
        }
        catch (SQLException ignored) {}

        for (Long userId : usersId)
        {
            Chat pv = chatController.getPv(loggedInUser.getId(), userId);
            if (pv != null)
            {
                Message message = new Message(pv, loggedInUser, Objects.requireNonNull(tweet));
                message.setSent(true);
                try
                {
                    message = Database.getDB().saveMessage(message);
                    pv.addToMessages(message.getId());
                    Database.getDB().saveChat(pv);
                } catch (SQLException ignored) {}
            }
        }
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
        Profile viewerProfile = Database.getDB().loadProfile(viewer.getId());

        if (ownerUser.getId().equals(viewer.getId()))
        {
            return true;
        }

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
        if (viewerProfile.getMuted().contains(tweet.getOwner()))
        {
            return false;
        }

        return (!ownerProfile.isPrivate() || ownerProfile.getFollowers().contains(viewer.getId()));
    }
}
