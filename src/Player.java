import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Player {

	final public Color color;
	public List<Card> hand = new ArrayList<>();
	private int completedSets;
	public int claims;
	
	public Player(Color color, int claims) {
		this.color = color;
		this.claims = claims;
	}
	
	public int getDrafts() {
		int territories = 0;
		int continentBonuses = 0;
		for (Continent continent : Continent.continents) {
			boolean controlsContinent = true;
			for (Territory territory : continent.territories) {
				if (territory.owner == this) {
					territories++;
				} else {
					controlsContinent = false;
				}
			}
			if (controlsContinent) {
				continentBonuses += continent.bonus;
			}
		}
		
		return Math.max(3, (int)(territories / 3) + continentBonuses);
	}
	
}
