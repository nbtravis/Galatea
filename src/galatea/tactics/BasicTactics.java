package galatea.tactics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import galatea.board.Board;
import galatea.board.Chain;
import galatea.board.Color;
import galatea.board.Point;

/**
 * Holds miscellaneous methods for use in Board.java. Most are "tactical," but
 * some are just for things like determining if a position is symmetrical.  
 */
public class BasicTactics {

	public static int extraLiberties(Board b, Point point, Set<Integer> sameChains, List<Point> liberties) {
		List<Chain> chains = b.chains;
		int[][] chainMap = b.chainMap;
		Color[][] board = b.board;
		Color turn = b.turn;
		int size = b.size;
		
		int x = point.x, y = point.y;
		
		int newLiberties = 0;
		for (Point lib: liberties) {
			int lx = lib.x, ly = lib.y;
			if (lx < x) {
				if (y > 0 && (board[lx][y-1] == turn && sameChains.contains(chains.get(chainMap[lx][y-1]).index))) continue;
				if (y < size-1 && (board[lx][y+1] == turn && sameChains.contains(chains.get(chainMap[lx][y+1]).index))) continue;
				if (lx > 0 && (board[lx-1][y] == turn && sameChains.contains(chains.get(chainMap[lx-1][y]).index))) continue;
			} else if (lx > x) {
				if (y > 0 && (board[lx][y-1] == turn && sameChains.contains(chains.get(chainMap[lx][y-1]).index))) continue;
				if (y < size-1 && (board[lx][y+1] == turn && sameChains.contains(chains.get(chainMap[lx][y+1]).index))) continue;
				if (lx < size-1 && (board[lx+1][y] == turn && sameChains.contains(chains.get(chainMap[lx+1][y]).index))) continue;
			} else if (ly < y) {
				if (x > 0 && (board[x-1][ly] == turn && sameChains.contains(chains.get(chainMap[x-1][ly]).index))) continue;
				if (x < size-1 && (board[x+1][ly] == turn && sameChains.contains(chains.get(chainMap[x+1][ly]).index))) continue;
				if (ly > 0 && (board[x][ly-1] == turn && sameChains.contains(chains.get(chainMap[x][ly-1]).index))) continue;
			} else {
				if (x > 0 && (board[x-1][ly] == turn && sameChains.contains(chains.get(chainMap[x-1][ly]).index))) continue;
				if (x < size-1 && (board[x+1][ly] == turn && sameChains.contains(chains.get(chainMap[x+1][ly]).index))) continue;
				if (ly < size-1 && (board[x][ly+1] == turn && sameChains.contains(chains.get(chainMap[x][ly+1]).index))) continue;
			}
			newLiberties++;
		}
		return newLiberties;
	}
	
	// Returns true if by playing a stone at point, you are committing
	// suicide for that single stone.
	public static boolean isSuicide(Board b, Point point) {
		List<Chain> chains = b.chains;
		int[][] chainMap = b.chainMap;
		Color[][] board = b.board;
		Color turn = b.turn;
		int size = b.size;
		
		int x = point.x, y = point.y;
		
		// See if any liberties around point
		if (x > 0 && board[x-1][y] == Color.EMPTY)
			return false;
		if (x < size-1 && board[x+1][y] == Color.EMPTY)
			return false;
		if (y > 0 && board[x][y-1] == Color.EMPTY)
			return false;
		if (y < size-1 && board[x][y+1] == Color.EMPTY)
			return false;
		
		Set<Integer> sameChains = new HashSet<Integer>();
		if (x > 0 && board[x-1][y] == turn)
			sameChains.add(chains.get(chainMap[x-1][y]).index);
		if (x < size-1 && board[x+1][y] == turn)
			sameChains.add(chains.get(chainMap[x+1][y]).index);
		if (y > 0 && board[x][y-1] == turn)
			sameChains.add(chains.get(chainMap[x][y-1]).index);
		if (y < size-1 && board[x][y+1] == turn)
			sameChains.add(chains.get(chainMap[x][y+1]).index);
		
		Set<Integer> oppositeChains = new HashSet<Integer>();
		if (x > 0 && board[x-1][y] == turn.opposite())
			oppositeChains.add(chains.get(chainMap[x-1][y]).index);
		if (x < size-1 && board[x+1][y] == turn.opposite())
			oppositeChains.add(chains.get(chainMap[x+1][y]).index);
		if (y > 0 && board[x][y-1] == turn.opposite())
			oppositeChains.add(chains.get(chainMap[x][y-1]).index);
		if (y < size-1 && board[x][y+1] == turn.opposite())
			oppositeChains.add(chains.get(chainMap[x][y+1]).index);
		
		for (int chainIndex: sameChains) {
			Chain chain = chains.get(chainIndex);
			if (chain.numLiberties() > 1)
				return false;
		}
		for (int chainIndex: oppositeChains) {
			Chain chain = chains.get(chainIndex);
			if (chain.numLiberties() <= 1) {
				chain.calculateLiberties(b);
				if (chain.numLiberties == 1)
					return false;
			}
		}
		return true;
	}
	
