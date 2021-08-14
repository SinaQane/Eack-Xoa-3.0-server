package model;

public class Bot
{
    private Long id = -1L;
    private Long userId = -1L;
    private String jarURL = "";
    private int kind = 1; // kind = 1, 2 or 3

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
