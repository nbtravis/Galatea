package galatea.simpolicy;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;

public interface SimPolicy {
	
	public Point getMove(Board board);
}
