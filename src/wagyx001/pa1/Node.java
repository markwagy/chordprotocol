package wagyx001.pa1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import wagyx001.pa1.core.DictionaryEntry;
import wagyx001.pa1.core.NodeInfo;
import wagyx001.pa1.core.Consts;
import wagyx001.pa1.core.FingerTable;
import wagyx001.pa1.core.INode;
import wagyx001.pa1.core.ISuperNode;
import wagyx001.pa1.core.Util;
import wagyx001.pa1.core.WordDictionary;
        
/**
 * The node is the workhorse of the system -
 * it maintains a finger table to find other nodes and
 * the nodes that are responsible for a particular key value
 * and it stores a subset of the words in the distributed dictionary
 * 
 */
public class Node implements INode {

    private NodeInfo nodeInfo;
    
    private String ipAddr;
    private int portNum;
    
    private ISuperNode superNode;

    private static Logger log;
    
    private WordDictionary myDictionary;
    
    private FingerTable fingerTable;
    private int predecessorId;
    
    /**
     * Constructor for Node class
     */
    public Node() {
        this.ipAddr = getMyIPAddress();
        myDictionary = new WordDictionary();
        try {
            // get the supernode from RMI registry to call initiate for this node
            Registry registry =
                    LocateRegistry.getRegistry(Consts.SUPERNODE_URL);
            try {
                superNode =
                        (ISuperNode) registry.lookup(Consts.SUPERNODE_RMI_DESC);
            } catch (NotBoundException ex) {
                log.error(
                        "Couldn't lookup registry because it isn't bound: " + ex);
            } catch (AccessException ex) {
                log.error(
                        "Couldn't lookup registry due to lack of access: " + ex);
            }
        } catch (RemoteException ex) {
            log.error("node couldn't join: " + ex);
        }
    }
    
    /**
     * Node main method
     */
    public static void main(String args[]) {
        // initialize log4j
        PropertyConfigurator.configure("config/log4j.properties");
        log = Logger.getLogger(Node.class );
        log.debug("Log4j has been initialized");
        
        log.info("Starting Node...");
        try {
            INode node = new Node();
            
            // join DHT
            node.join();
                        
            // do all the things necessary to make RMI happy
            log.debug("Exporting object");
            INode stub = 
                    (INode) UnicastRemoteObject.exportObject(node,0);
            log.debug("Getting registry");
            Registry registry = LocateRegistry.getRegistry();
            log.debug("Binding object to registry");
            registry.rebind(Consts.NODE_RMI_DESC, stub);
            log.info("Node listening");
        } catch (Exception e) {
            log.error("Node exception: " + e);
        }
    }
    
