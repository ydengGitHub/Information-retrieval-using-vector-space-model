import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * The program gets the name of a folder containing the document collection as
 * input. Then it builds both indices (Biword, and single word). The program
 * repeatedly prompts the user to enter a query q and an integer k and outputs
 * (names of) top k documents that matches the query. For each document, also
 * outputs the similarity score. The top k documents are determined as follows:
 * Let q = ti1ti2 ···tir, i.e, the query has r terms (after removing STOP words
 * and punctuation symbols). First retrieve a set S consisting of top 2k
 * documents (along with cosine similarities with q) that matches the query
 * using vector space model scoring.
 * 
 * @author YAN DENG
 *
 */
public class QueryProcessor {

	public static void main(String[] args) {
		String path;
		Scanner scanner = new Scanner(System.in);
		if (args.length == 1) {
			path = args[0].trim();
		} else {
			System.out.println("Please input the name of a folder containing the document collection:");
			path = scanner.nextLine().trim();
		}
		/*Initialize the WordIndex and BiWordIndex instance*/
		WordIndex wordIndex = new WordIndex(path);
		BiWordIndex biWordIndex = new BiWordIndex(path);
				
		while (true) {
			/*Take the input.*/
			String query;
			int k;
			System.out.println("Please enter a query q:");
			query = scanner.nextLine().trim();
			while (query.length() == 0) {
				System.out.println("The query can not be empty. Please enter a query q:");
				query = scanner.nextLine().trim();
			}
			System.out.println("Please enter an integer k to output top k documents that mathces the query:");
			String tmpString = scanner.nextLine().trim();
			while (tmpString.length() == 0) {
				System.out.println("k can not be empty. Please enter an integer k:");
				tmpString = scanner.nextLine().trim();
			}
			k = Integer.parseInt(tmpString);
			
			/*Calculate the top k matching documents.*/
			long startTime=System.currentTimeMillis();
			ArrayList<StringDoublePair> top2Klist = wordIndex.getTop2KDocs(query, k);
			ArrayList<StringDoublePair> topKResult = biWordIndex.getTopKDocs(query, top2Klist, k);
			
			/*Calculate the time used.*/
			long endTime=System.currentTimeMillis();
			long timeUsed=(endTime-startTime)/1000;
			System.out.println("Time used: "+timeUsed+" seconds.");
			System.out.println();
		}
	}
}
