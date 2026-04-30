import java.util.Random;

public class Computer {
    private char symbol;
    private int x, y;
    private boolean mod; // true: targeted, false: random
    private Random r = new Random();

    public Computer(char s, int x, int y) {
        this.symbol = s;
        this.x = x;
        this.y = y;
        this.mod = r.nextBoolean();
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isMod() {
        return mod;
    }

    public void setMod(boolean mod) {
        this.mod = mod;
    }

    public Random getR() {
        return r;
    }

    public void setR(Random r) {
        this.r = r;
    }

    public void move(char[][] maze, int pX, int pY) {
        int nextX = x, nextY = y;

        if (mod) {
            int targetX = -1, targetY = -1, minDist = 999;
            for (int i = 0; i < 21; i++) {
                for (int j = 0; j < 45; j++) {
                    char cell = maze[i][j];
                    if ("ABCD+>~^v=".indexOf(cell) != -1) {
                        int dist = Math.abs(this.x - j) + Math.abs(this.y - i);
                        if (dist < minDist) { minDist = dist; targetX = j; targetY = i; }
                    }
                }
            }
            if (targetX != -1) {
                if (x < targetX) nextX++; else if (x > targetX) nextX--;
                else if (y < targetY) nextY++; else if (y > targetY) nextY--;
            }
        } else {
            int dir = r.nextInt(4);
            if (dir == 0) nextY--; else if (dir == 1) nextX++;
            else if (dir == 2) nextY++; else if (dir == 3) nextX--;
        }

        if (maze[nextY][nextX] != '#' && maze[nextY][nextX] != 'P' && maze[nextY][nextX] != 'X') {
            this.x = nextX; this.y = nextY;
        }
    }
}