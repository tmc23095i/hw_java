
package HW240523;

public class HW240523_0 {
    public static void main_() throws Exception {
        System.out.println("*********************************");
        System.out.println("***** Homework_0 2024_05_23 *****");
        System.out.println("*********************************");

        System.out.println("\n>>> Print alphabet grid. ");
        printAlphabetGrid();

        System.out.println("\n>>> Print 9x9 multiplication table. ");
        printMultiplicationTable();

        System.out.println("\n*********************************");
        System.out.println("************** END **************");
        System.out.println("*********************************\n\n\n");
    }

    public static void printAlphabetGrid() {
        /*
         * HIJKLMN
         * OPQ RST
         * UVW XYZ
         */
        char l = 'A';
        int col_max = 7;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < col_max; col++) {
                // 逐个打印字母
                System.out.print(l + " ");
                l++;
                // 特殊打印 - 后两行
                if (2 <= row) {
                    col_max = 6;
                    // 特殊打印 - 中间空格
                    if (col == 2) {
                        System.out.print(" ");
                    }
                } else {
                    col_max = 7;
                }
            }
            System.out.print("\n");
        }
    }

    public static void printMultiplicationTable() {
        /*
         * 1x1
         * 1x2 2x2
         * 1x3 2x3 3x3
         * ...
         * col x row ...
         * a x b = c
         * ...
         */
        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= row; col++) {
                System.out.print(col + "x" + row + "=" + col * row + "\t");
            }
            // 打印换行
            System.out.print("\n");
        }

    }

}