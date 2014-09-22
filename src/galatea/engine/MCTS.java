package galatea.engine;

import galatea.board.Board;
import galatea.board.Chain;
import galatea.board.Color;
import galatea.board.Point;
import galatea.board.Score;
import galatea.simpolicy.Random;
import galatea.simpolicy.SimPolicy;
import galatea.treepolicy.TreePolicy;
import galatea.treepolicy.UCTRAVE;
import galatea.util.DeepCopy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MCTS {
	
	public Board board;
	public GameTree gameTree;
	private TreePolicy treePolicy;
	private SimPolicy simPolicy;
	
	public MCTS(Board board) {
		treePolicy = new UCTRAVE(8, .5, 400);
		simPolicy = new Random();
		this.board = board;
		gameTree = new GameTree(board, board.turn);
	}
	
	public void compute(int numSims) {
		for (int i = 0; i < numSims; i++) {
			Node leaf = treePolicy.getNode(gameTree.root);
			if (treePolicy.shouldExpand(leaf)) {
				leaf.expand();
				leaf = treePolicy.getNode(leaf);
			}
			runSimulation(leaf);
		}
	}
	
	public boolean updateGameTree(Point lastMove, Color turn) {
		for (Node child: gameTree.root.children) {
			if (child.recentPoint.equals(lastMove)) {
				gameTree.setRoot(child);
				board = child.board;
				return true;
			}
		}
		return false;
	}
	
	public Point getMove(Color turn, int seconds) {
		long start = System.nanoTime();
		while ((System.nanoTime()-start)/1000000000 < seconds) {
			// We don't want to be doing too many operations for the while loop 
			// check, so run x simulations inside for loop
			for (int i = 0; i < 500; i++) {
				Node leaf = treePolicy.getNode(gameTree.root);
				if (treePolicy.shouldExpand(leaf)) {
					leaf.expand();
					leaf = treePolicy.getNode(leaf);
				}
				runSimulation(leaf);
			}
		}
//		gameTree.printTree();
		Node best = treePolicy.getBest(gameTree.root);
		if (best == null) {
			Score score = new Score(gameTree.root.board);
			System.out.println("white: " + score.whiteScore + " black: " + score.blackScore);
			return null;
		}
		return best.recentPoint;
	}
	
	private void runSimulation(Node leaf) {
		Board board = (Board) DeepCopy.copy(leaf.board);
		// Keep track of all moves made for RAVE updates
		boolean[][][] moves = new boolean[board.size][board.size][3];
		Point p1, p2;
		while (true) {
			p1 = simPolicy.getMove(board);
			if (p1 != null)
				moves[p1.x][p1.y][board.turn.ordinal()] = true;
			board.addStone(board.turn, p1, false);
			p2 = simPolicy.getMove(board);
			if (p1 != null)
				moves[p1.x][p1.y][board.turn.ordinal()] = true;
			board.addStone(board.turn, p2, false);
			if (p1 == null && p2 == null) break;
		}
		
		Score score = new Score(board);
		if (score.whiteScore > score.blackScore)
			leaf.update(Color.WHITE, board, moves);
		else
			leaf.update(Color.BLACK, board, moves);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Board board = new Board(9, 0, 6.5);
		MCTS engine = new MCTS(board);
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			engine.board.printBoard();
			engine.board.printLiberties();
			
			// Get user move
			String[] fields = r.readLine().split(" ");
			Point p1;
			if (fields.length < 2) p1 = null;
			else p1 = new Point(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
			
			boolean b = engine.updateGameTree(p1, engine.board.turn);
			if (!b) {
				engine.board.addStone(engine.board.turn, p1, true);
				engine.gameTree = new GameTree(board, board.turn);
			}
			engine.board.printBoard();
			
			Point p2 = engine.getMove(engine.board.turn, 1);
			engine.gameTree.printTree();

			b = engine.updateGameTree(p2, engine.board.turn);
			if (!b) {
				engine.board.addStone(engine.board.turn, p2, true);
				engine.gameTree = new GameTree(board, board.turn);
			}
			
			if (p1 == null && p2 == null) break;			
		}
	}
}
