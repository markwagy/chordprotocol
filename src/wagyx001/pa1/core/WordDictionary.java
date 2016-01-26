package wagyx001.pa1.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class WordDictionary implements Serializable {

    public static long serialVersionUID = 23984524L;
    
    private Integer currentWordIndex;
    private String wordFileName;
    private ArrayList<DictionaryEntry> entries;
    private static Logger log;
    
    public WordDictionary() {
        log = Logger.getLogger(this.getClass());
        entries = new ArrayList<DictionaryEntry>();
        currentWordIndex = 0;
    }
    
    public WordDictionary(String wordFileName) {
	this.wordFileName = wordFileName;
	currentWordIndex = 0;
	entries = new ArrayList<DictionaryEntry>();
	parseWordsFile();
    }

    public void addEntry(DictionaryEntry e) {
        entries.add(e);
    }
    
    private void parseWordsFile() {
	try {
            BufferedReader reader = 
                    new BufferedReader(new FileReader(wordFileName));
            String line = null;
            while ((line = reader.readLine()) != null) {
                parseWordDefinitionLine(line);
            }
	} catch (IOException e) {
	    System.err.println("ERROR: Unable to parse words file: " + e);
	}
    }

    /**
     * Split a line into a DictionaryEntry value
     */
    private DictionaryEntry parseWordDefinitionLine(String line) {
	String splitCharacter = ":";
	String spl[] = line.split(splitCharacter);
        if (spl.length == 1) {
            // check for oddities 1
            System.err.println("WARNING: Empty string or odd character (" 
                    + line + "). Skipping...");
            return null;
        }
        else if (spl.length != 2) {
            // check for oddities 2
	    System.err.println("WARNING: Malformed word definition (" 
                    + spl.length + " elements instead of two.): " + spl[0]);
	    return null;
	} else {
            // add the value
	    DictionaryEntry d = new DictionaryEntry(spl[0].trim(), spl[1].trim());
            System.out.println("Adding new dictionary entry (" 
                    + currentWordIndex +"): " + d);
            // update word index
            currentWordIndex++;
            return d;
	}
    }

    public ArrayList<DictionaryEntry> getNEntries(Integer numberOfEntriesToReturn) {
	// if trying to get more entries than are left
	if (currentWordIndex + numberOfEntriesToReturn >= entries.size()) {
	    int numLeft = currentWordIndex + numberOfEntriesToReturn - entries.size();
	    System.out.println("WARNING: We've run out of words.");
	    System.out.println("You'll get the rest (" + numLeft + ")");
	    // shorten number of entries to return to num left in dictionary
	    numberOfEntriesToReturn = numLeft;
	} 
	
	// update current word index
	currentWordIndex += numberOfEntriesToReturn;

	// return entries
	return (ArrayList<DictionaryEntry>) 
                entries.subList(
                currentWordIndex, 
                currentWordIndex + numberOfEntriesToReturn);
    }
    
    public String toString() {
        String str = "WordDictioary:\n";
        for (DictionaryEntry e : entries)
            str += e.toString();
        return str;
    }

    /**
     * Get all entries in this dictionary
     */
    public ArrayList<DictionaryEntry> getEntries() {
        return entries;
    }
    
    /**
     * remove a value from the list of entries
     */
    public void removeEntry(DictionaryEntry entry) {
        entries.remove(entry);
    }
    
    /**
     * Get an entry given the word
     */
    public DictionaryEntry getEntry(String word) {
        for (DictionaryEntry e : entries) {
            if (e.getWord().equalsIgnoreCase(word)) {
                return e;
            }
        }
        log.warn("Didn't find entry: " + word + " in this dictionary: " + this);
        return null;
    }
	

}