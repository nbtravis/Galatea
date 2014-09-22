package galatea.tactics;

import galatea.board.Board;
import galatea.board.Chain;
import galatea.board.Color;
import galatea.board.Point;

public class BasicTactics {

	// Returns true if by playing a stone at point, you are committing
	// suicide for that single stone.
	public static boolean isSingleSuicide(Board board, Color color, Point point) {
		int x = point.x; int y = point.y;
		Color otherColor = color.opposite();
		
		// Check to see if there is a liberty
		if (x > 0 && board.board[x-1][y] != otherColor)
			return false;
		if (x < board.size-1 && board.board[x+1][y] != otherColor) 
			return false;
		if (y > 0 && board.board[x][y-1] != otherColor)
			return false;
		if (y < board.size-1 && board.board[x][y+1] != otherColor)
			return false;
		
		// Check to see if placing stone will capture group
		if (x > 0 && board.board[x-1][y] == otherColor) {
			Chain chain = board.chains.get(board.chainMap[x-1][y]);
			if (chain.numLiberties < 3)
				chain.calculateLiberties(board.board);
			if (chain.numLiberties == 1)
				return false;
		}
		if (x < board.size-1 && board.board[x+1][y] == otherColor) {
			Chain chain = board.chains.get(board.chainMap[x+1][y]);
			if (chain.numLiberties < 3)
				chain.calculateLiberties(board.board);
			if (chain.numLiberties == 1)
				return false;
		}
		if (y > 0 && board.board[x][y-1] == otherColor) {
			Chain chain = board.chains.get(board.chainMap[x][y-1]);
			if (chain.numLiberties < 3)
				chain.calculateLiberties(board.board);
			if (chain.numLiberties == 1)
				return false;
		}
		if (y < board.size-1 && board.board[x][y+1] == otherColor) {
			Chain chain = board.chains.get(board.chainMap[x][y+1]);
			if (chain.numLiberties < 3)
				chain.calculateLiberties(board.board);
			if (chain.numLiberties == 1)
				return false;
		}
		return true;
	}
	
	public static boolean fillsEye(Board board, Color color, Point point) {
		int x = point.x, y = point.y;
		int chainIndex = -1;
		if (x > 0) {
			if (board.board[x-1][y] != color) 
				return false;
			else if (chainIndex != -1 && board.chainMap[x-1][y] != chainIndex)
				return false;
			else {
				chainIndex = board.chainMap[x-1][y];
			}
		}
		if (x < board.size-1) {
			if (board.board[x+1][y] != color) 
				return false;
			else if (chainIndex != -1 && board.chainMap[x+1][y] != chainIndex)
				return false;
			else
				chainIndex = board.chainMap[x+1][y];
		}
		if (y > 0) {
			if (board.board[x][y-1] != color) 
				return false;
			else if (chainIndex != -1 && board.chainMap[x][y-1] != chainIndex)
				return false;
			else
				chainIndex = board.chainMap[x][y-1];
		}
		if (y < board.size-1) {
			if (board.board[x][y+1] != color) 
				return false;
			else if (chainIndex != -1 && board.chainMap[x][y+1] != chainIndex)
				return false;
			else
				chainIndex = board.chainMap[x][y+1];
		}
		if (board.chains.get(chainIndex).numLiberties > 2)
			return false;
		return true;
	}
	
}
