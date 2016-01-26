package wagyx001.pa1.core;

import java.io.Serializable;

public class DictionaryEntry implements Serializable {

    public static long serialVersionUID = 28934L;
    
    private String word;
    private String definition;

    public DictionaryEntry(String word, String definition) {
	this.word = word.trim();
	this.definition = definition.trim();
    }

    public String getWord() {
	return word;
    }

    public String getDefinition() {
	return definition;
    }
    
    public String toString() {
        return "[" + word + "] : [" + definition + "]\n";
    }
}
