import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

public class CardMenu extends Menu {

	private Player player;
	private boolean enabled = false;
	
	public CardMenu(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public void paint(Graphics2D g) {
		super.paint(g);
		for (int i = 0; i < player.hand.size(); i++) {
			//g.drawRect(x - width / 2 + 8, y - height / 2 + 18 + i * (g.getFontMetrics().getAscent() + 10), x , g.getFontMetrics().getAscent() + 4);
			g.drawString(player.hand.get(i).toString(), x - width / 2 + 10, y - height / 2 + 20 + i * (g.getFontMetrics().getAscent() + 10));
		}
	}
	
	public boolean click(Point mouse) {
		final int offset = (squareLength * getDice() + gap * (getDice() - 1)) / 2;
		for (int i = 0; i < player.hand.size(); i++) {
			if (mouse != null && playing.armies - (attacking == playing ? 1 : 0) >= i + 1
	                          && mouse.getX() >= x - offset + i * (squareLength + gap)
	                          && mouse.getX() <= x - offset + i * (squareLength + gap) + squareLength
			                  && mouse.getY() >= y - squareLength / 2 + 10
			                  && mouse.getY() <= y - squareLength / 2 + 10 + squareLength) {
				if (getDice() == 3) {
					attackingArmies = i + 1;
					playing = defending;
				} else {
					defendingArmies = i + 1;
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void enable(Player player) {
		this.player = player;
		enabled = true;
	}
	
	public void disable() {
		// Clear data
		enabled = false;
	}
	
}
