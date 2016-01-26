package wagyx001.pa1;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import wagyx001.pa1.core.Consts;
import wagyx001.pa1.core.DictionaryEntry;
import wagyx001.pa1.core.INode;
import wagyx001.pa1.core.ISuperNode;
import wagyx001.pa1.core.NodeInfo;
import wagyx001.pa1.core.WordDictionary;

public class Client {

    private NodeInfo superNodeLocation;
    private WordDictionary wordDictionary;
    private ISuperNode superNode;
    
    private static Logger log;
    
    /**
     * Client constructor
     */
    public Client() { 
        try {
            // get the supernode from RMI registry to call initiate for this node
            Registry registry =
                    LocateRegistry.getRegistry(Consts.SUPERNODE_URL);
            try {
                superNode =
                        (ISuperNode) (ISuperNode) registry.lookup(Consts.SUPERNODE_RMI_DESC);
            } catch (NotBoundException ex) {
                System.err.println("ERROR: Client couldn't lookup registry because it isn't bound: " + ex);
            } catch (AccessException ex) {
                System.err.println("ERROR: Client couldn't lookup registry due to lack of access: " + ex);
            }
        } catch (RemoteException ex) {
            System.err.append("ERROR: Client remote exception: " + ex);
        }
        /*
        System.out.println("Getting 4 word definitoins: ");        
        for (DictionaryEntry e : wordDictionary.getNEntries(4)) {
            System.out.println(e);
        }
         */
    }
    
    /**
     * Client main method
     * @param args 
     */
    public static void main(String args[]) {
        
        // initialize log4j
        PropertyConfigurator.configure("config/log4j.properties");
        log = Logger.getLogger(Client.class);
        log.debug("Log4j has been initialized");
        
        log.info("Starting Client...");
        String file = "";
        
        // optional first arg to client is words file
        if (args.length == 1) {
            file = args[0];
        } 
        
        Client client = new Client();
        client.run();
	log.info("Closing Client");
    }
    
    /**
     * Runs the client
     */
    private void run() {
        log.info("Client running...");
        String choice = "";
        while(!choice.equalsIgnoreCase("q")) {
            printMainMenu();
            choice = getInput();
            choice = forkMainOptions(choice);
        }
    }
    
    /**
     * Fork into options given user's choice
     */
    public String forkMainOptions(String choice) {
        if (choice.equalsIgnoreCase("q")) {
            return choice;
        } else if (choice.equalsIgnoreCase("a")) {
            doAddWordFile();
        } else if (choice.equalsIgnoreCase("p")) {
            doPingNode();
        } else if (choice.equalsIgnoreCase("g")) {
            doRandomNodeInfo();
        } else if (choice.equalsIgnoreCase("e")) {
            doEnterWordDefinitionByHand();
        }
        return choice;
    }
    
    /**
     * Add a definition "by hand" - at the client prompt
     */
    public void doEnterWordDefinitionByHand() {
        System.out.println(
                "You have chosen to enter a definition at the prompt");
        System.out.print("Please enter the word: ");
        String word = getInput();
        System.out.println("Please type the definition below:");
        String defn = getInput();
        DictionaryEntry entry = new DictionaryEntry(word, defn);
        log.info("Adding: " + entry);
        addWord(entry);
    }
    
    /**
     * Add word file
     */
    private void doAddWordFile() {
        System.out.println("Where is the dictionary file located?");
        String fileName = getInput();
        log.info("Adding words and defs from file: " + fileName);
        addWordDictionary(fileName);
    }
    
    /**
     * Get random node info from SuperNode
     */
    private void doRandomNodeInfo() {
        try {
            NodeInfo ni = superNode.giveRandomNodeInfo();
            System.out.println("Got this Node's info: ");
            System.out.println(ni);
        } catch (RemoteException ex) {
            log.error("Unable to get random node info from SuperNode: " + ex);
        }
    }
    
