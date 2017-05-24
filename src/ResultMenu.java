import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class ResultMenu extends Menu {

	private final Territory attacking, defending;
	private final List<Integer> attacker, defender;
	private final int squareLength = 30, xGap = 25, yGap = 15;
	private final int origAtt, origDef;
	private final Player origDefender;
	
	public ResultMenu(int x, int y, int width, int height, Territory attacking, Territory defending, List<Integer> attacker, List<Integer> defender, int origAtt, int origDef) {
		super(x, y, width, height);
		this.attacking = attacking;
		this.defending = defending;
		this.attacker = attacker;
		this.defender = defender;
		this.origAtt = origAtt;
		this.origDef = origDef;
		origDefender = defending.getOwner();
	}
	
	public void paint(Graphics2D g) {
		super.paint(g);

		g.drawString("" + (char) 8594, x - g.getFontMetrics().charWidth(8594) / 2, y - height / 2 + 25);
		g.setColor(attacking.getOwner().color);
		g.drawString(attacking.toString(), x - 80 - g.getFontMetrics().stringWidth(attacking.toString()) / 2, y - height / 2 + 19);
		g.drawString(origAtt + " " + (char) 8594 + " " + attacking.armies, x - 80 - g.getFontMetrics().stringWidth(origAtt + " " + (char) 8594 + " " + attacking.armies) / 2, y - height / 2 + 31);
		g.setColor(origDefender.color);
		g.drawString(defending.toString(), x + 80 - g.getFontMetrics().stringWidth(defending.toString()) / 2, y - height / 2 + 19);
		g.drawString(origDef + " " + (char) 8594 + " " + defending.armies, x + 80 - g.getFontMetrics().stringWidth(origDef + " " + (char) 8594 + " " + defending.armies) / 2, y - height / 2 + 31);
		
		for (int i = 0; i < 3; i++) {
			final int boxHeight = y - height / 2 + 40 + i * (squareLength + yGap);
			if (i < attacker.size()) {
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(x - squareLength - xGap / 2, boxHeight, squareLength, squareLength);
				g.setColor(Color.BLACK);
				g.drawRect(x - squareLength - xGap / 2, boxHeight, squareLength, squareLength);
				g.drawString(attacker.get(i).toString(), x - squareLength - xGap / 2 + squareLength / 2 - g.getFontMetrics().stringWidth(attacker.get(i).toString()) / 2, boxHeight + squareLength / 2 + g.getFontMetrics().getAscent() / 2);
			}
			if (i < defender.size()) {
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(x + xGap / 2, boxHeight, squareLength, squareLength);
				g.setColor(Color.BLACK);
				g.drawRect(x + xGap / 2, boxHeight, squareLength, squareLength);
				g.drawString(defender.get(i).toString(), x + xGap / 2 + squareLength / 2 - g.getFontMetrics().stringWidth(defender.get(i).toString()) / 2, boxHeight + squareLength / 2 + g.getFontMetrics().getAscent() / 2);
			}
			if (i < attacker.size() && i < defender.size()) {
				char winner = attacker.get(i) > defender.get(i) ? (char) 8592 : (char) 8594;
				g.drawString(winner + "", x - g.getFontMetrics().charWidth(winner) / 2, boxHeight + squareLength / 2 + g.getFontMetrics().getAscent() / 2);
			}
		}
	}
	
	public Territory getAttacking() {
		return attacking;
	}
		
	public Territory getDefending() {
		return defending;
	}
	
	public int getMin() {
		return attacker.size();
	}
	
}