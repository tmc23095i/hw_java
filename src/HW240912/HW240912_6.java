package HW240912;

import java.io.IOException;
import java.util.Vector;

public class HW240912_6 {
    public static void main_() throws IOException {
        // strings of sudoku
        String str0 = "000000000 000000000 000000000 - 000000000 000000000 000000000 - 000000000 000000000 000000000";
        String str1 = "080-205-001 002-094-050 000-000-820 000-080-097 000-007-310 210-500-000 600-400-003 300-070-045 070-300-000";
        String str2 = "003 500 004 780 000 000000 806 070000 000 000400 705 108620 003 049070 000 020090 000 600000 209 010";

        // new a sudoku obj
        Sudoku sudoku = new Sudoku();
        boolean b = sudoku.loadSudoku(str1);
        if (b == false) {
            System.out.println(">> load: failed!");
            return;
        }

        // get sudoku string
        String str = sudoku.toString();
        System.out.println(">> loading: " + str);

        // show the loaded sudoku
        System.out.println(">> loaded: \n" + sudoku.toPrints());

        // get answer of sudoku
        System.out.println(">> detectAnswer: " + sudoku.detectAnser());

        // commit all determinable cells
        sudoku.commitAll();
        System.out.println(">> commitAll: \n" + sudoku.toPrints());

        // get all sudokus in the world (all answers of empty sudoku)
        System.out.println(">> Any key to get the next answer, or Q to finish.");
        long ai = 0; //== type too short
        sudoku = new Sudoku();
        sudoku.loadSudoku(str0);
        while (true) {
            // get one
            Sudoku one = sudoku.guessAnswers();
            if (one != null) {
                ai++;
                System.out.println(">> guessAnswer: #" + ai + "\n" + one.toPrints());
            } else {
                System.out.println(">> guessAnswer: #OVER");
                break;
            }
            // finish
            int key = System.in.read();
            if ((key == 'Q') || (key == 'q')) {
                System.out.println(">> Finish guessing by user.");
                break;
            }
        }
        sudoku.guessEnd();

        //== history operations

    }

    public String margeStrings(String strs[]) {
        String retn = "";

        int cnt = strs.length;
        for (int i = 0; i < cnt - 1; i++) {
            retn += strs[i] + "\n";
        }
        retn += strs[cnt - 1];

        return retn;
    }

}

/**
 * 数独类, 用于求解任意数独(包括非完整数独)
 * @version v0.9.9
 * @author tmc23095i
 * 前言:
 *   1. 数独的规则:
 *     1. 每个`范围`都有9个格子, 这9个格子必须囊括数字1~9, 即每个数字都必须存在且不重复
 *     1. 这种`范围`有三个, 分别是行、列、块(将整个9x9数独面板依次划分为9个的3x3的九宫格)
 *     1. 在所有81个格子内填入数字, 且满足上述要求, 则为正确的数独答案
 *   1. 本类术语
 *     1. range: 上述的`范围`, 可以是每行或每列或每块, 也有时表示此范围内的朋友格子
 *     1. mate: 朋友格子, 根据上下文, 有时表示某一范围内的朋友格子, 也有时表示所有范围内的格子
 *     1. commit: 将某个数字填入某个某个格子
 *     1. proable-digit: 可能的数字, 即未被朋友格子占用的数字, 有时也会以enabled-digit表示
 *     1. determinable: 可被确定的数字, 即一个格子能填的数字已经确定了, 只有一个可能, 哪怕这个暂时还没有被提交, 有时也会以ascertainable表示
 *     1. mark: 死亡标记, 用于标记某个数字是否已被占用
 */
class Sudoku {
    //-- static
    private final static int col_cnt = 9; // the count of the cells per col in the sudoku
    private final static int row_cnt = 9; // the count of the cells per row in the sudoku
    private final static int cell_cnt = 81; // the count of the cells in the sudoku
    private final static int colcnt_inblo = 3; // the count of the cells per col in one block
    private final static int rowcnt_inblo = 3; // the count of the cells per col in one block
    private final static int cellcnt_inblo = 9; // the count of cells in one block

    private final static int NOP = -1; // used to represent the diff-range
    private final static int ROW = 0; // used to represent the row-range
    private final static int COL = 1; // used to represent the col-range
    private final static int BLO = 2; // used to represent the blo-range
    private final static int BLO_ONLY = 3; // used to represent the blo-range(non-row&non-col)
    private final static int BLO_ROW = 4; // used to represent the blo&row-range
    private final static int BLO_COL = 5;// used to represent the blo&col-range

    //-- private
    private Cell[] cells; // the cells of the sudoku
    //history
    private boolean enable_history;
    private History history;
    //guess
    private Sudoku guess_sudoku;
    private GuessTodo guess_todo;

    //-- public

    /**
     * 实例化一个空数独
     */
    public Sudoku() {
        // init the cell-array: alloc space
        initCells();

        enable_history = false;
        history = null;
    }

    /**
     * 从一个已有的数独, 实例化一个新的副本, 是最高效的拷贝构造器
     * @param src 已有的数独
     * @apiNote 函数通过寻找源数独中已提交的数字并在新数独中提交他们来实现拷贝, 因此标记列表可能与源不同, 但绝对正确, 此外会拷贝源的历史纪录
     */
    public Sudoku(Sudoku src) {
        //== 好像是不用检查的 因为本类数独如果有错误是不会提交的  所以现有的数独暂时都是格子独立且正确的
        this();

        int srcd;
        for (int i = 0; i < 81; i++) {
            srcd = src.getCellDigit(i);
            if (srcd == 0) {
                continue;
            }
            this.commit(i, srcd, false);
        }

        if (src.history != null) {
            this.history = new History(src.history);
            this.enable_history = src.enable_history;
        }
    }

    /**
     * 加载一个数独, 通过指定的数独字串, 从字串中读取数字(0~9), 抛弃其他字符, 并依次提交到数独中
     * @param s 数独字串
     * @return 返回真, 如果函数成功
     * @apiNote 如果函数失败, 数独会恢复到原来的样子
     */
    public boolean loadSudoku(String s) {
        if (s.length() < 81) { // invalid sudoku-string
            return false;
        }

        char backup[] = toHash();
        this.initCells();

        char str[] = s.toCharArray();
        int index = 0;
        int digit;
        for (char c : str) {
            digit = c - '0';

            // skip non-digit char
            if ((digit < 0) || (digit > 9)) {
                continue;
            }

            boolean b = commit(index++, digit);
            // bad sudoku
            if (b == false) {
                index = 0;
                break;
            }

            if (index == 81) {
                break;
            }
        }

        // recover to original suduko when failed
        if (index != 81) {
            loadCorrectHash(backup);
            return false;
        }

        return true;
    }

