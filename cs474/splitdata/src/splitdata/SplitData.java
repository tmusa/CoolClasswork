package splitdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;


public class SplitData {
	//run with command : -seed house-votes-84-data.csv breast-cancer-wisconsin-data.csv
	private static int groups = 5;
	private static HashMap<Integer, String> nominals;
	private static HashMap<Integer, String> tumor;
	private static Random r;
	private static long seed = 2046169844592413L;
	private static final String houseVotesFile = "house-votes-84.csv";
	private static final String breastCancerFile = "breast-cancer-wisconsin.csv";
	public static void main(String[] args) {
		r = new Random();
		nominals = new HashMap<Integer,String>();
		tumor = new HashMap<Integer,String>();
		nominals.put(1, "a");
		nominals.put(2, "b");
		nominals.put(3, "c");
		nominals.put(4, "d");
		nominals.put(5, "e");
		nominals.put(6, "f");
		nominals.put(7, "g");
		nominals.put(8, "h");
		nominals.put(9, "i");
		nominals.put(10, "j");
		tumor.put(2, "benign");
		tumor.put(4, "malignant");

		if(argsContains("-m", args)) {
			calcConfidence(args);
		}else {
			genFiles(args);
		}
	}
	private static void genFiles(String[] args) {
		int cancerIndex = indexOfArg(breastCancerFile, args);
		int votesIndex = indexOfArg(houseVotesFile, args);

		boolean nominal = argsContains("-n", args);
		boolean seedme = argsContains("-s", args);
		boolean g = argsContains("-g", args);
		if(g) {
			int groupset = indexContains("-g",args);
			try {
				groups = Integer.parseInt(args[groupset].split("=")[1]);
			}catch (Exception e) {
				groups = 5;
			}
		}
		if(seedme)
			r.setSeed(seed);


		String dataset = args[votesIndex];
		String datasetTwo = args[cancerIndex];
		fixHouseSet(dataset);
		fixCancerSet(datasetTwo, nominal);
		dataset = dataset.split("\\.")[0]+"_fix"+".csv";
		datasetTwo = datasetTwo.split("\\.")[0]+"_fix"+".csv";
		if(nominal) {
			for(int i = 0; i< groups; i++) {
				createSplits(i, dataset, false, r);
				createSplits(i, datasetTwo, true, r);
			}

		}else {
			for(int i = 0; i< groups; i++) {
				createSplits(i, dataset, false, r);
				createSplits(i, datasetTwo, false, r);
			}
		} 
	}
	private static void calcConfidence(String[] args) {
		int len = args.length-1;
		double[] dubs = new double[len];
		double sum = 0;
		double average = 0;
		double stdsum = 0;
		double stddev = 0;
		double[] range = new double[2];
		try {
			for(int i= 1; i<args.length; i++) {
				double d = Double.parseDouble(args[i]);
				dubs[i-1]= d;
				sum+= d;
			}
			average = sum/ len;
			for(double f : dubs) {
				stdsum += Math.pow((f-average), 2);	
			}
			stdsum = stdsum / (len-1);
			stddev = Math.sqrt(stdsum);

			range[0] = average - 1.960 *(stddev / Math.sqrt(len));
			range[1] = average + 1.960 *(stddev / Math.sqrt(len));
		}catch (NumberFormatException e) {
			System.out.println("Poorly formatted input!\n"
					+ "When calculating 95% confidence interval "
					+ "please input only numbers.\n"
					+ "Format: { -m (double d)* }");
		}
		System.out.println(Arrays.toString(range));
	}
	private static void fixHouseSet(String dataset) {
		String name = dataset.split("\\.")[0];
		String votesHeader = "handicapped-infants,water-project-cost-sharing,"
				+ "adoption-of-the-budget-resolution,physician-fee-freeze,el-salvador-aid,"
				+ "religious-groups-in-schools,anti-satellite-test-ban,aid-to-nicaraguan-contras,"
				+ "mx-missile,immigration,synfuels-corporation-cutback,education-spending,"
				+ "superfund-right-to-sue,crime,duty-free-exports,export-administration-act-south-africa,Class Name";
		try {
			FileReader f = new FileReader(dataset);
			BufferedReader b = new BufferedReader(f);
			String line ="";
			String fixedfile = votesHeader+ "\n";
			while((line = b.readLine()) != null) {
				String[] book = line.split(",");
				for(int i = 1; i< book.length; i++) {
					fixedfile += book[i] + ",";
				}
				fixedfile += book[0] +  "\n";
			}
			b.close();

			FileWriter fu = new FileWriter(name+"_fix"+".csv");
			fu.write(fixedfile);
			fu.close();
		}catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private static int indexContains(String flag, String[] args) {
		int i = 0;
		for(String a: args) {
			if (a.contains(flag)) {
				return i;
			}else {
				i++;
			}
		}
		return -1;
	}
	private static int indexOfArg(String flag, String[] args) {
		int i = 0;
		for(String a: args) {
			if (a.equals(flag)) {
				return i;
			}else {
				i++;
			}
		}
		return -1;
	}

	private static boolean argsContains(String flag, String[] args) {
		for(String a: args) {
			if(a.equals(flag) || a.contains(flag)) {
				return true;
			}	
		}
		return false;
	}
	private static void replaceMissing(String dataset) {
		try {
			FileReader f = new FileReader(dataset);
			BufferedReader b = new BufferedReader(f);

			String line = b.readLine();
			//			bu.write(line); //read and write header line to both the split and the training
			//			buu.write(line);
			int len = line.split(",").length;
			int[][] arr = new int[len][10];
			int[] mode = new int[len];
			//			String testfile = ""+line + "\n";
			while((line = b.readLine()) != null) {
				String[] l = line.split(",");
				for(int i = 1; i< len-1; i++) {
					if(!l[i].equals("?"))
						arr[i][Integer.parseInt(l[i])-1] += 1;
				}
			}
			for(int i = 1; i<len-1 ; i++) {
				int count =0;
				int c = 0;
				for(int j = 0; j< 10; j++) {
					if(arr[i][j] > count) {
						count = arr[i][j];
						c= j+1;
					}
				}
				mode[i] =c;
			}
			//at this point mode contains the most common values in each attribute row 2-10
			b.close();

			//			FileWriter fu = new FileWriter(dataset.split(".")[0]+"-nomissing.csv");
			//			fu.write(testfile);
			//			fu.close();
			//			b.close();
			//			fu.close();
			//			fuu.close();
		}catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void createSplits(int splitnum, String dataset, boolean bool, Random r) {
		String name = dataset.split("\\.")[0];
		try {
			FileReader f = new FileReader(dataset);
			BufferedReader b = new BufferedReader(f);
			boolean isCancer = dataset.equals(breastCancerFile);
			String line = b.readLine();

			String testfile = ""+line + "\n";
			String trainfile = ""+line+ "\n";
			while((line = b.readLine()) != null) {
				String[] book = line.split(",");
				if(!isCancer) {
					if(r.nextDouble() < (1.0/groups)) {
						testfile += line + "\n";
					}else {
						trainfile += line+"\n";
					}
				}else {
					String tmp = "";
					for(int i = 0; i< book.length-1; i++) {
						if(book[i].equals("?"))
							tmp+= "?,";
						else if(bool)
							tmp += nominals.get(Integer.parseInt(book[i])) + ",";
						else
							tmp += book[i] + ",";
					}
					tmp += tumor.get(Integer.parseInt(book[book.length-1]))+ "\n";

					if(r.nextDouble() < (1.0/groups)) {
						testfile += tmp;
					}else {
						trainfile+=tmp;
					}
				}
			}
			b.close();
			FileWriter fu;
			FileWriter fuu;
			if(!isCancer) {
				name = name.split("_")[0];
			}
			fu = new FileWriter("test"+name+"_"+splitnum+".csv");

			fuu = new FileWriter("train"+name+"_"+splitnum+".csv");
			fu.write(testfile);
			fuu.write(trainfile);
			fu.close();
			fuu.close();
		}catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void fixCancerSet (String dataset, boolean nominal) {
		String name = dataset.split("\\.")[0];
		try {
			FileReader f = new FileReader(dataset);
			BufferedReader b = new BufferedReader(f);

			String line = "";
			String cancerHeader = "Clump Thickness,"
					+ "Uniformity of Cell Size,Uniformity of Cell Shape,"
					+ "Marginal Adhesion,Single Epithelial Cell Size,"
					+ "Bare Nuclei,Bland Chromatin,Normal Nucleoli,Mitoses,Class";

			String fixedfile = cancerHeader + "\n";
			while((line = b.readLine()) != null) {
				String[] book = line.split(",");
				for(int i = 1; i< book.length-1; i++) {
					if(book[i].equals("?"))
						fixedfile+= "?,";
					else if (nominal)
						fixedfile += nominals.get(Integer.parseInt(book[i])) + ",";
					else
						fixedfile += book[i] + ",";
				}
				fixedfile += tumor.get(Integer.parseInt(book[book.length-1]))+ "\n";
			}
			b.close();

			FileWriter fu = new FileWriter(name+"_fix"+".csv");
			fu.write(fixedfile);
			fu.close();
		}catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
