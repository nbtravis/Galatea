package galatea.engine;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;
import galatea.simpolicy.SimFeatures;
import galatea.util.DeepCopy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A node in the MCTS GameTree. Holds information about winrates, RAVE
 * winrates, criticality values (see UCTRAVE), the parent node, children
 * nodes, and the corresponding board position. 
 */
public class Node {
	
	protected Board board;
	public Point recentPoint;
	
	// Sorted list of MoveScore's
	public List<Move> legalMoves = new ArrayList<>();
	private int moveIndex = 0;
	public double simsBeforeNextChild = 0;
	
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
	
	public Node(Node parent, Color color, Move move) {
		this.parent = parent;
		turn = parent.turn.opposite();
		this.board = (Board) DeepCopy.copy(parent.board);
		recentPoint = move.point;
		criticalityCounts = new int[board.board.length][board.board.length][3][2];
		visitedBoards = parent.visitedBoards;
		this.board.addStone(color, move.point);
		// Update information on previous move from move.simFeatures
		this.board.prevAtariedChains = move.simFeatures.prevAtariedChains;
		this.board.prevTwoLibbedChains = move.simFeatures.prevTwoLibbedChains;
	}
	
	// Create list of moves sorted by elo for progressive widening, then add the top 10
	public void expand() {
		for (Point move: board.getLegalMoves()) {
			SimFeatures simFeatures = board.addStoneFast(board.turn, move);
			legalMoves.add(new Move(move, simFeatures));
		}
		Collections.sort(legalMoves);
		moveIndex = legalMoves.size()-1;
		for (int i = 0; i < 1; i++) {
			addNextChild();
		}
	}
	
	/**
	 * TODO: Heavily test the parameters for updating simsBeforeNextChild.
	 */
	public void addNextChild() {
		boolean added = false;
		while (moveIndex >= 0 && !added) {
			Node child = new Node(this, turn, legalMoves.get(moveIndex));
			if (!visitedBoards.contains(child.board.zobristHash)) {
				children.add(child);
				visitedBoards.add(child.board.zobristHash);
				added = true;
				isLeaf = false;
			}
			moveIndex--;
		}
		simsBeforeNextChild += 20*Math.pow(1.2, children.size()-1);
	}
	
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
