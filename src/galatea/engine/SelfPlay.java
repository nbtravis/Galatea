package galatea.engine;

import galatea.board.Board;
import galatea.board.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This allows you to play yourself or another human in a text-based
 * environment. I created it mainly as a simple testing tool. 
 */
public class SelfPlay {

	public static void main(String[] args) throws IOException, InterruptedException {
		Board board = new Board(9, 0, 6.5);
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			board.printBoard();
			System.out.println();
			board.printLiberties();
			System.out.println();
			board.printLegalMoves();
			System.out.println();
			
			// Get user move
			String[] fields = r.readLine().split(" ");
			Point p1;
			if (fields.length < 2) p1 = null;
			else p1 = new Point(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
			
			board.addStone(board.turn, p1);
		}
	}
}
