public class Fireball {
    private int x;
    private int y;
    private int dirX;
    private int dirY;

    public Fireball(int startX, int startY, int dirX, int dirY) {
        this.x = startX;
        this.y = startY;
        this.dirX = dirX;
        this.dirY = dirY;
    }

    public void move() {
        this.x += this.dirX;
        this.y += this.dirY;
    }

    public int getX() { return x; }
    public int getY() { return y; }

}