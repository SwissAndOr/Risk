import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Territory {

    public static List<Territory> territories = new ArrayList<>();

    private final String name;
    public final Color color;
    public final int x, y;
    public List<Territory> connections;
    public int armies = 1;
    public int ghostArmies = 0;
    private Player owner;
    
    public Territory(String name, int x, int y) {       
    	this.name = name;
        this.color = new Color(Risk.game.map.getRGB(x, y));
        this.x = x;
        this.y = y;

        territories.add(this);
    }
    
    public static Territory search(int x, int y) {
		Color color = new Color(Risk.game.map.getRGB(x, y));
		for (Territory territory : territories) {
			if (territory.color.equals(color)) {
				return territory;
			}
		}
		
		return null;
    }
    
    public String toString() {
    	return name;
    }
    
    public Player getOwner() {
    	return owner;
    }
    
    public void setOwner(Player player) {
    	Player oldOwner = owner;
    	owner = player;
    	if (oldOwner != null) {
    		oldOwner.updateTerritories();
    	}
    	owner.updateTerritories();
    }

}
