package galatea.engine;

import galatea.board.Board;
import galatea.board.Color;

import java.util.HashSet;
import java.util.Set;

public class GameTree {
	
	protected Node root;
	
	public GameTree(Board board, Color turn) {
		root = new Node(board, turn);
	}
	
	public void setRoot(Node newRoot) {
		root = newRoot;
		newRoot.isRoot = true;
	}
	
	public void printTree() {
		printNode(root, 0);
		System.out.println(" " + root.board.turn.opposite() + 
		           " wins: " + root.wins + "/" + root.sims +
		           " winsAmaf: " + root.winsAmaf + "/" + root.simsAmaf);
	}
	
	public void printNode(Node node, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("--");
		}
		if (!node.isRoot)
			System.out.println(" " + node.board.turn.opposite() + " " + node.recentPoint.x + " " + node.recentPoint.y + 
					           " wins: " + node.wins + "/" + node.sims + 
					           " winsAmaf: " + node.winsAmaf + "/" + node.simsAmaf);
		else
			System.out.println(" " + node.board.turn.opposite() + 
			           " wins: " + node.wins + "/" + node.sims +
			           " winsAmaf: " + node.winsAmaf + "/" + node.simsAmaf);
		
		for (Node child: node.children){
			printNode(child, depth+1);
		}
	}
}
