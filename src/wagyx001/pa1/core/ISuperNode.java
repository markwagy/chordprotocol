package wagyx001.pa1.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to define the stub for the
 * SuperNode's remote methods.
 */
public interface ISuperNode extends Remote {
    public NodeInfo giveRandomNodeInfo()    
            throws RemoteException;
    public NodeInfo initiate(String url)    
            throws RemoteException;
    public boolean hasJoined(int nodeId)    
            throws RemoteException;
    public NodeInfo getNodeInfo(int nodeId) 
            throws RemoteException;
    public FingerTable getNewFingerTable(int nodeId)
            throws RemoteException;
    public int getPredecessor(int id)
            throws RemoteException;
}