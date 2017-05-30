import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CardMenu extends Menu {

	private Player player;
	private boolean enabled = false;
	private final int cardHeight = 20;
	private List<Card> selected = new ArrayList<>();
	
	public CardMenu(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public void paint(Graphics2D g, Point mouse) {
		super.paint(g);
		for (int i = 0; i < player.hand.size(); i++) {
			if (selected.contains(player.hand.get(i))) {
				g.setColor(new Color(120, 120, 120));
			} else if (mouse != null && mouse.getX() >= x - width / 2 + 10
                                     && mouse.getX() <= x + width / 2 - 10
                                     && mouse.getY() >= y - height / 2 + 10 + i * (cardHeight + 10)
                                     && mouse.getY() <= y - height / 2 + 10 + i * (cardHeight + 10) + cardHeight) {
				g.setColor(new Color(220, 220, 220));
			} else {
				g.setColor(Color.LIGHT_GRAY);
			}
			g.fillRect(x - width / 2 + 10, y - height / 2 + 10 + i * (cardHeight + 10), width - 20, cardHeight);
			g.setColor(Color.BLACK);
			g.drawRect(x - width / 2 + 10, y - height / 2 + 10 + i * (cardHeight + 10), width - 20, cardHeight);
			g.drawString(player.hand.get(i).toString(), x - width / 2 + 20, y - height / 2 + 10 + cardHeight / 2 + g.getFontMetrics().getAscent() / 2 + i * (cardHeight + 10));
		}
	}
	
	public int click(Point mouse, Player player) {
		for (int i = 0; i < player.hand.size(); i++) {
			if (mouse != null && mouse.getX() >= x - width / 2 + 10
	                          && mouse.getX() <= x + width / 2 - 10
			                  && mouse.getY() >= y - height / 2 + 10 + i * (cardHeight + 10)
			                  && mouse.getY() <= y - height / 2 + 10 + i * (cardHeight + 10) + cardHeight) {
				if (selected.contains(player.hand.get(i))) {
					return 0;
				}
				selected.add(player.hand.get(i));
				return check(player);
			}
		}
		return 0;
	}
	
	private int check(Player player) {
		if (selected.size() != 3) return 0;
		int wild = (int) selected.stream().filter((card) -> card.getDesign() == Card.Design.WILD).count();
		int designs = (selected.stream().filter((card) -> card.getDesign() == Card.Design.INFANTRY).count() > 0 ? 1 : 0) +
			          (selected.stream().filter((card) -> card.getDesign() == Card.Design.CAVALRY).count() > 0 ? 1 : 0) +
			          (selected.stream().filter((card) -> card.getDesign() == Card.Design.ARTILLERY).count() > 0 ? 1 : 0) +
			          wild;
		int inf = (int) selected.stream().filter((card) -> card.getDesign() == Card.Design.INFANTRY).count() + wild;
		int cav = (int) selected.stream().filter((card) -> card.getDesign() == Card.Design.CAVALRY).count() + wild;
		int art = (int) selected.stream().filter((card) -> card.getDesign() == Card.Design.ARTILLERY).count() + wild;
		
		if (Math.max(Math.max(designs, inf), Math.max(cav, art)) >= 3) {
			player.hand.removeIf((card) -> selected.contains(card));
			int matchBonus = !Collections.disjoint(player.getTerritories(), selected.stream().map((card) -> card.getTerritory()).collect(Collectors.toList())) ? 2 : 0;
			selected.clear();
			final int completed = Risk.game.setsCompleted;
			Risk.game.setsCompleted++;
			height = player.hand.size() * (cardHeight + 10) + 10;
			if (player.hand.size() == 0) {
				disable();
			}
			return 4 + 2 * Math.min(4, completed) + (completed >= 5 ? 3 : 0) + 5 * Math.max(0, completed - 5) + matchBonus;
		} else {
			selected.clear();
			return 0;
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void enable(Player player) {
		this.player = player;
		height = player.hand.size() * (cardHeight + 10) + 10;
		enabled = true;
	}
	
	public void disable() {
		selected.clear();
		enabled = false;
	}
	
}
