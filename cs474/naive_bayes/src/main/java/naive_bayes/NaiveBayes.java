package naive_bayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
/**
 * NaiveBayes class utilizes Document & NewsGroup classes to implement
 * naive bayes learning model for the 20 newsgroups problem. 
 * 
 * not particularly optimized for performance but runs well enough for 
 * the purpose of this assignment.
 * 
 * @author tmusa
 *
 */
public class NaiveBayes {
	/**
	 * This hashset is used just so I can accurately count the number of unique
	 *   words in the training vocabulary. 
	 * 
	 * The ArrayList of priors is just defined so I can access the priors later
	 *   without passing around a parameter. 
	 */
	private static HashSet<Integer> words;
	private static ArrayList<Double> priors;
	public static void main(String[] args)  {
		words = new HashSet<Integer>();
		priors = new ArrayList<Double>();

		/*
		 * I choose not to parse the vocab file because I don't use it
		 * the code exists here incase you would like to modify this file
		 * and need that snippet
		 */
		//		int numVocabWords = getVocabCount(args[0]);
		//This next block parses all the information we need for the model
		ArrayList<Document> docs = createAllDocuments(args[3]);
		ArrayList<NewsGroup> groups = creatNewsGroups(args[1], words.size());
		ArrayList<Integer> labels = createLabelList(args[2]);
		ArrayList<Document> testDocs = createAllDocuments(args[5]);
		ArrayList<Integer> testLabels = createLabelList(args[4]); 

		/*
		 * here we place the documents into their designated groups and print 
		 * their priors
		 */
		placeAllDocuments(groups, docs, labels);

		ArrayList<Integer> guessTrainingLabels = new ArrayList<Integer>();

		ArrayList<Integer> guessTestLabels = new ArrayList<Integer>();
		ArrayList<Integer> guessMLETestLabels = new ArrayList<Integer>();

		/*
		 * These next too blocks have code commented out that tests the 
		 * training data set against the MLE estimator. You can uncomment it
		 * to see that stuff
		 */
		//		ArrayList<Integer> guessTrainMLE = new ArrayList<Integer>();
		//		ArrayList<String> guessMLETrain = new ArrayList<String>();

		for(Document d: docs) {
			int group = mostLikelyGroup(groups, d, true).getId();
			guessTrainingLabels.add(group);
			//			NewsGroup g =  mostLikelyGroup(groups, d, false);
			//			guessTrainMLE.add(g.getId());
			//			guessMLETrain.add(g.getName());
		}

		//runs test data set against model. 
		for(Document d: testDocs) {
			guessTestLabels.add(mostLikelyGroup(groups, d, true).getId());
			guessMLETestLabels.add(mostLikelyGroup(groups, d, false).getId());
		}
		
		//next few blocks create and prepare the confusion matrices 
		int confusionTraining[][] = new int[20][20];
		int confusionM[][] = new int[20][20];
		int confusionB[][] = new int[20][20];

		createConfusion(guessTrainingLabels, labels, confusionTraining);
		createConfusion(guessMLETestLabels, testLabels, confusionM);
		createConfusion(guessTestLabels, testLabels, confusionB);

		String confusionT = printConfusion(Arrays.deepToString(confusionTraining));
		String confusionMLE = printConfusion(Arrays.deepToString(confusionM));
		String confusionBAY = printConfusion(Arrays.deepToString(confusionB));

		//prints the training data with the MLE estimator. 
		//		printTrainingMleAccuracy(docs, groups, labels, guessTrainMLE);


		//this try block write & prints the accuracy data with "writeAccuracy"
		//it also writes the confusion matrices to a file
		
		try {
			writeAccuracy(groups, docs, testDocs, labels, 
					testLabels, guessTestLabels, 
					guessTrainingLabels, guessMLETestLabels);
			writeConfusion(confusionT, confusionMLE, confusionBAY);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//prints matrices
		printMatrices(confusionT, confusionMLE, confusionBAY);
	}

	/*
	 * This method calculates the group that document d is mostlikely to belong to
	 * if b is true then it will use the bayesian estimator else it will use MLE
	 */
	public static NewsGroup mostLikelyGroup(List<NewsGroup> groups, Document d, boolean b) {
		ArrayList<Double> likelihoods = new ArrayList<Double>();
		double likely = probDocInGroup(groups.get(0),d,b);
		likelihoods.add(likely);
		NewsGroup group = groups.get(0);
		for(int i = 1; i < groups.size(); i++) { //20
			double l = probDocInGroup(groups.get(i),d,b);
			likelihoods.add(l);

			/*these next two if statements are part of an effort to handle 
			 * the zero likelihood problem 
			 */
			if(likely == 0) {
				likely = l;
				group = groups.get(i);
			}
			if(b == false && l == 0)
				l = likely;

			if(Math.max(l, likely) > likely) {
				group  = groups.get(i);
				likely = l;
			}
		}
		//in the case that all 20 group likelihoods were 0 (MLE failure) then we randomly choose
		//based on priors.
		if(allZeros(likelihoods)) {
			int j = randomGroup(priors);
			group = groups.get(j);
		}
		return group;
	}

	/*
	 * Calculates the probability that a specific document is in a specific group
	 */
	public static double probDocInGroup(NewsGroup n, Document d, boolean bayesian) {
		double ret = 0;
		for(Integer wordId: d.getWordList()) {
			if(bayesian) {
				double h = Math.log(n.bayesianEstimator(wordId));
				ret += h;
			}else {
				double mle = n.maximumLikelihoodEstimator(wordId);

				if(mle != 0) {

					Double est =  Math.log(mle);
					if(est == null)
						est = 0.0;
					ret += est;
				}else {
					//enforces the zero likelihood issue to percolate up rather than
					//throw an error when the log is calculated. 
					return 0;
				}
			}
		}
		return ret+ Math.log(n.getPrior());
	}

	//checks if all the likelihoods are zero
	private static boolean allZeros(ArrayList<Double> likelihoods) {
		for(double d: likelihoods) {
			if(d != 0)
				return false;
		}
		return true;
	}

	//selects random group based on prior knowledge
	public static int randomGroup(ArrayList<Double> p) {
		int ret = 0;
		double[] r = new double[20];
		r[0]=p.get(0);
		for(int i = 1; i<20; i++) {
			r[i] = p.get(i)+r[i-1];
		}
		double t= Math.random(); 
		for(int i = 1; i<20; i++) {
			if(t> r[i-1] && t<=r[i] ) {
				ret = i;
			}
		}
		return ret;
	}

	/* 
	 * utility method that calculates the overall and class accuracies.
	 */
	public static double accuracyUtil (List<Document> docs, List<Integer> trueLabels, List<Integer> guessLabels, NewsGroup opt) {
		double r = 0;
		double t = 0;
		for(int i = 0; i< docs.size(); i++) {
			if(opt== null) {
				if(trueLabels.get(i).equals(guessLabels.get(i)))
					r+=1;
			}else {
				if(opt.getId() == trueLabels.get(i)) {
					t+=1;
					if(trueLabels.get(i).equals(guessLabels.get(i))) {
						r+=1;
					}
				}
			}
		}
		return opt == null? r/docs.size(): r/t;
	}

	/*
	 * Places documents into their appropriate NewsGroups
	 * then does some 'clean up' work. Which means it sets the total words for the group,
	 * sets the occurrence of every word in the group. Then it prints the priors of each group. 
	 */
	public static void placeAllDocuments(ArrayList<NewsGroup> groups, ArrayList<Document> docs,
			ArrayList<Integer> labels) {
		for( int i = 0; i< labels.size(); i ++) {
			int groupIndex = labels.get(i)-1;
			groups.get(groupIndex).addDoc(docs.get(i));
		}
		for(NewsGroup g: groups) {
			g.setTotalWords();
			g.setOccurance();
			g.setPrior(docs.size());
			priors.add(g.getPrior());
			System.out.println(g.prettyPrintPrior() );
		}
	}

	//creates news groups from file
	public static ArrayList<NewsGroup> creatNewsGroups(String map, int numVocabWords) {
		ArrayList<NewsGroup> groups = new ArrayList<NewsGroup>();
		try {
			FileReader f = new FileReader(map);
			BufferedReader b = new BufferedReader(f);
			String line = "";

			while((line = b.readLine()) != null) {
				String[] l = line.split(",");
				NewsGroup n = new NewsGroup(Integer.parseInt(l[0]), l[1], numVocabWords);
				groups.add(n);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return groups;
	}

	//creates documents from file
	public static ArrayList<Document> createAllDocuments(String data){
		ArrayList<Document> docs = new ArrayList<Document>();
		try {
			FileReader f = new FileReader(data);
			BufferedReader b = new BufferedReader(f);
			String line = "";
			int curDocID = Integer.MIN_VALUE;
			int curDocIndex = -1;
			while((line = b.readLine()) != null) {
				String[] l = line.split(",");
				if(curDocID != Integer.parseInt(l[0])) {
					curDocID = Integer.parseInt(l[0]);
					Document d = new Document(Integer.parseInt(l[0]));
					docs.add(d);
					curDocIndex++;
				}
				int wordid = Integer.parseInt(l[1]);
				if(!words.contains(wordid))
					words.add(wordid);
				docs.get(curDocIndex).add(wordid, Integer.parseInt(l[2]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return docs;
	}

	//creates document labels from file
	public static ArrayList<Integer> createLabelList(String labels){
		ArrayList<Integer> label = new ArrayList<Integer>();
		try {
			FileReader f = new FileReader(labels);
			BufferedReader b = new BufferedReader(f);
			String line = "";
			while((line = b.readLine()) != null) {
				String[] l = line.split(",");
				label.add(Integer.parseInt(l[0]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return label;
	}

	//prints matrices to console in a 'pretty' way
	private static void printMatrices(String confusionT, String confusionMLE, String confusionBAY) {
		System.out.println("\n\nPrinting Confusions Matricies......//\n");
		System.out.println("Training Confusion Matrix");
		System.out.println(confusionT);
		System.out.println("\n\nTestMLE Confusion Matrix");
		System.out.println(confusionMLE);
		System.out.println("\n\nTestBayesian Confusion Matrix");
		System.out.println(confusionBAY);
	}

	//writes matrices to file in a 'pretty' way
	private static void writeConfusion(String confusionT, String confusionMLE, String confusionBAY) {
		FileWriter fw;
		try {
			fw = new FileWriter("confusion.txt");
			fw.write("Training Confusion Matrix\n");
			fw.write(confusionT + "\n");
			fw.write("\nTestMLE Confusion Matrix\n");
			fw.write(confusionMLE + "\n");
			fw.write("\nTestBayesian Confusion Matrix\n");
			fw.write(confusionBAY );
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//creates a string representation for the matrix
	private static String printConfusion(String deep) {
		Scanner s = new Scanner(deep);
		String r = "";
		s.useDelimiter("],");
		r += (" "+s.next().substring(1)+"]" +"\n");
		while(s.hasNext()) {
			String d = s.next();
			if(d.charAt(d.length()-1)!=']')
				r+=(d+"]" + "\n");
			else
				r+=(" "+d.substring(1) + "\n");
		}
		return r;
	}

	//creates confusion matrix
	private static void createConfusion(ArrayList<Integer> guesses, ArrayList<Integer> labels,
			int[][] confusion) {
		for(int i = 0; i< guesses.size(); i++) {
			int j = labels.get(i)-1;
			int k = guesses.get(i)-1;
			confusion[j][k] += 1;
		}

	}

	//prints and writes the accuracy data. That's print to console & write to a file
	private static void writeAccuracy(ArrayList<NewsGroup> groups, ArrayList<Document> docs, 
			ArrayList<Document> testdocs, ArrayList<Integer> labels, ArrayList<Integer> testlabels,
			ArrayList<Integer> guessTestLabels, ArrayList<Integer> guessTrainingLabels,
			ArrayList<Integer> guessMLETestLabels) throws IOException {

		FileWriter fw = new FileWriter("accuracy.txt");

		double a = accuracyUtil(docs, labels, guessTrainingLabels, null);

		fw.write("Overall Accuaracy :: Training: "+a + "\n");
		System.out.print("\n\nOverall Accuaracy :: Training: "+a + "\n");
		System.out.print("Class Accuracy:\n");
		fw.write("Class Accuracy:\n");

		for(NewsGroup g : groups) {
			a= accuracyUtil(docs, labels, guessTrainingLabels, g);
			System.out.print(g.getName()+": "+a + "\n");
			fw.write(g.getName()+": "+a + "\n");
		}

		a =accuracyUtil(testdocs, testlabels, guessMLETestLabels, null);

		fw.write("\n\nOverall Accuaracy :: TestMLE: "+a+ "\n");
		System.out.print("\n\nOverall Accuaracy :: TestMLE: "+a+ "\n");
		fw.write("Class Accuracy:\n");
		System.out.print("Class Accuracy:\n");

		for(NewsGroup g : groups) {
			a=accuracyUtil(testdocs, testlabels, guessMLETestLabels, g);
			fw.write(g.getName()+": "+a + "\n");
			System.out.print(g.getName()+": "+a + "\n");
		}

		a = accuracyUtil(testdocs, testlabels, guessTestLabels, null);

		fw.write("\n\nOverall Accuaracy :: TestBayesian: "+a + "\n");
		System.out.print("\n\nOverall Accuaracy :: TestBayesian: "+a + "\n");
		fw.write("Class Accuracy:\n");
		System.out.print("Class Accuracy:\n");

		for(NewsGroup g : groups) {
			a=accuracyUtil(testdocs, testlabels, guessTestLabels, g);
			fw.write(g.getName()+": "+a+"\n");
			System.out.print(g.getName()+": "+a+"\n");
		}
		fw.close();
	}

	//this method just prints the accuracy to the console (never used)
	private static void printAccuracy(ArrayList<NewsGroup> groups, ArrayList<Document> docs, 
			ArrayList<Document> testdocs, ArrayList<Integer> labels, ArrayList<Integer> testlabels,
			ArrayList<Integer> guessTestLabels, ArrayList<Integer> guessTrainingLabels,
			ArrayList<Integer> guessMLETestLabels) {
		System.out.print("Overall Accuaracy :: Training: "+accuracyUtil(docs, labels, guessTrainingLabels, null) + "\n");
		System.out.print("Class Accuracy:\n");
		for(NewsGroup g : groups) {
			System.out.print(g.getName()+": "+accuracyUtil(docs, labels, guessTrainingLabels, g) + "\n");
		}

		System.out.print("\n\nOverall Accuaracy :: TestMLE: "+accuracyUtil(testdocs, testlabels, guessMLETestLabels, null)+ "\n");
		System.out.print("Class Accuracy:\n");
		for(NewsGroup g : groups) {
			System.out.print(g.getName()+": "+accuracyUtil(testdocs, testlabels, guessMLETestLabels, g) + "\n");
		}

		System.out.print("\n\nOverall Accuaracy :: TestBayesian: "+accuracyUtil(testdocs, testlabels, guessTestLabels, null) + "\n");
		System.out.print("Class Accuracy:\n");
		for(NewsGroup g : groups) {
			System.out.print(g.getName()+": "+accuracyUtil(testdocs, testlabels, guessTestLabels, g)+"\n");
		}
	}

	//does what it says. is unused
	private static void printTrainingMleAccuracy(ArrayList<Document> docs, ArrayList<NewsGroup> groups,
			ArrayList<Integer> labels, ArrayList<Integer> guessTrainMLE) {
		double a = accuracyUtil(docs, labels, guessTrainMLE, null);


		System.out.print("\n\nOverall Accuaracy :: TrainingMLE: "+a + "\n");
		System.out.print("Class Accuracy:\n");

		for(NewsGroup g : groups) {
			a= accuracyUtil(docs, labels, guessTrainMLE, g);
			System.out.print(g.getName()+": "+a + "\n");
		}
	}


	//this method counts the number of lines in a file  (no longer used outside of testing)
	public static int getVocabCount(String filename) {
		int count = 0;
		try {
			FileReader f = new FileReader(filename);
			BufferedReader b = new BufferedReader(f);
			while((b.readLine()) != null) {
				count ++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

}
