package wagyx001.pa1.core;

public class Consts {
    // host name for the SuperNode
    public static final String SUPERNODE_URL = "localhost";
    
    // rmi descriptions
    public static final String SUPERNODE_RMI_DESC = "ISuperNode";
    public static final String NODE_RMI_DESC      = "INode";
    
    // TODO: change this back to M = 32
    public static final int M = 5; 
    
    public static final int MAX_NODE_KEY = (int) Math.pow((double)2, (double)M);
    
    // size of the finger table
    public static final int FINGERTABLE_SIZE = 5;

}
