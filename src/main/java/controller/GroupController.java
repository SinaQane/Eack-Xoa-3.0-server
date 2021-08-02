package controller;

import db.Database;
import model.Group;
import model.Profile;

import java.sql.SQLException;

public class GroupController
{
    // Finds a Group by its name from a profile
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

    // Adds a Group to profile's group list if it already doesn't exist
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
}