    /**
     * Get this machine's IP address
     * @return IP address
     */
    private String getMyIPAddress() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("I can't find my own ip address: " + e);
            return null;
        }
    }
    
    /**
     * Join the DHT by contacting the SuperNode
     */
    @Override
    public void join() {
        try {
            // initiate calls SuperNode to get info that connects this node to
            // the DHT
            log.info("Joining DHT");
            nodeInfo = superNode.initiate(ipAddr);
            log.info("We've been given this node info from SuperNode: "
                    + nodeInfo);
	    boolean joinSuccess = false;
            fingerTable = superNode.getNewFingerTable(nodeInfo.id);
            log.debug("Current finger table:\n" + fingerTable);
            predecessorId = superNode.getPredecessor(nodeInfo.id);
            joinSuccess = superNode.hasJoined(nodeInfo.id);
	    log.debug("Join success? " + joinSuccess);
            log.info("Successfully joined the system");
        } catch (RemoteException ex) {
            log.error("Problem with remote method invocation on SuperNode: " + ex);
        }
    }

    @Override
    public boolean addWordDefinition(DictionaryEntry entry) throws RemoteException {
        int key = Util.getHash(entry.getWord());
        log.debug("key from given word for addition: " + key);
        /*
         * the niList list will have a list of <code>NodeInfo</code> objects
         * the first of which will be the NodeInfo of the node that is to house
         * the given key...
         * ... if it is null, we couldn't find the correct node to give the word
         * to, so we have a problem
         */
        ArrayList<NodeInfo> niList = resolveKey(key);
        if (niList == null) {
            log.error("We couldn't find the correct node to add the word to: " 
                    + entry);
            return false;
        }
        
        // let's print out the path to the node that we are inserting the 
        // word into
        printNodePath(niList);
        
        // if we got here, the first value in the list should be the node
        // in which to insert the word/defn pair, so do it
        NodeInfo targetNodeInfo = niList.get(0);
        Registry reg = LocateRegistry.getRegistry(targetNodeInfo.ipAddress);
        try {
            INode targetNode = (INode) reg.lookup(Consts.NODE_RMI_DESC);
            targetNode.addToMyDictionary(entry);
        } catch (NotBoundException ex) {
            log.error("node bound error in addWordDefinition: " + ex);
        } catch (AccessException ex) {
            log.error("access exception in addWordDefinition: " + ex);
        }
        
        // return true, because all is well
        return true;
    }
    
    private void printNodePath(ArrayList<NodeInfo> niList) {
        log.info(
                "Path taken to resolve node: ");
        for (int i=niList.size()-1; i>= 0; i--) {
            log.info("node " + i + ": " + niList.get(i));
        }
    }
    
    /**
     * Add a word to this node's dictionary of word/definition pairs
     */
    @Override
    public void addToMyDictionary(DictionaryEntry entry) {
        myDictionary.addEntry(entry);
    }

    /**
     * Returns this node's info. This is used for "ping"s
     */
    @Override
    public NodeInfo getNodeInfo() throws RemoteException {
        log.debug("in getNodeInfo");
        return this.nodeInfo;
    }

    
    /**
     * returns a list of <code>NodeInfo</code> values... the first of which is the node
     * that the key resolves to (this way we can report the path as well
     * as the place to find the key
     * 
     * this is kind of a recursive call in a way - apart from the fact that
     * nodes are 'recursively' calling each other
     */
    @Override
    public ArrayList<NodeInfo> resolveKey(int key) {
        // if this node is responsible for the given key,
        // return a new list with this node as its first member
        if (isMyKey(key)) {
            log.debug("The given key (" + key + ") fits within my domain");
            log.debug("i.e. between my predecessor's id (" 
                    + predecessorId + ") and my id ("
                    + nodeInfo.id + ")");
            ArrayList<NodeInfo> nis = new ArrayList<NodeInfo>();
            nis.add(this.nodeInfo);
            return nis;
        }
        
        // otherwise find the next node to ask for the key
        // append this ndoe to the list of the returned result
        ArrayList<Integer> ftVals = fingerTable.getVals();
        for (int i=ftVals.size()-1; i>=0; i++) {
            int currVal = ftVals.get(i).intValue();
            if (key > currVal) {
                try {
                    // if the key is greater than the current node id value
                    // then this is the next node to contact, so get its info
                    NodeInfo targetNodeInfo = superNode.getNodeInfo(currVal);
                    // use rmi to contact this node, now that we have its info
                    Registry registry = 
                            LocateRegistry.getRegistry(targetNodeInfo.ipAddress);
                    INode node = 
                            (INode) registry.lookup(Consts.NODE_RMI_DESC);
                    ArrayList niList = node.resolveKey(key);
                    // add my own node info to this list
                    niList.add(this.nodeInfo);
                    return niList;
                } catch (NotBoundException ex) {
                    log.error("not bound from resolveKey: " + ex);
                } catch (AccessException ex) {
                    log.error("access exception from resolveKey: " + ex);
                } catch (RemoteException ex) {
                    log.error("Problem calling supernode: " + ex);
                }
            }
        }
        log.error("We weren't able to resolve the key " + key);
        return null;
    }
    
    /**
     * Returns true if this node is responsible for the given key
     * i.e. the key is between the predecessor's id and this node's id
     */
    private boolean isMyKey(int key) {
        return key > getPredecessorId() && key <= getId();
    }
    
    /**
     * Gets this node's predecessor id
     */
    private int getPredecessorId() {
        return predecessorId;
    }
    
    /**
     * Get this node's Id
     */
    private int getId() {
        return this.nodeInfo.id;
    }

    /**
     * Update this node's info as the result of a new node joining the DHT
     */
    @Override
    public void update(FingerTable fingerTable, int predecessorId) throws RemoteException {
        log.debug("Entering node's update method");
        this.fingerTable = fingerTable;
        log.debug("We got a new FingerTable: \n" + fingerTable);
        this.predecessorId = predecessorId;
        log.debug("updateing my dictionary");
        // update my dictionary to give away words that are no longer mine
        updateMyDictionary();
    }
    
    private void updateMyDictionary() {
        log.debug("in updateMyDictionary");
        for (DictionaryEntry entry : myDictionary.getEntries()) {
            int key = Util.getHash(entry.getWord());
            if (key <= predecessorId) {
                giveEntryToPredecessor(entry);
            }
        }
    }
    
    private void giveEntryToPredecessor(DictionaryEntry entry) {
        log.debug("in giveEntryToPredecessor");
        try {
            NodeInfo predNI = superNode.getNodeInfo(predecessorId);
            Registry reg = LocateRegistry.getRegistry(predNI.ipAddress);
            try {
                INode predNode = 
                        (INode) reg.lookup(Consts.NODE_RMI_DESC);
                log.debug("calling addToMyDictionary");
                predNode.addToMyDictionary(entry);
                log.debug("removing entry from dictionary: " + entry);
                myDictionary.removeEntry(entry);
            } catch (NotBoundException ex) {
                log.error("not bound issue in giveEntryToPredecessor: " + ex);
            } catch (AccessException ex) {
                log.error("access issue in giveEntryToPredecessor: " + ex);
            }
        } catch (RemoteException ex) {
            log.error("Remote exception in giveEntryToPredecessor: " + ex);
        }
    }

    /**
     * Return this node's word dictionary
     */
    @Override
    public WordDictionary getWordDictionary() throws RemoteException {
        return myDictionary;
    }

    @Override
    public DictionaryEntry getWord(String word) throws RemoteException {
        int key = Util.getHash(word);
        log.debug("key from given word for lookup: " + key);
        /*
         * the niList list will have a list of <code>NodeInfo</code> objects
         * the first of which will be the NodeInfo of the node that is to house
         * the given key...
         * ... if it is null, we couldn't find the correct node to give the word
         * to, so we have a problem
         */
        ArrayList<NodeInfo> niList = resolveKey(key);
        if (niList == null) {
            log.error("We couldn't find the correct node that has the word: " 
                    + word);
            return null;
        }
        
        // let's print out the path to the node that we are inserting the 
        // word into
        log.info("Printing path of nodes in the lookup");
        printNodePath(niList);
        
        // if we got here, the first value in the list should be the node
        // in which to lookup the word pair, so do it
        NodeInfo targetNodeInfo = niList.get(0);
        Registry reg = LocateRegistry.getRegistry(targetNodeInfo.ipAddress);
        try {
            INode targetNode = (INode) reg.lookup(Consts.NODE_RMI_DESC);
            DictionaryEntry entry = targetNode.getWord(word);
            return entry;
        } catch (NotBoundException ex) {
            log.error("node bound error in getWord(): " + ex);
        } catch (AccessException ex) {
            log.error("access exception in getWord(): " + ex);
        }

        log.warn("Unable to find word:" + word);
        return null;
    }
    
}