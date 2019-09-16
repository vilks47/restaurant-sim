package comp1206.sushi.common;

import comp1206.sushi.common.Staff;
import comp1206.sushi.common.Model;

import java.util.*;
import java.util.concurrent.locks.Lock;

public class Staff extends Model implements Runnable {

	private String name;
	private String status;
	private Number fatigue;
	private Stock stock;
	private Thread thread;
	
	public Staff(String name) {
	    this.name = name;
		super.setName(name);
		this.status = "Idle";
		this.setFatigue(0);
		this.stock = Stock.getInstance();
	}

	public void start() {
		if (thread == null){
			thread = new Thread(this);
			thread.start();
		}
	}
	@Override
	public void run(){
	    while(true){
			monitorDishStockLevels();
		}
	}

	private void monitorDishStockLevels(){
		while(true){
		    try {
				for(Dish dish : stock.getDishList()){
					this.setStatus("Idle");
					Integer dishesInStock = (Integer) stock.getDishes().get(dish);
					if((dishesInStock) < (Integer) dish.getRestockThreshold()){
//						Lock lock = dish.getLock();
//						lock.lock();
						Integer restockAmount = (Integer) dish.getRestockAmount();
						Map<Ingredient, Number> dishRecipe = dish.getRecipe();
						for(int i = 0; i < restockAmount; i++){
							this.setStatus("Checking enough ingredients for dish: " + dish.getName());
							for(Map.Entry<Ingredient, Number> ingredient : dishRecipe.entrySet()){
								boolean notEnough = true;

								Integer ingstock = 0;
								for(Map.Entry<Ingredient, Number> stockIng : stock.getIngredients().entrySet()){
									if(stockIng.getKey().getName().equals(ingredient.getKey().getName())){
									    ingstock = (Integer) stockIng.getValue();
									}
								}

								/*
								TODO
								after client connecting, stock becomes null
								check the ingredient.getname equals the one in stock...
								 */


								Integer ingval = (Integer) ingredient.getValue();

								if(ingval < ingstock){
									notEnough = false;
								}

								while (notEnough) {
									this.setStatus("Not enough ingredients for: " + dish.getName());
//									notEnough = (int) entry.getValue() < (int) entry.getKey().getRecipe().get(ingredient);
									int s1 = 0;
									for(Map.Entry<Ingredient, Number> stockIng : stock.getIngredients().entrySet()){
										if(stockIng.getKey().getName().equals(ingredient.getKey().getName())){
											s1 = (Integer) stockIng.getValue();
										}
									}
									int s2 = (int) dishRecipe.get(ingredient.getKey());
									notEnough = s1 < s2;
								}
//
								Random random = new Random();
								int dishPrepTime = random.nextInt(60) + 20;

								try {
									this.status = "Preparing dish: " + dish.getName();
									Thread.sleep(dishPrepTime * 10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								this.setStatus("Updating amount of ingredients left in stock.");
								Integer ingredientsInStock = 0;
								Ingredient ingredientToUpdate = null;
								for(Map.Entry<Ingredient, Number> stockIng : stock.getIngredients().entrySet()){
									if(stockIng.getKey().getName().equals(ingredient.getKey().getName())){
										ingredientsInStock = (Integer) stockIng.getValue();
										ingredientToUpdate = stockIng.getKey();
									}
								}
								Integer ingredientsInDish = (int) dishRecipe.get(ingredient.getKey());
								stock.getIngredientStockLevels().put(ingredientToUpdate, ingredientsInStock - ingredientsInDish);
							}
							Dish dishToUpdate = null;
							Integer newDishStock = 0;
							for(Map.Entry<Dish, Number> stockDish : stock.getDishes().entrySet()){
								if(stockDish.getKey().getName().equals(dish.getName())){
									newDishStock = (Integer) stockDish.getValue();
									dishToUpdate = stockDish.getKey();
								}
							}
							stock.getDishStockLevels().put(dishToUpdate, (Integer) newDishStock + 1);
						}
					}
				}
			} catch (ConcurrentModificationException e){
		    	monitorDishStockLevels();
			}}
		}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getFatigue() {
		return fatigue;
	}

	public void setFatigue(Number fatigue) {
		this.fatigue = fatigue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

}
