package controller;

import config.Config;
import constants.Constants;
import db.Database;
import event.events.settings.SettingsForm;
import model.Profile;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.ImageUtil;

import java.sql.SQLException;

public class SettingsController
{
    private static final Logger logger = LogManager.getLogger(SettingsController.class);

    public void editUser (SettingsForm form, long userId)
    {
        try
        {
            User user = Database.getDB().loadUser(userId);
            Profile profile = Database.getDB().loadProfile(userId);

            if (form.isBioChanging()) user.setBio(form.getBio());
            if (form.isNameChanging()) user.setName(form.getName());
            if (form.isEmailChanging()) user.setEmail(form.getEmail());
            if (form.isUsernameChanging()) user.setUsername(form.getUsername());
            if (form.isPasswordChanging()) user.setPassword(form.getPassword());
            if (form.isBirthDateChanging()) user.setBirthDate(form.getBirthDate());
            if (form.isPhoneNumberChanging()) user.setPhoneNumber(form.getPhoneNumber());
            if (form.isInfoStateChanging()) profile.setInfoState(form.isInfoState());
            if (form.isPrivateStateChanging()) profile.setPrivateState(form.isPrivateState());
            if (form.isLastSeenStateChanging()) profile.setLastSeenState(form.getLastSeenState());

            if (form.isPictureChanging())
            {
                if (form.getPicture().equals(""))
                {
                    profile.setPicture(ImageUtil.imageToString(new Config(Constants.CONFIG).getProperty("profilePicture")));
                }
                else
                {
                    profile.setPicture(form.getPicture());
                }
            }

            Database.getDB().saveUser(user);
            Database.getDB().saveProfile(profile);
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while editing user %s", userId));
        }
    }

    public void deactivate(long userId)
    {
        try
        {
            User user = Database.getDB().loadUser(userId);
            user.setActive(false);
            Database.getDB().saveUser(user);
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while deactivating user %s", userId));
        }
    }

    public void deleteAccount(long userId)
    {
        try
        {
            User user = Database.getDB().loadUser(userId);

            user.setEmail("");
            user.setUsername("");
            user.setPhoneNumber("");
            user.setDeleted(false);

            Database.getDB().saveUser(user);
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while deleting account for user %s", userId));
        }
    }
}
