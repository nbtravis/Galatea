package galatea.simpolicy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;
import galatea.engine.Move;

public class MMPolicy implements SimPolicy {

	@Override
	public Move getMove(Board board) {		
		List<Point> legalMoves = board.getLegalMoves();		
		Move[] moves = new Move[legalMoves.size()];
		double total = 0;
		int i = 0;
		for (Point legalMove: legalMoves) {
			SimFeatures simFeatures = board.addStoneFast(board.turn, legalMove);
			moves[i] = new Move(legalMove, simFeatures);
			total += moves[i].score;
			i++;
		}
		double rand = Math.random()*total;
		double totalSoFar = 0;
		for (int j = 0; j < moves.length; j++) {
			totalSoFar += moves[j].score;
			if (totalSoFar > rand)
				return moves[j];
		}
		return null;
	}
}