    /**
     * Ping a node for info
     * TODO: this should be done via lookup of id through nodes themselves
     * (via finger tables) rather than contacting the supernode
     */
    private void doPingNode() {
        System.out.println(
                "What is the node ID that you want to ping?");
        int nodeId;
        nodeId = Integer.valueOf(getInput());
   
        try {
            /* 
             * get node info from supernode so that we can
             * get this node's ip address for its RMI registry
             */
            try {
                NodeInfo nodeInfo = superNode.getNodeInfo(nodeId);
                // get the node from RMI Registry
                // at location specified by NodeInfo
                Registry registry =
                        LocateRegistry.getRegistry(nodeInfo.ipAddress);
                try {
                    INode node =
                            (INode) registry.lookup(Consts.NODE_RMI_DESC);
                    log.info("Contacting Node " + nodeId);
                    NodeInfo myNodeInfo = node.getNodeInfo();
                    log.debug("getting word dictionary");
                    WordDictionary myDict = node.getWordDictionary();
                    System.out.println("Got this info from the node:");
                    System.out.println(myNodeInfo);
                    System.out.println("Words: ");
                    System.out.println(myDict);
                } catch (NotBoundException ex) {
                    System.err.println("ERROR: Client couldn't lookup registry because it isn't bound: " + ex);
                } catch (AccessException ex) {
                    System.err.println("ERROR: Client couldn't lookup registry due to lack of access: " + ex);
                }
            } catch (RemoteException ex) {
                log.error("Error calling getNodeInfo using node id " + nodeId
                        + ": " + ex);
            }
            catch(Exception e) {
                log.error("Error: " + e);
            }
        } catch (Exception e1) {
            log.error("Error: " + e1);
        }
        
    }
    
    /**
     * Add a word dictionary object to this client to be sent to Nodes
     * @param wordDictionaryFile 
     */
    private void addWordDictionary(String wordDictionaryFile) {
        wordDictionary = new WordDictionary(wordDictionaryFile);
    }
    
    /**
     * Prints out the main menu for the client
     */
    public void printMainMenu() {
        String s = "CLIENT--\n";
        s += "What would you like to do?\n";
        s += "(a)dd words from file\n";
        s += "(p)ing a node\n";
        s += "(g)et a random node id from supernode\n";
        s += "(e)nter word/definition on command line\n";
        s += "(q)uit\n";
        
        System.out.println(s);
    }

    // simple input method to reduce clutter
    private static String getInput() {
        Scanner input = new Scanner(System.in);
        String val = input.next();
        return val;
    }
    
    
    /*
     * Get some node's info from the SuperNode to start our
     * search for the correct node in which to insert or fetch a word
     */
    private NodeInfo getSomeNodesInfo () {
        try {
            return superNode.giveRandomNodeInfo();
        } catch (RemoteException ex) {
            log.error("Unable to get random node info from SuperNode" + ex);
            return null;
        }
    }

    private void addWord(DictionaryEntry entry) {
        log.info("adding a word.defn to DHT: " + entry);
        // get a random node from the DHT to start with
        NodeInfo ni = getSomeNodesInfo();
        try {
            // add the word to the DHT 
            // (at this point, the node will figure out where
            // in the DHT to put it... that is hidden from the client)
            Registry registry = 
                    LocateRegistry.getRegistry(ni.ipAddress);
            INode node = 
                    (INode) registry.lookup(Consts.NODE_RMI_DESC);
            node.addWordDefinition(entry);
            log.debug("Successfully added: " + entry);
        } catch (NotBoundException ex) {
            log.error("NotBound issues with addWord: " + ex);
        } catch (AccessException ex) {
            log.error("Access issues with addWord: " + ex);
        } catch (RemoteException ex) {
            log.error("Unable to look up registry: " + ex);
        }
        
    }
    
    /**
     * Look up a definition in the DHT
     */
    private void getDefinition(String word) {
        log.info("Getting a word's definition from DHT: " + word);
        // get a random node from the DHT to start with
        NodeInfo ni = getSomeNodesInfo();
        try {
            // add the word to the DHT 
            // (at this point, the node will figure out where
            // in the DHT to get it... that is hidden from the client)
            Registry registry = 
                    LocateRegistry.getRegistry(ni.ipAddress);
            INode node = 
                    (INode) registry.lookup(Consts.NODE_RMI_DESC);
            DictionaryEntry entry = node.getWord(word);
            if (entry != null) {
                System.out.println("Found this entry: " + entry);
            } else {
                log.warn("Could find the word: " + word);
            }
        } catch (NotBoundException ex) {
            log.error("NotBound issues with addWord: " + ex);
        } catch (AccessException ex) {
            log.error("Access issues with addWord: " + ex);
        } catch (RemoteException ex) {
            log.error("Unable to look up registry: " + ex);
        }
        
    }


}