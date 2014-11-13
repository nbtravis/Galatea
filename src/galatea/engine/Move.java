package galatea.engine;

import galatea.board.Point;
import galatea.simpolicy.SimFeatures;

/**
 *  Used to sort moves by score for progressive widening.
 */
public class Move implements Comparable<Move> {
	
	public Point point;
	public SimFeatures simFeatures;
	public double score;
	
	public Move(Point point, SimFeatures simFeatures) {
		this.point = point;
		this.simFeatures = simFeatures;
		this.score = simFeatures.getScore();
	}

	@Override
	public int compareTo(Move o) {
		return Double.compare(score, o.score);
	}
}
