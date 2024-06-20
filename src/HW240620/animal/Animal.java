package HW240620.animal;

import HW240620.IFunc.IAnimalDo;

public class Animal implements IAnimalDo {
    //public
    public String name;
    public int gender;
    public int weight;
    public int height;

    public void setName(String name) {
        this.name = name;
    };

    public void setWeight(int weight) {
        this.weight = weight;
    };

    public void setHeight(int height) {
        this.height = height;
    };

    public void setGender_Male() {
        this.gender = gender_male;
    };

    public void setGender_Female() {
        this.gender = gender_female;
    };

    public void setGender_Other() {
        this.gender = gender_other;
    };

    public void whoAmI() {
        print(name + ": Hi, I am a " + class_name + " called " + name + ".");
    }

    public void getGender() {
        String gender_str;
        switch (gender) {
            case gender_female:
                gender_str = "female";
                break;
            case gender_male:
                gender_str = "male";
                break;
            default:
                gender_str = "other";
                break;
        }
        print(name + " is " + gender_str + ".");
    }

    public void getWeight() {
        print(name + " is " + this.weight + this.weight_unit + " weight.");

    };

    public void getHeight() {
        print(name + " is " + this.height + this.height_unit + " height.");

    };

    @Override
    public void eat() {
        print(this.name + " is eating...");
    };

    @Override
    public void sleep() {
        print(this.name + " is sleeping...");
    }

    @Override
    public void eat(String food) {
        print(this.name + " is eating " + food + "...");
    }

    @Override
    public void sleep(int time) {
        for (int i = 0; i < time; i++) {
            print(this.name + " has been sleeping for " + (i + 1) + "s...");
        }
    };

    //private
    private String class_name = this.getClass().getSimpleName();
    private static final int gender_male = 1;
    private static final int gender_female = 0;
    private static final int gender_other = -1;
    private String weight_unit = "kg";
    private String height_unit = "cm";

    protected void print(String msg) {
        System.err.println(msg);
    };

    protected void setWeightUnit(String unit) {
        this.weight_unit = unit;
    }

    protected void setHeightUnit(String unit) {
        this.height_unit = unit;
    }

    //con/destructor
    public Animal() {
        this.name = "anonymous";
        this.gender = -1;
        this.weight = -1;
        this.height = -1;
    }

    public Animal(String name) {
        this();
        this.name = name;
    }

    public Animal(String name, int gender, int weight, int height) {
        this.name = name;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
    }

}
