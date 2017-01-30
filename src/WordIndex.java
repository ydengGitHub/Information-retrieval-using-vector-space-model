import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class build an index for single words.
 * 
 * @author YAN DENG
 *
 */
public class WordIndex {

	private String path;
	private HashMap<String, Integer> termsMap; /*Used to check term's index*/
	private ArrayList<String> termsList;
	private HashMap<String, Integer> docsMap;/*Used to check document's index*/
	private ArrayList<String> docsList;
	private int numOfTerms;
	private int numOfDocs;
	private ArrayList<DictEntry> invertedIndex;
	private HashMap<String, Integer> queryList;

	/**
	 * Gets the name of a folder containing document collection as parameter.
	 * 
	 * @param path
	 */
	public WordIndex(String path) {
		this.path = path;
		termsMap = new HashMap<String, Integer>();
		termsList = new ArrayList<String>();
		docsMap = new HashMap<String, Integer>();
		docsList = new ArrayList<String>();
		this.numOfTerms = 0;
		this.numOfDocs = 0;
		this.invertedIndex = new ArrayList<DictEntry>();
		this.queryList = new HashMap<String, Integer>();
		this.buildIndex();
	}

	/**
	 * This method builds the inverted index. Go through all the documents in
	 * the given folder and collect all terms and store them in the termsList
	 * and termsMap.
	 */
	public void buildIndex() {
		if(!(invertedIndex==null||invertedIndex.isEmpty())){
			return;/*Inverted Index has already been builded.*/
		}
		File[] files = new File(path).listFiles();
		if (files.length == 0)
			throw new IllegalArgumentException("The folder is empty.");
		numOfDocs = 0;
		for (int i = 0; i < files.length; i++) {
			String docName = files[i].getName();
			if (docName.equals(".DS_Store"))
				continue;
			docsList.add(docName);
			docsMap.put(docName, numOfDocs);
			HashMap<String, Integer> docTerms = readFile(files[i]);
			if (docTerms == null || docTerms.size() == 0) {
				numOfDocs++;
				System.out.println(files[i].getName() + " doesn't contain any term.");
				continue;
			}
			for (String s : docTerms.keySet()) {
				if (termsMap.containsKey(s)) {
					int index = termsMap.get(s);
					invertedIndex.get(index).numOfDocs++;
					invertedIndex.get(index).postingsList.put(docName, docTerms.get(s));
				} else {
					termsList.add(s);
					termsMap.put(s, numOfTerms);
					HashMap<String, Integer> pList = new HashMap<String, Integer>();
					pList.put(docName, docTerms.get(s));
					invertedIndex.add(new DictEntry(s, 1, pList));
					numOfTerms++;
				}
			}
			numOfDocs++;
		}
		System.out.printf("Number of files: %d;\nNumber of terms: %d\n", numOfDocs,termsMap.size());
		
	}

	/**
	 * Each item of the array list is a tuple consisting of document name d and
	 * the frequency of term t in document d.
	 * 
	 * @param t
	 *            given term
	 * @return the ArrayList
	 */
	public ArrayList<PostListEntry> postingsList(String t) {
		t=t.toLowerCase();
		ArrayList<PostListEntry> postingsList = new ArrayList<PostListEntry>();
		if(!termsMap.containsKey(t)){
			System.out.println(t+" does not exist in dictionary.");
			return postingsList;
		}
		int index = termsMap.get(t);
		HashMap<String, Integer> postingsMap = invertedIndex.get(index).postingsList;
		for (String s : postingsMap.keySet()) {
			postingsList.add(new PostListEntry(s, postingsMap.get(s)));
		}
		Collections.sort(postingsList);
		return postingsList;
	}

