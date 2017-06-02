import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class SaveManager {

	private static Gson gson;
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(Player.class, new PlayerSerializer());
		gsonBuilder.registerTypeAdapter(Player.class, new PlayerDeserializer());
		gsonBuilder.registerTypeAdapter(Territory.class, new TerritorySerializer());
		gsonBuilder.registerTypeAdapter(Card.class, new CardSerializer());
		gsonBuilder.registerTypeAdapter(Card.class, new CardDeserializer());
		gson = gsonBuilder.create();
	}
	
	public static void save(File save) {
		JsonObject object = new JsonObject();
		object.add("phase", gson.toJsonTree(Risk.game.phase));
		object.addProperty("current player", Risk.game.getPlayerIndex(Risk.game.currentPlayer));
		object.addProperty("unclaimed", Risk.game.unclaimed);
		object.addProperty("drafts left", Risk.game.draftsLeft);
		object.addProperty("captured", Risk.game.capturedTerritory);
		object.addProperty("sets completed", Risk.game.setsCompleted);
		object.add("deck", gson.toJsonTree(Risk.game.deck));
		JsonArray players = new JsonArray();
		Risk.game.players.stream().forEach((player) -> players.add(gson.toJsonTree(player)));
		object.add("players", players);
		JsonArray territories = new JsonArray();
		Territory.territories.stream().forEach((ter) -> territories.add(gson.toJsonTree(ter)));
		object.add("territories", territories);
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(save))) {
			writer.write(gson.toJson(object));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void load(File save) {
		try (BufferedReader reader = new BufferedReader(new FileReader(save))) {
			JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
			object.get("players").getAsJsonArray().forEach((player) -> Risk.game.players.add(gson.fromJson(player, Player.class)));
			object.get("territories").getAsJsonArray().forEach((source) -> {
				Territory dest = Territory.territories.get(source.getAsJsonObject().get("index").getAsInt());
				Player owner = source.getAsJsonObject().get("owner").getAsInt() >= 0 ? Risk.game.players.get(source.getAsJsonObject().get("owner").getAsInt()) : null;
				dest.forceOwner(owner);
				dest.armies = source.getAsJsonObject().get("armies").getAsInt();
			});
			Risk.game.players.forEach((player) -> player.updateTerritories());
			Risk.game.phase             = gson.fromJson(object.get("phase"), Risk.Phase.class);
			Risk.game.currentPlayer     = Risk.game.players.get(object.get("current player").getAsInt());
			Risk.game.unclaimed         = object.get("unclaimed").getAsInt();
			Risk.game.draftsLeft        = object.get("drafts left").getAsInt();
			Risk.game.capturedTerritory = object.get("captured").getAsBoolean();
			Risk.game.setsCompleted     = object.get("sets completed").getAsInt();
			Risk.game.deck              = gson.fromJson(object.get("deck"), new TypeToken<List<Card>>(){}.getType());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static class PlayerSerializer implements JsonSerializer<Player> {
		public JsonElement serialize(Player player, Type type, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("index", Risk.game.getPlayerIndex(player));
			object.addProperty("claims", player.claims);
			object.add("hand", gson.toJsonTree(player.hand));
			return object;
		}
	}
	
	private static class PlayerDeserializer implements JsonDeserializer<Player> {
		public Player deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			Player player = new Player(object.get("index").getAsInt(), object.get("claims").getAsInt());
			player.hand = gson.fromJson(object.get("hand"), new TypeToken<List<Card>>(){}.getType());
			return player;
		}
	}
	
	private static class TerritorySerializer implements JsonSerializer<Territory> {
		public JsonElement serialize(Territory territory, Type type, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("index", Territory.territories.indexOf(territory));
			object.addProperty("owner", Risk.game.getPlayerIndex(territory.getOwner()));
			object.addProperty("armies", territory.armies);
			return object;
		}
	}
//	
//	private static class TerritoryDeserializer implements JsonDeserializer<Territory> {
//		public Territory deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
//			JsonObject object = json.getAsJsonObject();
//			Territory territory = new Player(object.get("index").getAsInt(), object.get("claims").getAsInt());
//			player.hand = gson.fromJson(object.get("hand"), new TypeToken<List<Card>>(){}.getType());
//			return player;
//		}	
//	}
//	
	
	private static class CardSerializer implements JsonSerializer<Card> {
		public JsonElement serialize(Card card, Type type, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("territory", Territory.territories.indexOf(card.getTerritory()));
			object.add("design", gson.toJsonTree(card.getDesign()));
			return object;
		}
	}
	
	private static class CardDeserializer implements JsonDeserializer<Card> {
		public Card deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			Territory territory = object.get("territory").getAsInt() >= 0 ? Territory.territories.get(object.get("territory").getAsInt()) : null;
			return new Card(territory, gson.fromJson(object.get("design"), Card.Design.class));
		}
	}
	
}
