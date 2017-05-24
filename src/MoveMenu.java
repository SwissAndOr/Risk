import java.awt.Color;
import java.awt.Graphics2D;

public class MoveMenu extends Menu {

		private final Territory source, destination;
		private final int min, max;
		private final boolean closeable;
		private int amount;
		
		public MoveMenu(int x, int y, int width, int height, Territory source, Territory destination, int min, boolean closeable) {
			super(x, y, width, height);
			this.source = source;
			this.destination = destination;
			this.min = min;
			this.max = source.armies - 1;
			this.amount = min;
			this.closeable = closeable;
		}
		
		public void paint(Graphics2D g) {
			super.paint(g);
			
			g.setColor(Color.BLACK);
			g.drawString("" + (char) 8594, x - g.getFontMetrics().charWidth(8594) / 2, y - 30);
			g.drawString(source.toString(), x - 80 - g.getFontMetrics().stringWidth(source.toString()) / 2, y - height / 2 + 19);
			g.drawString(source.armies + "", x - 80 - g.getFontMetrics().stringWidth(source.armies + "") / 2, y - height / 2 + 31);
			g.drawString(destination.toString(), x + 80 - g.getFontMetrics().stringWidth(destination.toString()) / 2, y - height / 2 + 19);
			g.drawString(destination.armies + "", x + 80 - g.getFontMetrics().stringWidth(destination.armies + "") / 2, y - height / 2 + 31);
			g.drawString("(" + min + " - " + max + ")", x - g.getFontMetrics().stringWidth("(" + min + " - " + max + ")") / 2, y);
			g.drawString(amount + "", x - g.getFontMetrics().stringWidth(amount + "") / 2, y + 15);
		}
		
		public void setAmount(int amount) {
			this.amount = Math.min(max, Math.max(min, amount));
		}
		
		public int getAmount() {
			return amount;
		}
		
		public Territory getSource() {
			return source;
		}
		
		public Territory getDestination() {
			return destination;
		}
		
		public boolean isCloseable() {
			return closeable;
		}
		
	}