package galatea.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chain implements Serializable {

	private static final long serialVersionUID = 702293880815759451L;
	
	private int index;
	public Color color;
	protected List<Point> points = new ArrayList<Point>();
	
	// Use a fixed size array of liberties since we usually only care about 
	// liberties when there aren't many of them
	private Point[] liberties = new Point[10];
	public int numLiberties;
	
	public Chain(int index, Color color, Point initialPoint) {
		this.index = index;
		this.color = color;
		points.add(initialPoint);
	}
	
	protected void calculateLiberties(Color[][] board) {
		int i = 0;
		Set<Point> seen = new HashSet<Point>();
		for (Point p: points) {
			Point p1 = new Point(p.x-1, p.y);
			if (p.x > 0 && board[p.x-1][p.y] == Color.EMPTY && !seen.contains(p1)) { 
				liberties[i] = p1;
				i++;
				if (i >= 10) break;
				seen.add(p1);
			}
			p1 = new Point(p.x+1, p.y);
			if (p.x < board.length-1 && board[p.x+1][p.y] == Color.EMPTY && !seen.contains(p1)) {
				liberties[i] = p1;
				i++;
				if (i >= 10) break;
				seen.add(p1);
			}
			p1 = new Point(p.x, p.y-1);
			if (p.y > 0 && board[p.x][p.y-1] == Color.EMPTY && !seen.contains(p1)) {
				liberties[i] = p1;
				i++;
				if (i >= 10) break;
				seen.add(p1);
			}
			p1 = new Point(p.x, p.y+1);
			if (p.y < board.length-1 && board[p.x][p.y+1] == Color.EMPTY && !seen.contains(p1)) {
				liberties[i] = p1;
				i++;
				if (i >= 10) break;
				seen.add(p1);
			}
		}
		numLiberties = i;
	}
	
	protected int countLiberties() {
		int count = 0;
		for (int i = 0; i < 10; i++) {
			if (liberties[i] != null) 
				count++;
		}
		return count;
	}
}
