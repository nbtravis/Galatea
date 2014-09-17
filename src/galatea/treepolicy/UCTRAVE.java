package galatea.treepolicy;

import galatea.board.Color;
import galatea.engine.Node;

public class UCTRAVE implements TreePolicy {

	private int simsBeforeExpand;
	private double ucbWeight, simsEquiv;
	
	public UCTRAVE(int simsBeforeExpand, double ucbWeight, double simsEquiv) {
		this.simsBeforeExpand = simsBeforeExpand;
		this.ucbWeight = ucbWeight;
		this.simsEquiv = simsEquiv;
	}
	
	public Node getNode(Node node) {
		if (node.isLeaf) return node;
		
		Node argmax = null;
		double max = -1;
		for (Node child: node.children) {
			double value = UCTRAVEValue(node, child);
			if (value > max) {
				argmax = child;
				max = value;
			}
		}
		return getNode(argmax);
	}
	
	public Node getBest(Node node) {
		if (node.isLeaf) return node;
		
		Node argmax = null;
		double max = -1;
		for (Node child: node.children) {
			double value = ((double) child.wins)/child.sims;
			if (value > max) {
				argmax = child;
				max = value;
			}
		}
		return argmax;
	}
	
	// Taken from Pachi (described in Petr Baudis' thesis)
	private double criticalityValue(Node parent, Node child) {
		int x = child.recentPoint.x, y = child.recentPoint.y;
		int total = parent.criticalityCounts[x][y][0][1] +
					parent.criticalityCounts[x][y][1][0] + parent.criticalityCounts[x][y][1][1] +
					parent.criticalityCounts[x][y][2][0] + parent.criticalityCounts[x][y][2][1];
		int wins = parent.criticalityCounts[x][y][1][0] + parent.criticalityCounts[x][y][2][0];
		int blackTotal = parent.criticalityCounts[x][y][2][0] + parent.criticalityCounts[x][y][2][1];
		int blackWins = (parent.turn == Color.BLACK ? parent.wins : (parent.sims - parent.wins));
		
		double winRate = ((double)wins)/total;
		double blackWinRate = ((double)blackWins/parent.sims);
		double blackOwnershipRate = ((double)blackTotal/total);
		return (winRate - (2*blackOwnershipRate*blackWinRate - blackOwnershipRate - blackWinRate + 1));
	}
	
	private double UCTRAVEValue(Node parent, Node child) {
		int wins = child.wins, sims = child.sims, 
			winsAmaf = child.winsAmaf, simsAmaf = child.simsAmaf;
		
		// Add criticality "wins" and "sims" to amaf wins and sims
		double critValue = criticalityValue(parent, child);
		int simsCrit = (int)Math.round(Math.abs(critValue)*simsAmaf);
		int winsCrit = (critValue > 0 ? simsCrit : 0);
		simsAmaf += simsCrit; winsAmaf += winsCrit;
		
		double raveWeight = simsAmaf/(simsAmaf + sims + simsAmaf*sims/simsEquiv);
		
		return (1-raveWeight)*((double) wins)/sims + 
			   raveWeight*((double) winsAmaf)/simsAmaf +
			   ucbWeight*Math.sqrt(Math.log(child.parent.sims)/sims);
	}

	public boolean shouldExpand(Node node) {
		if (node.sims >= simsBeforeExpand) return true;
		return false;
	}
}
