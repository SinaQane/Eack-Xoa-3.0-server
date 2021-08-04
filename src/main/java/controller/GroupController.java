package controller;

import db.Database;
import event.events.groups.ManageGroupForm;
import model.Group;
import model.Profile;
import model.User;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class GroupController
{
    public List<List<Long>> getGroups(long userId)
    {
        List<Long> groups = new LinkedList<>();

        try
        {
            Profile profile = Database.getDB().loadProfile(userId);
            groups = profile.getGroups();
        } catch (SQLException ignored) {}

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
                User user = Database.getDB().loadUser(username);
                toAddId.add(user.getId());
            }
            for (String username : toRemove)
            {
                User user = Database.getDB().loadUser(username);
                toRemoveId.add(user.getId());
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
        } catch (SQLException ignored) {}
    }

    // Finds a Group by its name from a profile
    public Group getGroupByName(Profile profile, String groupName)
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
}
