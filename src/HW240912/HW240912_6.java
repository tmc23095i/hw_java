package HW240912;

import java.io.IOException;
import java.util.Stack;

public class HW240912_6 {

    public static void main_() throws IOException {
        // strings of sudoku
        String str0 = "000000000 000000000 000000000 - 000000000 000000000 000000000 - 000000000 000000000 000000000";
        String str1 = "000 000 200000 000 480 230 040 006 300 700 000 020 904 050 009 031 000 007 000 090 000 080 005 000 200 060";
        String str2 = "003 500 004 780 000 000000 806 070000 000 000400 705 108620 003 049070 000 020090 000 600000 209 010";
        String str3 = "600000278000700005400005003000602000002034080504000609308006400000001007000809000";

        // new a sudoku obj
        Sudoku sudoku = new Sudoku();
        boolean b = sudoku.loadSudoku(str0);
        if (b == false) {
            System.out.println(">> load: failed!");
        } else {
            System.out.println(">> load: " + b);
        }

        // get sudoku string
        String str = sudoku.toString();
        System.out.println(">> toString: " + str);

        // get print text of sudoku
        String panel = sudoku.toPrints();
        System.out.println(">> toPrints: \n" + panel);

        // get answer of sudoku
        Sudoku answer = sudoku.detectAnser();
        System.out.println(">> detectAnswer: " + answer);

        // commit all determinable cells
        sudoku.commitDeterminables();
        panel = sudoku.toPrints();
        System.out.println(">> commitDeterminables: \n" + panel);

        int ai = 0;
        System.out.println(">> Any key to get the next answer, or Q to finished.");
        do {
            // get all answers one by one
            answer = sudoku.beginAnswerTry();
            if (answer == null) {
                System.out.println(">> beginAnswerTry: #OVER");
                break;
            } else {
                System.out.println(">> beginAnswerTry: #" + ai++ + "\n" + answer.toPrints());
            }
            // finish
            int key = System.in.read();
            if (key == 'Q') {
                System.out.println(">> Finished by user.");
                break;
            }
        } while (true);
        sudoku.endAnswerTry();

    }

}

class Sudoku {
    //-- private
    private Cell[] cells;
    private final int col_cnt;
    private final int row_cnt;
    private final int cell_cnt;
    private final float blo_scale;
    private final int blo_colcnt;
    private final int blo_rowcnt;
    private final int blo_cellcnt; // the count of cells in one blo
    private static final int ROW = 0;
    private static final int COL = 1;
    private static final int BLO = 2;
    private Locals$AnswerGuess locals;
    private final boolean DEBUG = false;

    //-- public

    public Sudoku() {
        // the size of thesudoku
        row_cnt = col_cnt = 9;
        blo_scale = 1f / 3;
        // calc cnount
        cell_cnt = row_cnt * col_cnt;
        blo_colcnt = (int) (col_cnt * blo_scale);
        blo_rowcnt = (int) (row_cnt * blo_scale);
        blo_cellcnt = (int) (cell_cnt * blo_scale * blo_scale);

        // init cells: array-allocation and index-assignment
        initCells();
    }

    private void initCells() {
        cells = new Cell[cell_cnt];
        for (int i = 0; i < cell_cnt; i++) {
            cells[i] = new Cell();
        }
    }

    public Sudoku(int rowCount, int colCount, int bloScale) {
        // the size of thesudoku
        row_cnt = rowCount;
        col_cnt = colCount;
        blo_scale = bloScale;

        // calc cnount
        cell_cnt = row_cnt * col_cnt;
        blo_colcnt = (int) (col_cnt * blo_scale);
        blo_rowcnt = (int) (row_cnt * blo_scale);
        blo_cellcnt = (int) (cell_cnt * blo_scale);

        // init cells: array-allocation and index-assignment
        initCells();
    }

    public Sudoku(Sudoku s) {
        this();

        char hash[] = s.toHash();
        loadSudoku(hash);
    }

    public boolean loadSudoku(String s) {
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
                // recover original suduko and return
                loadCorrectHash(backup);
                return false;
            }

