package wagyx001.pa1.core;

import java.io.Serializable;
import java.util.ArrayList;

public class FingerTable implements Serializable {
    
    static final long serialVersionUID = 1392184724L;

    ArrayList<Integer> vals;
    
    public FingerTable() {
        vals = new ArrayList<Integer>();
    }
    
    public ArrayList<Integer> getVals() {
        return vals;
    }
    
    public void add(Integer v) {
        vals.add(v);
    }
    
    public void add(int v) {
        vals.add(new Integer(v));
    }
    
    @Override
    public String toString() {
        String s = "Finger Table:\n";
        for (int i=0; i<vals.size(); i++) {
            s += "[" + (i+1) + " | " + vals.get(i) + "]\n";
        }
        return s;
    }
    
}