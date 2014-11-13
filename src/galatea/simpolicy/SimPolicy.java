package galatea.simpolicy;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;
import galatea.engine.Move;

public interface SimPolicy {
	
	public Move getMove(Board board);
}
