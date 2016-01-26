package wagyx001.pa1.core;

import java.io.Serializable;

/**
 * Class to hold info that a node needs to function in the DHT
 */
public class NodeInfo implements Serializable, Comparable {
    
    public static long serialVersionUID = 2341234L;
    
    public int id;
//    public int predecessorNodeId;
    public String ipAddress;
    public int portNumber;
//    public FingerTable fingerTable;
    
    public NodeInfo(int id, String ipAddress, int portNumber) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }
    
    /**
     * Update this nodeinfo with new finger table and predecessor id info
     * because a new node has joined 
     */
    /*
    public void update(FingerTable newFingerTable, int predecessorId) {
//        this.fingerTable = newFingerTable;
//        this.predecessorNodeId = predecessorId;
    }
     * 
     */
    
    /*
    public void setFingerTable(FingerTable ft) {
        this.fingerTable = ft;
    }
     */

    /*
    public FingerTable getFingerTable() {
        return fingerTable;
    }
     * 
     */
    
    public String toString() {
        return  "<Node Info>\n"
                + "id:\t\t\t" + id + "\n"
                + "ip address:\t\t" + ipAddress + "\n"
                + "port number:\t" + portNumber + "\n";
//                + "predecessor id:\t" + predecessorNodeId + "\n"
//                + fingerTable;
    }

    /**
     * Comparison method to implement Comparable interface.
     * Compares NodeInfo based on id values
     * @param t: Object to compare to
     * @return 0 if same, -1 if less than, +1 if more than
     */
    @Override
    public int compareTo(Object t) {
        NodeInfo ni = (NodeInfo) t;
        if (ni.id > this.id) {
            return -1;
        } else if (ni.id < this.id) {
            return 1;
        } else if (ni.id == this.id) {
            return 0;
        } else {
            System.err.println(
                    "ERROR: objects not comparable: " + this + " and " + ni);
            return 0;
        }
    }
}