    /**
     * 加载一个数独, 通过指定的数独哈希, 哈希可通过本类的方法获取
     * @param s 数独哈希
     * @return 返回真, 如果函数成功
     * @apiNote 如果函数失败, 数独会恢复到原来的样子
     */
    public boolean loadSudoku(char[] hash) {
        char backup[] = toHash();
        this.initCells();

        int index = 0;
        for (char c : hash) {
            int hnib = c >>> 4;
            int lnib = c & 0b1111;
            boolean b = commit(index++, hnib);
            if (b == false) { // bad sudoku
                index = 0;
                break;
            }
            if (lnib != 0b1111) {
                b = commit(index++, lnib);
                if (b == false) { // bad sudoku
                    index = 0;
                    break;
                }
            }
        }

        // recover to original suduko when failed
        if (index != 81) {
            loadCorrectHash(backup);
            return false;
        }

        return true;
    }

    /**
     * 获取一个数独的“哈希”, 是任何数独的唯一标识, 具有不可碰撞性, 可以展开(恢复)到一个数独
     * @return 返回数独的哈希
     */
    public char[] toHash() {
        char hash[] = new char[(cell_cnt + 1) / 2];
        int digit;

        for (int i = 0, hi = 0; i < cell_cnt; i += 2) {
            digit = cells[i].getDigit();
            digit <<= 4;
            hash[hi++] = (char) digit;
        }
        for (int i = 1, hi = 0; i < cell_cnt; i += 2) {
            digit = cells[i].getDigit();
            hash[hi++] += (char) digit;
        }

        // if the cell-cnt is not a even numb, then set the low nibble of the last hashchar to 0b1111
        if ((cell_cnt & 1) == 1) {
            hash[hash.length - 1] |= 0b0000_1111;
        }

        return hash;
    }

    /**
     * 获取一个数独的字串, 包含81个字符, 每个字符对应了每个格子的数字, 未提交的数字将以0指示
     */
    public String toString() {
        String str = "";
        for (Cell cell : cells) {
            str += cell.getDigit();
        }
        str += "\0";
        return str;
    }

    /**
     * 获取一个可以用于打印的数独面板字串, 字串以图形化的方式展示当前的数独
     * @return 返回字串
     */
    public String toPrints() {
        char onecell[] = new char[3];
        onecell[0] = ' ';
        onecell[1] = 'd';
        onecell[2] = ' ';
        String h_tab = "+-----------+-----------+-----------+";

        int ci = 0;
        int cd = 0;
        String output = "";
        for (int _4_ = 0; _4_ < 3; _4_++) {
            output = output + h_tab + '\n';
            for (int _3_ = 0; _3_ < 3; _3_++) {
                output += "| ";
                for (int _2_ = 0; _2_ < 3; _2_++) {
                    for (int _1_ = 0; _1_ < 3; _1_++) {
                        cd = cells[ci++].getDigit();
                        if (cd == 0) {
                            onecell[1] = ' ';
                        } else {
                            onecell[1] = (char) (cd + '0');
                        }
                        output += new String(onecell);
                    }
                    output += " | ";
                }
                output += "\n";
            }
        }
        output += h_tab;

        return output;
        /*
        +-----------+-----------+-----------+ \n
        |  2  6  3  |  5  1  7  |  8  9  4  | \n
        |  7  8  1  |  9  4  2  |  3  5  6  | \n
        |  9  4  5  |  8  3  6  |  2  7  1  | \n
        +-----------+-----------+-----------+ \n
        |  5  1  8  |  6  9  4  |  7  3  2  | \n
        |  4  3  9  |  7  2  5  |  1  6  8  | \n
        |  6  2  7  |  1  8  3  |  5  4  9  | \n
        +-----------+-----------+-----------+ \n
        |  1  7  4  |  3  6  8  |  9  2  5  | \n
        |  3  9  2  |  4  5  1  |  6  8  7  | \n
        |  8  5  6  |  2  7  9  |  4  1  3  | \n
        +-----------+-----------+-----------+ \n
        */
    }

    /**
     * 获取所有与指定格子同范围的朋友格子的索引数组
     * @param ci 指定格子的索引
     * @return 返回同范围朋友格子索引的数组
     */
    public int[] getAllMates(int ci) {
        int mate_cnt = row_cnt - 1 + col_cnt - 1 + 4;
        int mates[] = new int[mate_cnt];
        int mi = 0;
        int range[];

        range = getRangeMates(ci, ROW);
        for (int i : range) {
            mates[mi++] = i;
        }
        range = getRangeMates(ci, COL);
        for (int i : range) {
            mates[mi++] = i;
        }
        range = getRangeMates(ci, BLO_ONLY);
        for (int i : range) {
            mates[mi++] = i;
        }

        return mates;
    }

