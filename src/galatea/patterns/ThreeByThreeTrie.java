package galatea.patterns;

import galatea.board.Board;
import galatea.board.Point;
import galatea.sgf.SGF;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ThreeByThreeTrie implements Serializable {

	private static final long serialVersionUID = -8369344394568866677L;

	TrieNode root = new TrieNode(0);
	
	public ThreeByThreeTrie() {
	}
	
	private void generateTrie() {
		Map<ThreeByThree, Integer> patternCounts = new TreeMap<ThreeByThree, Integer>();
		
		File sgfDir = new File("./data");
		for (File sgfFile: sgfDir.listFiles()) {
			SGF sgf = new SGF(sgfFile.toString());
			Board board = new Board(19, 0, 0);
			Point move;
			while ((move = sgf.nextMove()) != null) {
				ThreeByThree pattern1 = new ThreeByThree(board, move),
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
				board.addStone(board.turn, move, false);
			}
		}
		
		int threshold = 200;
		int count = 0;
		for (Map.Entry<ThreeByThree, Integer> entry: patternCounts.entrySet()) {
			if (entry.getValue() > threshold) {
				count++;
				root.insert(entry.getKey().pattern);
			}
		}
		System.out.println(count);
	}
	
	public static void main(String[] args) {
		ThreeByThreeTrie trie = new ThreeByThreeTrie();
		trie.generateTrie();
	}
}
