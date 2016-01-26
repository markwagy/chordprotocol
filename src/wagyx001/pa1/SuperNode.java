package wagyx001.pa1;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import wagyx001.pa1.core.NodeInfo;
import wagyx001.pa1.core.Consts;
import wagyx001.pa1.core.FingerTable;
import wagyx001.pa1.core.INode;
import wagyx001.pa1.core.ISuperNode;
import wagyx001.pa1.core.Util;

/**
 * The SuperNode acts on requests from Nodes to join the DHT and
 * gives initial information (such as their node id and initial finger table)
 * that they need to start.
 * 
 * The SuperNode maintains a list of NodeInfo objects to keep track of
 * how to assign initial node information.
 * 
 * It is important to note that we do not rely on any sort 
 * of ordering of the NodeInfo list, but rather their assigned node ids
 * to identify them.
 * 
 */
public class SuperNode implements ISuperNode {

    // nodeInfos keeps a list of active nodes' info
    ArrayList<NodeInfo> nodeInfos;
    
    private static Logger log;
    
    /**
     * Constructor for SuperNode
     */
    public SuperNode() {
        nodeInfos = new ArrayList<NodeInfo>();
    }
    
    /**
     * SuperNode main method
     */
    public static void main(String args[]) {
        
        // initialize log4j
        PropertyConfigurator.configure("config/log4j.properties");
        log = Logger.getLogger( SuperNode.class );
        log.debug("Log4j has been initialized");
        
        // start supernode
        log.info("Starting SuperNode...");
	try {
            ISuperNode superNode = new SuperNode();            
            ISuperNode stub = 
		(ISuperNode) UnicastRemoteObject.exportObject(superNode,0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(Consts.SUPERNODE_RMI_DESC, stub);
            log.info("SuperNode listening");
        } catch (Exception e) {
            log.error("SuperNode exception: " + e);
        }
    }
    
    /**
     * Get a new finger table for given node id (thisId)
     * We only add as many finger table entries as there are nodes
     * in the DHT and below the max number of finger table entries
     * (FINGERTABLE_SIZE)
     */
    @Override
    public FingerTable getNewFingerTable(int thisId) throws RemoteException {
        FingerTable ft = new FingerTable();
        log.debug("Getting new finger table for node id: " + thisId);
        for (int i=1; i<=Consts.FINGERTABLE_SIZE; i++) {
            // we need to handle the case where nothing is in the 
            // node list here:
            if (nodeInfos.isEmpty()) {
                // if there are no other nodes, just keep our own address 
                // in the finger table
                log.debug("nodeInfos is empty, so our id is always the id in the finger table");
                ft.add(thisId);
                continue;
            }
            int cal = (int) ((thisId + Math.pow(2,(i-1))) % Consts.MAX_NODE_KEY);
            log.debug("cal value: " + cal);
            int val = getSuccessorId(cal);
            log.debug("val value: " + val);
            ft.add(val);
        }
        log.debug("new finger table for node " + thisId + ":" + ft);
        return ft;
    }
    
    /**
     * Give a random node's info
     * This will be used by the Client as a starting point in which to
     * begin the search for the correct node in which to insert keys or 
     * get a lookup of a key.
     * @return
     * @throws RemoteException 
     */
    @Override
    public NodeInfo giveRandomNodeInfo() throws RemoteException {
        Random rand = new Random();
        int i = rand.nextInt(nodeInfos.size());
        return nodeInfos.get(i);
    }
    
    /**
     * This method is called by a Node when it wants to join the network
     * of peers. The Node contacts the SuperNode with a request...
     * ... then the SuperNode returns info about successor, predecessor
     * Note that the SuperNode is responsible for telling the Node where
     * he fits into the DHT
     * (within the NodeInfo structure)
     * @return
     * @throws RemoteException 
     */
    @Override
    public NodeInfo initiate(String url) throws RemoteException {
        log.debug("in initiate()");
        int port = getNewNodePortNum();
        log.debug("Assigning node at " + url + " port number " + port);
        int thisId = getNewNodeId(url, port);
        log.debug("Assigning node at " + url + " id " + thisId);
        NodeInfo ni = new NodeInfo(thisId, url, port);
        // add node to list of node info that this supernode maintains
        nodeInfos.add(ni);
        log.info("Added node:\n" + ni);
        return ni;
    }
    
    /**
     * Gets the predecessor node for a given id value (key value)
     * @param id: hash key value to find the predecessor node
     */
    @Override
    public int getPredecessor(int id) throws RemoteException {
        // sort nodeInfos by their default sort order - nodeId
        Collections.sort(nodeInfos);
        
        // log.debug("Sorted nodeInfos: " + nodeInfos);
        log.debug("in get predecessorId() and looking for predecessor of " + id);
        
        int dhtSize = nodeInfos.size();
        log.debug("size of DHT: " + dhtSize);
 
        if (nodeInfos.get(0).id > id) {
            // if the id is smaller than the smallest id, its predecessor
            // is the node with the largest id (modulo arithmetic)
            log.debug("id is smaller than the smallest id: " + 
                    nodeInfos.get(0).id);
            log.debug("so we are giving predecessor of last node: " 
                    + nodeInfos.get(dhtSize-1).id);
            return nodeInfos.get(dhtSize-1).id;
        }
        // otherwise, we march down the node info list and return the first
        // id that is smaller than the input id
        for (int i=dhtSize-1; i>=0; i--) {
            int currId = nodeInfos.get(i).id;
            log.debug("checking id: " + currId 
                    + " to see if it is the predecessor");
            if (currId <= id) {
                log.debug("this id is the predecessor: " + currId);
                return currId;
            }
        }
        log.warn("We've run out of entries, so the predecessor must be the largest value");
        return nodeInfos.get(dhtSize-1).id;
    }
    
    /**
     * Gets successor Id for a given key id
     * For documentation, see <code>getPredecessorId</code> method
     * as it is pretty much the same, just reversed
     */
    private int getSuccessorId(int id) {
        Collections.sort(nodeInfos);
        int dhtSize = nodeInfos.size();
        int lastPos = nodeInfos.size()-1;
        
        if (nodeInfos.get(lastPos).id < id) {
            return nodeInfos.get(0).id;
        }
        
        for (int i=0; i<dhtSize; i++) {
            int currId = nodeInfos.get(i).id;
            if (currId >= id) {
                return currId;
            }
        }
        log.error("Couldn't find successor of " + id
                + ". This shouldn't happen");
        return -1;
    }
    
    /**
     * Get a NodeInfo's position in the NodeInfo list by its node id
     * @param nodeInfoId: the node id that is being queried
     * @return NodeInfo list position index
     */
    private int getNodeInfoPositionById(int nodeInfoId) {
        for (int i=0; i<nodeInfos.size(); i++) {
            log.debug("size: " + nodeInfos.size());
            NodeInfo ni = nodeInfos.get(i);
            log.debug("Checking node " + ni.id + " against " + nodeInfoId);
            if (ni.id == nodeInfoId) {
                return i;
            }
        }
        log.debug("Only one node");
        return 0;
    }

    /**
     * RMI that allows new node to report that it has joined our DHT
     * @param nodeId
     * @throws RemoteException 
     */
    @Override
    public boolean hasJoined(int nodeId) throws RemoteException {
        log.info("A new node has joined the system: " + nodeId);
        // update nodeinfo list values and nodes
        for (NodeInfo ni : nodeInfos) {
            if (ni.id != nodeId) {
                FingerTable newFT = getNewFingerTable(ni.id);
                int predId = getPredecessor(ni.id);
                Registry reg = LocateRegistry.getRegistry(ni.ipAddress);
                try {
                    INode node =
                            (INode) reg.lookup(Consts.NODE_RMI_DESC);
                    // update the node with its new info
                    node.update(newFT, predId);
                } catch (NotBoundException ex) {
                    log.error("hasJoined not bound issue: " + ex);
                } catch (AccessException ex) {
                    log.error("hasJoined access issue: " + ex);
                }
            }

        }
	return true;
    }
    
    /**
     * Get a port number for a node that isn't currently in use
     * @return 
     */
    private int getNewNodePortNum() {
        int nodeNum = nodeInfos.size();
        int base = 50000;
         return base + nodeNum;
    }
    
    /**
     * Get a new ID val for a Node (based on hashing the ip and port of the Node)
     * @param ip
     * @param port
     * @return 
     */
    private int getNewNodeId(String ip, int port) {
        return Util.getHash(ip + port);
    }

    /**
     * Get a NodeInfo by node id... used by Client to get IP and port info
     * based on node id
     */
    @Override
    public NodeInfo getNodeInfo(int nodeId) throws RemoteException {
        for (NodeInfo ni : nodeInfos) {
            if (ni.id == nodeId) {
                return ni;
            }
        }
        log.error("Couldn't find node using this id: " + nodeId);
        return null;
    }

}