package galatea.engine;

import galatea.board.Board;
import galatea.board.Color;

import java.util.HashSet;
import java.util.Set;

public class GameTree {
	
	protected Node root;
	
	public GameTree(Board board, Color turn) {
		root = new Node(board, turn);
		root.expand();
	}
	
	public void setRoot(Node newRoot) {
		root = newRoot;
		newRoot.isRoot = true;
	}
	
	public void printTree() {
		printNode(root, 0);
		System.out.println(" " + root.board.turn + 
		           " wins: " + root.wins + "/" + root.sims +
		           " winsAmaf: " + root.winsAmaf + "/" + root.simsAmaf +
		           " numChildren: " + root.children.size() + 
		           " simsBeforeNextChild: " + root.simsBeforeNextChild);
	}
	
	public void printNode(Node node, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("--");
		}
		if (!node.isRoot)
			System.out.println(" " + node.board.turn + " " + node.recentPoint.x + " " + node.recentPoint.y + 
					           " wins: " + node.wins + "/" + node.sims + 
					           " winsAmaf: " + node.winsAmaf + "/" + node.simsAmaf +
					           " numChildren: " + node.children.size() +
					           " simsBeforeNextChild: " + node.simsBeforeNextChild);
		else
			System.out.println(" " + node.board.turn + 
			           " wins: " + node.wins + "/" + node.sims +
			           " winsAmaf: " + node.winsAmaf + "/" + node.simsAmaf +
			           " numChildren: " + node.children.size() +
			           " simsBeforeNextChild: " + root.simsBeforeNextChild);
		
		for (Node child: node.children){
			printNode(child, depth+1);
		}
	}
}
