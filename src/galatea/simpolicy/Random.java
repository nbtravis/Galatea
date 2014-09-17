package galatea.simpolicy;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;

import java.util.ArrayList;
import java.util.List;

public class Random implements SimPolicy {
	
	public Point getMove(Board board) {
		int count = 0;
		while (count < 10) {
			int index = (int) Math.floor(Math.random()*board.emptyPoints.size());
			if (board.emptyPoints.get(index) != null && board.isLegal(board.turn, board.emptyPoints.get(index)))
				return board.emptyPoints.get(index);
			count++;
		}
		
		List<Point> legalPoints = new ArrayList<Point>();
		for (Point emptyPoint: board.emptyPoints) {
			if (emptyPoint != null && board.isLegal(board.turn, emptyPoint))
				legalPoints.add(emptyPoint);
		}
		if (legalPoints.size() == 0)
			return null;
		int index = (int) Math.floor(Math.random()*legalPoints.size());
		return legalPoints.get(index);
	}
}