            if (index == 81) {
                break;
            }
        }

        return true;
    }

    public boolean loadSudoku(char[] hash) {
        char backup[] = toHash();
        this.initCells();

        int index = 0;
        for (char c : hash) {
            int hnib = c >>> 4;
            int lnib = c & 0b1111;
            boolean b = commit(index++, hnib);
            if (b == false) {
                loadCorrectHash(backup);
                return false;
            }
            if (lnib != 0b1111) {
                b = commit(index++, lnib);
                if (b == false) {
                    loadCorrectHash(backup);
                    return false;
                }
            }
        }

        return true;
    }

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

    public String toString() {
        String str = "";
        for (Cell cell : cells) {
            str += cell.getDigit();
        }
        str += "\0";
        return str;
    }

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

    public int[] getMates(int ci) {
        int mates[] = new int[row_cnt + col_cnt + blo_cellcnt - 3];
        int mi = 0;
        int range[];

        range = rangeMates(ci, ROW);
        for (int i : range) {
            mates[mi++] = i;
        }
        range = rangeMates(ci, COL);
        for (int i : range) {
            mates[mi++] = i;
        }
        range = rangeMates(ci, BLO);
        for (int i : range) {
            mates[mi++] = i;
        }

        return mates;
    }

    public boolean isFinished() {
        for (Cell cell : cells) {
            if (cell.getDigit() == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean commit(int index, int digit) {
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
        markMates(index, digit);
        return true;
    }

    public int detectCell(int index) {
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

    public Sudoku detectAnser() {
        // find out the answer of curr-sudoku, and returns the answer-sudoku
        // returns null when did not or can not find the answer

        Sudoku test = new Sudoku(this);
        int retn = test.updateToAnswer();
        if (retn == 1) {
            return test;
        }
        return null;
    }

    public int[] conflictCells(int index, int digit) {
        // returns the index of conflict-cells (by checking committed digit)

        // get the index of all the conflict-cells
        int confs[] = new int[row_cnt + col_cnt + blo_cellcnt - 3];
        int ci = 0;
        int mates[] = getMates(index);
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

    public int commitDeterminables() {
        // detect the determinable digit of each cell and commit them
        // returns the count of detected
        // returns -1 when found a wrong
        //! careful: even if the func fails,  will not recover the sudoku

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
                    return -1;
                }
                if (d == 0) {
                    continue;
                }
                // when found
                commit(i, d);
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
                    return -1;
                }
                if (d == 0) {
                    continue;
                }
                // when found
                commit(i, d);
                cnt++;
                found_new = true;
            }
        } while (found_new == true);

        return cnt;
    }

    //-- private

    private int updateToAnswer() {
        // detect all determinable cells and committed them
        // returns -1 when a bad sudoku
        // returns 1 when a solved sudoku
        // returns 0 when a pending sudoku (need to guess)
        //! careful: even if the func fails,  will not recover the sudoku

        int cnt = commitDeterminables();
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

    private Sudoku(char[] correct_hash) {
        this();
        loadCorrectHash(correct_hash);
    }

    private int[] rangeMates(int ci, int range) {
        int mates[]; // the array of the index of the mate-cells
        int x = ci % col_cnt; // the x-coord of the curr-cell
        int y = ci / col_cnt; // the y-coord of the curr-cell
        int fi; // the index of the first cell in range
        int ri;// the index of the mate-cell
        int mi;// the index in array

        switch (range) {
            case ROW: //ROW
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
            case COL: //COL
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
            case BLO: //BLO
                int fx = x / blo_colcnt * blo_colcnt; // the x-coord of the first cell in blo
                int fy = y / blo_rowcnt * blo_rowcnt; // the y-coord of the first cell in blo
                fi = fx + fy * blo_cellcnt;
                mi = 0;
                mates = new int[blo_cellcnt - 1];
                ri = fi - 1;
                for (int by = 0; by < blo_rowcnt; by++) {
                    for (int bx = 0; bx < blo_colcnt; bx++) {
                        ri++;
                        if (ri == ci) {
                            continue;
                        }
                        mates[mi++] = ri;
                    }
                    ri -= blo_colcnt;
                    ri += col_cnt;
                }
                break;
            default:
                mates = new int[] { -1 };
                return mates;
        }

        return mates;
    }

    private void markMates(int index, int digit) {
        int mates[] = getMates(index);
        for (int mi : mates) {
            cells[mi].mark(digit);
        }
    }

    private int detectBySelf(int index) {
        // check the digits-mark of this cell and find out the sole probable digit
        // returns this digit when found
        // returns 0 when more than one probable digits found
        // returns -1 when a wrong (no probable digit in this cell)

        int digit = cells[index].confirmDigit();
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

    private int detectByMates(int index) {
        // find out the digit which is disabled in all range-cells by their digits-marks, that means only curr-cell can enable it
        // returns the digit when found
        // returns 0 when didnt find
        // returns -1 when a wrong (this digit was also disabled by cerr-cell)

        int digit;
        for (int d = 1; d <= 9; d++) {
            for (int r = 0; r < 3; r++) {
                digit = d;
                int mates[] = rangeMates(index, r);
                for (int mi : mates) {
                    if (cells[mi].isMarked(d) == false) {
                        digit = 0;
                        break;
                    }
                }
                // this digit is also enabled in a range-cell, so skip to the next range
                if (digit == 0) {
                    continue;
                }
                // the determinable digit found
                if (cells[index].isMarked(digit) == true) {
                    return -1;
                }
                return digit;
            }
        }

        // none determinable digit found
        return 0;
    }

    private class Cell {
        //-- private

        private short data; // zzzy_yyyy_yyyy_xxxx y:dead-digit-mark x:active-digit
        private final int D_WIDTH = 4; // active digit bit count
        private final int M_WIDTH = 9; // dead digit mark bit count

        //-- public

        public Cell() {
            data = 0;
        }

        public int getDigit() {
            int bm = (1 << D_WIDTH) - 1;
            return data & bm;
        }

        public void setDigit(int d) {
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

        public int confirmDigit() {
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

        public boolean isMarked(int d) {
            boolean marked = false;
            int bm = 1 << D_WIDTH << d >>> 1;
            if ((data & bm) == bm) {
                marked = true;
            }
            return marked;
        }

        public void mark(int d) {
            int bm = 1 << D_WIDTH << d >>> 1;
            data |= bm;
        }

        public void unmark(int d) {
            int bm = 1 << D_WIDTH << d >>> 1;
            data &= ~bm;
        }

    }

    //-- guess answer
    class Locals$AnswerGuess {
        private Guess guess;
        private Stack<Guess> guesses;

        class Guess {
            char hash[]; // (for backup) the hash of the sudoku to to guess
            int index; // the index of the cell to guess
            int digits[]; // all digits to guess
            int di; // the index of the curretn guessing digit

            private Guess() {
                di = 0;
            }

            public Guess(Sudoku sudoku_to_guess) {
                // init the local vars with the specified sudoku

                this();
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
                for (int d = 1; d < 9; d++) {
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
                // save local var `hash`
                hash = sudoku_to_guess.toHash();
                // save local var `index`
                index = index_;
            }

        }

        public Locals$AnswerGuess() {
            guess = null;
            guesses = new Stack<Guess>();
        }

        public void initLevel(Sudoku sudoku) {
            // create a new local and use it

            guesses.push(guess);
            guess = new Guess(sudoku);
        }

        public void gobackLevel() {
            // discard the curretn local of change to the previous local

            if (guesses.isEmpty() == false) {
                guess = guesses.pop();
            }
        }

        public boolean isZeroLevel() {
            return guess == null;
        }

        public Sudoku sudoku() {
            // returns a new Sudoku obj from hash in current local

            Sudoku s = new Sudoku();
            s.loadCorrectHash(guess.hash);
            return s;
        }

        public int index() {
            // returns the current local var `index`

            return guess.index;
        }

        public String printDigits() {
            // (for debugging) print the current local var `digits` string

            if (guess == null) {
                return "null";
            }
            String s = "";
            if (guess.di < 0 || guess.di >= guess.digits.length) {
                s = s + "[" + guess.di + "]";
            } else {
                s += guess.digits[guess.di];
            }
            s += " @ ";
            s += guess.index;
            s += " | ";
            for (int d : guess.digits) {
                s += d;
                s += " ";
            }
            return s;
        }

        public int nextDigit() {
            // returns the next probable digit in current local
            // returns -1 when out of bound

            if (guess.di == guess.digits.length) {
                return -1;
            }
            if (guess.di < 0) {
                guess.di = 0;
            }
            return guess.digits[guess.di++];
        }

        public int prevDigit() {
            // returns the previous probable digit in current local
            // returns -1 when out of bound

            if (guess.di == 0) {
                return -1;
            }
            return guess.digits[--guess.di];

        }
    }

    private void LOGOUT(String s) {
        if (!DEBUG) {
            return;
        }
        System.out.println(s);
    }

    private void LOGOUT(Locals$AnswerGuess s) {
        if (!DEBUG) {
            return;
        }
        String str = s.printDigits();
        System.out.println(str);
    }

    public Sudoku beginAnswerTry() {

        if (locals == null) { // lv0: init-call
            // update the sudoku and check answer
            Sudoku try_sudoku = new Sudoku(this);
            int r = try_sudoku.updateToAnswer();
            LOGOUT("[update(null)]: " + r);
            switch (r) {
                case -1:
                    return null;
                case 1:
                    return try_sudoku;
                default:
                    // unsure answer, need to guess, see below...
                    break;
            }

            // init locals
            locals = new Locals$AnswerGuess();

            // init the local of the next level
            locals.initLevel(try_sudoku);
            LOGOUT("[nullGuess]: ");
            LOGOUT(locals);
        }

        // recursion unrolling, each iteration(level) like a call, and `local` is a vars set of this level
        while (true) { // lv1+
            // new a guessing sudoku of this level
            Sudoku try_sudoku = new Sudoku(locals.sudoku());

            // get the vars of this level
            int try_index = locals.index();
            int try_digit = locals.nextDigit();
            if (try_digit == -1) { // no more digits to guess
                LOGOUT("[nomoreDigit]: " + try_digit + "@" + try_index);
                // change `local` to the previous level
                locals.gobackLevel();
                // when the previous level is lv0
                if (locals.isZeroLevel() == true) {
                    return null;
                }
                LOGOUT("[prevGuess]: ");
                LOGOUT(locals);
                // go to the previous level
                continue;
            }
            // try this digit
            try_sudoku.commit(try_index, try_digit);
            int result = try_sudoku.updateToAnswer();
            LOGOUT("[update(commit)]: " + result + " - " + try_digit + "@" + try_index);
            switch (result) {
                case 1: // find answer, and return it
                    return try_sudoku;
                case -1: // find error, and try the next digit
                    continue;
                default: // cant find answer, so guess the next cell
                    locals.initLevel(try_sudoku); // init the next level with current sudoku
                    LOGOUT("[nextGuess]: ");
                    LOGOUT(locals);
                    continue; // goto the next level
            }
        }
    }

    public void endAnswerTry() {
        locals = null;
    }

    //-- abandoned
    private int firstMateIndex(int ci, int range) {
        int x = ci % col_cnt; // the x-coord of the curr-cell
        int y = ci / col_cnt; // the y-coord of the curr-cell
        int fi; // the index of the first cell in range

        switch (range) {
            case ROW: //ROW
                fi = y * col_cnt + 0;
                break;
            case COL: //COL
                fi = x + 0;
                break;
            case BLO: //BLO
                int fx = x / blo_colcnt * blo_colcnt; // the x-coord of the first cell in blo
                int fy = y / blo_rowcnt * blo_rowcnt; // the y-coord of the first cell in blo
                fi = fx + fy * blo_cellcnt;
                break;
            default:
                fi = -1;
                break;
        }

        return fi;
    }

    private int nextMateIndex(int ci, int range) {
        int x = ci % col_cnt; // the x-coord of the curr-cell
        int y = ci / col_cnt; // the y-coord of the curr-cell
        int fi; // the index of the first cell in range
        int ni; // the index of the next cell in range
        int max_ni; // the max index of the next cell in range

        switch (range) {
            case ROW: //ROW
                fi = y * col_cnt + 0;
                max_ni = fi + col_cnt - 1;
                ni = ci + 1;
                break;
            case COL: //COL
                fi = x + 0;
                max_ni = fi + col_cnt * (row_cnt - 1);
                ni = ci + col_cnt;
                break;
            case BLO: //BLO
                int fx = x / blo_colcnt * blo_colcnt; // the x-coord of the first cell in blo
                int fy = y / blo_rowcnt * blo_rowcnt; // the y-coord of the first cell in blo
                fi = fx + fy * blo_cellcnt;
                max_ni = fi + (blo_colcnt - 1) + (blo_rowcnt - 1) * col_cnt;
                int bi = ci - fi; // the index in blo of the curr-cell
                int nbx = (bi + 1) % blo_colcnt; // the x-coord in blo of the next mate-cell
                int nby = (bi + 1) / blo_colcnt * blo_colcnt; // the y-coord in blo of the next mate-cell
                ni = fi + nbx + nby * col_cnt;
                break;
            default:
                return -1;
        }

        if (ni > max_ni) {
            ni = -1;
        }
        return ni;
    }
}