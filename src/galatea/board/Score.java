package galatea.board;

public class Score {
	
	public double whiteScore = 0, blackScore = 0;

	public Score(Board board) {
		for (int i = 0; i < board.size; i++) {
			for (int j = 0; j < board.size; j++) {
				if (board.board[i][j] == Color.WHITE)
					whiteScore += 1;
				else if (board.board[i][j] == Color.BLACK)
					blackScore += 1;
			}
		}
		whiteScore += board.komi;
		blackScore -= Math.max(board.handicap-1, 0);
	}
}