package naive_bayes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Document class represents a document in the dataset
 * 
 */
public class Document {
	/**
	 * id of document
	 */ 
	private int docId;
	/**
	 * mapping of words to their word counts
	 */
	private HashMap<Integer, Integer> words;
	/**
	 * a list of words
	 */
	private List<Integer> wordList;
	
	//I believe this is an unused constructor
	public Document() {
		this.wordList = new ArrayList<Integer>();
		this.words = new HashMap<Integer, Integer>();
	}
	/**
	 * Constructor for Document
	 * @param docId
	 */
	public Document(int docId) {
		this.wordList = new ArrayList<Integer>();
		this.words = new HashMap<Integer, Integer>();
		this.docId = docId;
	}
	
	public int getWordCount(Integer wordId) {
		return this.words.get(wordId) == null ? 0 : this.words.get(wordId);
	}
	public void add(int wordId, int value) {
		this.words.put(wordId, value);
		this.wordList.add(wordId);
	}
	
	public List<Integer> getWordList() {
		return this.wordList;
	}
	
	public int getId() {
		return this.docId;
	}
	
	public int getTotalWords() {
		int tot = 0;
		for(Integer i : wordList) {
			tot+= this.words.get(i);
		}
		return tot;
	}
	public boolean equals(Object o) {
		if(o instanceof Document) {
			return this.docId == ((Document)o).docId;
		}else {
			return false;
		}
	}
	
	public int getTotalUniqueWords() {
		return words.size();
	}
}
