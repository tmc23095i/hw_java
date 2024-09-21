
import HW240523.HW240523_0;
import HW240530.HW240530_1;
import HW240606.HW240606_2;
import HW240613.HW240613_3;
import HW240620.HW240620_4;
import HW240829.HW240829_5;
import HW240912.HW240912_6;

@SuppressWarnings("unused")

public class App {
    public static void main(String[] args) throws Exception {
        //
        boolean EXIT_APP = false;
        while (EXIT_APP == false) {
            //
            System.out.print("Type the index of homework to execute it, or `-1` to exit: ");
            //
            int hw_i = ioinValue();
            switch (hw_i) {
                case -1:
                    EXIT_APP = true;
                    break;
                case 0:
                    HW240523_0.main_();
                    break;
                case 1:
                    HW240530_1.main_();
                    break;
                case 2:
                    HW240606_2.main_();
                    break;
                case 3:
                    HW240613_3.main_();
                    break;
                case 4:
                    HW240620_4.main_();
                    break;
                case 5:
                    //== ...
                    HW240829_5.main_();
                    break;
                case 6:
                    HW240912_6.main_();
                    break;
                // ... code for the next hw
                default:
                    HW240523_0.main_();
                    HW240530_1.main_();
                    HW240606_2.main_();
                    HW240613_3.main_();
                    HW240620_4.main_();
                    HW240829_5.main_();
                    HW240912_6.main_();
                    // ... code for the next hw
                    break;
            }
        }
        //
        System.out.println("Exiting...");
    };

    public static int ioinValue() throws Exception {
        //
        int ch_io;//one char of the std-in stream
        int[] digits = { 0, 0, 0, 0, 0, 0, 0, 0 };//the digits of the value user typed
        int digit_cnt = 0;//the digit count of the value

        int value_sign = 1;// if the value is positive or negative
        int prev_ch = 0;//the previous char, used to check if the value begin with `-`

        boolean VALUE_READING = false;//to mark if the value is typing
        //get digits of value
        while (true) {
            // get one char from io stream
            ch_io = System.in.read(); // char to number
            int digit = ch_io - (int) '0';
            // if non-digit char
            if (!((digit >= 0) && (digit <= 9))) {
                //the value completed
                if (VALUE_READING == true) {
                    break;
                }
                //the value has not been typed
                else {
                    prev_ch = ch_io;
                    continue;
                }
            }
            //if digit char
            else {
                //the fitst digit
                if (VALUE_READING == false) {
                    //mark that the value is typing
                    VALUE_READING = true;
                    //check if the value is negative
                    if (prev_ch == '-') {
                        value_sign = -1;
                    }
                }
                //record the digit of the value
                digits[digit_cnt] = digit;
                digit_cnt++;
                // limit value range: +/- 99,999,999
                if (digit_cnt == 8) {
                    break;
                }
            }

        }

        // calc the value
        int value = 0;//the value that user typed
        for (int i = 0; i < digit_cnt; i++) {
            //the index of the current place
            int i_place = digit_cnt - 1 - i;
            //the digit of the current place
            int place_digit = digits[i_place];
            //calc the weight of the current place
            int place_weight = 1;
            for (int i__ = 0; i__ < i; i__++) {
                place_weight *= 10;
            }
            //add the current place value to the final value
            value += place_digit * place_weight;
        }

        //
        return value * value_sign;
    };

    //
}
