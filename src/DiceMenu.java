import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class DiceMenu extends Menu {

	private final Territory attacking, defending;
	private final int squareLength = 40, gap = 15;
	private int attackingArmies, defendingArmies;
	private Territory playing;
	
	public DiceMenu(int x, int y, int width, int height, Territory attacking, Territory defending) {
		super(x, y, width, height);
		this.attacking = attacking;
		this.defending = defending;
		this.playing = attacking;
	}
	
	public void paint(Graphics2D g, Point mouse) {
		super.paint(g);
		
		g.drawString("" + (char) 8594, x - g.getFontMetrics().charWidth(8594) / 2, y - height / 2 + 25);
		g.setColor(attacking.getOwner().color);
		g.drawString(attacking.toString(), x - 80 - g.getFontMetrics().stringWidth(attacking.toString()) / 2, y - height / 2 + 19);
		g.drawString(attacking.armies + "", x - 80 - g.getFontMetrics().stringWidth(attacking.armies + "") / 2, y - height / 2 + 31);
		g.setColor(defending.getOwner().color);
		g.drawString(defending.toString(), x + 80 - g.getFontMetrics().stringWidth(defending.toString()) / 2, y - height / 2 + 19);
		g.drawString(defending.armies + "", x + 80 - g.getFontMetrics().stringWidth(defending.armies + "") / 2, y - height / 2 + 31);
		final int offset = (squareLength * getDice() + gap * (getDice() - 1)) / 2;
		for (int i = 0; i < getDice(); i++) {
			if (mouse != null && playing.armies - (attacking == playing ? 1 : 0) >= i + 1
			                  && mouse.getX() >= x - offset + i * (squareLength + gap)
			                  && mouse.getX() <= x - offset + i * (squareLength + gap) + squareLength
			                  && mouse.getY() >= y - squareLength / 2 + 10
			                  && mouse.getY() <= y - squareLength / 2 + 10 + squareLength) {
				g.setColor(new Color(220, 220, 220));
			} else {
				g.setColor(playing.armies - (attacking == playing ? 1 : 0) >= i + 1 ? Color.LIGHT_GRAY : new Color(120, 120, 120));
			}
			g.fillRect(x - offset + i * (squareLength + gap), y - squareLength / 2 + 10, squareLength, squareLength);
			g.setColor(Color.BLACK);
			g.drawRect(x - offset + i * (squareLength + gap), y - squareLength / 2 + 10, squareLength, squareLength);
			g.drawString("" + (i + 1), x - offset + squareLength / 2 - g.getFontMetrics().stringWidth("" + (i + 1)) / 2 + i * (squareLength + gap), y + g.getFontMetrics().getAscent() / 2 + 10);
		}
	}
		
	public boolean click(Point mouse) {
		final int offset = (squareLength * getDice() + gap * (getDice() - 1)) / 2;
		for (int i = 0; i < getDice(); i++) {
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
	
	public boolean pastNoReturn() {
		return defending == playing;
	}
	
	private int getDice() {
		return attacking == playing ? 3 : 2;
	}
	
	public Territory getAttacking() {
		return attacking;
	}
		
	public Territory getDefending() {
		return defending;
	}
		
	public int getAttackingArmies() {
		return attackingArmies;
	}
		
	public int getDefendingArmies() {
		return defendingArmies;
	}
		
}