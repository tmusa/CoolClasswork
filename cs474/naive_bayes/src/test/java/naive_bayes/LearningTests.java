package naive_bayes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

public class LearningTests {
	private static final int TOTAL_WORDS_ACROSS_GROUPS = 2765300; //num lines in train_data_cvs
	private static final int NUMBER_OF_GROUPS = 20;
	private static String[] args = {"vocabulary.txt" 
			,"map.csv" 
			,"train_label.csv" 
			,"train_data.csv" 
			,"test_label.csv"
			, "test_data.csv"};
	private static ArrayList<NewsGroup> groups;
	private static ArrayList<Document> docs;
	private static ArrayList<Integer> labels;
	@BeforeClass
	public static void setUp() {
		int numVocabWords = NaiveBayes.getVocabCount(args[0]);
		groups = NaiveBayes.creatNewsGroups(args[1], numVocabWords);
		docs = NaiveBayes.createAllDocuments(args[3]);
		labels = NaiveBayes.createLabelList(args[2]);
		NaiveBayes.placeAllDocuments(groups,docs,labels);

	}
	@Test
	public void numberDocsAboveZero() {
		assertTrue(docs.size() > 0);
	}

	@Test
	public void allGroupsHaveAtLeastOneDoc() {
		boolean nil = false;
		for(NewsGroup g: groups) {
			if(g.getDocs().isEmpty())
				nil = true;
		}
		assertFalse(nil);
	}

	@Test
	public void numberGroupsIsTwenty() {
		assertTrue(groups.size() == NUMBER_OF_GROUPS);
	}

	@Test
	public void noZeroWordDoc() {
		boolean nil = false;
		for(NewsGroup g: groups) {
			if(g.getDocs().isEmpty())
				nil = true;
			for(Document d: g.getDocs()) {
				if(d.getTotalWords() == 0)
					nil = true;
			}
		}
		assertFalse(nil);
	}

	/**
	 * Tests that the number of words parsed into groups 
	 * equals the number of words in the data file
	 */
	@Test
	public void totalWordsInGroups() {
		int totalWords = 0;
		for(NewsGroup g: groups) {
			totalWords+= g.getTotalWords();
		}
		System.out.println(totalWords);
		assertTrue(totalWords == TOTAL_WORDS_ACROSS_GROUPS);
	}

	/**
	 * Tests that priors add up to 1 and that no prior is zero
	 */
	@Test
	public void priorsAreReasonable() {
		double tot = 0;
		boolean zeroPrior = false;
		for(NewsGroup g: groups) {
			double p = g.getPrior();
			tot+=p;
			if(p == 0.0) {
				zeroPrior = true;
			}
		}
		assertTrue((tot == 1) && !zeroPrior);
	}
	/**
	 * Tests that the bayesian estimator will never output zero
	 */
	@Test 
	public void baysNeverZeroResult() {
		boolean nil = false;
		//word ids are only positive so -1 is testing a "word not found" case
		for(int i = -1; i< 100; i++) {
			for(NewsGroup g: groups) {
				if(g.bayesianEstimator(i) == 0)
					nil = true;
			}
		}
		assertFalse(nil);
	}

	@Test
	public void mleZeroResultWhenNotFound() {
		boolean ye = true;
		for(NewsGroup g: groups) {
			//no word id has a negative value 
			if(g.maximumLikelihoodEstimator(-1) != 0)
				ye = false;
		}
		assertTrue(ye);
	}

	@Test 
	public void estimatorPredictsSame() {
		double bestBays = Double.MIN_VALUE;
		double bestMle = Double.MIN_VALUE;
		NewsGroup boo = null;
		String g1 = "";
		String g2 = "";
		boolean disagree = false;
		for(int i = 20; i < 21; i++) {
			for(NewsGroup g: groups) {
				double b = g.bayesianEstimator(i);
				double m = g.maximumLikelihoodEstimator(i);
				if(b>bestBays) {
					bestBays = b;
					boo = g;
					g1 = g.getName();
				}
				if(m>bestMle) {
					bestMle = m;
					g2 = g.getName();
				}
			}
			if(!g1.equals(g2)) {
				disagree = true;
			}
			System.out.println(boo.occuranceWord(20));
			System.out.println(boo.getTotalWords());
			System.out.println(20);
			System.out.println(i+ " "+ g1 +" "+ bestBays+ " : " +g2+ " "+ bestMle);
		}
		assertFalse(disagree);
	}
	
	@Test
	public void groupPredictionSame() {
		Document inGroup1 = groups.get(0).getDocs().get(0);
		NewsGroup g1 = NaiveBayes.mostLikelyGroup(groups, inGroup1, true); //bayesian 
		NewsGroup g2 = NaiveBayes.mostLikelyGroup(groups, inGroup1, false);//mle
		NewsGroup g3 = groups.get(0);
		assertTrue(g1.getId() == g2.getId() && g1.getId() == g3.getId());
	}

}
