
public class Card {

	public enum Design {
		INFANTRY,
		CAVALRY,
		ARTILLERY,
		WILD
	}
	
	private Territory territory;
	private Design design;
	
	public Card(Territory territory, Design design) {
		this.territory = territory;
		this.design = design;
	}
	
	public String toString() {
		return (design == Design.WILD ? design.toString() : territory.toString() + " (" + design.toString() + ")");
	}
	
	public Territory getTerritory() {
		return territory;
	}
	
	public Design getDesign() {
		return design;
	}
	
}
