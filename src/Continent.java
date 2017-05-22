import java.util.ArrayList;
import java.util.List;

public class Continent {

    public static List<Continent> continents = new ArrayList<>();

    public final List<Territory> territories;
    public final String name;
    public final int bonus;
    
    public Continent(String name, int bonus, List<Territory> territories) {
    	this.name = name;
    	this.territories = territories;
        this.bonus = bonus;
        
        continents.add(this);
    }

}
