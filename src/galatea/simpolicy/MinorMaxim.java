package galatea.simpolicy;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import galatea.board.Board;
import galatea.board.Point;
import galatea.engine.Move;
import galatea.patterns.ThreeByThreeTrie;
import galatea.sgf.SGF;
import galatea.util.DeepCopy;

/**
 * An implementation of the Minorization-Maximization algorithm for determining
 * the elo (gamma) values of move features. See "Computing Elo Ratings of Move
 * Patterns in the Game of Go" by Remi Coulom.
 * 
 * TODO: Make the gamma values take into account the stage of the game by 
 * learning different gamma values for each possible number of stones on the
 * board.
 */
public class MinorMaxim {
	
	private ThreeByThreeTrie patternTrie = new ThreeByThreeTrie(true);
	
	// gamma associated with each non-pattern feature 
	private List<double[]> gammas = new ArrayList<double[]>() {{
		add(new double[]{1,1,1,1,1,1,1,1,1,1});
		add(new double[]{1,1,1,1,1,1,1,1,1,1});
		add(new double[]{1,1,1,1,1,1,1,1,1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
		add(new double[]{1,1});
	}};
	private double[] patternGammas = new double[patternTrie.numPatterns+1];
	
	public MinorMaxim() {
	}
	
	private void learnGammas() {
		// Initialize gammas to 1
		Arrays.fill(patternGammas, 1);
		
		// The mutex group of patterns will be considered separately
		List<int[]> mutexGroups = new ArrayList<int[]>() {{
			add(new int[]{0,1,2,3,4,5,6,7,8,9});
			add(new int[]{0,1,2,3,4,5,6,7,8,9});
			add(new int[]{0,1,2,3,4,5,6,7,8,9});
			add(new int[]{0,1});
			add(new int[]{0,1});
			add(new int[]{0,1});
			add(new int[]{0,1});
			add(new int[]{0,1});
			add(new int[]{0,1});
			add(new int[]{0,1});
			add(new int[]{0,1});
			add(new int[]{0,1});
		}};
		 
		for (int i = 1; i <= 10; i++) {  
			// Learn non-pattern gammas
			for (int mi = 0; mi < mutexGroups.size(); mi++) {
				System.out.println(mi + "th mutex group");
				
				int[] mutexGroup = mutexGroups.get(mi);
				int[] wins = new int[mutexGroup.length];
				double[] denoms = new double[mutexGroup.length];
				
				File sgfDir = new File("./src/database/19x19");
				for (File sgfFile: sgfDir.listFiles()) {
					SGF sgf = new SGF(sgfFile.toString());
					Board board = new Board(19, 0, 0);
					
					// Don't consider first two moves 
					board.addStone(board.turn, sgf.nextPoint()); 
					board.addStone(board.turn, sgf.nextPoint());
					Point nextPoint;
					Move nextMove = null;
					int moveNumber = 2;
					while ((nextPoint = sgf.nextPoint()) != null) {
						moveNumber++;
						double E = 0; // total strength
						double[] C = new double[mutexGroup.length]; // team strengths
						
						List<Point> legalMoves = board.getLegalMoves();
						for (Point legalMove: legalMoves) {
							SimFeatures simFeatures = board.addStoneFast(board.turn, legalMove);
														
							double C_team = 1;
							// Mutex groups are equivalent to features in this sense (TODO)
							for (int fi = 0; fi < simFeatures.features.length; fi++) {
								if (fi != mi)
									C_team *= gammas.get(fi)[simFeatures.features[fi]];
							}
							C_team *= patternGammas[simFeatures.patternId];
							
							int mutexGroupVal = simFeatures.features[mi];
							E += C_team*gammas.get(mi)[mutexGroupVal];
							
							// Update wins if chosen move
							if (legalMove.equals(nextPoint)) {
								wins[simFeatures.features[mi]]++;
								nextMove = new Move(nextPoint, simFeatures);
							}
							// Update team strengths
							C[mutexGroupVal] += C_team;
						}
						for (int fi = 0; fi < mutexGroup.length; fi++) {
							denoms[fi] += C[fi]/E;
						}
						
						// Finally make move and move on to next move
						board.addStone(board.turn, nextPoint);
						board.prevAtariedChains = nextMove.simFeatures.prevAtariedChains;
						board.prevTwoLibbedChains = nextMove.simFeatures.prevTwoLibbedChains;
					}
				}
				
				// Update gammas
				for (int fi = 0; fi < mutexGroup.length; fi++) {
					System.out.println("wins: " + wins[fi] + " denom: " + denoms[fi]);
					gammas.get(mi)[fi] = wins[fi]/denoms[fi];
				}
			}
				
			// Learn pattern gammas
			System.out.println("patterns");
			
			int[] wins = new int[patternTrie.numPatterns+1];
			double[] denoms = new double[patternTrie.numPatterns+1];
			
			File sgfDir = new File("./src/database/19x19"); 
			for (File sgfFile: sgfDir.listFiles()) {
				SGF sgf = new SGF(sgfFile.toString());
				Board board = new Board(19, 0, 0);
				
				// Don't consider first two moves 
				board.addStone(board.turn, sgf.nextPoint()); 
				board.addStone(board.turn, sgf.nextPoint());
				Point nextPoint;
				Move nextMove = null;
				while ((nextPoint = sgf.nextPoint()) != null) {
					double E = 0; // total strength
					double[] C = new double[patternTrie.numPatterns+1]; // team strengths
					Arrays.fill(C, 1);
					
					List<Point> legalMoves = board.getLegalMoves();
					for (Point legalMove: legalMoves) {
						SimFeatures simFeatures = board.addStoneFast(board.turn, legalMove);

						int patternId = simFeatures.patternId;
						
						double C_team = 1;
						// Mutex groups are equivalent to features in this sense (TODO)
						for (int fi = 0; fi < simFeatures.features.length; fi++)
							C_team *= gammas.get(fi)[simFeatures.features[fi]];
						
						E += C_team*patternGammas[patternId];
						
						// Update wins if chosen move
						if (legalMove.equals(nextPoint)) {
							wins[patternId]++;
							if (patternId != 0) {
								for (int j = 0; j < 7; j++)
									wins[simFeatures.equivalentPatternIds[j]]++;
							}
							nextMove = new Move(nextPoint, simFeatures);
						}
						// Update team strengths (for all equivalent patterns)
						C[patternId] += C_team;
						if (patternId != 0) {
							for (int j = 0; j < 7; j++)
								C[simFeatures.equivalentPatternIds[j]] += C_team;
						}
					}
					for (int fi = 0; fi < patternTrie.numPatterns+1; fi++)
						denoms[fi] += C[fi]/E;

					// Finally make move and move on to next move
					board.addStone(board.turn, nextPoint);
					board.prevAtariedChains = nextMove.simFeatures.prevAtariedChains;
					board.prevTwoLibbedChains = nextMove.simFeatures.prevTwoLibbedChains;
				}
			}
			
			// Update gammas
			for (int fi = 0; fi < patternTrie.numPatterns+1; fi++) {
				System.out.println("wins: " + wins[fi] + " denom: " + denoms[fi]);
				patternGammas[fi] = wins[fi]/denoms[fi];
			}
		}
	}
	
	private void writeGammasToFile() {
		try {
			FileWriter w = new FileWriter(new File("./src/resources/gammas.out"));
			for (int i = 0; i < gammas.size(); i++) {
				for (int j = 0; j < gammas.get(i).length; j++)
					w.write(gammas.get(i)[j] + "\n");
			}
			for (int i = 0; i <= patternTrie.numPatterns; i++) {
				w.write(patternGammas[i] + "\n");
			}
			w.close();
		} catch (Exception e) {}
	}
	
	public static void main(String[] args) {
		MinorMaxim mm = new MinorMaxim();
		mm.learnGammas();
		mm.writeGammasToFile();
	}
}
