package wagyx001.pa1.core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface to define the stub for the
 * Node's remote methods
 */
public interface INode extends Remote {
    boolean addWordDefinition(DictionaryEntry entry) 
            throws RemoteException;
    NodeInfo getNodeInfo()                      
            throws RemoteException;
    public void join() 
            throws RemoteException;
    public ArrayList<NodeInfo> resolveKey(int key) 
            throws RemoteException;
    public void addToMyDictionary(DictionaryEntry entry)
            throws RemoteException;
    public void update(FingerTable fingerTable, int predecessorId)
            throws RemoteException;
    public WordDictionary getWordDictionary()
            throws RemoteException;
    public DictionaryEntry getWord(String word)
            throws RemoteException;
}
