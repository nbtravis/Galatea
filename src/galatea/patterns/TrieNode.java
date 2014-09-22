package galatea.patterns;

import galatea.board.Color;

public class TrieNode {
	
	private int depth;
	private TrieNode[] children = new TrieNode[4];
	public double gamma;
	
	public TrieNode(int depth) {
		this.depth = depth;
		for (int i = 0; i < 4; i++)
			children[i] = null;
		gamma = 1;
	}
	
	public TrieNode getTrieNode(Color[][] pattern) {
		if (depth == 8) return this;
		
		switch (depth) {
		case 0:
			if (children[pattern[0][0].ordinal()] != null)
				return children[pattern[0][0].ordinal()].getTrieNode(pattern);
			break;
		case 1:
			if (children[pattern[0][1].ordinal()] != null)
				return children[pattern[0][1].ordinal()].getTrieNode(pattern);
			break;
		case 2:
			if (children[pattern[0][2].ordinal()] != null)
				return children[pattern[0][2].ordinal()].getTrieNode(pattern);
			break;
		case 3:
			if (children[pattern[1][2].ordinal()] != null)
				return children[pattern[1][2].ordinal()].getTrieNode(pattern);
			break;
		case 4:
			if (children[pattern[2][2].ordinal()] != null)
				return children[pattern[2][2].ordinal()].getTrieNode(pattern);
			break;
		case 5:
			if (children[pattern[2][1].ordinal()] != null)
				return children[pattern[2][1].ordinal()].getTrieNode(pattern);
			break;
		case 6:
			if (children[pattern[2][0].ordinal()] != null)
				return children[pattern[2][0].ordinal()].getTrieNode(pattern);
			break;
		case 7:
			if (children[pattern[1][0].ordinal()] != null)
				return children[pattern[1][0].ordinal()].getTrieNode(pattern);
			break;
		}
		return null;
	}
	
	public void insert(Color[][] pattern) {
		switch (depth) {
		case 0:
			if (children[pattern[0][0].ordinal()] == null)
				children[pattern[0][0].ordinal()] = new TrieNode(depth+1);
			children[pattern[0][0].ordinal()].insert(pattern);
			break;
		case 1:
			if (children[pattern[0][1].ordinal()] == null)
				children[pattern[0][1].ordinal()] = new TrieNode(depth+1);
			children[pattern[0][1].ordinal()].insert(pattern);
			break;
		case 2:
			if (children[pattern[0][2].ordinal()] == null)
				children[pattern[0][2].ordinal()] = new TrieNode(depth+1);
			children[pattern[0][2].ordinal()].insert(pattern);
			break;
		case 3:
			if (children[pattern[1][2].ordinal()] == null)
				children[pattern[1][2].ordinal()] = new TrieNode(depth+1);
			children[pattern[1][2].ordinal()].insert(pattern);
			break;
		case 4:
			if (children[pattern[2][2].ordinal()] == null)
				children[pattern[2][2].ordinal()] = new TrieNode(depth+1);
			children[pattern[2][2].ordinal()].insert(pattern);
			break;
		case 5:
			if (children[pattern[2][1].ordinal()] == null)
				children[pattern[2][1].ordinal()] = new TrieNode(depth+1);
			children[pattern[2][1].ordinal()].insert(pattern);
			break;
		case 6:
			if (children[pattern[2][0].ordinal()] == null)
				children[pattern[2][0].ordinal()] = new TrieNode(depth+1);
			children[pattern[2][0].ordinal()].insert(pattern);
			break;
		case 7:
			if (children[pattern[1][0].ordinal()] == null)
				children[pattern[1][0].ordinal()] = new TrieNode(depth+1);
			children[pattern[1][0].ordinal()].insert(pattern);
			break;
		}
	}

}
