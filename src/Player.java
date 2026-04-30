public class Player {private int x;
    private int y;
    private int life;
    private int score;
    private int fireballCount;


    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.life = 100;
        this.score = 0;
        this.fireballCount = 0;
    }


    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public int getLife() { return life; }
    public void setLife(int life) { this.life = life; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getFireballCount() { return fireballCount; }
    public void addFireball() { this.fireballCount++; }
    public void useFireball() { if(this.fireballCount > 0) this.fireballCount--; }

}