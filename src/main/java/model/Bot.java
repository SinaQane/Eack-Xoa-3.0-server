package model;

public class Bot
{
    private Long id = -1L;
    private Long userId = -1L;
    private String jarURL = "";

    // kind is an integer showing that is the bot is a private bot (1), a two player game (2) or a group bot (3)
    private int kind = 1;

    public Bot() {}

    public Bot(int kind, Long userId, String jarURL)
    {
        this.kind = kind;
        this.userId = userId;
        this.jarURL = jarURL;
    }

    public int getKind()
    {
        return kind;
    }

    public void setKind(int kind)
    {
        this.kind = kind;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getJarURL()
    {
        return jarURL;
    }

    public void setJarURL(String jarURL)
    {
        this.jarURL = jarURL;
    }
}