	// TODO: Logic here isn't perfect
	public static boolean fillsEye(Board b, Point point) {
		List<Chain> chains = b.chains;
		int[][] chainMap = b.chainMap;
		Color[][] board = b.board;
		Color turn = b.turn;
		int size = b.size;
		
		int x = point.x, y = point.y;
		
		// See if any liberties around point
		if (x > 0 && board[x-1][y] != turn)
			return false;
		if (x < size-1 && board[x+1][y] != turn)
			return false;
		if (y > 0 && board[x][y-1] != turn)
			return false;
		if (y < size-1 && board[x][y+1] != turn)
			return false;
		
		Set<Integer> sameChains = new HashSet<Integer>();
		if (x > 0 && board[x-1][y] == turn)
			sameChains.add(chains.get(chainMap[x-1][y]).index);
		if (x < size-1 && board[x+1][y] == turn)
			sameChains.add(chains.get(chainMap[x+1][y]).index);
		if (y > 0 && board[x][y-1] == turn)
			sameChains.add(chains.get(chainMap[x][y-1]).index);
		if (y < size-1 && board[x][y+1] == turn)
			sameChains.add(chains.get(chainMap[x][y+1]).index);
		
		for (int chainIndex: sameChains) {
			Chain chain = chains.get(chainIndex);
			if (chain.numLiberties <= 0) {
				chain.calculateLiberties(b);
				if (chain.numLiberties == 0)
					return false;
			}
			if (chain.numLiberties >= 3)
				return false;
			if (chain.mergedChains.size() == 1 && chain.points.size() == 1)
				return false;
		}
		return true;
	}
	
	// "Go distance" heuristic (|dx| + |dy| + max(|dx|,|dy|))
	public static int dist(Point p1, Point p2) {
		if (p1 == null || p2 == null) return Integer.MAX_VALUE;
		
		int x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;
		int dx = Math.abs(x1-x2), dy = Math.abs(y1-y2);
		return dx + dy + Math.max(dx, dy);
	}
	
	public static int distToEdge(Board board, Point move) {
		int distToTopBottomEdge = Math.min(move.x, board.size-1-move.x);
		int distToLeftRightEdge = Math.min(move.y, board.size-1-move.y);
		int distToEdge = Math.min(distToTopBottomEdge, distToLeftRightEdge);
		return distToEdge;
	}
	
	public static boolean xAxisSym(Board b) {
		Color[][] board = b.board; 
		for (int i = 0; i < b.size/2; i++) {
			for (int j = 0; j < b.size; j++) {
				if (board[i][j] != board[b.size-1-i][j]) return false;
			}
		}
		return true;
	}
	
	public static boolean yAxisSym(Board b) {
		Color[][] board = b.board; 
		for (int i = 0; i < b.size/2; i++) {
			for (int j = 0; j < b.size; j++) {
				if (board[j][i] != board[j][b.size-1-i]) return false;
			}
		}
		return true;
	}
	
	public static boolean diag1Sym(Board b) {
		Color[][] board = b.board; 
		for (int i = 0; i < b.size; i++) {
			for (int j = 0; j < i; j++) {
				if (board[i][j] != board[j][i]) return false;
			}
		}
		return true;
	}
	
	public static boolean diag2Sym(Board b) {
		Color[][] board = b.board; 
		for (int i = 0; i < b.size; i++) {
			for (int j = 0; j < b.size-1-i; j++) {
				if (board[i][j] != board[b.size-1-j][b.size-1-i]) return false;
			}
		}
		return true;
	}
}
