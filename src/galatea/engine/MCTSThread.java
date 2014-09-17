package galatea.engine;

import galatea.board.Point;


public class MCTSThread implements Runnable {

	public volatile Thread t;
	private MCTS engine;

	public MCTSThread(MCTS engine) {
		this.engine = engine;
	}
	
	@Override
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thisThread == t) {
			engine.compute(50);
		}
	}
	
	public void start() {
		t = new Thread(this);
		t.start();
	}
	
	public void stop() {
		t = null;
	}
}
