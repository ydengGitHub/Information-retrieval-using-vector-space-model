/**
 * This class store the docName, value pairs.
 * @author YAN
 *
 */
public class StringDoublePair implements Comparable<StringDoublePair> {
	public String docName;
	public double num;

	public StringDoublePair(String s, double d) {
		this.docName = s;
		this.num = d;
	}

	@Override
	public int compareTo(StringDoublePair o) {
		double diff = this.num - o.num;
		if (diff == 0) {
			return 0;
		} else if (diff < 0) {
			return -1;
		} else {
			return 1;
		}

	}
}
