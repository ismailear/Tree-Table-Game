import enigma.console.Console;
import java.awt.event.KeyEvent;

public class TreeScreen {
    private Console cn;
    private TreeNode[] allNodes;
    private TreeNode cursor;

    public int penalty = 0;
    public boolean finished = false;
    public int treeScore = 0;

    // new expanded coordinates that don't overlap with the menu
    final private int[][] nodeCoords = {
            {0, 0},   // 0 empty
            {55, 4},  // 1 root (Level 1)
            {31, 8},  {79, 8}, // 2-3 (Level 2)
            {19, 12}, {43, 12}, {67, 12}, {91, 12}, // 4-7 (Level 3)
            {13, 16}, {25, 16}, {37, 16}, {49, 16}, {61, 16}, {73, 16}, {85, 16}, {97, 16}, // 8-15 (Level 4)
            {10, 20}, {16, 20}, {22, 20}, {28, 20}, {34, 20}, {40, 20}, {46, 20}, {52, 20}, {58, 20}, {64, 20}, {70, 20}, {76, 20}, {82, 20}, {88, 20}, {94, 20}, {100, 20} // 16-31 (Level 5)
    };

    public TreeScreen(Console cn) {
        this.cn = cn;
        this.allNodes = new TreeNode[32];
        initializeTreeStructure();
        this.cursor = allNodes[1]; // cursor starts at root
    }

    private void initializeTreeStructure() {
        for (int i = 1; i <= 31; i++) {
            allNodes[i] = new TreeNode();
        }
        for (int i = 1; i <= 15; i++) {
            allNodes[i].left  = allNodes[2 * i];
            allNodes[i].right = allNodes[2 * i + 1];
            allNodes[2 * i].parent     = allNodes[i];
            allNodes[2 * i + 1].parent = allNodes[i];
        }
    }

    public void handleKey(int rkey) {
        if (rkey == KeyEvent.VK_W) {
            if (cursor.parent != null) { cursor = cursor.parent; penalty++; }
        }
        else if (rkey == KeyEvent.VK_A) {
            if (cursor.left != null) { cursor = cursor.left; penalty++; }
        }
        else if (rkey == KeyEvent.VK_D) {
            if (cursor.right != null) { cursor = cursor.right; penalty++; }
        }
        else if (rkey == KeyEvent.VK_R) {
            removeSymbol();
        }
        else if (rkey == KeyEvent.VK_F) {
            finishTree();
        }
        draw();
    }

    public void placeSymbol(String symbol) {
        if (cursor != null && cursor.symbol == null) {
            cursor.symbol = symbol;
            moveToNextEmptySlot();
        }
    }

    public String removeSymbol() {
        if (cursor != null && cursor.symbol != null) {
            String s = cursor.symbol;
            cursor.symbol = null;
            penalty += 2;
            return s;
        }
        return null;
    }

    private void moveToNextEmptySlot() {
        for (int i = 1; i <= 31; i++) {
            if (allNodes[i].symbol == null) {
                cursor = allNodes[i];
                return;
            }
        }
    }

    public void finishTree() {
        int varCount = countVariables(allNodes[1]);
        int depth    = getDepth(allNodes[1]);

        if (varCount < 3 || depth < 3 || !isValid(allNodes[1])) {
            penalty += 10;
            finished = false;
            cn.getTextWindow().setCursorPosition(0, 27);
            cn.getTextWindow().output("ERROR: Invalid tree! -10 penalty.      ");
            cn.getTextWindow().setCursorPosition(0, 28);
            cn.getTextWindow().output("Min 3 variables and depth must be 3.   ");
        } else {
            treeScore = calculateTreeScore();
            finished  = true;

            String infix   = toInfix(allNodes[1]);
            String postfix = toPostfix(allNodes[1]);

            cn.getTextWindow().setCursorPosition(0, 27);
            cn.getTextWindow().output("Success! Score: " + treeScore + "                 ");
            cn.getTextWindow().setCursorPosition(0, 28);
            cn.getTextWindow().output("Infix:   " + infix + "                                 ");
            cn.getTextWindow().setCursorPosition(0, 29);
            cn.getTextWindow().output("Postfix: " + postfix + "                               ");
            cn.getTextWindow().setCursorPosition(0, 30);
            cn.getTextWindow().output("Press ENTER to go to Table screen...             ");
        }
    }

    public int calculateTreeScore() {
        int count = 0;
        for (int i = 1; i <= 31; i++) {
            if (allNodes[i] != null && allNodes[i].symbol != null) count++;
        }
        return 10 * count;
    }

    private int countVariables(TreeNode n) {
        if (n == null || n.symbol == null) return 0;
        if (n.symbol.matches("[A-Da-d]") && n.left == null && n.right == null) return 1;
        return countVariables(n.left) + countVariables(n.right);
    }

    public int getDepth(TreeNode n) {
        if (n == null || n.symbol == null) return 0;
        return 1 + Math.max(getDepth(n.left), getDepth(n.right));
    }

    private boolean isValid(TreeNode n) {
        if (n == null || n.symbol == null) return false;
        // replaced math signs with logic signs from the doc
        boolean isOperator = "+>v^=~".contains(n.symbol);
        boolean isVariable = n.symbol.matches("[A-Da-d]");

        if (n.symbol.equals("~")) return isValid(n.left) || isValid(n.right); // ~ usually takes one child
        if (isVariable) return n.left == null && n.right == null;
        if (isOperator) return isValid(n.left) && isValid(n.right);
        return false;
    }

    public String toInfix(TreeNode n) {
        if (n == null || n.symbol == null) return "";
        if (n.left == null && n.right == null) return n.symbol;
        return "(" + toInfix(n.left) + n.symbol + toInfix(n.right) + ")";
    }

    public String toPostfix(TreeNode n) {
        if (n == null || n.symbol == null) return "";
        return toPostfix(n.left) + toPostfix(n.right) + n.symbol;
    }

    public TreeNode getRoot() {
        return allNodes[1];
    }

    public int getPenalty() {
        return penalty;
    }

    public void draw() {
        // added extra spaces to clear old text on screen
        cn.getTextWindow().setCursorPosition(0, 0);
        cn.getTextWindow().output("=== TREE SCREEN ===                                                              ");
        cn.getTextWindow().setCursorPosition(0, 1);
        cn.getTextWindow().output("W:Parent  A:Left  D:Right  T:Place  R:Undo  F:Finish                             ");

        for (int i = 1; i <= 31; i++) {
            renderNode(i);
        }

        cn.getTextWindow().setCursorPosition(0, 25);
        cn.getTextWindow().output("Penalty: " + penalty + "          ");
    }

    private void renderNode(int index) {
        int x = nodeCoords[index][0];
        int y = nodeCoords[index][1];

        char symbol = (allNodes[index].symbol != null) ? allNodes[index].symbol.charAt(0) : '.';

        // put [ ] around the cursor, clear others
        if (allNodes[index] == cursor) {
            cn.getTextWindow().output(x - 1, y, '[');
            cn.getTextWindow().output(x, y, symbol);
            cn.getTextWindow().output(x + 1, y, ']');
        } else {
            cn.getTextWindow().output(x - 1, y, ' ');
            cn.getTextWindow().output(x, y, symbol);
            cn.getTextWindow().output(x + 1, y, ' ');
        }
    }
}