	/**
	 * Prints the contents of the list returned by postingsList(t) in a
	 * human-readable form.
	 * 
	 * @param t
	 *            given term
	 */
	public void printPostingsList(String t) {
		System.out.println("Postings List of term '" + t + "':");
		System.out.printf("%-25s| %-35s\n", "-----------------------", "-----------------------------------");
		System.out.printf("%-25s| %-35s\n","Document Name","Number of Times " + t + " appears");
		System.out.printf("%-25s+ %-35s\n", "-----------------------", "-----------------------------------");
		ArrayList<PostListEntry> postList = this.postingsList(t);
		for (PostListEntry si : postList) {
			System.out.printf("%-25s| %-35s\n",si.docName,si.numAppears);
		}
		System.out.printf("%-25s| %-35s\n\n", "-----------------------", "-----------------------------------");
	}

	/**
	 * Returns the weight of term t in document d.
	 * 
	 * @param term
	 *            given term
	 * @param doc
	 *            given document
	 * @return the weight of term t in document d
	 */
	public double weight(String term, String doc) {
		double weight = 0;
		int index = termsMap.get(term);
		double dft = invertedIndex.get(
				index).numOfDocs;/* number of documents in which t appears */
		HashMap<String, Integer> postingsList = invertedIndex.get(index).postingsList;
		double tftd;/* number of times term t appears in document d */
		if (postingsList == null || (!postingsList.containsKey(doc))) {
			return 0;
		} else {
			tftd = postingsList.get(doc);
		}
		weight = (Math.log(1 + tftd) / Math.log(2)) * (Math.log10(this.numOfDocs / dft));
		return weight;
	}

