package galatea.treepolicy;

import galatea.engine.Node;

public interface TreePolicy {
	
	public Node getNode(Node node);
	public Node getBest(Node node);
	public boolean shouldExpand(Node node);
}
