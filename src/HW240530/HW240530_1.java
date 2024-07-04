
package HW240530;

public class HW240530_1 {
    public static void main_() throws Exception {
        System.out.println("*********************************");
        System.out.println("***** Homework_1 2024_05_30 *****");
        System.out.println("*********************************");

        System.out.println("\n>>> Does text contain datetime-string. ");
        String txt_test = "}057/,076%  kjbktre54e&@:》?U2024/05/30_20:15:36_Thu]\\]'\\9-8 jm;[,jbhdalku897cgh785e7ingf]]";
        hasDatetimeStr(txt_test);

        System.out.println("\n>>> Move lower-case chars of string to the front. ");
        moveLowercaseToFront();

        System.out.println("\n*********************************");
        System.out.println("************** END **************");
        System.out.println("*********************************\n\n\n");
    }

    public static void moveLowercaseToFront() {
        // src: AaBbCc...XxYyZz
        // dest: abc...xyzABC...xyzABC

        // create src string
        String str = "";
        for (int i = 0; i < 26; i++) {
            char l = (char) ((int) 'a' + i);
            char L = (char) ((int) 'A' + i);
            str += L;
            str += l;
        }
        // show
        System.out.println(str);

        // reorder
        String temp = "";
        int str_len = str.length();
        int a_ascii = (int) 'a';
        // get lower
        for (int i = 0; i < str_len; i++) {
            char ch_lower = str.charAt(i);
            int ch_ascii = (int) ch_lower;
            if (ch_ascii >= a_ascii) {
                temp += ch_lower;
            }
        }
        // get upper
        for (int i = 0; i < str_len; i++) {
            char ch_upper = str.charAt(i);
            int ch_ascii = (int) ch_upper;
            if (ch_ascii < a_ascii) {
                temp += ch_upper;
            }
        }
        // modify
        str = temp;

        // return
        System.out.println(str);
    }

    static void hasDatetimeStr(String text) {
        // datetime format: 2024/05/30_20:15:36_Thu
        // patterns:
        // \d{4}/[01]?\d/[0123]?\d_[012]\d(:[012345]\d){2}(_Mon|_Tue|_Wed|_Thu|_Fri|_Sat|_Sun)

        // make datetime regex
        String regex_datetime = "";
        // year
        regex_datetime += "\\d{4}";
        // /month
        regex_datetime += "/[01]?\\d";
        // /day
        regex_datetime += "/[0123]?\\d";
        // _hour
        // regex_datetime += "_[012]\\d";
        regex_datetime += "_([01]\\d|2[0123])";
        // :mn:sc
        regex_datetime += "(:[012345]\\d){2}";
        // _week
        regex_datetime += "(_Mon|_Tue|_Wed|_Thu|_Fri|_Sat|_Sun)";

        String new_rgex = ".*" + regex_datetime + ".*";

        // show
        System.out.println("Text:  " + text);
        System.out.println("RgEx:  " + new_rgex);
        System.out.println("Rslt:  " + text.matches(new_rgex));
    }

}