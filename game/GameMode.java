package javawzgdyx.game;

public enum GameMode {
    SOLO("狂扁 deepseek", "MODE 01", 1),
    DUEL("双人对打", "MODE 02", 2);

    private final String title;
    private final String tag;
    private final int playerCount;

    GameMode(String title, String tag, int playerCount) {
        this.title = title;
        this.tag = tag;
        this.playerCount = playerCount;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public boolean isDuel() {
        return this == DUEL;
    }
}
