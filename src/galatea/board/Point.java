package galatea.board;

import java.io.Serializable;

public class Point implements Serializable {

	private static final long serialVersionUID = 6311204091549713300L;
	
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
		return x^y;
	}
}
