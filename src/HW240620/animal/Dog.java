package HW240620.animal;

public class Dog extends Animal {
    public int poop_interest_level;

    //con/destructor

    public Dog() {
        super();
    };

    public Dog(String name) {
        super(name);
    }

    public Dog(int poop_interest_level) {
        super();
        this.poop_interest_level = poop_interest_level;
    };

    public Dog(String name, int poop_interest_level) {
        super(name);
        this.poop_interest_level = poop_interest_level;
    };

    public Dog(String name, int gender, int weight, int height) {
        super(name, gender, weight, height);
    }

    public Dog(String name, int gender, int weight, int height, int poop_interest_level) {
        super(name, gender, weight, height);
        this.poop_interest_level = poop_interest_level;
    }

    public void eatPoop() {
        print(name + " is eating poop, and wanna eat " + this.poop_interest_level + " times again!");
    };

}
