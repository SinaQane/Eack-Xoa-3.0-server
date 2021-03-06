package util;

import config.Config;
import constants.Constants;
import db.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations
{
    private static final Logger logger = LogManager.getLogger(Validations.class);

    // Email regex
    private final Pattern EMAIL_REGEX;

    // International phone number regex (like +989123456789)
    private final Pattern PHONE_REGEX;

    /* Username regex with these rules:
           Only contains alphanumeric characters, underscore and dot.
           Underscore and dot can't be at the end or start of a username.
           Underscore and dot can't be next to each other.
           Underscore or dot can't be used multiple times in a row.
           Number of characters must be between 8 and 20. */
    private final Pattern USERNAME_REGEX;

    static Validations validations;

    private Validations()
    {
        EMAIL_REGEX = Pattern.compile(new Config(Constants.CONFIG).getProperty(String.class, "email.regexp"));
        PHONE_REGEX = Pattern.compile(new Config(Constants.CONFIG).getProperty(String.class, "phone.regexp"));
        USERNAME_REGEX = Pattern.compile(new Config(Constants.CONFIG).getProperty(String.class, "username.regexp"));
    }

    public static Validations getValidations()
    {
        if (validations == null)
        {
            validations = new Validations();
        }
        return validations;
    }

    public boolean usernameIsUnavailable(String username)
    {
        boolean exists = false;
        try
        {
            exists = Database.getDB().itemAvailable("username", username);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while validating username %s", e, username));
        }
        return exists;
    }

    public boolean usernameIsInvalid(String username)
    {
        Matcher matcher = USERNAME_REGEX.matcher(username);
        return !matcher.find();
    }

    public boolean emailIsUnavailable(String email)
    {
        boolean exists = false;
        try
        {
            exists = Database.getDB().itemAvailable("email", email);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while validating email %s", e, email));
        }
        return exists;
    }

    public boolean emailIsInvalid(String email)
    {
        Matcher matcher = EMAIL_REGEX.matcher(email);
        return !matcher.find();
    }

    public boolean phoneNumberIsUnavailable(String phoneNumber)
    {
        if (phoneNumber.equals(""))
        {
            return false;
        }
        boolean exists = false;
        try
        {
            exists = Database.getDB().itemAvailable("phone_number", phoneNumber);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while validating phone number %s", e, phoneNumber));
        }
        return exists;
    }

    public boolean phoneNumberIsInvalid(String phoneNumber)
    {
        if (phoneNumber.equals(""))
        {
            return false;
        }
        Matcher matcher = PHONE_REGEX.matcher(phoneNumber);
        return !matcher.find();
    }
}