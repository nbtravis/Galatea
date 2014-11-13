package galatea.board;

import java.io.Serializable;

public class Point implements Serializable, Comparable<Point> {

	public int x,y;
	
	public Point(int x, int y) {
		this.x = x; this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		return (x == ((Point) o).x && y == ((Point) o).y);
	}
	
	@Override
	public int hashCode() {
		return (x + "_" + y).hashCode();
	}

	@Override
	public int compareTo(Point o) {
		if (x - o.x == 0)
			return y - o.y;
		return x - o.x;
	}
	
	private static final long serialVersionUID = 6311204091549713300L;
}
