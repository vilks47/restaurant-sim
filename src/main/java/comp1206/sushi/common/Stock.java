package comp1206.sushi.common;
import comp1206.sushi.server.ServerInterface;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class Stock {
    private static final Stock STOCK = new Stock();

    private Map<Ingredient, Number> ingredients;
    private Map<Dish, Number> dishes;


    public Stock(){
        this.ingredients = new HashMap<>();
        this.dishes = new HashMap<>();
    }

    public static Stock getInstance(){
        return STOCK;
    }

    public Map<Ingredient, Number> getIngredients() {
        return ingredients;
    }

    public Map<Dish, Number> getDishes() {
        return dishes;
    }

    public synchronized  void putDish(Dish dishToAdd, Integer quantity){
        dishes.put(dishToAdd, quantity);
    }

    public synchronized  void putIngredient(Ingredient ingredientToAdd, Integer quantity){
        ingredients.put(ingredientToAdd, quantity);
    }

    public Map<Dish, Number> getDishStockLevels() {
        return dishes;
    }

    public Map<Ingredient, Number> getIngredientStockLevels() {
        return ingredients;
    }

    public void removeIngredientsFromDish(Dish dishToRestock){
        Map<Ingredient, Number> recipe = dishToRestock.getRecipe();
        for (Map.Entry<Ingredient, Number> ingredient : recipe.entrySet()) {
            Ingredient ingredientToRemove = (Ingredient) ingredient.getKey();
            Integer ingredientQuantity = (Integer) ingredients.get(ingredientToRemove) - (Integer) ingredient.getValue();
            ingredients.put(ingredient.getKey(), ingredientQuantity);
        }
    }

    public synchronized void removeDish(Dish dishToRemove) throws ServerInterface.UnableToDeleteException {
//        Boolean dishFound = false;

        try {
//            dishes.keySet().remove(dishToRemove);
            for(Dish d : dishes.keySet()){
                if(d.equals(dishToRemove)){
                    dishes.remove(d);
                }
            }
        } catch (Exception e){
            throw new ServerInterface.UnableToDeleteException("Dish not found");

        }
//        for(Dish dish : dishes.keySet()){
//            if(dish.equals(dishToRemove)){
//                dishFound = true;
//                dishes.remove(dishToRemove);
//            }
//        }

//        if(!dishFound){
//            throw new ServerInterface.UnableToDeleteException("Dish not found");
//        }
    }


    public void removeIngredient(Ingredient ingredientToRemove) throws ServerInterface.UnableToDeleteException {
        Boolean ingredientFound = false;

        for(Ingredient ingredient : ingredients.keySet()){
            if(ingredient.equals(ingredientToRemove)){
                ingredientFound = true;
                ingredients.remove(ingredientToRemove);
            }
        }

        if(!ingredientFound){
            throw new ServerInterface.UnableToDeleteException("Dish not found");
        }
    }

    public Integer ingredientsInStock(Ingredient ingredient){
        Integer ingredientStock = 0;
        if(ingredients.containsKey(ingredient)){
            ingredientStock = (Integer) ingredients.get(ingredient);
        }
        return ingredientStock;
    }

    public Integer dishesInStock(Dish dish){
        Integer dishStock = 0;
        if(dishes.containsKey(dish)){
            dishStock = (Integer) dishes.get(dish);
        }
        return dishStock;
    }

    public List<Dish> getDishList() {
        List<Dish> dishList = new ArrayList<>();
        for(Map.Entry<Dish, Number> entry : dishes.entrySet()){
            dishList.add(entry.getKey());
        }
        return dishList;
    }

    public List<Ingredient> getIngredientList() {
        List<Ingredient> ingredientList = new ArrayList<>();
        for(Ingredient ing : ingredients.keySet()){
            ingredientList.add(ing);
        }
        return ingredientList;
    }
}
