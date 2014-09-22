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

import galatea.simpolicy.SimFeatures;
import galatea.tactics.BasicTactics;

public class Board implements Serializable {
	
	private static final long serialVersionUID = -4962209626762134868L;

	public Color[][] board;
	public Color turn = Color.WHITE;
	
	public int[][] chainMap;
	public List<Chain> chains = new ArrayList<Chain>();
	private int nextChainIndex = 0;
	
	private int[][][] zobristValues;
	public int zobristHash;
	
	public int size;
	public int handicap;
	public double komi;
	
	private Point koPoint = null;
	
	// TODO: Optimize lazy deletes
	public List<Point> emptyPoints = new ArrayList<Point>();
	private int lazyDeletes = 0;
	// The index of the point in the emptyPoints List
	private int[][] pointToIndex;
	
	private Point recentPoint = null;
	private int[] simFeatures;
	
	// Requires size == 19, 13 or 9 && handicap <= 9 
	public Board(int size, int handicap, double komi) {
		board = new Color[size][size];
		chainMap = new int[size][size];
		zobristValues = new int[size][size][3];
		pointToIndex = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				board[i][j] = Color.EMPTY;
				for (int k = 0; k < 3; k++)
					zobristValues[i][j][k] = (int) Math.floor(Math.random()*Integer.MAX_VALUE);
				emptyPoints.add(new Point(i, j));
				pointToIndex[i][j] = emptyPoints.size()-1;
			}
		}
		
		zobristHash = zobristHash();
		this.size = size;
		this.handicap = handicap;
		this.komi = komi;
		setHandicap(handicap);
	}
	
	public int zobristHash() {
		int hash = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				hash ^= zobristValues[i][j][board[i][j].ordinal()];
			}
		}
		return hash;
	}
	
	public void updateHash(Point p) {
		int x = p.x, y = p.y;
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
		if (handicap == 0)
			turn = Color.BLACK;
		for (Point point: points) {
			addStone(Color.BLACK, point, true);
		}
	}
	
	public boolean isLegal(Color color, Point point) {
		if (koPoint != null && koPoint.x == point.x && koPoint.y == point.y)
			return false;
		if (BasicTactics.isSingleSuicide(this, color, point))
			return false;
		if (BasicTactics.fillsEye(this, color, point))
			return false;
		return true;
	}
	
	// Assumes it is a legal move
	public void addStone(Color color, Point point, boolean rehash) {
		koPoint = null;
		turn = color.opposite();
		recentPoint = point;
		
		// Flags to update simFeatures
		boolean atari = false, selfAtari = false,
				twoLibs = false, selfTwoLibs = false,
				chainRemoved = false;
		
		// This is for implementing passes
		if (point == null) return;
		
		int x = point.x; int y = point.y;
		board[x][y] = color;
		emptyPoints.set(pointToIndex[x][y], null);
		lazyDeletes++;
		if (lazyDeletes > emptyPoints.size()/2)
			resetEmptyPoints();
		
		List<Integer> chainsAddedTo = new ArrayList<Integer>();
		if (x > 0 && board[x-1][y] != Color.EMPTY) {
			int i = updateChain(x-1, y, color, point, chainsAddedTo);
			if (i >= 0) chainsAddedTo.add(i);
			else if (i == -4) chainRemoved = true;
			else if (i == -3) twoLibs = true;
			else if (i == -2) atari = true;
		}
		if (x < size-1 && board[x+1][y] != Color.EMPTY) {
			int i = updateChain(x+1, y, color, point, chainsAddedTo);
			if (i >= 0) chainsAddedTo.add(i);
			else if (i == -4) chainRemoved = true;
			else if (i == -3) twoLibs = true;
			else if (i == -2) atari = true;
		}
		if (y > 0 && board[x][y-1] != Color.EMPTY) {
			int i = updateChain(x, y-1, color, point, chainsAddedTo);
			if (i >= 0) chainsAddedTo.add(i);
			else if (i == -4) chainRemoved = true;
			else if (i == -3) twoLibs = true;
			else if (i == -2) atari = true;
		}
		if (y < size-1 && board[x][y+1] != Color.EMPTY) {
			int i = updateChain(x, y+1, color, point, chainsAddedTo);
			if (i >= 0) chainsAddedTo.add(i);
			else if (i == -4) chainRemoved = true;
			else if (i == -3) twoLibs = true;
			else if (i == -2) atari = true;
		}

		// New chain
		if (chainsAddedTo.size() == 0) {
			chainMap[x][y] = nextChainIndex;
			Chain chain = new Chain(nextChainIndex, color, point);
			chain.calculateLiberties(board);
			chains.add(chain);
			nextChainIndex++;
		// Merge chains
		} else {
			int i = mergeChains(x, y, chainsAddedTo);
			if (i == -2) selfTwoLibs = true;
			else if (i == -1) selfAtari = true;
		}
		
		if (rehash) {
			if (chainRemoved) zobristHash = zobristHash();
			else updateHash(point);
		}
	}
	
	// -4 = chain removed, -3 = twoLibs, -2 = atari, -1 = chain updated 
	// already or no chain removed, >= 0 everything else
	private int updateChain(int x, int y, Color color, Point point, List<Integer> chainsAddedTo) {
		int chainIndex = chainMap[x][y];
		
		if (chainsAddedTo.contains(chainIndex)) {
			// If there are 3 instances of the same chainIndex, we need to
			// decrease a liberty
			int count = 0;
			for (int i: chainsAddedTo) {
				if (i == chainIndex) count++;
			}
			if (count >= 1) chains.get(chainIndex).numLiberties--;
			return -1;
		}
		
		if (board[x][y] != color) {
			Chain chain = chains.get(chainIndex);
			chain.numLiberties--;
			if (chain.numLiberties < 3)
				chain.calculateLiberties(board);
			if (chain.numLiberties == 0 && chain.points.size() > 1) {
				removeChain(chainIndex);
				return -4;
			} else if (chain.numLiberties == 0) { // Ko
				removeChain(chainIndex);
				koPoint = new Point(x,y);
				return -4;
			} else if (chain.numLiberties == 2) {
				return -3;
			} else if (chain.numLiberties == 1) {
				return -2;
			}
			return -1;
		} else {
			Chain chain = chains.get(chainIndex);
			chain.points.add(point);
			chainMap[point.x][point.y] = chainIndex;
			return chainIndex;
		}
	}
	
	private void removeChain(int chainIndex) {
		Chain chain = chains.get(chainIndex);
		for (Point p: chain.points) {
			board[p.x][p.y] = Color.EMPTY;
			emptyPoints.add(new Point(p.x, p.y));
			pointToIndex[p.x][p.y] = emptyPoints.size()-1; 
		}
		chains.set(chainIndex, null);
	}
	
	// -2 = self two libs, -1 = self atari, >= 0 anything else 
	private int mergeChains(int x, int y, List<Integer> chainsAddedTo) {
		int mainChainIndex = chainsAddedTo.get(0);
		chainMap[x][y] = mainChainIndex;
		Chain mainChain = chains.get(mainChainIndex);
		for (int i = 1; i < chainsAddedTo.size(); i++) {
			Chain chain = chains.get(chainsAddedTo.get(i));
			for (Point p: chain.points) {
				chainMap[p.x][p.y] = mainChainIndex;
				if (p.x != x || p.y != y)
					mainChain.points.add(p);
			}
			chains.set(chainsAddedTo.get(i), null);
		}
		if (mainChain.numLiberties < 3) {
			mainChain.calculateLiberties(board);
			if (mainChain.numLiberties == 2)
				return -2;
			else if (mainChain.numLiberties == 1)
				return -1;
			else if (mainChain.numLiberties == 0)
				removeChain(mainChainIndex);
		}
		return 0;
	}
	
	// Updates emptyPoints since we do lazy deletes
	private void resetEmptyPoints() {
		lazyDeletes = 0;
		List<Point> tmp = new ArrayList<Point>();
		int nullCount = 0;
		int[] nullCounts = new int[emptyPoints.size()]; 
		for (int i = 0; i < emptyPoints.size(); i++) {
			Point p = emptyPoints.get(i);
			if (p != null)
				tmp.add(p);
			else
				nullCount++;
			nullCounts[i] = nullCount;
		}
		emptyPoints = tmp;
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (pointToIndex[i][j] < nullCounts.length && pointToIndex[i][j] >= 0)
					pointToIndex[i][j] -= nullCounts[pointToIndex[i][j]];
			}
		}
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
					if (chains.get(chainMap[i][j]).numLiberties > 9)
						p = '9';
					else 
						p = (""+chains.get(chainMap[i][j]).numLiberties).charAt(0);
				}
				System.out.print(p + " ");
			}
			System.out.println();
		}
	}
}
