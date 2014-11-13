package galatea.patterns;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;
import galatea.sgf.SGF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/** 
 * A trie for storing a collection of ThreeByThree's 
 */
public class ThreeByThreeTrie implements Serializable {

	public int numPatterns;
	public TrieNode root = new TrieNode(-1, 0);
	
	public ThreeByThreeTrie(boolean fromFile) {
		if (fromFile) {
			try {
				ThreeByThreeTrie trie = deserialize();
				root = trie.root;
				numPatterns = trie.numPatterns;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void generateTrie() {
		Map<ThreeByThree, Integer> patternCounts = new TreeMap<ThreeByThree, Integer>();
		
		int i = 0;
		File sgfDir = new File("./src/database/19x19"); 
		for (File sgfFile: sgfDir.listFiles()) {
			System.out.println("Processing patterns from " + i + "th file: " + sgfFile.toString()); i++;
			SGF sgf = new SGF(sgfFile.toString());
			Board board = new Board(19, 0, 0);
			Point nextPoint;
			while ((nextPoint = sgf.nextPoint()) != null) {
				ThreeByThree pattern1 = new ThreeByThree(board, nextPoint),
						 pattern2 = pattern1.reflectX(),
						 pattern3 = pattern1.reflectY(),
						 pattern4 = pattern2.reflectY(),
						 pattern5 = pattern1.rotate90(),
						 pattern6 = pattern2.rotate90(),
						 pattern7 = pattern3.rotate90(),
						 pattern8 = pattern4.rotate90();
				int count = (patternCounts.get(pattern1) == null ? 1 : patternCounts.get(pattern1)+1);
				patternCounts.put(pattern1, count);
				patternCounts.put(pattern2, count);
				patternCounts.put(pattern3, count);
				patternCounts.put(pattern4, count);
				patternCounts.put(pattern5, count);
				patternCounts.put(pattern6, count);
				patternCounts.put(pattern7, count);
				patternCounts.put(pattern8, count);
				board.addStone(board.turn, nextPoint);
			}
		}
		
		int threshold = 50;
		int id = 0;
		for (Map.Entry<ThreeByThree, Integer> entry: patternCounts.entrySet()) {
			if (entry.getValue() > threshold) {
				id++;
				root.insert(id, entry.getKey().pattern);
			}
		}
		numPatterns = id;
		System.out.println(id);
		try {
			serialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void serialize() throws IOException {
		FileOutputStream fos = new FileOutputStream("./src/resources/trie.out");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
	}
	
	private ThreeByThreeTrie deserialize() throws IOException, ClassNotFoundException {
		String resourcePath = "trie.out";
		InputStream is = ThreeByThreeTrie.class.getClassLoader().getResourceAsStream(resourcePath);
		ObjectInputStream ois = new ObjectInputStream(is);
		ThreeByThreeTrie trie = (ThreeByThreeTrie) ois.readObject();
		ois.close();
		return trie;
	}
	
	public static void main(String[] args) {
		ThreeByThreeTrie trie = new ThreeByThreeTrie(false);
		trie.generateTrie();
		Color[][] pattern = new Color[3][3];
		pattern[0][0] = Color.EMPTY;
		pattern[1][0] = Color.EMPTY;
		pattern[2][0] = Color.EMPTY;
		pattern[0][1] = Color.EMPTY;
		pattern[0][2] = Color.WHITE;
		pattern[1][1] = Color.EMPTY;
		pattern[1][2] = Color.BLACK;
		pattern[2][1] = Color.EMPTY;
		pattern[2][2] = Color.WHITE;
		System.out.println(trie.root.getTrieNode(pattern));
	}
	
	private static final long serialVersionUID = -8369344394568866677L;
}
