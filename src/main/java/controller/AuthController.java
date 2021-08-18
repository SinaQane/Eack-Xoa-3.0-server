package controller;

import config.Config;
import constants.Constants;
import db.Database;
import event.events.authentication.LoginForm;
import event.events.authentication.SignUpForm;
import exceptions.authentication.LoginFailed;
import exceptions.authentication.SignUpFailed;
import model.Profile;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import response.responses.authentication.LoginResponse;
import response.responses.authentication.OfflineLoginResponse;
import response.responses.authentication.SignUpResponse;
import util.ImageUtil;
import util.Token;
import util.Validations;

import java.sql.SQLException;

public class AuthController
{
    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final Token tokenGenerator = new Token();

    public LoginResponse login (LoginForm form)
    {
        String username = form.getUsername();
        String password = form.getPassword();
        try
        {
            User user = Database.getDB().loadUser(username);
            if (!user.isDeleted())
            {
                if (user.getPassword().equals(password))
                {
                    if (user.isDeactivated())
                    {
                        user.setActive(true);
                        user = Database.getDB().saveUser(user);
                        logger.debug(String.format("user %s reactivated their account", user.getId()));
                    }
                    Database.getDB().updateLastSeen(user.getId());
                    logger.debug(String.format("user %s logged in", user.getId()));
                    return new LoginResponse(user, tokenGenerator.newToken(), null);
                }
                else
                {
                    logger.warn(String.format("wrong password login attempt to %s", user.getId()));
                    return new LoginResponse(null, "", new LoginFailed("wrong username or password"));
                }
            }
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while getting user %s", username));
        }
        return new LoginResponse(null, "", new LoginFailed("entered user doesn't exist"));
    }

    public OfflineLoginResponse offlineLogin(Long id)
    {
        try
        {
            User user = Database.getDB().loadUser(id);
            if (user.isDeactivated())
            {
                user.setActive(true);
                user = Database.getDB().saveUser(user);
            }
            Database.getDB().updateLastSeen(id);
            logger.debug(String.format("user %s logged in", user.getId()));
            return new OfflineLoginResponse(user, tokenGenerator.newToken());
        }
        catch (SQLException ignored)
        {
            logger.error(String.format("database error while getting user with id: %s", id));
        }
        return new OfflineLoginResponse(null, "");
    }

    public SignUpResponse signUp(SignUpForm form)
    {
        String phoneNumber = form.getPhoneNumber();
        String username = form.getUsername();
        String password = form.getPassword();
        String email = form.getEmail();
        String name = form.getName();

        if (name.equals(""))
        {
            return new SignUpResponse(null, "", new SignUpFailed("name cannot be empty"));
        }
        if (Validations.getValidations().usernameIsInvalid(username))
        {
            return new SignUpResponse(null, "", new SignUpFailed("please enter a valid username"));
        }
        if (Validations.getValidations().usernameIsUnavailable(username))
        {
            return new SignUpResponse(null, "", new SignUpFailed("there is already an account with this username"));
        }
        if (Validations.getValidations().emailIsInvalid(email))
        {
            return new SignUpResponse(null, "", new SignUpFailed("please enter a valid email"));
        }
        if (Validations.getValidations().emailIsUnavailable(email))
        {
            return new SignUpResponse(null, "", new SignUpFailed("there is already an account with this email"));
        }
        if (Validations.getValidations().phoneNumberIsInvalid(phoneNumber))
        {
            return new SignUpResponse(null, "", new SignUpFailed("please enter a valid phone number"));
        }
        if (Validations.getValidations().phoneNumberIsUnavailable(phoneNumber))
        {
            return new SignUpResponse(null, "", new SignUpFailed("there is already an account with this phone number"));
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setPhoneNumber(phoneNumber);
        user.setPhoneNumber(form.getPhoneNumber());
        user.setBirthDate(form.getBirthDate());
        user.setEmail(form.getEmail());

        String picture = form.getPicture();
        if (picture.equals(""))
        {
            picture = ImageUtil.imageToString(new Config(Constants.CONFIG).getProperty("profilePicture"));
        }

        Profile profile = new Profile();
        profile.setPicture(picture);

        try
        {
            user = Database.getDB().saveUser(user);
            Database.getDB().saveProfile(profile);

            logger.debug(String.format("user %s signed up", user.getId()));
            return new SignUpResponse(user, "", null);
        }
        catch (SQLException ignored)
        {
            logger.error("database error while saving a new user");
        }

        return new SignUpResponse(null, "", new SignUpFailed("signup failed for some internal reason"));
    }
}
