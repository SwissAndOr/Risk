import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
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
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Risk extends JPanel implements MouseListener, KeyListener {

	public enum Phase {
		CLAIM,
		DRAFT,
		ATTACK,
		ATTACKING,
		DEFENDING,
		FORTIFY
	}
	
	public static Risk game;
	
	public BufferedImage map;
	public Phase phase = Phase.CLAIM;
	public Player currentPlayer;
	public List<Player> players = new ArrayList<>();
	private Territory selected;
	private DiceMenu diceMenu;
	private ResultMenu resultMenu;
	private MoveMenu moveMenu;
	private CardMenu cardMenu;
	public List<Card> deck = new ArrayList<>();
	private File save;
	
	public int unclaimed;
	public int draftsLeft;
	public boolean capturedTerritory;
	public int setsCompleted = 0;
	
	public Risk() {
		try {
			map = ImageIO.read(new File("map.png"));
		} catch (IOException e) {
			System.out.println("Failed to load 'map.png'");
			return;
		}
		
		setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
		addMouseListener(this);
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		game = this;
		
		loadBindings();
		loadTerritories();
		cardMenu = new CardMenu(map.getWidth() / 2, map.getHeight() / 2, 300, 180);
	}
	
	public Risk(int playerAmount) {
		this();
		
		loadDeck();
		
		for (int i = 0; i < playerAmount; i++) {
			players.add(new Player(i, 50 - 5 * playerAmount));
		}
		currentPlayer = players.get(0);
		
		new Thread(loop(60)).start();
	}
	
	public Risk(File save) {
		this();
		
		this.save = save;
		SaveManager.load(save);
		
		new Thread(loop(60)).start();
	}
	
	private Runnable loop(int FPS) {
		return () -> {
			long startTime = System.currentTimeMillis();
			
			while (true) {
				startTime = System.currentTimeMillis();
				repaint();
				try {
					long wait = (1000 / FPS) - (System.currentTimeMillis() - startTime);
					if (wait > 0) {
						Thread.sleep(wait);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public void paint(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(map, 0, 0, null);

		g.setColor(currentPlayer.color);
		g.fillRect(map.getWidth() / 2 - 15, map.getHeight() - 40, 30, 30);
		g.setColor(Color.BLACK);
		g.drawRect(map.getWidth() / 2 - 15, map.getHeight() - 40, 30, 30);
		g.setColor(Color.BLACK);
		g.drawString(phase.toString(), map.getWidth() / 2 - 20, map.getHeight() - 45);
		g.drawString("Territories: " + currentPlayer.getTerritories().size(), map.getWidth() / 2 + 20, map.getHeight() - 34);
		g.drawString("Continent Bonuses: " + currentPlayer.getContinents().stream().mapToInt(c -> c.bonus).sum(), map.getWidth() / 2 + 20, map.getHeight() - 22);
		g.drawString("Cards: " + currentPlayer.hand.size(), map.getWidth() / 2 + 20, map.getHeight() - 10);
		g.drawString("Sets Completed: " + setsCompleted, map.getWidth() / 2 - 450, map.getHeight() - 10);
		
		if (phase == Phase.CLAIM) {
			g.drawString(currentPlayer.claims + " claims left", map.getWidth() / 2 - 100, map.getHeight() - 22);
		} else if (phase == Phase.DRAFT) {
			g.drawString(draftsLeft + " drafts left", map.getWidth() / 2 - 100, map.getHeight() - 22);
		}
		
		if (selected != null) {
			g.setColor(Color.BLACK);
			g.drawOval(selected.x - 15, selected.y - 15, 30, 30);
		}
		
		Point mouse = getMousePosition();
		if (mouse != null) {
			Territory hovering = Territory.search(mouse.x, mouse.y);
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
			if (territory.getOwner() == null) continue;
			g.setColor(territory.getOwner().color);
			g.fillRect(territory.x - 5, territory.y - 12, territory.armies < 10 ? 15 : 23, 14);
			g.setColor(Color.BLACK);
			g.drawRect(territory.x - 5, territory.y - 12, territory.armies < 10 ? 15 : 23, 14);
			g.setColor(territory.getOwner().textColor);
			g.drawString(territory.armies + "", territory.x, territory.y);

			if (territory.ghostArmies != 0) {
				g.setColor(territory.getOwner().color);
				g.fillRect(territory.x - 5, territory.y + 3, territory.ghostArmies < 10 ? 15 : 23, 14);
				g.setColor(Color.BLACK);
				g.drawRect(territory.x - 5, territory.y + 3, territory.ghostArmies < 10 ? 15 : 23, 14);
				g.setColor(territory.getOwner().textColor);
				g.drawString(territory.ghostArmies + "", territory.x, territory.y + 15);
			}
		}
		
		final int barX = map.getWidth() / 2 + 175, barWidth = 100, barHeight = 30;
		if (mouse != null && mouse.getX() >= barX && mouse.getX() <= barX + barWidth && mouse.getY() >= map.getHeight() - barHeight) {
			g.setColor(new Color(220, 220, 220));
		} else {
			g.setColor(Color.LIGHT_GRAY);
		}
		if ((currentPlayer.hand.size() >= 5 && phase == Phase.DRAFT) || currentPlayer.hand.size() == 0) {
			g.setColor(new Color(120, 120, 120));
		}
		g.fillRect(barX, map.getHeight() - barHeight, barWidth, barHeight);
		g.setColor(Color.BLACK);
		g.drawRect(barX, map.getHeight()- barHeight, barWidth, barHeight);
		final String cardTab = cardMenu.isEnabled() ? (char) 9660 + " Cards " + (char) 9660 : (char) 9650 + " Cards " + (char) 9650;
		g.drawString(cardTab, barX + barWidth / 2 - g.getFontMetrics().stringWidth(cardTab) / 2, map.getHeight() - barHeight / 2 + g.getFontMetrics().getAscent() / 2);
		
		if (diceMenu != null) diceMenu.paint(g, getMousePosition());
		if (resultMenu != null) resultMenu.paint(g);
		if (moveMenu != null) moveMenu.paint(g);
		if (cardMenu.isEnabled()) cardMenu.paint(g, getMousePosition());
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
				while (i + 1 < line.length()) {
					connections.add(new int[] {Character.getNumericValue(line.charAt(i)), Character.getNumericValue(line.charAt(i + 1))});
					i += 3;
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

	public int getPlayerIndex(Player player) {
		return players.indexOf(player);
	}
	
	private boolean nextPlayer() { // Returns whether it looped back to the first player
		currentPlayer = players.get(players.indexOf(currentPlayer) < players.size() - 1 ? players.indexOf(currentPlayer) + 1 : 0);
		draftsLeft = currentPlayer.getDrafts();
		return players.indexOf(currentPlayer) == 0;
	}
	
	private void attack(Territory attacking, Territory defending, int attackingArmies, int defendingArmies) {
		final int origAtt = attacking.armies, origDef = defending.armies;
		List<Integer> attacker = new ArrayList<>(), defender = new ArrayList<>();
		for (int i = 0; i < attackingArmies; i++) {
			attacker.add((int)(Math.random() * 6 + 1));
		}
		for (int i = 0; i < defendingArmies; i++) {
			defender.add((int)(Math.random() * 6 + 1));
		}
		Collections.sort(attacker, Collections.reverseOrder());
		Collections.sort(defender, Collections.reverseOrder());
		for (int i = 0; i < Math.min(attacker.size(), defender.size()); i++) {
			if (attacker.get(i) > defender.get(i)) {
				defending.armies--;
			} else {
				attacking.armies--;
			}
		}
		
		resultMenu = new ResultMenu(map.getWidth() / 2, map.getHeight() / 2, 300, 180, diceMenu.getAttacking(), diceMenu.getDefending(), attacker, defender, origAtt, origDef);
		
		if (defending.armies == 0) {
			if (defending.getOwner().getTerritories().size() == 1) {
				currentPlayer.hand.addAll(defending.getOwner().hand);
				players.remove(defending.getOwner());
			}
			defending.setOwner(currentPlayer);
			capturedTerritory = true;
		}
		diceMenu = null;
		selected = null;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) { }
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && !(currentPlayer.hand.size() >= 5 && phase == Phase.DRAFT) && currentPlayer.hand.size() > 0) {
			final int barX = map.getWidth() / 2 + 175, barWidth = 100, barHeight = 30;
			if (e.getX() >= barX && e.getX() <= barX + barWidth && e.getY() >= map.getHeight() - barHeight) {
				if (cardMenu.isEnabled()) {
					cardMenu.disable();
				} else {
					cardMenu.enable(currentPlayer);
				}
			}
		}
		if (diceMenu != null || resultMenu != null || moveMenu != null || cardMenu.isEnabled()) {
			switch (phase) {
				case ATTACKING:
					if (e.getButton() == MouseEvent.BUTTON1 && diceMenu.click(getMousePosition())) {
						currentPlayer = diceMenu.getDefending().getOwner();
						phase = Phase.DEFENDING;
					}
					break;
				case DEFENDING:
					if (e.getButton() == MouseEvent.BUTTON1 && diceMenu.click(getMousePosition())) {
						currentPlayer = diceMenu.getAttacking().getOwner();
						phase = Phase.ATTACK;
						attack(diceMenu.getAttacking(), diceMenu.getDefending(), diceMenu.getAttackingArmies(), diceMenu.getDefendingArmies());
					}
					break;
				case DRAFT:
					if (!cardMenu.isEnabled()) return;
					draftsLeft += cardMenu.click(getMousePosition(), currentPlayer);
					break;
			}
			return;
		}
		Territory clicked = Territory.search(e.getX(), e.getY());
		if (clicked == null) return;
		switch (phase) {
			case CLAIM:
				if (clicked.getOwner() == null) {
					clicked.setOwner(currentPlayer);
					currentPlayer.claims--;
					unclaimed--;
					nextPlayer();
					if(currentPlayer.claims <= 0) {
						phase = Phase.DRAFT;
						if (currentPlayer.hand.size() >= 5) {
							cardMenu.enable(currentPlayer);
						}
					}
				} else if (unclaimed == 0 && clicked.getOwner() == currentPlayer) {
					clicked.armies++;
					currentPlayer.claims--;
					nextPlayer();
					if(currentPlayer.claims <= 0) {
						phase = Phase.DRAFT;
						if (currentPlayer.hand.size() >= 5) {
							cardMenu.enable(currentPlayer);
						}
					}
				}
				break;
			case DRAFT:
				if (clicked.getOwner() != currentPlayer) return;
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
				if (clicked.getOwner() == currentPlayer) {
					selected = clicked;
				} else if (selected != null && selected.connections.contains(clicked)) {
					diceMenu = new DiceMenu(map.getWidth() / 2, map.getHeight() / 2, 300, 110, selected, clicked);
					phase = Phase.ATTACKING;
				}
				break;
			case FORTIFY:
				if (clicked.getOwner() != currentPlayer || e.getButton() != MouseEvent.BUTTON1) return;
				if (selected == null) {
					selected = clicked;
				} else if (selected.connections.contains(clicked)) {
					moveMenu = new MoveMenu(map.getWidth() / 2, map.getHeight() / 2, 300, 110, selected, clicked, clicked.armies - 1, true);
					selected = null;
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

	private void loadBindings() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "save");
		getActionMap().put("save", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(".");
				if (save != null) {
					chooser.setCurrentDirectory(save);
					chooser.setSelectedFile(new File(save.getName()));
				}
				chooser.setDialogTitle("Risk Game Saver");
				chooser.setFileFilter(new FileNameExtensionFilter("Risk Saves", ".save"));
				if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
					File file = new File(chooser.getSelectedFile().getPath() + (chooser.getSelectedFile().getPath().toLowerCase().endsWith(".save") ? "" : ".save"));
					save = file;
					SaveManager.save(file);
				}
			}
		});
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (moveMenu != null) {
				moveMenu.getSource().armies -= moveMenu.getAmount();
				moveMenu.getDestination().armies += moveMenu.getAmount();
				moveMenu = null;
				if (phase == Phase.FORTIFY) {
					phase = Phase.DRAFT;
					selected = null;
					nextPlayer();
				}				
				return;
			}
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
					if (resultMenu != null) {
						if (resultMenu.getDefending().armies == 0) {
							moveMenu = new MoveMenu(map.getWidth() / 2, map.getHeight() / 2, 300, 110, resultMenu.getAttacking(), resultMenu.getDefending(), resultMenu.getMin(), false);
						} else {
							diceMenu = new DiceMenu(map.getWidth() / 2, map.getHeight() / 2, 300, 110, resultMenu.getAttacking(), resultMenu.getDefending());
							phase = Phase.ATTACKING;
						}
						resultMenu = null;
						return;
					}
					phase = Phase.FORTIFY;
					if (selected != null) {
						selected.ghostArmies = 0;
						selected = null;
					}
					if (capturedTerritory) {
						if (deck.size() > 0) {
							currentPlayer.hand.add(deck.remove(0));
						}
						capturedTerritory = false;
					}
					break;
				case FORTIFY:
					phase = Phase.DRAFT;
					selected = null;
					nextPlayer();
					if (currentPlayer.hand.size() > 5) {
						cardMenu.enable(currentPlayer);
					}
					break;
			}
		} else if (moveMenu != null && (e.getKeyChar() == '+' || e.getKeyChar() == '-')) {
			moveMenu.setAmount(moveMenu.getAmount() + (e.getKeyChar() == '+' ? 1 : -1));
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			selected = null;
			if (diceMenu != null && !diceMenu.pastNoReturn()) {
				diceMenu = null;
				phase = Phase.ATTACK;
			} else if (moveMenu != null && moveMenu.isCloseable()) {
				moveMenu = null;
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }
}
