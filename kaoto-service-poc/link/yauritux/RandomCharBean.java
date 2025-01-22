package link.yauritux;

import java.util.Random;

public class RandomCharBean {

    public String generateRandomChar() {
        char random = (char) ('a' + new Random().nextInt(26));
        return String.valueOf(random);
    }
}