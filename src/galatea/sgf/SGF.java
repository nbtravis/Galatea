package galatea.sgf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import galatea.board.Point;

// This parses only the main line of an sgf file (not meant as a general-
// purpose SGF parser)
public class SGF {
	
	private List<Point> moves = new ArrayList<Point>();
	private int moveIndex = 0;
	
	private Point coordToPoint(String coord) {
		int x = (int) (coord.charAt(2) - 'a');
		int y = (int) (coord.charAt(3) - 'a');
		return new Point(y, x);
	}
	
	public SGF(String file) {
		Pattern p = Pattern.compile("[BW]\\[[a-z][a-z]");
		Matcher m = null;
		try {
			m = p.matcher(new BufferedReader(new FileReader(file)).readLine());
		} catch (Exception e) {}
		while (m.find())
			moves.add(coordToPoint(m.group()));
	}
	
	public Point nextMove() {
		if (moveIndex >= moves.size()) return null;
		Point next = moves.get(moveIndex);
		moveIndex++;
		return next;
	}
}
