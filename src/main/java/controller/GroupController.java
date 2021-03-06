package controller;

import db.Database;
import event.events.groups.ManageGroupForm;
import model.Group;
import model.Profile;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class GroupController
{
    private static final Logger logger = LogManager.getLogger(GroupController.class);

    public List<List<Long>> getGroups(long userId)
    {
        List<Long> groups = new LinkedList<>();

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);
            groups = profile.getGroups();
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading profile %s", e, userId));
        }

        List<List<Long>> result = new LinkedList<>();

        for (int i = 0; i < groups.size(); i = i + 5)
        {
            List<Long> temp = new LinkedList<>();
            temp.add(groups.get(i));

            for (int j = 1; j < 5; j++)
            {
                if (i + j < groups.size())
                {
                    temp.add(groups.get(i + j));
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

    public void manageGroup(ManageGroupForm form, long userId)
    {
        Long id = form.getId();
        String title = form.getTitle();
        List<String> toAdd = form.getToAdd();
        List<String> toRemove = form.getToRemove();

        List<Long> toAddId = new LinkedList<>();
        List<Long> toRemoveId = new LinkedList<>();

        try
        {
            for (String username : toAdd)
            {
                if (!username.equals(""))
                {
                    User user = Database.getDB().loadUser(username);
                    toAddId.add(user.getId());
                }
            }
            for (String username : toRemove)
            {
                if (!username.equals(""))
                {
                    User user = Database.getDB().loadUser(username);
                    toRemoveId.add(user.getId());
                }
            }

            if (id.equals(-1L))
            {
                Group group = new Group();
                group.setTitle(title);
                group.setMembers(toAddId);
                group = Database.getDB().saveGroup(group);
                Profile profile = Database.getDB().loadProfile(userId);
                profile.addToGroups(group.getId());
                Database.getDB().saveProfile(profile);
            }
            else
            {
                Group group = Database.getDB().loadGroup(id);
                group.setTitle(title);

                for (Long memberId : toAddId)
                {
                    group.addToGroup(memberId);
                }
                for (Long memberId : toRemoveId)
                {
                    group.removeFromGroup(memberId);
                }

                Database.getDB().saveGroup(group);
            }
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while managing group %s", e, id));
        }
    }

    // Finds a Group by its name from a profile
    public Group getGroupByName(Long profileId, String groupName)
    {
        Profile profile = null;
        try
        {
            profile = Database.getDB().loadProfile(profileId);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while loading profile %s", e, profileId));
        }

        if (profile != null)
        {
            for (Long groupId : profile.getGroups())
            {
                Group group = null;
                try
                {
                    group = Database.getDB().loadGroup(groupId);
                }
                catch (SQLException e)
                {
                    logger.error(String.format("%s: database error while loading group %s", e, groupId));
                }

                assert group != null;
                if (group.getTitle().equals(groupName))
                {
                    return group;
                }
            }
        }
        return null;
    }
}
