import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Risk extends JPanel implements MouseListener, KeyListener {

	private enum Phase {
		CLAIM,
		DRAFT,
		ATTACK,
		FORTIFY
	}
	
	public static Risk game;
	
	public BufferedImage map;
	private Phase phase = Phase.CLAIM;
	private Player currentPlayer;
	private List<Player> players = new ArrayList<>();
	private Territory selected;
	private List<Card> deck = new ArrayList<>();
	
	private int unclaimed;
	private int draftsLeft;
	private boolean capturedTerritory;
	
	public Risk(int playerAmount) {
		try {
			map = ImageIO.read(new File("map.png"));
		} catch (IOException e) {
			System.out.println("Failed to load 'map.png'");
			return;
		}
		
		setPreferredSize(new Dimension(map.getWidth(null), map.getHeight(null)));
		addMouseListener(this);
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		game = this;
		
		loadTerritories();
		loadDeck();
		
		Color[] playerColors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK};
		for (int i = 0; i < playerAmount; i++) {
			players.add(new Player(playerColors[i], (int) Math.ceil(42f / playerAmount)));//50 - 5 * playerAmount));
		}
		currentPlayer = players.get(0);
		
		new Thread(loop(60)).start();
	}
	
	private Runnable loop(int FPS) {
		return () -> {
			long currentTime, previousTime = System.currentTimeMillis();
			
			while (true) {
				currentTime = System.currentTimeMillis();

				if (currentTime - previousTime >= 1000 / FPS) {
					previousTime = currentTime;
					repaint();
					currentTime = System.currentTimeMillis();
				}
			}
		};
	}

	@Override
	public void paint(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		g.drawImage(map, 0, 0, null);

		g.setColor(currentPlayer.color);
		g.fillRect(map.getWidth() / 2 - 15, map.getHeight() - 40, 30, 30);
		g.setColor(Color.BLACK);
		g.drawString(phase.toString(), map.getWidth() / 2 - 20, map.getHeight() - 45);
		
		if (selected != null) {
			g.setColor(Color.BLACK);
			g.drawOval(selected.x - 15, selected.y - 15, 30, 30);
		}
		
		if (getMousePosition() != null) {
			Territory hovering = Territory.search(getMousePosition().x, getMousePosition().y);
			if (hovering != null) {
				g.setColor(Color.BLACK);
				g.drawOval(hovering.x - 15, hovering.y - 15, 30, 30);
				
				if (selected != null) {
					for (Territory candidate : selected.connections) {
						if (hovering == candidate) {
							g.drawLine(selected.x, selected.y, hovering.x, hovering.y);
						}
					}
				}
			}
		}
		
		for (Territory territory : Territory.territories) {		
			if (territory.owner == null) continue;
			g.setColor(Color.WHITE);
			g.fillRect(territory.x - 5, territory.y - 12, territory.armies < 10 ? 15 : 23, 14);
			g.setColor(Color.BLACK);
			g.drawRect(territory.x - 5, territory.y - 12, territory.armies < 10 ? 15 : 23, 14);
			g.setColor(territory.owner.color);
			g.drawString(territory.armies + "", territory.x, territory.y);

			if (territory.ghostArmies != 0) {
				g.setColor(Color.WHITE);
				g.fillRect(territory.x - 5, territory.y + 3, territory.ghostArmies < 10 ? 15 : 23, 14);
				g.setColor(Color.BLACK);
				g.drawRect(territory.x - 5, territory.y + 3, territory.ghostArmies < 10 ? 15 : 23, 14);
				g.setColor(currentPlayer.color);
				g.drawString(territory.ghostArmies + "", territory.x, territory.y + 15);
			}
		}
		
	}
	
	private boolean loadTerritories() {
		Scanner scanner;
		try {
			scanner = new Scanner(new File("map.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Failed to load 'map.txt'");
			return false;
		}
		
		String continent = "";
		int bonus = 0;
		List<Territory> territories = new ArrayList<>();
		Map<Territory, List<int[]>> allConnections = new HashMap<>();
		while (scanner.hasNextLine()) { // TODO: Make this a lot nicer
			String line = scanner.nextLine();
			
			if (line.equals("")) {
				new Continent(continent, bonus, territories);
				territories = new ArrayList<>();
			} else if (line.charAt(0) == '"') {
				int end = line.lastIndexOf('"');
				continent = line.substring(1, end);
				bonus = Integer.parseInt(line.substring(end + 2, line.lastIndexOf(':')));
			} else {
				int xEnd = line.indexOf(' ');
				int yEnd = line.indexOf(' ', xEnd + 1);
				int nameEnd = line.indexOf('"', yEnd + 2);
				List<int[]> connections = new ArrayList<>();
				
				int i = nameEnd + 2;
				while (i < line.length()) {
					connections.add(new int[] {Character.getNumericValue(line.charAt(i)), Character.getNumericValue(line.charAt(i + 1))});
					i += 2;
				}
								
				Territory newTerritory = new Territory(line.substring(yEnd + 2, nameEnd), Integer.parseInt(line.substring(0, xEnd)), Integer.parseInt(line.substring(xEnd + 1, yEnd)));
				territories.add(newTerritory);
				allConnections.put(newTerritory, connections);
			}
		}
		scanner.close();
		unclaimed = Territory.territories.size();
		for (Territory territory : Territory.territories) {
			List<Territory> connections = new ArrayList<>();
	    	for (int[] connection : allConnections.get(territory)) {
	    		connections.add(Continent.continents.get(connection[0]).territories.get(connection[1]));
	    	}
			territory.connections = Collections.unmodifiableList(connections);
		}
		
		return true;
	}
	
	private void loadDeck() {
		deck.add(new Card(null, Card.Design.WILD));
		deck.add(new Card(null, Card.Design.WILD));
		int infLeft = Territory.territories.size() / 3;
		int cavLeft = Territory.territories.size() / 3;
		int artLeft = Territory.territories.size() / 3;
		for (Territory territory : Territory.territories) {
			double rand = Math.random();
			double infProb = (double) infLeft / (infLeft + cavLeft + artLeft);
			double cavProb = (double) cavLeft / (infLeft + cavLeft + artLeft);

			if (rand < infProb) {
				deck.add(new Card(territory, Card.Design.INFANTRY));
				infLeft--;
			} else if (rand < infProb + cavProb) {
				deck.add(new Card(territory, Card.Design.CAVALRY));
				cavLeft--;
			} else {
				deck.add(new Card(territory, Card.Design.ARTILLERY));
				artLeft--;
			}
		}
		Collections.shuffle(deck);
	}

	private boolean nextPlayer() { // Returns whether it looped back to the first player
		currentPlayer = players.get(players.indexOf(currentPlayer) < players.size() - 1 ? players.indexOf(currentPlayer) + 1 : 0);
		draftsLeft = currentPlayer.getDrafts();
		return players.indexOf(currentPlayer) == 0;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Territory clicked = Territory.search(e.getX(), e.getY());
		if (clicked == null) return;
		switch (phase) {
			case CLAIM:
				if (clicked.owner == null) {
					clicked.owner = currentPlayer;
					currentPlayer.claims--;
					unclaimed--;
					nextPlayer();
				} else if (unclaimed == 0 && clicked.owner == currentPlayer) {
					clicked.armies++;
					if(nextPlayer() && --currentPlayer.claims <= 0) {
						phase = Phase.DRAFT;
					}
				}
				break;
			case DRAFT:
				if (clicked.owner != currentPlayer) return;
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (draftsLeft > 0) {
						clicked.ghostArmies++;
						draftsLeft--;
					}
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					if (clicked.ghostArmies > 0) {
						clicked.ghostArmies--;
						draftsLeft++;
					}
				}
				break;
			case ATTACK:
				if (clicked.owner == currentPlayer) {
					if (selected != clicked) {
						if (selected != null) {
							selected.ghostArmies = 0;
						}
						selected = clicked;
					}
					
					if (e.getButton() == MouseEvent.BUTTON1) {
						selected.ghostArmies = Math.min(3, Math.min(selected.armies - 1, selected.ghostArmies + 1));
					} else if (e.getButton() == MouseEvent.BUTTON3) {
						selected.ghostArmies = Math.max(0, selected.ghostArmies - 1);
					}
					if (selected.ghostArmies == 0) {
						selected = null;
					}
				} else if (selected != null && selected.connections.contains(clicked)) {
					selected.armies -= selected.ghostArmies;
					int defenderDice = Math.min(2, clicked.armies);
					List<Integer> attacker = new ArrayList<>(), defender = new ArrayList<>();
					for (int i = 0; i < selected.ghostArmies; i++) {
						attacker.add((int)(Math.random() * 6 + 1));
					}
					for (int i = 0; i < defenderDice; i++) {
						defender.add((int)(Math.random() * 6 + 1));
					}
					Collections.sort(attacker, Collections.reverseOrder());
					Collections.sort(defender, Collections.reverseOrder());
					for (int i = 0; i < Math.min(attacker.size(), defender.size()); i++) {
						if (attacker.get(i) > defender.get(i)) {
							clicked.armies--;
						} else {
							selected.ghostArmies--;
						}
					}
					
					if (clicked.armies == 0) { // TODO: Move more than the attacking armies
						clicked.owner = currentPlayer;
						clicked.armies += selected.ghostArmies;
						capturedTerritory = true;
					} else {
						selected.armies += selected.ghostArmies;
					}
					selected.ghostArmies = 0;
					selected = null;
				}
				break;
			case FORTIFY:
				if (clicked.owner != currentPlayer) return;
				if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
					if (selected != clicked) {
						if (selected != null) {
							selected.ghostArmies = 0;
						}
						selected = clicked;
					}
					
					if (e.getButton() == MouseEvent.BUTTON1) {
						selected.ghostArmies = Math.min(selected.armies - 1, selected.ghostArmies + 1);
					} else if (e.getButton() == MouseEvent.BUTTON3) {
						selected.ghostArmies = Math.max(0, selected.ghostArmies - 1);
					}
					if (selected.ghostArmies == 0) {
						selected = null;
					}
				} else if (e.getButton() == MouseEvent.BUTTON2 && selected.connections.contains(clicked)) {
					selected.armies -= selected.ghostArmies;
					clicked.armies += selected.ghostArmies;
					selected.ghostArmies = 0;
					selected = null;
					phase = Phase.DRAFT;
					nextPlayer();
				}
				break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			switch (phase) {
				case DRAFT:
					if (draftsLeft > 0) return;
					for (Territory territory : Territory.territories) {
						territory.armies += territory.ghostArmies;
						territory.ghostArmies = 0;
					}
					phase = Phase.ATTACK;
					break;
				case ATTACK:
					phase = Phase.FORTIFY;
					if (selected != null) {
						selected.ghostArmies = 0;
						selected = null;
					}
					if (capturedTerritory) {
						currentPlayer.hand.add(deck.remove(0));
						capturedTerritory = false;
					}
					break;
				case FORTIFY:
					phase = Phase.DRAFT;
					if (selected != null) {
						selected.ghostArmies = 0;
						selected = null;
					}
					nextPlayer();
					break;
		}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }
}
