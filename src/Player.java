import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {

	final public Color color;
	final public Color textColor;
	public List<Card> hand = new ArrayList<>();
	private List<Territory> territories;
	private List<Continent> continents;
	public int claims;
	
	public Player(int index, int claims) {
		Color[] playerColors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK, Color.MAGENTA};
		Color[] playerTextColors = {Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.WHITE, Color.WHITE};
		this.color = playerColors[index];
		this.textColor = playerTextColors[index];
		this.claims = claims;
		updateTerritories();
	}
	
	public int getDrafts() {
		return Math.max(3, (int)(territories.size() / 3) + continents.stream().mapToInt(c -> c.bonus).sum());
	}
	
	public List<Territory> getTerritories() {
		return territories;
	}
	
	public List<Continent> getContinents() {
		return continents;
	}
	
	public void updateTerritories() {
		territories = Territory.territories.stream().filter(t -> t.getOwner() == this).collect(Collectors.toList());
		continents = Continent.continents.stream().filter(c -> c.territories.size() == c.territories.stream().filter(t -> t.getOwner() == this).count()).collect(Collectors.toList());
	}
	
}
