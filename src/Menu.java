import java.awt.Color;
import java.awt.Graphics2D;

public class Menu {

	public enum Type {
		DICE
	}
	
	protected int x, y;
	protected int width, height;
	
	public Menu(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void paint(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(x - width / 2, y - height / 2, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(x - width / 2, y - height / 2, width, height);
	}
	
}
