package galatea.engine;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;
import galatea.util.DeepCopy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
	
	protected Board board;
	public Point recentPoint;
	public int[][][][] criticalityCounts;
	public boolean isRoot = false;
	public boolean isLeaf = true;
	public Color turn;
	
	private Set<Integer> visitedBoards;
	
	public int wins = 1, sims = 2;
	public int winsAmaf = 1, simsAmaf = 2;
	
	public Node parent;
	public List<Node> children = new ArrayList<Node>();
	
	public Node(Board board, Color turn) {
		isRoot = true;
		this.turn = turn;
		this.board = (Board) DeepCopy.copy(board);
		criticalityCounts = new int[board.board.length][board.board.length][3][2];
		visitedBoards = new HashSet<Integer>();
	}
	
	public Node(Node parent, Color color, Point point) {
		this.parent = parent;
		turn = parent.turn.opposite();
		this.board = (Board) DeepCopy.copy(parent.board);
		recentPoint = point;
		criticalityCounts = new int[board.board.length][board.board.length][3][2];
		visitedBoards = parent.visitedBoards;
		this.board.addStone(color, point, true);
	}
	
	protected void expand() {
		for (Point emptyPoint: board.emptyPoints) {
			if (emptyPoint != null && board.isLegal(turn, emptyPoint)) {
				Node child = new Node(this, turn, emptyPoint);
				if (!visitedBoards.contains(child.board.zobristHash)) {
					children.add(child);
					visitedBoards.add(child.board.zobristHash);
				}
			}
		}
		if (children.size() > 0)
			isLeaf = false;
	}
	
	// TODO: make AMAF more intelligent
	protected void update(Color winner, Board finalBoard, boolean[][][] moves) {
		sims++; 
		if (winner != turn)
			wins++;
		// Update AMAF (RAVE) values
		if (isLeaf && !isRoot) {
			int parentTurn = parent.turn.ordinal();
			for (Node sibling: parent.children) {
				int x = sibling.recentPoint.x, y = sibling.recentPoint.y;
				if (moves[x][y][parentTurn] == true)
					sibling.updateRAVE(winner);
			}
		}
		// Update criticality values
		if (!isRoot) {
			for (Node sibling: parent.children) {
				int x = sibling.recentPoint.x, y = sibling.recentPoint.y;
				Color color = finalBoard.board[x][y];
				int winIndex = (color == winner ? 0 : 1);
				parent.criticalityCounts[x][y][color.ordinal()][winIndex]++;
			}
		}
		if (!isRoot) {
			moves[recentPoint.x][recentPoint.y][parent.turn.ordinal()] = true;
			parent.update(winner, finalBoard, moves);
		}
	}
	
	protected void updateRAVE(Color winner) {
		simsAmaf++;
		if (winner != turn)
			winsAmaf++;
	}
}
