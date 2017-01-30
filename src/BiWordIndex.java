import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * This class build an index for bi-words.
 * @author YAN DENG
 *
 */
public class BiWordIndex {
	private String path;
	private HashMap<String, Integer> termsMap;
	private ArrayList<String> termsList;
	private HashMap<String, Integer> docsMap;
	private ArrayList<String> docsList;
	private int numOfBiWords;
	private int numOfDocs;
	private ArrayList<DictEntry> invertedIndex;
	private HashSet<String> queryBiWords;

	/**
	 * Gets the name of a folder containing document collection as parameter.
	 * 
	 * @param path
	 */
	public BiWordIndex(String path) {
		this.path = path;
		termsMap = new HashMap<String, Integer>();
		termsList = new ArrayList<String>();
		docsMap = new HashMap<String, Integer>();
		docsList = new ArrayList<String>();
		this.numOfBiWords = 0;
		this.numOfDocs = 0;
		this.invertedIndex = new ArrayList<DictEntry>();
		this.buildIndex();
	}

	/**
	 * This method builds the inverted index. Go through all the documents in
	 * the given folder and collect all terms and store them in the termsList
	 * and termsMap.
	 */
	public void buildIndex() {
		if (!(invertedIndex == null || invertedIndex.isEmpty())) {
			return;/* Inverted Index has already been builded. */
		}
		File[] files = new File(path).listFiles();
		if (files.length == 0)
			throw new IllegalArgumentException("The folder is empty.");
		numOfDocs = 0;
		// System.out.println("Number of files:" + files.length);
		for (int i = 0; i < files.length; i++) {
			String docName = files[i].getName();
			if (docName.equals(".DS_Store"))
				continue;
			docsList.add(docName);
			docsMap.put(docName, numOfDocs);
			HashSet<String> docBiWords = readFile(files[i]);
			if (docBiWords == null || docBiWords.size() == 0) {
				numOfDocs++;
//				System.out.println(files[i].getName() + " doesn't contain any biword.");
				continue;
			}
			for (String s : docBiWords) {
				if (termsMap.containsKey(s)) {
					int index = termsMap.get(s);
					invertedIndex.get(index).numOfDocs++;
					invertedIndex.get(index).postingsList.add(docName);
				} else {
					termsList.add(s);
					termsMap.put(s, numOfBiWords);
					HashSet<String> pList = new HashSet<String>();
					pList.add(docName);
					invertedIndex.add(new DictEntry(s, 1, pList));
					numOfBiWords++;
				}
			}
			numOfDocs++;
		}
//		System.out.println("Number of files:" + numOfDocs);
		System.out.printf("Number of biwords: %d\n\n",numOfBiWords);
	}

	/**
	 * Each item of the array list is a tuple consisting of document name d and
	 * the frequency of term t in document d.
	 * 
	 * @param biword
	 *            given term
	 * @return the ArrayList
	 */
	public ArrayList<String> postingsList(String biword) {
		biword=biword.toLowerCase();
		ArrayList<PostListEntry> postingsList = new ArrayList<PostListEntry>();
		ArrayList<String> list = new ArrayList<String>();
		if(!termsMap.containsKey(biword)){
			System.out.println(biword+" does not exist in biword dictionary.");
			return list;
		}
		int index = termsMap.get(biword);
		HashSet<String> postingsSet = invertedIndex.get(index).postingsList;
		for (String s : postingsSet) {
			postingsList.add(new PostListEntry(s));
		}
		Collections.sort(postingsList);		
		for (PostListEntry p : postingsList) {
			list.add(p.docName);
		}
		return list;
	}

	/**
	 * Prints the contents of the list returned by postingsList(t) in a
	 * human-readable form.
	 * 
	 * @param biword
	 *            given term
	 */
	public void printPostingsList(String biword) {
		System.out.println("Postings List of biword '" + biword + "':");
		System.out.println("--------------------");
		System.out.println("Document Name");
		System.out.println("--------------------");
		ArrayList<String> postList = this.postingsList(biword);
		for (String s : postList) {
			System.out.println(s);
		}
		System.out.printf("--------------------\n\n");
	}

