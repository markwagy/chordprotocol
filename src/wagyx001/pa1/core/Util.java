package wagyx001.pa1.core;

import java.util.Random;

public class Util {
    
    /**
     * Compute a hash from the given string
     */
    public static int getHash(String val) {
        // add a random number to the hash calculation to spread
        // out the node ids since they won't differ very much from each
        // other based solely on ip address and port if the machines are similarn
        Random rand = new Random();
        int randNum = rand.nextInt(Consts.MAX_NODE_KEY);
        int hashCode = (val + randNum).hashCode() % Consts.MAX_NODE_KEY;
        if (hashCode < 0) {
            // reverse the sign, can't have negative values
            hashCode = -hashCode;
        }
        return hashCode;
    }
}
