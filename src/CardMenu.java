import java.awt.Graphics2D;
import java.util.List;

public class CardMenu extends Menu {

	private boolean enabled = false;
	
	public CardMenu(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public void paint(Graphics2D g, List<Card> cards) {
		super.paint(g);
		for (int i = 0; i < cards.size(); i++) {
			g.drawString(cards.get(0).toString(), x - width / 2 + 5, y - height / 2 + 10 + i * (g.getFontMetrics().getAscent() + 10));
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}
