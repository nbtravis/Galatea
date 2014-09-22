package galatea.patterns;

import java.util.HashMap;
import java.util.Map;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;

public class ThreeByThree implements Comparable<ThreeByThree> {
	
	public Color[][] pattern = new Color[3][3];
	
	public ThreeByThree(Board board, Point p) {
		Map<Color, Color> colorMap = new HashMap<Color, Color>();
		if (board.turn == Color.WHITE) {
			colorMap.put(Color.WHITE, Color.WHITE);
			colorMap.put(Color.BLACK, Color.BLACK);
		} else {
			colorMap.put(Color.WHITE, Color.BLACK);
			colorMap.put(Color.BLACK, Color.WHITE);
		}
		colorMap.put(Color.EMPTY, Color.EMPTY);
		
		int x = p.x, y = p.y;
		
		if (x > 0 && y > 0) pattern[0][0] = colorMap.get(board.board[x-1][y-1]);
		else pattern[0][0] = Color.OFFBOARD;
		if (x > 0) pattern[0][1] = colorMap.get(board.board[x-1][y]);
		else pattern[0][1] = Color.OFFBOARD;
		if (x > 0 && y < board.size-1) pattern[0][2] = colorMap.get(board.board[x-1][y+1]);
		else pattern[0][2] = Color.OFFBOARD;
		if (y < board.size-1) pattern[1][2] = colorMap.get(board.board[x][y+1]);
		else pattern[1][2] = Color.OFFBOARD;
		if (x < board.size-1 && y < board.size-1) pattern[2][2] = colorMap.get(board.board[x+1][y+1]);
		else pattern[2][2] = Color.OFFBOARD;
		if (x < board.size-1) pattern[2][1] = colorMap.get(board.board[x+1][y]);
		else pattern[2][1] = Color.OFFBOARD;
		if (x < board.size-1 && y > 0) pattern[2][0] = colorMap.get(board.board[x+1][y-1]);
		else pattern[2][0] = Color.OFFBOARD;
		if (y > 0) pattern[1][0] = colorMap.get(board.board[x][y-1]);
		else pattern[1][0] = Color.OFFBOARD;
		
		pattern[1][1] = Color.EMPTY;
	}
	
	public ThreeByThree() {
	}
	
	public ThreeByThree rotate90() {
		ThreeByThree newPattern = new ThreeByThree();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				newPattern.pattern[i][j] = this.pattern[j][i];
		}
		return newPattern;
	}
	
	public ThreeByThree reflectX() {
		ThreeByThree newPattern = new ThreeByThree();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				newPattern.pattern[i][j] = this.pattern[i][2-j];
		}
		return newPattern;
	}
	
	public ThreeByThree reflectY() {
		ThreeByThree newPattern = new ThreeByThree();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				newPattern.pattern[i][j] = this.pattern[2-i][j];
		}
		return newPattern;
	}
	
	@Override
	public int hashCode() {
		String code = "";
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				code += pattern[i][j].ordinal();
		}
		return Integer.parseInt(code);
	}
	
	@Override
	public boolean equals(Object other) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				if (pattern[i][j] != ((ThreeByThree) other).pattern[i][j]) 
					return false;
		}
		return true;
	}
	
	public void printPattern() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) 
				System.out.print(pattern[i][j].ordinal() + " ");
			System.out.println();
		}
	}

	@Override
	public int compareTo(ThreeByThree o) {
		return hashCode() - o.hashCode();
	}
}
