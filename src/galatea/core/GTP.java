package galatea.core;

import galatea.board.Board;
import galatea.board.Color;
import galatea.board.Point;
import galatea.engine.GameTree;
import galatea.engine.MCTS;
import galatea.engine.MCTSThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GTP {
	
	private int boardsize = -1, handicap = 0;
	private double komi = 6.5;
	private MCTS engine = null;
	private MCTSThread backgroundTreeSearch = null;
	private List<String> knownCommands = Arrays.asList(new String[] {
		"protocol_version", "name", "version", "known_command",
		"list_commands", "boardsize", "clear_board", "komi", "quit",
		"fixed_handicap", "play", "genmove"
	});
	
	
	public GTP() {
	}
	
	private String pointToVertex(Point p) {
		String vertex = "";
		int x = p.x, y = p.y;
		vertex += boardsize-x;
		if (y <= 7) {
			char c = (char) ('a' + y);
			vertex = c + vertex;
		} else {
			char c = (char) ('a' + y + 1);
			vertex = c + vertex;
		}
		return vertex.toUpperCase();
	}
	
	private Point vertexToPoint(String vertex) {
		vertex = vertex.toLowerCase();
		if (vertex.equals("pass")) return null;
		
		int x = -(Integer.parseInt(vertex.substring(1, vertex.length()))-boardsize);
		int y;
		if (vertex.charAt(0) < 'i')
			y = (int) (vertex.charAt(0) - 'a');
		else
			y = (int) (vertex.charAt(0) - 'a' - 1);
		return new Point(x, y);
	}
	
	private void doCommand(String id, String command, List<String> arguments) throws InterruptedException {
		if (command.equals("protocol_version")) {
			System.out.println(id + 3 + "\n");
			
		} else if (command.equals("name")) {
			System.out.println(id + "Killer\n");
			
		} else if (command.equals("version")) {
			System.out.println(id + 0.01 + "\n");
			
		} else if (command.equals("known_command")) {
			if (knownCommands.contains(arguments.get(0)))
				System.out.println(id + "true\n");
			else
				System.out.println(id + "false\n");
			
		} else if (command.equals("list_commands")) {
			System.out.println(id + knownCommands.get(0));
			for (int i = 1; i < knownCommands.size(); i++)
				System.out.println(knownCommands.get(i));
			System.out.println();
			
		} else if (command.equals("quit")) {
			System.exit(0);
			
		} else if (command.equals("boardsize")) {
			boardsize = Integer.parseInt(arguments.get(0));
			if (handicap >= 0 && komi > -.5) {
				engine = new MCTS(new Board(boardsize, handicap, komi));
				backgroundTreeSearch = new MCTSThread(engine);
				backgroundTreeSearch.start();
			}
			System.out.println(id + "\n");
			
		} else if (command.equals("clear_board")) {
			engine = new MCTS(new Board(boardsize, handicap, komi));
			backgroundTreeSearch = new MCTSThread(engine);
			backgroundTreeSearch.start();
			System.out.println(id + "\n");
			
		} else if (command.equals("komi")) {
			komi = Double.parseDouble(arguments.get(0));
			if (boardsize >= 0 && handicap >= 0) {
				engine = new MCTS(new Board(boardsize, handicap, komi));
				backgroundTreeSearch = new MCTSThread(engine);
				backgroundTreeSearch.start();
			}
			System.out.println(id + "\n");
			
		// Assumes boardsize given already
		} else if (command.equals("fixed_handicap")) {
			handicap = Integer.parseInt(arguments.get(0));
			if (boardsize >= 0 && komi > -.5) {
				engine = new MCTS(new Board(boardsize, handicap, komi));
				backgroundTreeSearch = new MCTSThread(engine);
				backgroundTreeSearch.start();
			}
			List<Point> points = Board.getHandicapPoints(boardsize, handicap);
			System.out.print(id);
			for (Point point: points)
				System.out.print(pointToVertex(point) + " ");
			System.out.println("\n");
		
		} else if (command.equals("play")) {
			Color color;
			if (arguments.get(0).charAt(0) == 'w' || arguments.get(0).charAt(0) == 'W')
				color = Color.WHITE;
			else
				color = Color.BLACK;
			Point point = vertexToPoint(arguments.get(1));

			// Stop background thread and wait
			backgroundTreeSearch.stop();
			while (backgroundTreeSearch.t != null)
				Thread.sleep(50);
			
			// Update gameTree
			boolean b = engine.updateGameTree(point, color.opposite());
			if (!b) {
				engine.board.addStone(color, point, true);
				engine.gameTree = new GameTree(engine.board, engine.board.turn);
			}
			System.out.println(id + "\n");
		
		} else if (command.equals("genmove")) {
			Color color;
			if (arguments.get(0).charAt(0) == 'w' || arguments.get(0).charAt(0) == 'W')
				color = Color.WHITE;
			else
				color = Color.BLACK;
			
			// Stop background thread and wait
			backgroundTreeSearch.stop();
			while (backgroundTreeSearch.t != null)
				Thread.sleep(50);
			
			// Get move from engine, then restart background thread
			Point point = engine.getMove(color, 19);
			boolean b = engine.updateGameTree(point, color.opposite());
			if (!b) {
				engine.board.addStone(color, point, true);
				engine.gameTree = new GameTree(engine.board, engine.board.turn);
			}
			backgroundTreeSearch.start();
			
			if (point != null)
				System.out.println(id + pointToVertex(point) + "\n");
			else
				System.out.println(id + "pass\n");
			
		} else if (command.equals("undo")) {
			System.out.println("?cannot undo\n");
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		GTP gtp = new GTP(); 
		
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = r.readLine()) != null) {
			String[] fields = line.split("[^A-Za-z_0-9.-]+");
			List<String> lfields = new ArrayList<String>();
			for (String field: fields) {
				if (!field.equals("")) lfields.add(field); 
			}
			
			if (lfields.size() == 0) continue;
			String id = "=";
			String command;
			List<String> arguments;
			if (lfields.get(0).replaceAll("[0-9]", "").equals("")) {
				id += lfields.get(0) + " ";
				command = lfields.get(1);
				arguments = lfields.subList(2, lfields.size());
			} else {
				id += " ";
				command = lfields.get(0);
				arguments = lfields.subList(1, lfields.size());
			}
			
			try {
				gtp.doCommand(id, command, arguments);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
