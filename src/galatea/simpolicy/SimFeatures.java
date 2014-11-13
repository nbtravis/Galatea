package galatea.simpolicy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import galatea.board.Board;
import galatea.board.Chain;
import galatea.board.Point;
import galatea.patterns.ThreeByThree;
import galatea.patterns.ThreeByThreeTrie;
import galatea.patterns.TrieNode;
import galatea.tactics.BasicTactics;

/**
 * Holds information about the features of a move. It uses the features and the
 * gamma values learned in MinorMaxim to assign a score to each move, which is 
 * then used by the tree and simulation policies.
 */
public class SimFeatures implements Serializable {

	protected int[] features = new int[12];
	protected static int[] maxValue = {9,9,9,1,1,1,1,1,1,1,1,1};
	
	private static ThreeByThreeTrie patternTrie = new ThreeByThreeTrie(true);
	protected int patternId = 0;
	protected int[] equivalentPatternIds = new int[7];
	
	// These are used in Board.addStoneFast. They are passed on to the board 
	// instance if the move represented by these simFeatures is chosen
	public Set<Integer> prevAtariedChains, prevTwoLibbedChains;
	
	// load gammas from file
	private static List<double[]> gammas = new ArrayList<double[]>();
	private static double[] patternGammas = new double[patternTrie.numPatterns+1];
	static {
		try {
			String resourcePath = "gammas.out";
			InputStream is = SimFeatures.class.getClassLoader().getResourceAsStream(resourcePath);
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			String line;
			for (int i = 0; i < 3; i++) {
				double[] mutexGroupGammas = new double[10];
				for (int j = 0; j < 10; j++) {
					line = r.readLine();
					mutexGroupGammas[j] = Double.parseDouble(line);
					if (Double.isNaN(mutexGroupGammas[j]))
						mutexGroupGammas[j] = 0;
				}
				gammas.add(mutexGroupGammas);
			}
			for (int i = 0; i < 9; i++) {
				double[] mutexGroupGammas = new double[10];
				for (int j = 0; j <= 1; j++) {
					line = r.readLine();
					mutexGroupGammas[j] = Double.parseDouble(line);
					if (Double.isNaN(mutexGroupGammas[j]))
						mutexGroupGammas[j] = 0;
				}
				gammas.add(mutexGroupGammas);
			}
			int i = 0;
			while (i <= patternTrie.numPatterns && (line = r.readLine()) != null) {
				patternGammas[i] = Double.parseDouble(line);
				if (Double.isNaN(patternGammas[i]))
					patternGammas[i] = 0;
				if (Math.abs(patternGammas[i]) < 0.0000000001) System.out.println(line);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public SimFeatures() {
	}
	
	public double getScore() {
		double score = 1;
		for (int i = 0; i < features.length; i++) {
			try {
				score *= gammas.get(i)[features[i]];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		score *= patternGammas[patternId];
		return score;
	}
	
	public void setDistanceFeatures(Board board, Point move) {
		int[] distanceFeatures = new int[3];
		
		// Distance to last move (must be >= 2)
		int distToLastMove = BasicTactics.dist(move, board.lastMove);
		if (distToLastMove <= 8)
			distanceFeatures[0] = distToLastMove;
		else
			distanceFeatures[0] = 9;
		
		// Distance to two moves ago (can be >= 0)
		int distToTwoMovesAgo = BasicTactics.dist(move, board.twoMovesAgo);
		if (distToTwoMovesAgo <= 8)
			distanceFeatures[1] = distToTwoMovesAgo;
		else
			distanceFeatures[1] = 9;
		
		// Distance to edge of board
		distanceFeatures[2] = BasicTactics.distToEdge(board, move);
		
		System.arraycopy(distanceFeatures, 0, features, 0, distanceFeatures.length);
	}
	
	public void setAtariFeatures(boolean ko, boolean atariWhenTwoLibs,
								 boolean selfAtari, boolean extensionWhenAtari, 
								 boolean extensionWhenAtari2) {
		int[] atariFeatures = new int[6];
		boolean atari = prevAtariedChains.size() > 0;
		atariFeatures[0] = (ko && atari ? 1 : 0);
		atariFeatures[1] = (atariWhenTwoLibs ? 1 : 0);
		atariFeatures[2] = (atari ? 1 : 0);
		atariFeatures[3] = (selfAtari ? 1 : 0);
		atariFeatures[4] = (extensionWhenAtari ? 1 : 0);
		atariFeatures[5] = (extensionWhenAtari2 ? 1 : 0);
		
		System.arraycopy(atariFeatures, 0, features, 3, atariFeatures.length);
	}
	
	public void setCaptureFeatures(boolean ko, boolean chainRemoved,
								   boolean captureWhenAtari) {
		int[] captureFeatures = new int[3];
		captureFeatures[0] = (captureWhenAtari ? 1 : 0);
		captureFeatures[1] = (chainRemoved && ko ? 1 : 0);
		captureFeatures[2] = (chainRemoved ? 1 : 0);
		
		System.arraycopy(captureFeatures, 0, features, 9, captureFeatures.length);
	}
	
	public void setPattern(ThreeByThree pattern) {
		TrieNode trieNode = patternTrie.root.getTrieNode(pattern.pattern);
		if (trieNode == null) {
			patternId = 0;
		} else {
			patternId = trieNode.id;
			ThreeByThree pattern2 = pattern.reflectX(),
					 	 pattern3 = pattern.reflectY(),
					 	 pattern4 = pattern2.reflectY(),
					 	 pattern5 = pattern.rotate90(),
					 	 pattern6 = pattern2.rotate90(),
					 	 pattern7 = pattern3.rotate90(),
					 	 pattern8 = pattern4.rotate90();
			equivalentPatternIds[0] = patternTrie.root.getTrieNode(pattern2.pattern).id;
			equivalentPatternIds[1] = patternTrie.root.getTrieNode(pattern3.pattern).id;
			equivalentPatternIds[2] = patternTrie.root.getTrieNode(pattern4.pattern).id;
			equivalentPatternIds[3] = patternTrie.root.getTrieNode(pattern5.pattern).id;
			equivalentPatternIds[4] = patternTrie.root.getTrieNode(pattern6.pattern).id;
			equivalentPatternIds[5] = patternTrie.root.getTrieNode(pattern7.pattern).id;
			equivalentPatternIds[6] = patternTrie.root.getTrieNode(pattern8.pattern).id;
		}	
	}
	
	private static final long serialVersionUID = 6975387998663147500L;
}
