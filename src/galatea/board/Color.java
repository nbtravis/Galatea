package galatea.board;

public enum Color {
	EMPTY,
	WHITE,
	BLACK;
	
	public Color opposite() {
		if (this == Color.BLACK) return Color.WHITE;
		if (this == Color.WHITE) return Color.BLACK;
		return Color.EMPTY;
	}
}
