import enigma.core.Enigma;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static enigma.console.Console cn = Enigma.getConsole("Tree & Table Maze Game", 110, 35);
    public KeyListener klis;
    public int keypr, rkey;

    static char[][] maze = new char[21][45];
    public FireballQueue activeFireballs = new FireballQueue(20);
    public ArrayList<Computer> robots = new ArrayList<>();
    public Queue inputQueue = new Queue(10);
    public Stack backpack = new Stack(8);
    public Player p = new Player(5, 5);
    public TreeScreen treeScreen;

    public int globalCounter = 0;
    public int lastDirX = 1, lastDirY = 0;
    public boolean isTreeMode = false;       // default mode is backpack
    public int activeScreen = 1;             // 1 for maze, 2 for tree
    public int previousScreen = 1;           // to check if screen changed

    public static void main(String[] args) throws Exception {
        new Main();
    }

    Main() throws Exception {
        treeScreen = new TreeScreen(cn);

        klis = new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {
                if (keypr == 0) { keypr = 1; rkey = e.getKeyCode(); }
            }
            public void keyReleased(KeyEvent e) {}
        };
        cn.getTextWindow().addKeyListener(klis);

        gameLoop();
    }

    public static void loadMaze() throws Exception {
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("src/maze.txt"));
        for (int i = 0; i < 21; i++) {
            String line = br.readLine();
            if (line != null) maze[i] = line.toCharArray();
        }
        br.close();
    }

    public void setupGame() throws Exception {
        loadMaze();
        for (int i = 0; i < 10; i++) inputQueue.enqueue(generateRandomItem());
        for (int i = 0; i < 10; i++) spawnItem((char)inputQueue.dequeue());
    }

    private char generateRandomItem() {
        Random rnd = new Random();
        int r = rnd.nextInt(10);
        if (r < 7) return "ABCD+>~^v=".charAt(rnd.nextInt(10)); // 70% symbol
        if (r < 9) return '@'; // 20% fireball
        return 'X'; // 10% robot
    }

    private void spawnItem(char item) {
        Random rnd = new Random();
        int rx, ry;
        do {
            rx = rnd.nextInt(43) + 1;
            ry = rnd.nextInt(19) + 1;
        } while (maze[ry][rx] != ' ' && maze[ry][rx] != '.'); // wait until we find empty space

        if (item == 'X') robots.add(new Computer('X', rx, ry));
        maze[ry][rx] = item;
    }

    private void clearScreen() {
        for (int i = 0; i < 35; i++) {
            cn.getTextWindow().setCursorPosition(0, i);
            for (int j = 0; j < 110; j++) {
                cn.getTextWindow().output(' ');
            }
        }
    }

    public void gameLoop() throws Exception {
        setupGame();
        maze[p.getY()][p.getX()] = 'P';

        while (p.getLife() > 0) {
            if (keypr == 1) {
                if (rkey == KeyEvent.VK_1) activeScreen = 1;
                else if (rkey == KeyEvent.VK_2) activeScreen = 2;
            }

            // clear screen if user changed the view
            if (activeScreen != previousScreen) {
                clearScreen();
                previousScreen = activeScreen;
            }

            if (activeScreen == 1) { // MAZE SCREEN
                if (keypr == 1) {
                    handlePlayerInput();
                    keypr = 0;
                }
                moveFireballs();
                if (globalCounter % 4 == 0) moveRobots();
                checkRobotDamage();
                if (globalCounter % 20 == 0) updateInputQueue();
                renderAll();
            }
            else if (activeScreen == 2) { // TREE SCREEN
                cn.getTextWindow().setCursorPosition(0, 0);
                treeScreen.draw();
                if (keypr == 1) {
                    if (rkey == KeyEvent.VK_T && !backpack.isEmpty()) {
                        int item = backpack.pop(); // take item from bag
                        treeScreen.placeSymbol(String.valueOf((char)item)); // put it to tree
                    } else {
                        treeScreen.handleKey(rkey); // other tree screen keys
                    }
                    keypr = 0;
                }
            }

            Thread.sleep(100);
            if (activeScreen == 1) globalCounter++;
        }

        clearScreen();
        cn.getTextWindow().setCursorPosition(50, 15);
        cn.getTextWindow().output("GAME OVER!");
    }

    private void handlePlayerInput() {
        int nx = p.getX(), ny = p.getY();
        if (rkey == KeyEvent.VK_LEFT) { nx--; lastDirX = -1; lastDirY = 0; }
        else if (rkey == KeyEvent.VK_RIGHT) { nx++; lastDirX = 1; lastDirY = 0; }
        else if (rkey == KeyEvent.VK_UP) { ny--; lastDirX = 0; lastDirY = -1; }
        else if (rkey == KeyEvent.VK_DOWN) { ny++; lastDirX = 0; lastDirY = 1; }
        else if (rkey == KeyEvent.VK_M) isTreeMode = !isTreeMode;
        else if (rkey == KeyEvent.VK_SPACE && p.getFireballCount() > 0) {
            p.useFireball();
            activeFireballs.enqueue(new Fireball(p.getX(), p.getY(), lastDirX, lastDirY));
        }

        char target = maze[ny][nx];
        if (target != '#' && target != 'X') {
            if (target == '@') p.addFireball();
            else if (target != ' ' && target != '.') {
                if (!isTreeMode) {
                    if (!backpack.isFull()) backpack.push((int)target);
                    else treeScreen.placeSymbol(String.valueOf(target));
                } else {
                    treeScreen.placeSymbol(String.valueOf(target));
                }
            }
            maze[p.getY()][p.getX()] = ' ';
            p.setX(nx); p.setY(ny);
            maze[p.getY()][p.getX()] = 'P';
        }
    }

    private void moveFireballs() {
        int size = activeFireballs.size();
        for (int i = 0; i < size; i++) {
            Fireball fb = activeFireballs.dequeue();
            if (maze[fb.getY()][fb.getX()] == 'o') maze[fb.getY()][fb.getX()] = ' ';
            fb.move();

            // ignore if out of bounds
            if (fb.getY() < 0 || fb.getY() >= 21 || fb.getX() < 0 || fb.getX() >= 45) continue;
            char next = maze[fb.getY()][fb.getX()];

            if (next == 'X') { // hit robot
                robots.removeIf(bot -> bot.getX() == fb.getX() && bot.getY() == fb.getY());
                p.setScore(p.getScore() + 50);
                maze[fb.getY()][fb.getX()] = ' ';
                activeFireballs.enqueue(fb);
            } else if (next == ' ' || next == '.') { // keep moving
                maze[fb.getY()][fb.getX()] = 'o';
                activeFireballs.enqueue(fb);
            }
        }
    }

    private void moveRobots() {
        for (Computer bot : robots) {
            maze[bot.getY()][bot.getX()] = ' ';
            bot.move(maze, p.getX(), p.getY());
            maze[bot.getY()][bot.getX()] = 'X';
        }
    }

    private void checkRobotDamage() {
        for (Computer bot : robots) {
            // check if robot is next to player
            if (Math.abs(bot.getX() - p.getX()) + Math.abs(bot.getY() - p.getY()) == 1) {
                p.setLife(p.getLife() - 5);
            }
        }
    }

    private void updateInputQueue() {
        spawnItem((char)inputQueue.dequeue());
        inputQueue.enqueue(generateRandomItem());
    }

    public void renderAll() {
        for (int i = 0; i < 21; i++) {
            cn.getTextWindow().setCursorPosition(0, i);
            for (int j = 0; j < 45; j++) {
                char c = maze[i][j];
                if (c == '.') cn.getTextWindow().output(' ');
                else cn.getTextWindow().output(c);
            }
        }

        cn.getTextWindow().setCursorPosition(50, 4);
        cn.getTextWindow().output("Time     : " + (globalCounter / 10) + " s   ");
        cn.getTextWindow().setCursorPosition(50, 5);
        cn.getTextWindow().output("Score    : " + p.getScore() + "    ");
        cn.getTextWindow().setCursorPosition(50, 6);
        cn.getTextWindow().output("Fireball : " + p.getFireballCount() + "    ");
        cn.getTextWindow().setCursorPosition(50, 7);
        cn.getTextWindow().output("Life     : " + p.getLife() + "    ");
        cn.getTextWindow().setCursorPosition(50, 8);
        cn.getTextWindow().output("Storage  : " + (isTreeMode ? "Tree    " : "Backpack"));

        renderBackpackVisual();
    }

    private void renderBackpackVisual() {
        int startX = 70, startY = 12;
        for (int i = 0; i < 8; i++) {
            cn.getTextWindow().setCursorPosition(startX, startY + i);
            cn.getTextWindow().output("|   |");
        }
        cn.getTextWindow().setCursorPosition(startX, startY + 8);
        cn.getTextWindow().output("+---+");
        cn.getTextWindow().setCursorPosition(startX, startY + 9);
        cn.getTextWindow().output("Backpack");
    }
}