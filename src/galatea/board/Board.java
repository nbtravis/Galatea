package galatea.board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import galatea.patterns.ThreeByThree;
import galatea.simpolicy.SimFeatures;
import galatea.tactics.BasicTactics;

/**
 * This is the class that holds all of the board information.
 */
public class Board implements Serializable {
	
	public Color[][] board;
	public Color turn = Color.WHITE;
	
	public int[][] chainMap;
	public List<Chain> chains = new ArrayList<Chain>();
	protected int nextChainIndex = 1;
	
	private int[][][] zobristValues;
	public int zobristHash = 0;
	
	public int size;
	public int handicap;
	public double komi;
	
	public boolean xAxisSym = true, yAxisSym = true,
			       diag1Sym = true, diag2Sym = true;
	
	public Point koPoint = null;
	
	public Set<Point> emptyPoints = new HashSet<Point>();
	
	// Variables needed for SimFeatures
	public int movesSoFar = 0;
	public Point lastMove = null, twoMovesAgo = null;
	public Set<Integer> prevAtariedChains = null, prevTwoLibbedChains = null;
	
	/**
	 * Requires size == 19, 13 or 9 && handicap <= 9 
	 */
	public Board(int size, int handicap, double komi) {
		board = new Color[size][size];
		chainMap = new int[size][size];
		chains.add(null);
		zobristValues = new int[size][size][3];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				board[i][j] = Color.EMPTY;
				for (int k = 0; k < 3; k++)
					zobristValues[i][j][k] = (int) Math.floor(Math.random()*Integer.MAX_VALUE);
				zobristHash ^= zobristValues[i][j][Color.EMPTY.ordinal()];
				emptyPoints.add(new Point(i, j));
			}
		}
		
		this.size = size;
		this.handicap = handicap;
		this.komi = komi;
		setHandicap(handicap);
	}
	
	public void updateHash(int x, int y) {
		zobristHash ^= zobristValues[x][y][Color.EMPTY.ordinal()];
		zobristHash ^= zobristValues[x][y][board[x][y].ordinal()];
	}
	
	public static List<Point> getHandicapPoints(int size, int handicap) {
		List<Point> points = new ArrayList<Point>();
		if (size < 7 || handicap < 2) return points;
		int edgeDist = (size < 13 ? 3 : 4);
		
		points.add(new Point(size-edgeDist, edgeDist-1));
		points.add(new Point(edgeDist-1, size-edgeDist));
		if (handicap < 3) return points;
		points.add(new Point(edgeDist-1, edgeDist-1));
		if (handicap < 4) return points;
		points.add(new Point(size-edgeDist, size-edgeDist));
		// Even sized boards can have at most 4 handi stones according to GTP
		if (handicap < 5 || size%2 == 0) return points;
		if (handicap%2 == 1)
			points.add(new Point(size/2, size/2));
		if (handicap < 6) return points;
		points.add(new Point(size/2, edgeDist-1));
		points.add(new Point(size/2, size-edgeDist));
		if (handicap < 8) return points;
		points.add(new Point(edgeDist-1, size/2));
		points.add(new Point(size-edgeDist, size/2));
		return points;
	}
	
	private void setHandicap(int handicap) {
		List<Point> points = getHandicapPoints(size, handicap);
		if (handicap == 0) turn = Color.BLACK;
		for (Point point: points) addStone(Color.BLACK, point);
	}
	
	/**
	 * Determines if a move is legal. 
	 */
	public boolean isLegal(Color color, Point point) {
		int x = point.x, y = point.y;
		if (koPoint != null && koPoint.x == x && koPoint.y == y)
			return false;
		if (BasicTactics.isSuicide(this, point))
			return false;
		if (BasicTactics.fillsEye(this, point))
			return false;
		// Some simple hacks to reduce the search space early in the game
		// TODO: Currently this needs to be commented out to run MinorMaxim
		// since moves are played that aren't considered legal.
		if (movesSoFar <= 6) {
			if (movesSoFar < 2 && BasicTactics.distToEdge(this, point) == 1)
				return false;
			if (BasicTactics.distToEdge(this, point) == 0)
				return false;
			if (xAxisSym && x < size/2)
				return false;
			if (yAxisSym && y < size/2)
				return false;
			if (diag1Sym && x < y)
				return false;
			if (diag2Sym && y < size-1-x)
				return false;
		}
		return true;
	}
	
	public List<Point> getLegalMoves() {
		List<Point> moves = new ArrayList<Point>();
		for (Point move: emptyPoints) {
			if (isLegal(turn, move))
				moves.add(move);
		}
		return moves;
	}
	
	/**
	 * Adds stone to board for a single move.
	 */
	public void addStone(Color color, Point point) {
		movesSoFar++;
		koPoint = null;
		prevAtariedChains = null; prevTwoLibbedChains = null;
		// This is for implementing passes
		if (point == null) {
			turn = turn.opposite();
			twoMovesAgo = lastMove;
			lastMove = null;
			return;
		}
		
		if (movesSoFar <= 6) {
			xAxisSym = BasicTactics.xAxisSym(this);
			yAxisSym = BasicTactics.yAxisSym(this);
			diag1Sym = BasicTactics.diag1Sym(this);
			diag2Sym = BasicTactics.diag2Sym(this);
		}
		
		int x = point.x, y = point.y;		
		
		Set<Integer> sameChains = new HashSet<Integer>();
		Set<Integer> oppositeChains = new HashSet<Integer>();
		List<Point> liberties = new ArrayList<Point>();
		if (x > 0) {
			if (board[x-1][y] == turn)
				sameChains.add(chains.get(chainMap[x-1][y]).index);
			else if (board[x-1][y] == turn.opposite())
				oppositeChains.add(chains.get(chainMap[x-1][y]).index);
			else
				liberties.add(new Point(x-1, y));
		}
		if (x < size-1) {
			if (board[x+1][y] == turn)
				sameChains.add(chains.get(chainMap[x+1][y]).index);
			else if (board[x+1][y] == turn.opposite())
				oppositeChains.add(chains.get(chainMap[x+1][y]).index);
			else
				liberties.add(new Point(x+1, y));
		}
		if (y > 0) {
			if (board[x][y-1] == turn)
				sameChains.add(chains.get(chainMap[x][y-1]).index);
			else if (board[x][y-1] == turn.opposite())
				oppositeChains.add(chains.get(chainMap[x][y-1]).index);
			else
				liberties.add(new Point(x, y-1));
		}
		if (y < size-1) {
			if (board[x][y+1] == turn)
				sameChains.add(chains.get(chainMap[x][y+1]).index);
			else if (board[x][y+1] == turn.opposite())
				oppositeChains.add(chains.get(chainMap[x][y+1]).index);
			else
				liberties.add(new Point(x, y+1));
		}
		
		if (sameChains.size() == 0) { // Create new chain
			chainMap[x][y] = nextChainIndex;
			chains.add(new Chain(nextChainIndex, turn, point, liberties.size()));
			nextChainIndex++;
		} else {
			int newLiberties = BasicTactics.extraLiberties(this, point, sameChains, liberties);
			boolean first = true;
			Chain defaultChain = null;
			for (int chainIndex: sameChains) {
				if (first) {
					chainMap[x][y] = chainIndex;
					chains.get(chainIndex).addPoint(point, newLiberties);
					defaultChain = chains.get(chainIndex);
					first = false;
				} else {
					defaultChain.merge(chains.get(chainIndex));
				}
			}
		}
		
		// Add stone to board
		board[x][y] = turn;
		
		for (int chainIndex: oppositeChains) {
			Chain chain = chains.get(chainIndex);
			chain.numLiberties--;
			if (chain.numLiberties <= 0)
				chain.calculateLiberties(this);
			if (chain.numLiberties == 0) {
				if (chain.points.size() == 1 && chain.mergedChains.size() == 1)
					removeSingleStone(chain);
				else
					removeChain(chain, chainIndex);
			}
		}
		
		updateHash(x, y);
		twoMovesAgo = lastMove; lastMove = point;
		emptyPoints.remove(point);
		turn = turn.opposite();
	}
	
	/**
	 * TODO: This method is called when a group consisting of a single stone
	 * is captured. It was created for ko and zobrist hashing optimization, but
	 * it may be unnecessary now. 
	 */
	private void removeSingleStone(Chain chain) {
		int x = chain.points.get(0).x, y = chain.points.get(0).y;
		koPoint = new Point(x, y);
		updateHash(x, y);
		emptyPoints.add(koPoint);
		board[x][y] = Color.EMPTY;
		chains.set(chain.index, null);
		
		Set<Integer> visited = new HashSet<>();
		if (x > 0 && board[x-1][y] == turn && !visited.contains(chains.get(chainMap[x-1][y]).index)) {
			chains.get(chainMap[x-1][y]).mainChain.numLiberties++;
			visited.add(chains.get(chainMap[x-1][y]).index);
		}
		if (x < size-1 && board[x+1][y] == turn && !visited.contains(chains.get(chainMap[x+1][y]).index)) {
			chains.get(chainMap[x+1][y]).mainChain.numLiberties++;
			visited.add(chains.get(chainMap[x+1][y]).index);
		}
		if (y > 0 && board[x][y-1] == turn && !visited.contains(chains.get(chainMap[x][y-1]).index)) {
			chains.get(chainMap[x][y-1]).mainChain.numLiberties++;
			visited.add(chains.get(chainMap[x][y-1]).index);
		}
		if (y < size-1 && board[x][y+1] == turn && !visited.contains(chains.get(chainMap[x][y+1]).index)) {
			chains.get(chainMap[x][y+1]).mainChain.numLiberties++;
			visited.add(chains.get(chainMap[x][y+1]).index);
		}
	}
	
	/**
	 * Removes a chain by taking in a mainChain and iterating through its
	 * mergedChains and their points.  
	 */
	private void removeChain(Chain chain, int chainIndex) {
		for (Chain c: chain.mergedChains) {
			chains.set(c.index, null);
			for (Point p: c.points) {
				int x = p.x, y = p.y;
				Set<Integer> visited = new HashSet<>();
				if (x > 0 && board[x-1][y] == turn && !visited.contains(chains.get(chainMap[x-1][y]).index)) {
					chains.get(chainMap[x-1][y]).mainChain.numLiberties++;
					visited.add(chains.get(chainMap[x-1][y]).index);
				}
				if (x < size-1 && board[x+1][y] == turn && !visited.contains(chains.get(chainMap[x+1][y]).index)) {
					chains.get(chainMap[x+1][y]).mainChain.numLiberties++;
					visited.add(chains.get(chainMap[x+1][y]).index);
				}
				if (y > 0 && board[x][y-1] == turn && !visited.contains(chains.get(chainMap[x][y-1]).index)) {
					chains.get(chainMap[x][y-1]).mainChain.numLiberties++;
					visited.add(chains.get(chainMap[x][y-1]).index);
				}
				if (y < size-1 && board[x][y+1] == turn && !visited.contains(chains.get(chainMap[x][y+1]).index)) {
					chains.get(chainMap[x][y+1]).mainChain.numLiberties++;
					visited.add(chains.get(chainMap[x][y+1]).index);
				}
				updateHash(x, y);
				board[x][y] = Color.EMPTY;
				emptyPoints.add(p);
			}
		}
	}
	
	/**
	 * Determines if a stone is surrounded (used in isKo). 
	 */
	private boolean stoneSurrounded(Color turn, int x, int y) {
		int count = 0, total = 0;
		if (x > 0 && board[x-1][y] == turn) {
			count++; total++;
		} else if (x > 0) {
			total++;
		}
		if (x < size-1 && board[x+1][y] == turn) {
			count++; total++;
		} else if (x < size-1) {
			total++;
		}
		if (y > 0 && board[x][y-1] == turn) {
			count++; total++;
		} else if (y > 0) {
			total++;
		}
		if (y < size-1 && board[x][y+1] == turn) {
			count++; total++;
		} else if (y < size-1) {
			total++;
		}
		
		if (count != total-1)
			return false;
		return true;
	}
	
	private boolean isKo(Color turn, int x, int y) {
		Color opposite = turn.opposite();
		boolean ko = false;
		for (int i = 0; i < 1; i++) {
			if (x > 0) {
				if (board[x-1][y] != opposite)
					return false;
				else if (stoneSurrounded(turn, x-1, y))
					ko = true;
			}
			if (x < size-1) {
				if (board[x+1][y] != opposite)
					return false;
				else if (stoneSurrounded(turn, x+1, y))
					ko = true;
			}
			if (y > 0) {
				if (board[x][y-1] != opposite)
					return false;
				else if (stoneSurrounded(turn, x, y-1))
					ko = true;
			}
			if (y < size-1) {
				if (board[x][y+1] != opposite)
					return false;
				else if (stoneSurrounded(turn, x, y+1))
					ko = true;
			}
		}
		return ko;
	}
	
	/**
	 * "Adds a stone without actually adding the stone" in ordder to determine
	 * the features of the move for the tree and simulation policies. 
	 */
	public SimFeatures addStoneFast(Color turn, Point point) {
		SimFeatures simFeatures = new SimFeatures();
		
		boolean ko = false, 
				chainRemoved = false, 
				atari = false, 
				selfAtari = false, 
				selfTwoLibs = false, 
				extensionWhenAtari = false, 
				extensionWhenAtari2 = false,
				captureWhenAtari = false, 
				atariWhenTwoLibs = false;
		Set<Integer> capturedChains = new HashSet<>(),
					 atariedChains = new HashSet<>(),
					 twoLibbedChains = new HashSet<>();

		// Pass move
		if (point == null) {
			simFeatures.prevAtariedChains = atariedChains;
			simFeatures.prevTwoLibbedChains = twoLibbedChains;
			simFeatures.setDistanceFeatures(this, point);
			simFeatures.setAtariFeatures(ko, atariWhenTwoLibs, selfAtari, extensionWhenAtari, extensionWhenAtari2);
			simFeatures.setCaptureFeatures(ko, chainRemoved, captureWhenAtari);
			simFeatures.setPattern(null);
			return simFeatures;
		}
					
		ThreeByThree pattern = new ThreeByThree(this, point);
		
		Color opposite = turn.opposite();
		int x = point.x, y = point.y;
		
		// Determine if ko
		ko = isKo(turn, x, y);
		
		// Determine if capture, atari or twoLibs
		Set<Integer> oppositeChains = new HashSet<Integer>();
		if (x > 0 && board[x-1][y] == opposite)
			oppositeChains.add(chains.get(chainMap[x-1][y]).index);
		if (x < size-1 && board[x+1][y] == opposite)
			oppositeChains.add(chains.get(chainMap[x+1][y]).index);
		if (y > 0 && board[x][y-1] == opposite)
			oppositeChains.add(chains.get(chainMap[x][y-1]).index);
		if (y < size-1 && board[x][y+1] == opposite)
			oppositeChains.add(chains.get(chainMap[x][y+1]).index);
		
		for (int chainIndex: oppositeChains) {
			Chain chain = chains.get(chainIndex);
			if (chain.numLiberties <= 3) 
				chain.calculateLiberties(this);
			if (chain.numLiberties == 1) {
				chainRemoved = true;
				capturedChains.add(chain.index);
			} else if (chain.numLiberties == 2) {
				atari = true;
				atariedChains.add(chain.index);
			} else if (chain.numLiberties == 3) {
				twoLibbedChains.add(chain.index);
			}
		}
		
		// Determine if selfAtari or selfTwoLibs, captureWhenAtari and extensionWhenAtari
		Set<Integer> sameChains = new HashSet<Integer>();
		if (x > 0 && board[x-1][y] == turn)
			sameChains.add(chains.get(chainMap[x-1][y]).index);
		if (x < size-1 && board[x+1][y] == turn)
			sameChains.add(chains.get(chainMap[x+1][y]).index);
		if (y > 0 && board[x][y-1] == turn)
			sameChains.add(chains.get(chainMap[x][y-1]).index);
		if (y < size-1 && board[x][y+1] == turn)
			sameChains.add(chains.get(chainMap[x][y+1]).index);
		
		List<Point> liberties = new ArrayList<Point>();
		if (x > 0 && board[x-1][y] == Color.EMPTY)
			liberties.add(new Point(x-1, y));
		if (x < size-1 && board[x+1][y] == Color.EMPTY)
			liberties.add(new Point(x+1, y));
		if (y > 0 && board[x][y-1] == Color.EMPTY)
			liberties.add(new Point(x, y-1));
		if (y < size-1 && board[x][y+1] == Color.EMPTY)
			liberties.add(new Point(x, y+1));
		
		int newLiberties = BasicTactics.extraLiberties(this, point, sameChains, liberties);
		
		int totalLibs = 0;
		for (int chainIndex: sameChains) {
			Chain chain = chains.get(chainIndex);
			if (totalLibs < 2 && chain.numLiberties <= 1)
				chain.calculateLiberties(this);
			totalLibs += chain.numLiberties;
		}
		if (atari && prevTwoLibbedChains != null && areAdjacent(atariedChains, prevTwoLibbedChains)) 
			atariWhenTwoLibs = true;
		if (chainRemoved && prevAtariedChains != null && areAdjacent(capturedChains, prevAtariedChains))
			captureWhenAtari = true;
		totalLibs += newLiberties;
		if (totalLibs == 2 && !atariWhenTwoLibs && !captureWhenAtari) 
			selfTwoLibs = true;
		else if (totalLibs == 1 && !captureWhenAtari) 
			selfAtari = true;
		else if (totalLibs > 1 && containsOne(prevAtariedChains, sameChains)) {
			if (totalLibs == 2) extensionWhenAtari = true;
			else extensionWhenAtari2 = true;
		}
		
		simFeatures.prevAtariedChains = atariedChains;
		simFeatures.prevTwoLibbedChains = twoLibbedChains;
		simFeatures.setDistanceFeatures(this, point);
		simFeatures.setAtariFeatures(ko, atariWhenTwoLibs, selfAtari, extensionWhenAtari, extensionWhenAtari2);
		simFeatures.setCaptureFeatures(ko, chainRemoved, captureWhenAtari);
		simFeatures.setPattern(pattern);
		return simFeatures;
	}
	
	/**
	 * Determines if one of the chains in chains1 is adjacent to one of the 
	 * chains in chains2 (used in addStoneFast).
	 */
	public boolean areAdjacent(Set<Integer> chains1, Set<Integer> chains2) {
		if (chains2 == null) return false;
		
		for (int index: chains1) {
			Chain c = chains.get(index);
			for (Chain c1: c.mergedChains) {
				for (Point p: c1.points) {
					int x = p.x, y = p.y;
					if (x > 0 && board[x-1][y] == turn && chains2.contains(chains.get(chainMap[x-1][y]).index))
						return true;
					if (x < size-1 && board[x+1][y] == turn && chains2.contains(chains.get(chainMap[x+1][y]).index))
						return true;
					if (y > 0 && board[x][y-1] == turn && chains2.contains(chains.get(chainMap[x][y-1]).index))
						return true;
					if (y < size-1 && board[x][y+1] == turn && chains2.contains(chains.get(chainMap[x][y+1]).index))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true iff the size of intersection of the chain indices from 
	 * chains1 and chainIndices is >= 1 (used in addStoneFast).
	*/
	public boolean containsOne(Set<Integer> chains1, Set<Integer> chains2) {
		if (chains1 == null || chains2.size() == 0) return false;

		for (int chainIndex: chains2) {
			if (chains1.contains(chainIndex))
				return true;
		}
		return false;
	}
	
	public void printBoard() {
		int offset = (size != 9 ? 4 : 3);
		List<Integer> specialIndices = Arrays.asList(new Integer[] {size/2, offset-1, size-offset});
		 
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Color c = board[i][j];
				char p;
				if (c == Color.EMPTY && specialIndices.contains(i) && specialIndices.contains(j))
					p = '∗';
				else if (c == Color.EMPTY)
					p = '·';
				else if (c == Color.BLACK)
					p = 'X';
				else
					p = 'O';
				System.out.print(p + " ");
			}
			System.out.println();
		}
	}
	
	public void printLiberties() {
		int offset = (size != 9 ? 4 : 3);
		List<Integer> specialIndices = Arrays.asList(new Integer[] {size/2, offset-1, size-offset});
		 
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Color c = board[i][j];
				char p;
				if (c == Color.EMPTY && specialIndices.contains(i) && specialIndices.contains(j))
					p = '∗';
				else if (c == Color.EMPTY)
					p = '·';
				else {
					if (chains.get(chainMap[i][j]).numLiberties() > 9)
						p = '9';
					else 
						p = (""+chains.get(chainMap[i][j]).numLiberties()).charAt(0);
				}
				System.out.print(p + " ");
			}
			System.out.println();
		}
	}
	
	public void printLegalMoves() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Point p = new Point(i, j);
				if (emptyPoints.contains(p) && isLegal(turn, p))
					System.out.print("T ");
				else if (!emptyPoints.contains(p))
					System.out.print("E ");
				else if (BasicTactics.isSuicide(this, p))
					System.out.print("S ");
				else if (BasicTactics.fillsEye(this, p))
					System.out.print("F ");
				else if (koPoint != null && koPoint.x == p.x && koPoint.y == p.y)
					System.out.print("K ");
			}
			System.out.println();
		}
	}
	
	private static final long serialVersionUID = -4962209626762134868L;
}