	/**
	 * Go through the given file, and store the non-repeated terms with their
	 * #appears in a hash map.
	 * 
	 * @param file
	 * @return Returns the hashset of terms in the given file
	 */
	private HashMap<String, Integer> readFile(File file) {
		HashMap<String, Integer> docTerms = new HashMap<String, Integer>();
		try {
			Scanner scan = new Scanner(new FileInputStream(file));
			String[] line;
			while (scan.hasNextLine()) {
				line = scan.nextLine().split(
						"[,.:;\\s\\']+"); /* s means single white space */
				if (line == null || line.length == 0)
					continue;
				for (int i = 0; i < line.length; i++) {
					String word = line[i].toLowerCase();
					if (!(word.length() < 3 || (word.length() == 3 && word.equals("the")))) {
						if (docTerms.containsKey(word)) {
							Integer num = docTerms.get(word);
							docTerms.replace(word, num + 1);
						} else {
							docTerms.put(word, 1);
						}
					}
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docTerms;
	}

	/**
	 * Calculate the cosine similarities between the query q and all documents
	 * 
	 * @param query
	 * @return a ArrayList containing the <docName, cosine similarities> pairs
	 */
	public ArrayList<StringDoublePair> computeSimilarities(String query) {
		System.out.println("Computing the cosine similarities between the query and " + numOfDocs + " files......");
		ArrayList<StringDoublePair> sims = new ArrayList<StringDoublePair>();
		double[] length = new double[numOfDocs];
		for (int i = 0; i < numOfDocs; i++) {
			length[i] = computeVdi(docsList.get(i));
		}

		File tmpFile = new File("query_file.txt");
		try {
			PrintWriter writer = new PrintWriter(tmpFile);
			writer.println(query);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		queryList = readFile(tmpFile);
//		for(String s:queryList.keySet()){
//			System.out.println(s+" appears in query "+queryList.get(s)+" times");
//		}

		double[] scores = new double[numOfDocs];
//		Arrays.fill(scores, 0.0);
		for (String term:queryList.keySet()) {
			HashMap<String, Integer> postingsList = invertedIndex.get(termsMap.get(term)).postingsList;
			for (String doc : postingsList.keySet()) {
				double weightTq = weightTq(term);
				double weightTd = weight(term, doc);				
				scores[docsMap.get(doc)] += weightTd * weightTq;
			}
		}
//		System.out.println("After First step: scores[0]="+scores[0]+"; scores[n]="+scores[numOfDocs-1]);
		/*Compute ||v(q)||*/
		double vqSquare = 0;
		for (String term : queryList.keySet()) {
			if (!termsMap.containsKey(term)) {
				continue;
			}
			vqSquare += Math.pow(weightTq(term), 2);
		}
		double vq = Math.sqrt(vqSquare);

		for (int l = 0; l < numOfDocs; l++) {
			scores[l] = scores[l] / (length[l] * vq);
			sims.add(new StringDoublePair(docsList.get(l), scores[l]));
		}
		return sims;
	}

	/**
	 * Retrieve a set S consisting of top 2k documents
	 * 
	 * @param query
	 * @param k
	 * @return
	 */
	public ArrayList<StringDoublePair> getTop2KDocs(String query, int k) {
		ArrayList<StringDoublePair> sims = this.computeSimilarities(query);
//		System.out.println("After Third step: scores[0]="+sims.get(0).num+"; scores[n]="+sims.get(numOfDocs-1).num);
		Collections.sort(sims);
//		System.out.println("After Fourth step: scores[0]="+sims.get(0).num+"; scores[n]="+sims.get(numOfDocs-1).num);
		ArrayList<StringDoublePair> result = new ArrayList<StringDoublePair>();
		for (int i = 0; i < 2 * k; i++) {
			result.add(sims.get(numOfDocs - 1 - i));
		}
		outputTopKResult(result,2*k);
		return result;
	}

	/**
	 * Output the Top k result only based on the  similarities
	 * @param result
	 * @param k
	 */
	public void outputTopKResult(ArrayList<StringDoublePair> result,int k) {
		System.out.println("Top " + k + " documents based on similarities:");
		System.out.printf("%-25s| %-30s\n", "-----------------------", "------------------------------");
		System.out.printf("%-25s| %-30s\n", "Document Name", "cosine similarites with query");
		System.out.printf("%-25s+ %-30s\n", "-----------------------", "------------------------------");
		for (StringDoublePair pair : result) {
			System.out.printf("%-25s| %-30f\n", pair.docName, pair.num);
		}
		System.out.printf("%-25s| %-30s\n\n", "-----------------------", "------------------------------");				
	}
	
	
	
	/**
	 * Compute the ||v(di)||
	 * 
	 * @param docIndex
	 * @return
	 */
	private double computeVdi(String doc) {
		double vDiSquare = 0;
		for (int i = 0; i < numOfTerms; i++) {
			vDiSquare += Math.pow(weight(termsList.get(i), doc), 2);
		}
		return Math.sqrt(vDiSquare);
	}

	/**
	 * Compute the weight(t,q) for term t in query q
	 * 
	 * @param termIndex
	 * @return
	 */
	private double weightTq(String term) {
		double weight = 0;
		double tftq;/*
		 * number of times term t appears in document d
		 */		
		if (!queryList.containsKey(term)) {
			return 0;
		} else {
			tftq = queryList.get(term);
		}
		weight = (Math.log(1 + tftq) / Math.log(2));
		return weight;
	}

	/**
	 * Inner class, to store the <docName, numAppears> pair.
	 * 
	 * @author YAN
	 *
	 */
	class PostListEntry implements Comparable<PostListEntry> {
		public String docName;
		public int numAppears;

		public PostListEntry(String name, int num) {
			this.docName = name;
			this.numAppears = num;
		}

		@Override
		public int compareTo(PostListEntry o) {
			return docsMap.get(this.docName) - docsMap.get(o.docName);
		}
	}

	/**
	 * Inner class, to store the inverted list entry.
	 * 
	 * @author YAN DENG
	 *
	 */
	class DictEntry {
		public String termName;
		public Integer numOfDocs;
		public HashMap<String, Integer> postingsList;

		public DictEntry(String name, Integer num, HashMap<String, Integer> list) {
			this.termName = name;
			this.numOfDocs = num;
			this.postingsList = list;
		}
	}
}
