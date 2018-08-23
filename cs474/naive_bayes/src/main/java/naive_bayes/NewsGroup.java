package naive_bayes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
/**
 * NewsGroup class represents a newsgroup in the dataset
 * @author tmusa
 *
 */
public class NewsGroup {
	/**
	 * NewsGroup id
	 */ 
	private int id;
	/**
	 * list of documents belonging to the group
	 */
	private List<Document> docs;
	/**
	 * Prior for the group
	 */
	private double prior;
	/**
	 * total words in the news group
	 */
	private int totalWords;
	/**
	 * vocabulary size (of the training dataset)
	 */
	private int vocabSize;
	/**
	 * name of news group
	 */
	private String name;
	/**
	 * map of wordids to their total wordcount across 
	 * all the documents in the groups
	 */
	private HashMap<Integer,Integer> occurance;

	//unused constructor
	public NewsGroup() {
		occurance = new HashMap<Integer,Integer>();
		docs = new ArrayList<Document>();
	}
	/**
	 * NewsGroup constructor
	 * @param id
	 * @param name
	 * @param totalVocabSize
	 */
	public NewsGroup(int id, String name, int totalVocabSize) {
		occurance = new HashMap<Integer,Integer>();
		docs = new ArrayList<Document>();
		this.id = id;
		this.name = name;
		this.vocabSize = totalVocabSize;
	}
	//sets the occurrence of every word (yes I realized I misspelled it)
	public void setOccurance() {
		//		ArrayList<Integer> ids = new ArrayList<Integer>();
		HashSet<Integer> ids = new HashSet<Integer>();
		//ArrayList<Integer> values = new ArrayList<Integer>();
		for(Document d: docs) {
			for(int word : d.getWordList()) {
				ids.add(word);
			}
		}
		for(int i : ids) {
			int value = 0;
			for(Document d: docs) {
				value+= d.getWordCount(i);
			}
			occurance.put(i, value);
		}
	}


	public int occuranceWord(int wordId) {
		return occurance.get(wordId) == null ? 0 : occurance.get(wordId);
	}

	public int occuranceWordAlt(int wordId) {
		int tot = 0;
		for(Document d: docs) {
			tot+= d.getWordCount(wordId);
		}
		return tot;
	}

	public boolean contains(Document doc) {
		return docs.contains(doc);
	}

	public double maximumLikelihoodEstimator(int wordId) {
		return  occuranceWord(wordId) / (double) totalWords;
	}
	public double bayesianEstimator(int wordId) {
		return (occuranceWord(wordId) +1) / ((double) totalWords + vocabSize);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Document> getDocs() {
		return docs;
	}
	public void addDoc(Document doc) {
		this.docs.add(doc);
	}
	public void setDocs(List<Document> docs) {
		this.docs = docs;
	}
	public double getPrior() {
		return prior;
	}
	public void setPrior(double prior) {
		this.prior = prior;
	}
	public void setPrior(int totalDataSetSize) {
		this.prior = ((double) this.docs.size()) / (double) totalDataSetSize;
	}
	public int getTotalWords() {
		return totalWords;
	}
	public void setTotalWords() {
		int tw= 0;
		for(Document d : this.docs) {
			tw += d.getTotalWords();
		}
		this.totalWords = tw;
	}

	public String toString() {
		return id + " , " + name;
	}

	public String getName() {
		return name;
	}
	public String prettyPrintPrior() {
		return "P( "+name+ " ) = "+ prior;
	}

	public int getTotalUniqueWords() {
		int t= 0;
		for(Document d: docs)
			t+=d.getTotalUniqueWords();
		return t;
	}

}
