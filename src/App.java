
import HW240523.HW240523_0;
import HW240530.HW240530_1;
import HW240606.HW240606_2;
import HW240613.HW240613_3;
import HW240620.HW240620_4;

@SuppressWarnings("unused")

public class App {
    public static void main(String[] args) throws Exception {
        //
        int i_hw = 4;
        switch (i_hw) {
            case 0:
                HW240523_0.main_(args);
                break;
            case 1:
                HW240530_1.main_(args);
                break;
            case 2:
                HW240606_2.main_(args);
                break;
            case 3:
                HW240613_3.main_(args);
                break;
            case 4:
                HW240620_4.main_(args);
                break;

            default:
                HW240523_0.main_(args);
                HW240530_1.main_(args);
                HW240606_2.main_(args);
                HW240613_3.main_(args);
                HW240620_4.main_(args);
                break;
        }
        //

    }
}