	/**
	 * Go through the given file, and store the non-repeated terms with their
	 * #appears in a hash map.
	 * 
	 * @param file
	 * @return Returns the hashset of terms in the given file
	 */
	private HashSet<String> readFile(File file) {
		HashSet<String> docBiWords = new HashSet<String>();
		try {
			Scanner scan = new Scanner(new FileInputStream(file));
			String[] line;
			String lastWord = null;
			while (scan.hasNextLine()) {
				line = scan.nextLine().split(
						"[,.:;\\s\\']+"); /* s means single white space */
				if (line == null || line.length == 0)
					continue;
				ArrayList<String> words = new ArrayList<String>();
				for (int i = 0; i < line.length; i++) {
					String word = line[i].toLowerCase();
					if (!(word.length() < 3 || (word.length() == 3 && word.equals("the")))) {
						words.add(word);
					}
				}
				if (words == null || words.size() == 0)
					continue;
				if (lastWord != null) {
					docBiWords.add(lastWord + " " + words.get(0));
				}
				lastWord = words.get(words.size() - 1);
				for (int j = 0; j < words.size() - 1; j++) {
					String biWord = words.get(j) + " " + words.get(j + 1);
					docBiWords.add(biWord);
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docBiWords;
	}

	/**
	 * Retrieve all the bi-words in the query
	 * 
	 * @param query
	 */
	public void readQuery(String query) {
		File tmpFile = new File("query_bi_file.txt");
		try {
			PrintWriter writer = new PrintWriter(tmpFile);
			writer.println(query);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		queryBiWords = readFile(tmpFile);
	}

	public ArrayList<StringDoublePair> getTopKDocs(String query, ArrayList<StringDoublePair> list, int k) {
		this.readQuery(query);
		ArrayList<StringIntegerDouble> tmpList = new ArrayList<StringIntegerDouble>();
		for (int i = 0; i < list.size(); i++) {
			StringDoublePair pair = list.get(i);
			tmpList.add(new StringIntegerDouble(pair.docName, 0, pair.num));
		}
		for (String biword : queryBiWords) {
			if (!termsMap.containsKey(biword)) {
				continue;
			} else {
				HashSet<String> postingsList = invertedIndex.get(termsMap.get(biword)).postingsList;
				for (int j = 0; j < list.size(); j++) {
					String doc = list.get(j).docName;
					if (postingsList.contains(doc)) {
						tmpList.get(j).numOfBiWords++;
					}
				}
			}
		}

		Collections.sort(tmpList);
		ArrayList<StringDoublePair> result = new ArrayList<StringDoublePair>();
		for (int l = 0; l < k; l++) {
			result.add(new StringDoublePair(tmpList.get(tmpList.size() - 1 - l).docName,
					tmpList.get(tmpList.size() - 1 - l).cosineSim));
		}
		outputTopKResult(result, k);
		return result;
	}

	/**
	 * Output the Top k result
	 * 
	 * @param result
	 * @param k
	 */
	public void outputTopKResult(ArrayList<StringDoublePair> result, int k) {
		System.out.println("Top " + k + " documents that matches the query:");
		System.out.printf("%-25s- %-30s\n", "-------------------------", "------------------------------");
		System.out.printf("%-25s| %-30s|\n", "Document Name", "cosine similarites with query");
		System.out.printf("%-25s+ %-30s\n", "-------------------------", "------------------------------");
		for (StringDoublePair pair : result) {
			System.out.printf("%-25s| %-30f|\n", pair.docName, pair.num);
		}
		System.out.printf("%-25s- %-30s\n\n", "-------------------------", "------------------------------");
	}

	/**
	 * Inner class, to store the <docIndex, numAppears> pair.
	 * 
	 * @author YAN
	 *
	 */
	class PostListEntry implements Comparable<PostListEntry> {
		public String docName;

		public PostListEntry(String name) {
			this.docName = name;
		}

		@Override
		public int compareTo(PostListEntry o) {
			return docsMap.get(this.docName) - docsMap.get(o.docName);
		}
	}

	/**
	 * Inner class, to store the inverted list entry.
	 * 
	 * @author YAN
	 *
	 */
	class DictEntry {
		public String termName;
		public Integer numOfDocs;
		public HashSet<String> postingsList;

		public DictEntry(String name, Integer num, HashSet<String> list) {
			this.termName = name;
			this.numOfDocs = num;
			this.postingsList = list;
		}
	}

	/**
	 * Inner class, to store the <docName, numOfBiWords, cosine similarity> of a
	 * document
	 * 
	 * @author YAN
	 *
	 */
	class StringIntegerDouble implements Comparable<StringIntegerDouble> {
		private String docName;
		private Integer numOfBiWords;
		private double cosineSim;

		public StringIntegerDouble(String doc, Integer num, double sim) {
			this.docName = doc;
			this.numOfBiWords = num;
			this.cosineSim = sim;
		}

		@Override
		public int compareTo(StringIntegerDouble o) {
			if (this.numOfBiWords == o.numOfBiWords) {
				if (this.cosineSim == o.cosineSim) {
					return 0;
				} else if (this.cosineSim < o.cosineSim) {
					return -1;
				} else {
					return 1;
				}
			} else {
				return this.numOfBiWords - o.numOfBiWords;
			}

		}
	}
}
