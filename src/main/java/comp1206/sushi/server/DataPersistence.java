package comp1206.sushi.server;

import java.util.ArrayList;
import java.util.List;
import comp1206.sushi.common.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class DataPersistence {

    private Server server;

    public DataPersistence(Server server){
        this.server = server;
    }

    public void saveData(){
        try {
            FileWriter writer = new FileWriter("data.txt", false);
            writer.write("POSTCODE:" + server.getRestaurantPostcode().getName() + '\n');
            writer.write("RESTAURANT:" + server.getRestaurantName() + ":" + server.getRestaurantPostcode().getName() + '\n');
            for (Supplier supplier : server.getSuppliers()) {
                writer.write("SUPPLIER:" + supplier.getName() + ":" + supplier.getName() + '\n');
            }
            for (Ingredient ingredient : server.getIngredients()) {
                writer.write("INGREDIENT:" + ingredient.getName() + ":" + ingredient.getUnit() + ":" + ingredient.getSupplier() + ":" + ingredient.getRestockThreshold() + ":" + ingredient.getRestockAmount() + '\n');
            }
            for (Dish dish : server.getDishes()) {
                writer.write("DISH:" + dish.getName() + ":" + dish.getDescription() + ":" + dish.getPrice() + ":" + dish.getRestockThreshold() + ":" + dish.getRestockAmount());
                Integer commaIterator = 0;
                if(dish.getRecipe().isEmpty()){
                    writer.write("");
                } else {
                    for(Map.Entry<Ingredient, Number> item : dish.getRecipe().entrySet()){
                        writer.write(item.getValue() + " * " + item.getKey().getName());
                        commaIterator++;
                        if(commaIterator < dish.getRecipe().size()){
                            writer.write(",");
                        }
                    }
                }
                writer.write('\n');
            }
            for (User user : server.getUsers()) {
                writer.write("USER:" + user.getName() + ":" + user.getPassword() + ":" + user.getAddress() + ":" + user.getPostcode() + '\n');
            }
            for (Postcode postcode : server.getPostcodes()) {
                writer.write("POSTCODE:" + postcode.getName() + '\n');
            }
            for (Staff staff : server.getStaff()) {
                writer.write("STAFF:" + staff.getName() + '\n');
            }
            for (Drone drone : server.getDrones()) {
                writer.write("DRONE:" + drone.getSpeed() + '\n');
            }
            for (Order order : server.getOrders()) {
                User orderUser = null;
                for(User user : server.getUsers()){
                    if(user.getOrders().contains(order)){
                        orderUser = user;
                    }
                }
                try {
                    writer.write("ORDER:" + orderUser.getName() + ":" + order.getDescription() + '\n');
                } catch (NullPointerException e){
                    writer.write("ORDER:" + "Admin" + ":" + order.getDescription() + '\n');
                }
            }
            for (Map.Entry<Dish, Number> dishes : server.getStock().getDishes().entrySet()) {
                writer.write("STOCK:" + dishes.getKey().getName() + ":" + dishes.getValue() + '\n');
            }
            for (Map.Entry<Ingredient, Number> ingredient : server.getStock().getIngredients().entrySet()) {
                writer.write("STOCK:" + ingredient.getKey().getName() + ":" + ingredient.getValue() + '\n');
            }

            writer.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }





}