    /**
     * 检查当前数独的所有格子是否全部已经提交数字
     * @return 返回真, 如果所有格子都已经提交数字
     * @apiNote 单纯检查每个格子是否有提交数字, 不检查矛盾
     */
    public boolean isFinished() {
        for (Cell cell : cells) {
            if (cell.getDigit() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取指定格子的数字
     * @param index 指定格子的索引
     * @return 返回指定格子已提交的数字
     * @return 返回0, 如果没有数字被提交过
     */
    public int getCellDigit(int index) {
        return cells[index].getDigit();
    }

    /**
     * 提交指定的数字到指定的格子
     * @param index 要提交到的格子
     * @param digit 要被提交的数字
     * @param USE_HISTORY 指定是否纪录历史, 如果可用
     * @return 返回假, 如果数字不正确(此数字在此格子中不可用)
     * @apiNote 如果要提交的数字是0, 则函数不做任何事情, 且返回真
     */
    private boolean commit(int index, int digit, boolean USE_HISTORY) {
        // commit one cell with the specified digit
        // returns true when no wrong happened
        // returns false when the specified digit isnt right
        //! careful: if the digit==0, this func changes nothing and returns true instead of false
        if (digit == 0) {
            return true;
        }

        // this digit has conflict with another mate-cell
        if (cells[index].isMarked(digit) == true) {
            return false;
        }

        cells[index].setDigit(digit);
        markAllMates(index, digit);

        if (USE_HISTORY == true) {
            if ((history != null) && (enable_history == true)) {
                Operation op = new Operation(Operation.COMMIT, index, digit);
                history.newdo(op);
            }
        }

        return true;
    }

    /**
     * 提交指定的数字到指定的格子, 不带历史纪录版本, 无论history如何, 都不会写入历史纪录
     * @param index 要提交到的格子
     * @param digit 要被提交的数字
     * @return 返回假, 如果数字不正确(此数字在此格子中不可用)
     * @apiNote 如果要提交的数字是0, 则函数不做任何事情, 且返回真
     */
    public boolean commit(int index, int digit) {
        boolean b = commit(index, digit, false);
        return b;
    }

    /**
     * 提交指定的数字到指定的格子, 带历史纪录的版本(History-Version), 如果数独已安装且启用了历史纪录, 则成功commit时会添加此条纪录
     * @param index 要提交到的格子
     * @param digit 要被提交的数字
     * @return 返回假, 如果数字不正确(此数字在此格子中不可用)
     * @apiNote 如果要提交的数字是0, 则函数不做任何事情, 且返回真
     */
    public boolean commitH(int index, int digit) {
        boolean b = commit(index, digit, true);
        return b;
    }

    /**
     * 取消提交指定的格子
     * @param index 要取消的格子索引
     * @param USE_HISTORY 指定是否纪录历史, 如果可用
     * @apiNote 如果此格子未提交任何数字, 则此函数不进行任何操作
     */
    private void uncommit(int index, boolean USE_HISTORY) {
        int digit = cells[index].getDigit();
        if (digit == 0) {
            return;
        }

        // update self mark-list and committed-digit
        cells[index].data = 0;
        int mates[] = getAllMates(index);
        for (int mi : mates) {
            int md = getCellDigit(mi);
            if (md != 0) {
                cells[index].mark(md);
            }
        }

        unmarkAllMates(index, digit);

        if (USE_HISTORY == true) {
            if ((history != null) && (enable_history == true)) {
                Operation op = new Operation(Operation.UNCOMMIT, index, digit);
                history.newdo(op);
            }
        }
    }

    /**
     * 调试用, 获取指定格子及其所有朋友格子的详细信息
     * @param index 指定格子的索引
     * @return 返回字串数组, 每个字串包含一个格子的详细信息
     * @apiNote 格式: [Debug Mates] 格子范围: d已提交数字 @格子索引 | 格子标记列表
     */
    public String[] debugMates(int index) {
        String retn[] = new String[1 + 8 + 8 + 8];
        int ri = 0;
        int digit = getCellDigit(index);
        int marks[] = new int[9];
        String mark_str;

        mark_str = "";
        marks = cells[index].debugMarks();
        for (int m : marks) {
            mark_str += m;
        }
        marks = null;
        retn[ri++] = "[Debug Mates] " + "curr-cell: d" + digit + " @" + index + " | " + mark_str;

        for (int r = 0; r < 3; r++) {
            String range_str = "";
            switch (r) {
                case ROW:
                    range_str = "row-mates";
                    break;
                case COL:
                    range_str = "col-mates";
                    break;
                case BLO:
                    range_str = "blo-mates";
                    break;
                default:
                    break;
            }
            int mates[] = getRangeMates(index, r);
            for (int mi : mates) {
                mark_str = "";
                marks = cells[mi].debugMarks();
                for (int m : marks) {
                    mark_str += m;
                }
                marks = null;
                retn[ri++] = "[Debug Mates] " + range_str + ": d" + getCellDigit(mi) + " @" + mi + " | " + mark_str;
            }
        }

        return retn;
    }

    /**
     * 取消提交指定的格子, 非历史纪录版本
     * @param index 要取消的格子索引
     * @apiNote 如果此格子未提交任何数字, 则此函数不进行任何操作
     */
    public void uncommit(int index) {
        uncommit(index, false);
    }

    /**
     * 取消提交指定的格子, 支持历史纪录版本
     * @param index 要取消的格子索引
     * @apiNote 如果此格子未提交任何数字, 则此函数不进行任何操作
     */
    public void uncommitH(int index) {
        uncommit(index, true);
    }

    /**
     * 提交所有可被提交的格子, 此函数为非历史纪录版本, 因此不会写入任何历史纪录
     * @return 返回此函数提交的格子数
     * @return 返回-1, 如果发现一个错误
     * @apiNote 出于性能和场景率考虑, 此函数被设计为直接修改数独, 无论函数成功与否, 如需防止数独被破坏, 请自行备份原始哈希并重载数独, 或启用历史纪录并失败时撤销操作
     */
    public int commitAll() {
        int r = fill(false);
        return r;
    }

    /**
     * 填充数独(即提交所有可被提交的格子), 此函数为历史纪录版本, 因此会根据此数独的设定决定是否写入每条commit纪录
     * @return 返回此函数提交的格子数
     * @return 返回-1, 如果发现一个错误
     * @apiNote 出于性能和场景率考虑, 此函数被设计为直接修改数独, 无论函数成功与否, 如需防止数独被破坏, 请自行备份原始哈希并重载数独, 或启用历史纪录并失败时撤销操作
     */
    public int commitAllH() {
        int r = fill(true);
        return r;
    }

    /**
     * 探测一个格子是否存在可被提交的数字
     * @param index 要探测的格子索引
     * @return 返回可被提交的数字, 如果存在
     * @return 返回0, 如果无法确定可被提交的数字
     * @return 返回-1, 如果发现一个错误(在数独中发现冲突的两个格子)
     */
    public int detectDigit(int index) {
        // try to find out the determinable digit of this cell
        // return it when found
        // returns 0, when didnot find
        // returns -1, when found a wrong (a bad sudoku)

        int digit;
        digit = detectBySelf(index);
        if (digit > 0) {
            return digit;
        }
        if (digit == -1) {
            return -1;
        }

        digit = detectByMates(index);
        if (digit > 0) {
            return digit;
        }
        if (digit == -1) {
            return -1;
        }

        return 0;
    }

    /**
     * 根据现有数独, 探测所有格子可被提交的数字, 如果最终得到答案则返回
     * @return 返回答案数独, 如果探测到
     * @return 返回null, 如果探测所有格子后, 任意有未确定的格子
     */
    public Sudoku detectAnser() {
        // find out the answer of curr-sudoku, and returns the answer-sudoku
        // returns null when did not or can not find the answer

        Sudoku test = new Sudoku(this);
        int retn = test.updateToAnswer(false);
        if (retn == 1) {
            return test;
        }
        return null;
    }

    /**
     * 逐个返回当前数独对象的所有答案
     * @return 返回一个答案数独
     * @return 返回null, 如果没有更多答案了
     * @apiNote 当中途不再需要更多答案时, 应调用对应的结束函数, 以释放此函数占用的堆空间(包含一个数独对象(带最大10w历史纪录)和一个待猜对象)
     */
    public Sudoku guessAnswers() {
        // lv0: detect the answer
        if (guess_sudoku == null) {
            guess_sudoku = new Sudoku(this.toHash());
            int r = guess_sudoku.updateToAnswer(false);
            switch (r) {
                case -1: // bad sudoku, returns null
                    guessEnd();
                    return null;
                case 1: // detected, returns the answer
                    guess_todo = null;
                    return new Sudoku(guess_sudoku.toHash());
                default: // need to guess, init GuessTodo
                    guess_todo = new GuessTodo(guess_sudoku);
                    guess_sudoku.installHistory(99999);
                    break;
            }
        }

        // lv0.5: answer detected above, so no more answers
        if (guess_todo == null) {
            guessEnd();
            return null;
        }

        // lv1+: didnt detect answer, so need to guess
        while (true) {
            // get the cell and digit to guess
            int index = guess_todo.index();
            int digit = guess_todo.digit();
            // when no more guess-digit
            if (digit == -1) {
                // undo update guess-sudoku (did this when guessing the previous cell )
                boolean b = guess_sudoku.undo();
                if (b == false) { // when no update operation, means didnt guess any cell before, here is lv1
                    guessEnd();
                    return null;
                }
                // undo commit (did this when guessing the previous cell )
                Operation op = guess_sudoku.history.undo(); // get the previous guess-cell and guess-digit
                guess_sudoku.uncommit(op.index); // undo this commit
                // recover guess-todo to the previous level
                guess_todo = new GuessTodo(guess_sudoku);
                while (guess_todo.digit() != op.digit) {
                }
                continue;// go back to the previous level(cell)
            }

            // try one digit of current level(cell)
            Sudoku test = new Sudoku(guess_sudoku);
            test.commit(index, digit, false);
            switch (test.updateToAnswer(false)) {
                case 1:// found answer
                    return test;// returns answer
                case -1:// found error
                    continue;// try the next digit
                default:// nothing found
                    guess_sudoku.commit(index, digit, true);
                    guess_sudoku.updateToAnswer(true);
                    guess_todo = new GuessTodo(guess_sudoku);
                    continue;// guess the next cell
            }
        }
    }

    /**
     * 释放此系列函数占用的内存, 可用于手动释放
     */
    public void guessEnd() {
        guess_sudoku.uninstallHistory();
        guess_sudoku = null;
        guess_todo = null;
    }

    /**
     * 指定一个格子和数字, 寻找所有与此格子和数字冲突的格子索引
     * @param index 指定格子的索引
     * @param digit 指定的数字
     * @return 返回冲突格子的索引数组
     * @apiNote 通过检查同范围内朋友格子已提交的数字, 而非标记列表
     */
    public int[] getAllConflicts(int index, int digit) {
        // returns the index of conflict-cells (by checking committed digit)

        // get the index of all the conflict-cells
        int confs[] = new int[row_cnt + col_cnt + cellcnt_inblo - 3];
        int ci = 0;
        int mates[] = getAllMates(index);
        for (int mi : mates) {
            // when same digit
            if (cells[mi].getDigit() == digit) {
                confs[ci++] = mi;
            }
        }

        // return
        int retn[] = new int[ci];
        for (int ri = 0; ri < ci; ri++) {
            retn[ri] = confs[ri];
        }
        confs = null;
        return retn;
    }

    /**
     * 为数独对象安装一个历史纪录对象, 并启用历史纪录功能
     * @param max_cnt 指定最大可纪录的历史数量
     * @apiNote 如果已经有一个历史纪录, 则旧的会直接被丢弃!
     */
    public void installHistory(int max_cnt) {
        history = new History(max_cnt);
        enable_history = true;
    }

    /**
     * 卸载已安装的历史纪录对象, 并关闭历史纪录功能, 如果没有则此函数不做任何事情
     */
    public void uninstallHistory() {
        history = null;
        enable_history = false;
    }

    /**
     * 启用历史纪录功能, 如果无历史纪录对象, 则不起任何作用
     */
    public void enableHistory() {
        enable_history = true;
    }

    /**
     * 禁用历史纪录功能
     */
    public void disableHistory() {
        enable_history = false;
    }

    /**
     * 撤销数独操作, 支持的函数均以H结尾
     * @return 返回否, 如果未执行和执行失败
     * @apiNote 须数独对象带有并启用了历史纪录对象
     */
    public boolean undo() {
        return doHistory(true);
    }

    /**
     * 重做数独操作, 支持的函数均以H结尾
     * @return 返回否, 如果未执行和执行失败
     * @apiNote 须数独对象带有并启用了历史纪录对象
     */
    public boolean redo() {
        return doHistory(false);
    }

    /**
     * 获取历史纪录字串数组, 每个字串包含一条历史纪录
     * @param op_cnt 要获取的纪录数
     * @param max_redo_cnt 其中最多包含几条redo数
     * @return 返回多条纪录字串的数组
     * @apiNote 格式: [History] 操作代码 @操作数一(格子) :操作数二(数字)
     */
    public String[] getHistorys(int op_cnt, int max_redo_cnt) {
        if (history != null) {
            String strs[] = history.getStrings(op_cnt, max_redo_cnt);
            return strs;
        }
        return null;
    }

    //-- private

    /**
     * 加载数独, 从一个指定的数独哈希, 此哈希必须正确, 因为不进行任何额外检查, 多用于从备份的哈希恢复数独
     * @param correct_hash 要加载的数独哈希
     * @apiNote 由于不可知哈希是否被篡改, 同时为确保类外的操作皆安全, 所以仅类内可用
     * 
     */
    private Sudoku(char[] correct_hash) {
        this();
        loadCorrectHash(correct_hash);
    }

    /**
     * 实例化cell-array的每个元素
     */
    private void initCells() {
        cells = new Cell[cell_cnt];
        for (int i = 0; i < cell_cnt; i++) {
            cells[i] = new Cell();
        }
    }

    /**
    * 加载数独, 从一个指定的数独哈希, 此哈希必须正确, 因为不进行任何额外检查, 多用于从备份的哈希恢复数独
    * @param hash 要加载的数独哈希
    * @apiNote 由于不可知哈希是否被篡改, 同时为确保类外的操作皆安全, 所以仅类内可用
    */
    private void loadCorrectHash(char[] hash) {
        // !!! For internal use only: hash must from a correct sudoku, cos this func doesnt backup original sudoku and doesnt check digit conflicts form hash

        // reset cells
        initCells();

        // load cells from hash
        int index = 0;
        for (char c : hash) {
            int hnib = c >>> 4;
            commit(index++, hnib);
            int lnib = c & 0b1111;
            if (lnib != 0b1111) {
                commit(index++, lnib);
            }
        }
    }

    /**
     * 填充数独(即提交所有可被提交的格子)
     * @param USE_HISTORY 指定是否纪录历史, 如果可用
     * @return 返回此函数提交的格子数
     * @return 返回-1, 如果发现一个错误
     * @apiNote 出于性能和场景率考虑, 此函数被设计为直接修改数独, 无论函数成功与否, 如需防止数独被破坏, 请自行备份原始哈希并重载数独, 或启用历史纪录并失败时撤销操作
     */
    private int fill(boolean USE_HISTORY) {
        // detect the determinable digit of each cell and commit them
        // returns the count of detected
        // returns -1 when found a wrong
        //! careful: even if the func fails,  will not recover the sudoku

        Operation op = new Operation(Operation.ALLDETERM, 0, 0);
        boolean record_history = ((history != null) && (enable_history == true) && (USE_HISTORY == true));

        if (record_history == true) {
            history.newdo(op);
        }

        int cnt = 0;
        boolean found_new;
        // using `find...Marked` and `find...Range` instead of `detectOneCell(i)` to find all determinable digits
        // to find more digits or wrong quickly, using `find...Marked` first, cos it is more quickly than `find...Range`
        do {
            found_new = false;
            for (int i = 0; i < cell_cnt; i++) {
                // skip the committed cells
                int cur_d = cells[i].getDigit();
                if (cur_d != 0) {
                    continue;
                }
                // find out the digit
                int d = detectBySelf(i);
                if (d == -1) {
                    if (record_history == true) {
                        history.newdo(op);
                    }
                    return -1;
                }
                if (d == 0) {
                    continue;
                }
                // when found
                commit(i, d, USE_HISTORY);
                cnt++;
                found_new = true;
            }
        } while (found_new == true);

        // to find all determinable digits, using `fidn...Range` is necessary
        do {
            found_new = false;
            for (int i = 0; i < cell_cnt; i++) {
                // skip the committed cells
                int cur_d = cells[i].getDigit();
                if (cur_d != 0) {
                    continue;
                }
                // find out the digit
                int d = detectByMates(i);
                if (d == -1) {
                    if (record_history == true) {
                        history.newdo(op);
                    }
                    return -1;
                }
                if (d == 0) {
                    continue;
                }
                // when found
                commit(i, d, USE_HISTORY);
                cnt++;
                found_new = true;
            }
        } while (found_new == true);

        if (record_history == true) {
            history.newdo(op);
        }
        return cnt;
    }

    /**
     * 填充数独, 并检查是否为答案
     * @param USE_HISTORY 指定是否纪录历史, 如果可用
     * @return 返回1, 如果最后的数独是个答案
     * @return 返回0, 如果数独需要进一步解答(guess), 即存在不能确定的格子
     * @return 返回-1, 如果在数独中发现错误
     */
    private int updateToAnswer(boolean USE_HISTORY) {
        // detect all determinable cells and committed them
        // returns -1 when a bad sudoku
        // returns 1 when a solved sudoku
        // returns 0 when a pending sudoku (need to guess)
        //! careful: even if the func fails,  will not recover the sudoku

        int cnt = fill(USE_HISTORY);
        if ((cnt == -1)) {
            // sudoku is bad
            return -1;
        }

        boolean over = isFinished();
        if (over == true) {
            // all cells were committed
            return 1;
        } else {
            // some cell didnt find determinable digit
            return 0;
        }
    }

    /**
     * 取某范围内的其他格子的索引数组
     * @param ci curretnIndex, 当前格子的在数独中的索引
     * @param range 指定是哪个范围, 是行还是列还是块还是块(且不同行、不同列)
     * @return 返回此范围内朋友格子在数独中的索引数组
     */
    private int[] getRangeMates(int ci, int range) {
        int mates[]; // the array of the index of the mate-cells
        int x = ci % col_cnt; // the x-coord on sudoku of the curr-cell
        int y = ci / col_cnt; // the y-coord on sudoku of the curr-cell
        int fx; // the x-coord on sudoku of the first cell in range
        int fy; // the y-coord on sudoku of the first cell in range
        int fi; // the index of the first cell in range
        int ri; // the index of the mate-cell in sudoku
        int mi; // the index in array

        switch (range) {
            case ROW: // ROW
                fi = y * col_cnt + 0;
                mi = 0;
                mates = new int[col_cnt - 1];
                ri = fi - 1;
                for (int i = 0; i < col_cnt; i++) {
                    ri++;
                    if (ri == ci) {
                        continue;
                    }
                    mates[mi++] = ri;
                }
                break;
            case COL: // COL
                fi = x + 0;
                mi = 0;
                mates = new int[row_cnt - 1];
                ri = fi - col_cnt;
                for (int i = 0; i < row_cnt; i++) {
                    ri += col_cnt;
                    if (ri == ci) {
                        continue;
                    }
                    mates[mi++] = ri;
                }
                break;
            case BLO: // BLO
                fx = x / colcnt_inblo * colcnt_inblo;
                fy = y / rowcnt_inblo * rowcnt_inblo;
                fi = fx + fy * col_cnt;
                mi = 0;
                mates = new int[cellcnt_inblo - 1];
                ri = fi - 1;
                for (int by = 0; by < rowcnt_inblo; by++) {
                    for (int bx = 0; bx < colcnt_inblo; bx++) {
                        ri++;
                        if (ri == ci) {
                            continue;
                        }
                        mates[mi++] = ri;
                    }
                    ri -= colcnt_inblo;
                    ri += col_cnt;
                }
                break;
            case BLO_ONLY: // BLO
                fx = x / colcnt_inblo * colcnt_inblo;
                fy = y / rowcnt_inblo * rowcnt_inblo;
                fi = fx + fy * col_cnt;
                mi = 0;
                mates = new int[cellcnt_inblo - 1 - 4];
                ri = fi - 1;
                for (int by = 0; by < rowcnt_inblo; by++) {
                    for (int bx = 0; bx < colcnt_inblo; bx++) {
                        ri++;
                        int rx = ri % col_cnt; // the x-coord on sudoku of the mate-cell
                        int ry = ri / col_cnt; // the y-coord on sudoku of the mate-cell
                        if ((rx == x) || (ry == y)) { // skip the blo-mate-cell in the same row-range or col-range
                            continue;
                        }
                        mates[mi++] = ri;
                    }
                    ri -= colcnt_inblo;
                    ri += col_cnt;
                }
                break;
            default:
                mates = new int[] { -1 };
                return mates;
        }

        return mates;
    }

    /**
     * 为当前格子的所有同范围的朋友格子标记当前数字不可用
     * @param index 当前格子的索引
     * @param digit 当前格子的数字
     */
    private void markAllMates(int index, int digit) {
        int mates[] = getAllMates(index);
        for (int mi : mates) {
            cells[mi].mark(digit);
        }
    }

    /**
     * 检查指定的数字是否在指定格子的指定范围中已被提交
     * @param digit 要检查的数字
     * @param ci 指定的格子
     * @param range 指定的范围
     * @return 返回真, 如果指定的数字在指定的范围呢
     */
    private boolean wasDigitInRange(int digit, int ci, int range) {
        int mates[] = getRangeMates(ci, range);
        for (int mi : mates) {
            if (cells[mi].getDigit() == digit) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取两个格子所在的相同范围
     * @param c1 格子1
     * @param c2 格子2
     * @return 返回相同的范围的int代码
     */
    private int getSameRange(int c1, int c2) {
        int in_range = 0;
        int x1 = c1 % col_cnt; // the x-coord on sudoku of the cell-1
        int y1 = c1 / col_cnt; // the y-coord on sudoku of the cell-1
        int x2 = c2 % col_cnt; // the x-coord on sudoku of the cell-2
        int y2 = c2 / col_cnt; // the y-coord on sudoku of the cell-2
        if (y1 == y2) {
            in_range += 0b1;
        }
        if (x1 == x2) {
            in_range += 0b10;
        }
        int bxB = x1 / colcnt_inblo * colcnt_inblo; // the left x-coord of the blo-range where cell-1 in
        int bxE = bxB + 3; // the right...
        int byB = y1 / rowcnt_inblo * rowcnt_inblo; // the top y-coord of the blo-range where cell-1 in
        int byE = byB + 3; // the bottom...
        if ((bxB <= x2) && (x2 < bxE) && (byB <= y2) && (y2 < byE)) {
            in_range += 0b100;
        }

        switch (in_range) {
            case 0:
            default:
                return NOP;
            case 0b1:
                return ROW;
            case 0b10:
                return COL;
            case 0b100:
                return BLO_ONLY;
            case 0b101:
                return BLO_ROW;
            case 0b110:
                return BLO_COL;
        }
    }

    /**
     * 为指定格子的所有朋友格子们取消标记指定的数字
     * @param index 指定的格子
     * @param digit 指定的数字
     * @apiNote 如果朋友格子的指定数字也被他自己的其他朋友格子标记过, 则不会取消, 也因此此函数计算偏多, 时间O(240)远超重载一个数独O(81), 优点在于空间O(1), 历史纪录类依赖此函数
     */
    private void unmarkAllMates(int index, int digit) {
        int mates[] = getAllMates(index);
        for (int mi : mates) {
            if (getCellDigit(mi) != 0) {
                continue;
            }
            int range = getSameRange(mi, index);
            /* switch (range) {
                case ROW:
                    if (true == wasDigitInRange(digit, mi, COL)) {
                        continue;
                    }
                    if (true == wasDigitInRange(digit, mi, BLO)) {
                        continue;
                    }
                    break;
                case COL:
                    if (true == wasDigitInRange(digit, mi, ROW)) {
                        continue;
                    }
                    if (true == wasDigitInRange(digit, mi, BLO)) {
                        continue;
                    }
                    break;
                case BLO_ONLY:
                case BLO_ROW:
                case BLO_COL:
                    if (true == wasDigitInRange(digit, mi, ROW)) {
                        continue;
                    }
                    if (true == wasDigitInRange(digit, mi, COL)) {
                        continue;
                    }
                    break;
                default:
                    break;
            } */
            if (range != ROW) { // check the cells in the same row of the mate-cell, if they disabled the digit of the mate-cell
                if (true == wasDigitInRange(digit, mi, ROW)) {
                    continue;
                }
            }
            if (range != COL) { // check the cells in the same col of the mate-cell, if they disabled the digit of the mate-cell
                if (true == wasDigitInRange(digit, mi, COL)) {
                    continue;
                }
            }
            if ((range != BLO_ONLY) && (range != BLO_ROW) && ((range != BLO_COL))) { // check the cells in the same blo of the mate-cell, if they disabled the digit of the mate-cell
                if (true == wasDigitInRange(digit, mi, BLO)) {
                    continue;
                }
            }
            // no mates of the mate-cell have committed this digit, means this digit was marked only by curr-cell, so unmark is safe.
            cells[mi].unmark(digit);
        }
    }

    /**
     * 检查这个格子自身的标记列表, 查看是否存在唯一一个可以使用的数字
     * @param index 要检查的格子的索引
     * @return 返回唯一可用的数字, 如果存在
     * @return 返回0, 如果不只一个数字可用
     * @return 返回-1, 如果所有数字都不可用
     */
    private int detectBySelf(int index) {
        // check the digits-mark of this cell and find out the sole probable digit
        // returns this digit when found
        // returns 0 when more than one probable digits found
        // returns -1 when a wrong (no probable digit in this cell)

        int digit = cells[index].ascertain();
        if (digit == -1) {
            // all digits of this cell is disabled
            return -1;
        }
        if (digit == 0) {
            // did not found
            return 0;
        }

        return digit;
    }

    /**
     * 指定一个格子, 检查与其同一范围的每个格子的标记列表, 是否存在一个数字在所有朋友格子中都不可用
     *
     * @param index 要检查的格子的索引
     * @return 返回第一个符合的数字
     * @return 返回0, 如果不存在这样的数字
     * @return 返回-1, 如果发现一个错误(这个数字在指定的格子中也不可用, 即同一个范围内没有格子可用此数字)
     */
    private int detectByMates(int index) {
        // find out the digit which is disabled in all range-cells by their digits-marks, that means only curr-cell can enable it
        // returns the digit when found
        // returns 0 when didnt find
        // returns -1 when a wrong (this digit was also disabled by curr-cell)

        for (int d = 1; d <= 9; d++) {
            for (int r = 0; r < 3; r++) {
                boolean MATE_AVAILABLE = false;
                int range_mates[] = getRangeMates(index, r);
                for (int mi : range_mates) {
                    if (cells[mi].isMarked(d) == true) {
                        continue;
                    }
                    MATE_AVAILABLE = true;
                    break;
                }
                // when the digit is also available in a range-cell
                if (MATE_AVAILABLE == true) {
                    continue; // check the next range
                }
                // when all mate-cells in this range cannt commit this digit
                if (cells[index].isMarked(d) == true) { // this digit was also disabled in the curr-cell
                    return -1;
                }
                return d;
            }
        }

        // none determinable digit found
        return 0;
    }

    /**
     * 执行历史纪录撤销或重做
     * @param UNDO 指定是撤销还是重做
     * @return 返回否, 如果未执行(执行失败), 可能原因是 1.新操作后redo 2.无纪录时undo
     */
    private boolean doHistory(boolean UNDO) {
        Operation op;
        boolean ALLDETERM = false;
        do {
            // get operation
            if (UNDO == true) {
                op = history.undo();
            } else {
                op = history.redo();
            }

            // return when no operation
            if (op == null) {
                return false;
            }

            // when operation is commitAllDeterminables()
            if (op.code == Operation.ALLDETERM) {
                if (ALLDETERM == false) {
                    ALLDETERM = true;
                    continue;
                } else {
                    break;
                }
            }

            // do operation(s)
            switch (op.code) {
                case Operation.COMMIT:
                    if (UNDO == true) {
                        uncommit(op.index);
                    } else {
                        commit(op.index, op.digit);
                    }
                    break;
                case Operation.UNCOMMIT:
                    if (UNDO == true) {
                        commit(op.index, op.digit);
                    } else {
                        uncommit(op.index);
                    }
                    break;
                default:
                    break;
            }
        } while (ALLDETERM == true);
        return true;
    }

    //-- Guess

    /**
     * 猜测预备类, 用于存储准备猜测的格子和其要被猜测的数字们
     */
    class GuessTodo {
        private int index; // the index of the cell to guess
        private int digits[]; // array of digits to guess
        private int di; // the index of the curretn guessing digit

        //-- public

        /**
         * 给定一个数独, 寻找第一个未提交的格子, 并纪录此格子所有未被禁用的数字
         * @param sudoku_to_guess 给定的数独
         */
        public GuessTodo(Sudoku sudoku_to_guess) {
            di = 0;

            // get the first uncommitted cell to guess
            int index_ = 0;
            for (Cell cell : sudoku_to_guess.cells) {
                if (cell.getDigit() == 0) {
                    break;
                }
                index_++;
            }
            if (index_ == cell_cnt) {
                //! careful: dont use when a finished sudoku
            }

            // get all probable digits of this cell
            int digits_[] = new int[9];
            int digit_cnt = 0;
            for (int d = 1; d <= 9; d++) {
                if (sudoku_to_guess.cells[index_].isMarked(d) == false) {
                    // save this probable digit
                    digits_[digit_cnt++] = d;
                }
            }
            if (digit_cnt == 0) {
                //! careful: dont use when a bad sudoku
            }

            // save local var `digits`
            digits = new int[digit_cnt];
            for (int i = 0; i < digit_cnt; i++) {
                digits[i] = digits_[i];
            }
            digits_ = null;
            // save local var `index`
            index = index_;
        }

        /**
         * 调试当前对象用
         * @return 返回一个字串, 字串包含了当前对象的所有信息
         * @apiNote 格式: [GuessTodo] d当前数字 @当前格子 | 所有待猜数字
         */
        public String debug() {
            int cur_d = (di < digits.length) ? digits[di] : -1;
            String s = "[GuessTodo] d" + cur_d + " @" + index + " |";
            for (int d : digits) {
                s += " " + d;
            }
            return s;
        }

        /**
         * 取当前待猜数字, 并将索引指向下个待猜数字
         * @return 返回当前数字
         * @return 返回-1, 如果没有更多数字了
         */
        public int digit() {
            // returns the current probable digit
            // returns -1 when out of bound

            if (di == digits.length) { // out of bounds
                return -1;
            }

            return digits[di++];
        }

        /**
         * 取当前待猜格子
         * @return 返回待猜格子的索引
         */
        public int index() {
            return this.index;
        }
    }

    /**
     * 输出调试信息, 当前的guess-sudoku和guess-todo以及历史纪录
     */
    private void debugGuess() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");

        if (guess_todo != null) {
            System.out.println("[guess-todo]\n" + guess_todo.debug());
        }

        if (guess_sudoku != null) {
            System.out.println("[guess-sudoku]\n" + guess_sudoku.toPrints());
            System.out.println("[history]\n");
            String hs[] = guess_sudoku.getHistorys(30, 1);
            for (String h : hs) {
                System.out.println(h);
            }
        }

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    //-- Cell

    /**
     * 格子类, 存储了数独中一个格子的信息, 包含已提交的数字和每个数字的可用情况
     */
    private class Cell {
        //-- private

        /**
         * 低4位: 这个格子已提交的数字, 0为未提交
         * 紧接的9位: 这个格子的标记列表, 由低到高分别表示了数字1~9的可用状态, 1为不可用(已被其他格子占用)
         * 高3位: 保留(未使用)
         */
        private short data; // zzzy_yyyy_yyyy_xxxx z:reserved y:dead-digit-marks x:committed-digit
        private static final int D_WIDTH = 4; // bit-width of the committed-digit
        private static final int M_WIDTH = 9; // bit-width of the dead-digit-marks

        //-- public

        /**
         * 实例化一个可以是任意数字、未提交的格子
         */
        public Cell() {
            data = 0; // 全bit皆0, 即未提交数字, 可以是任意数字
        }

        /**
         * 从低4位中取已提交的数字
         * @return 返回已提交的数字
         * @return 返回0, 如果还没有数字被提交
         */
        public int getDigit() {
            int bm = (1 << D_WIDTH) - 1;
            return data & bm;
        }

        /**
         * 提交指定数字
         * @param d 要提交的数字, 如果为0, 则此函数不做任何事情直接返回
         * @apiNote 不进行其他任何检查, 直接覆写committed-digit和mark-list
         */
        public void setDigit(int d) {
            if (d == 0) {
                return;
            }

            // set dead-mark
            int bm = 1 << D_WIDTH << d >>> 1;
            data = (short) ~bm;

            // set active digit
            bm = (1 << D_WIDTH) - 1;
            bm = ~bm;
            data &= bm;
            data += d;

            return;
        }

        /**
         * 检查这个格子其实 是否已经可以被确定(即仅剩一个可用的数字)
         * @return 返回唯一可用的数字
         * @return 返回0, 如果可用的数字不止一个
         * @return 返回-1, 如果出现错误(所有数字都不可用)
         */
        public int ascertain() {
            // find out the sole enabled digit and returns it
            // returns -1 when all digits were disabled
            // returns 0 when didnt find

            int bm = 1 << D_WIDTH >>> 1;
            boolean marked;
            int digit = 0;
            for (int d = 1; d <= M_WIDTH; d++) {
                marked = (((bm << d) & data) == 1);
                if (marked == false) {
                    // if the digit is not zero, means any enable digit has beed found before
                    if (digit != 0) {
                        // 0: more than one enable digit
                        return 0;
                    }
                    digit = d; // save the first enable digit
                }
            }

            //  -1: no digit is enable! means bad cell
            if (digit == 0) {
                return -1;
            }

            // return the only enable digit
            return digit;
        }

        /**
         * 检查某个数字是否可用
         * @param d 要检查的数字
         * @return 返回真, 如果可用
         */
        public boolean isMarked(int d) {
            boolean marked = false;
            int bm = 1 << D_WIDTH << d >>> 1;
            if ((data & bm) == bm) {
                marked = true;
            }
            return marked;
        }

        /**
         * 标记一个数字为不可用
         * @param d 要标记的数字
         * @apiNote 单纯将这个数字对应的标记位置1, 不进行其他任何操作
         */
        public void mark(int d) {
            d -= 1;
            int bm = 1 << D_WIDTH << d;
            data |= bm;
        }

        /**
         * 标记一个数字为可用状态
         * @param d 要标记的数字
         * @apiNote 单纯将这个数字对应的标记位置0, 不进行其他任何操作
         */
        public void unmark(int d) {
            d -= 1;
            int bm = 1 << D_WIDTH << d;
            data &= ~bm;
        }

        /**
         * 获取此格子每个数字的可用情况, 0为可用, 1为禁用(已被占用)
         * @return 返回int数组, 依次为数字1~9的可用情况
         */
        public int[] debugMarks() {
            int marks[] = new int[9];
            for (int i = 0; i < 9; i++) {
                marks[i] = ((data >>> D_WIDTH >>> i) & 0b1);
            }
            return marks;
        }
    }

    //-- History

    /**
     * 操作类, 包含操作的详细信息, 作为与历史纪录类交互的方便接口
     */
    private class Operation {
        //-- All Public

        public static final int COMMIT = 0;
        public static final int UNCOMMIT = 1;
        public static final int ALLDETERM = 2;
        public int code;
        public int index;
        public int digit;

        /**
         * 实例一个操作对象
         * @param code 操作代码
         * @param i 操作数一之格子索引
         * @param d 操作数二之数字
         */
        public Operation(int code, int i, int d) {
            this.index = i;
            this.digit = d;
            this.code = code;
        }
    }

    /**
     * 历史纪录类, 用于纪录操作信息
     */
    private class History {
        private Vector<Short> ops; // datas of history operations
        private int op_index; // current operation index
        private int max_op_cnt; // max op cnt limit
        private int undo_cnt; // already undid op cnt

        //-- public

        /**
         * 拷贝构造, 实例一个旧的历史纪录副本
         * @param from 要拷贝的源
         */
        public History(History from) {
            this.max_op_cnt = from.max_op_cnt;
            this.op_index = from.op_index;
            this.undo_cnt = from.undo_cnt;

            this.ops = new Vector<>();
            for (int i = 0; i < from.ops.size(); i++) {
                ops.add(from.ops.get(i));
            }
        }

        /**
         * 实例一个指定最大记录数的新历史纪录
         * @param max_op_cnt 指定最大记录数量
         */
        public History(int max_op_cnt) {
            ops = new Vector<>();
            op_index = -1;
            this.max_op_cnt = max_op_cnt;
            undo_cnt = 0;
        }

        /**
         * 获取历史纪录字串数组, 每个字串包含一条历史纪录
         * @param count 要获取的纪录数
         * @param max_redo_cnt 其中最多包含几条redo数
         * @return 返回多条纪录字串的数组
         * @apiNote 格式: [History] 操作代码 @操作数一(格子) :操作数二(数字)
         */
        public String[] getStrings(int count, int max_redo_cnt) {
            int redo_cnt = (undo_cnt > max_redo_cnt) ? max_redo_cnt : undo_cnt;
            count -= redo_cnt;
            int cnt;
            if (ops.size() == max_op_cnt) {
                cnt = max_op_cnt;
            } else {
                cnt = op_index + 1;
            }
            cnt = (cnt < count) ? cnt : count;
            cnt += redo_cnt;

            String strs[] = new String[cnt];

            int opi = op_index + redo_cnt;
            for (int i = 0; i < cnt; i++) {
                short data = ops.get(opi--);
                Operation op = toOperation(data);
                strs[i] = ("[History] " + op.code + " @" + op.index + " :" + op.digit);
            }

            return strs;
        }

        /**
         * 纪录一个新的操作
         * @param op 要纪录的操作
         */
        public void newdo(Operation op) {
            short op_data = getData(op);

            op_index++;
            if ((ops.size() < max_op_cnt) && (op_index == ops.size())) {
                ops.add(op_data);
            }
            if (op_index == max_op_cnt) {
                op_index = 0;
            }
            ops.set(op_index, op_data);

            undo_cnt = 0;
        }

        /**
         * 模拟撤销, 即获取当前操作, 并纪录指针移到前一个操作
         * @return 返回包含当前操作信息的Operation对象
         * @return 返回null, 如果还没有任何纪录
         */
        public Operation undo() {
            if (op_index == -1) {
                if (ops.size() == max_op_cnt) {
                    op_index = max_op_cnt - 1;
                } else {
                    return null;
                }
            }

            short op_data = ops.get(op_index);
            Operation op = toOperation(op_data);

            op_index--;
            undo_cnt++;
            return op;
        }

        /**
         * 模拟重做, 即将纪录指针移到下个操作, 并获取此操作
         * @return 返回下个操作的信息
         * @return 返回null, 如果没有下个操作
         */
        public Operation redo() {
            if (undo_cnt == 0) {
                return null;
            }
            undo_cnt--;

            op_index++;
            if (op_index == max_op_cnt) {
                op_index = 0;
            }
            short op_data = ops.get(op_index);

            Operation op = toOperation(op_data);
            return op;
        }

        //-- prevate
        /**
         * 将Operation对象的数据转为short数据返回, 用于存储
         * @param op 要从中提取数据的Operation对象
         * @return 返回等值的short数据
         */
        private short getData(Operation op) {
            short op_data = 0; // rr_ccc_iiii_iii_dddd | reserved code index digit
            op_data += op.digit;
            op_data += (op.index << 4);
            op_data += (op.code << 11);
            return op_data;
        }

        /**
         * 将short类型的操作数据转为Operation对象返回, 用于为外部提供方便接口
         * @param op_data 要提取操作信息的short数据
         * @return 返回等值的Operation对象
         */
        private Operation toOperation(short op_data) {
            int code = (op_data >>> 11) & 0b111;
            int index = (op_data >>> 4) & 0b1111_111;
            int digit = op_data & 0b1111;
            Operation op = new Operation(code, index, digit);
            return op;
        }
    }

}