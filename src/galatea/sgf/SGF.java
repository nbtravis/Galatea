package galatea.sgf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import galatea.board.Board;
import galatea.board.Point;

/**
 * This only parses the main line of a standard, no handicap game.
 * It is not intended as a general purpose SGF parser; it merely exists to 
 * extract enough information from SGF files to be used for MinorMaxim
 * learning. 
 */
public class SGF {
	
	private List<Point> moves = new ArrayList<Point>();
	private int moveIndex = 0;
	private boolean passesAtEnd = false;
	
	private Point coordToPoint(String coord) {
		int x = (int) (coord.charAt(2) - 'a');
		int y = (int) (coord.charAt(3) - 'a');
		return new Point(y, x);
	}
	
	public SGF(String file) {
		Pattern p1 = Pattern.compile("[BW]\\[[a-z][a-z]"),
				p2 = Pattern.compile("(B+[0-9]|W+[0-9])");
		Matcher m1 = null, m2 = null;
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			StringBuffer s = new StringBuffer();
			String line;
			while ((line = r.readLine()) != null)
				s.append(line);
			m1 = p1.matcher(s.toString());
			m2 = p2.matcher(s.toString());
			r.close();
		} catch (Exception e) {}
		while (m1.find()) {
			String match = m1.group();
			moves.add(coordToPoint(match));
		}
		if (m2.find())
			passesAtEnd = true;
	}
	
	public Point nextPoint() {
		if (moveIndex >= moves.size())
			return null;
		Point next = moves.get(moveIndex);
		moveIndex++;
		return next;
	}
	
	public static void main(String[] args) {
		SGF sgf = new SGF("./data/14vv-gokifu-20121104-Lee_Sedol-Yun_Junsang.sgf");
		Board board = new Board(19, 0, 0);
		int moveCount = 2;
		Point move;
		board.addStone(board.turn, sgf.nextPoint());
		board.addStone(board.turn, sgf.nextPoint());
		while ((move = sgf.nextPoint()) != null) {
			System.out.println(moveCount); moveCount++;
			System.out.println(board.lastMove.x + " " + board.lastMove.y);
			board.addStone(board.turn, move);
			board.printBoard();
		}
	}
}
