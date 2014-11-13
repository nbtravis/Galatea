package galatea.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Chain implements Serializable {

	// Chains are merged into a mainChain, which holds the overall group
	// information (this index is the index of the mainChain).
	public int index;
	public Chain mainChain;
	
	public Color color;
	public List<Point> points = new ArrayList<Point>();
	
	private boolean upToDate = false;
	
	public List<Chain> mergedChains = new ArrayList<Chain>();

	public int numLiberties;
	
	public int numLiberties() {
		return mainChain.numLiberties;
	}
	
	/**
	 * Only a mainChain will ever be merged, and it will only be merged with
	 * another mainChain.
	 */
	public void merge(Chain other) {
		other.mainChain = this;
		other.index = index;
		for (Chain merged: other.mergedChains) {
			mergedChains.add(merged);
			merged.mainChain = mainChain;
			merged.index = index;
		}
		numLiberties = Math.max(mainChain.numLiberties, other.numLiberties);
		upToDate = false;
	}
	
	public void addPoint(Point point, int newLiberties) {
		numLiberties += newLiberties-1;
		points.add(point);
	}
	
	public void calculateLiberties(Board board) {
		if (upToDate) return;
		
		boolean[][] used = new boolean[board.size][board.size];

		int count = 0;
		for (Chain chain: mergedChains) {
			for (Point p1: chain.points) {
				int x = p1.x, y = p1.y;
				if (x > 0 && board.board[x-1][y] == Color.EMPTY) {
					if (!used[x-1][y]) {
						used[x-1][y] = true;
						count++;
					}
				}
				if (x < board.size-1 && board.board[x+1][y] == Color.EMPTY) {
					if (!used[x+1][y]) {
						used[x+1][y] = true;
						count++;
					}
				}
				if (y > 0 && board.board[x][y-1] == Color.EMPTY) {
					if (!used[x][y-1]) {
						used[x][y-1] = true;
						count++;
					}
				} 
				if (y < board.size-1 && board.board[x][y+1] == Color.EMPTY) {
					if (!used[x][y+1]) {
						used[x][y+1] = true;
						count++;
					}
				}
			}
		}
		numLiberties = count;
		upToDate = true;
	}
	
	public Chain(int index, Color color, Point initialPoint, int numLiberties) {
		this.index = index;
		this.mainChain = this;
		this.color = color;
		points.add(initialPoint);
		this.numLiberties = numLiberties;
		mergedChains.add(this);
	}

	private static final long serialVersionUID = 702293880815759451L;
}
