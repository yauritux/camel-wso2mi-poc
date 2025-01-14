package link.yauritux.camelservicepoc.util;

import java.util.Random;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
public class RandomAlpha {

    public static char randomChar() {
        Random random = new Random();

        String alphanum = "abcdefghijklmnopqrstuvwxyz";
        return alphanum.charAt(random.nextInt(alphanum.length()));
    }
}
