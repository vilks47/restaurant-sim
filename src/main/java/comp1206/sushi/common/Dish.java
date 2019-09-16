package comp1206.sushi.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

public class Dish extends Model implements Serializable {

	private String name;
	private String description;
	private Number price;
	private Map <Ingredient,Number> recipe;
	private Number restockThreshold;
	private Number restockAmount;

	public Dish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.restockThreshold = restockThreshold;
		this.restockAmount = restockAmount;
		this.recipe = new HashMap<Ingredient,Number>();
	}



	public void setDescription(String description) {
		notifyUpdate("description", this.description, description);
		this.description = description;
	}

	public void setPrice(Integer price) {
		notifyUpdate("price", this.price, price);
		this.price = price;
	}

	public void setRecipe(Map<Ingredient, Number> ingredients) {

		notifyUpdate("ingredients", this.recipe, ingredients);
		this.recipe = ingredients;
	}

	public void setRestockAmount(int restockAmount) {
		notifyUpdate("restock amount", this.restockAmount, restockAmount);
		this.restockAmount = restockAmount;
	}

	public void setRestockThreshold(int threshold) {
		notifyUpdate("restock threshold", this.restockThreshold, threshold);
		this.restockThreshold = threshold;
	}

	public Number getPrice() {
		return price;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Map<Ingredient, Number> getRecipe() {
		return recipe;
	}

	public Number getRestockAmount() {
		return restockAmount;
	}

	public Number getRestockThreshold() {
		return restockThreshold;
	}

}
