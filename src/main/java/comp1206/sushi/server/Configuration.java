package comp1206.sushi.server;

import comp1206.sushi.common.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class.
 * Parses configuration files in a given format.
 */
public class Configuration {

    /**
     * Member variables in which the parsed data will be stored.
     */
    private String configurationFile;

    private Stock stock;
    private List<Dish> dishes;
    private List<Drone> drones;
    private List<Ingredient> ingredients;
    private List<Order> orders;
    private List<Postcode> postcodes;
    private List<Staff> staff;
    private List<Supplier> suppliers;
    private List<User> users;
    private Restaurant restaurant;
    private Server server;

    private HashMap<String, Integer> orderIDs;

    public Configuration(String configurationFile, Server server) {
        this.configurationFile = configurationFile;
        this.stock = server.getStock();
        this.dishes = server.getDishes();
        this.drones = server.getDrones();
        this.ingredients = server.getIngredients();
        this.orders = server.getOrders();
        this.postcodes = server.getPostcodes();
        this.staff = server.getStaff();
        this.suppliers = server.getSuppliers();
        this.users = server.getUsers();
        this.restaurant = server.getRestaurant();
        this.server = server;
    }

    public void loadConfig() throws IOException{
        FileReader fileReader = new FileReader(configurationFile);

        try{
            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            String line = bufferedReader.readLine();
            String line;
            while((line = bufferedReader.readLine()) != null){
//                if(line.equals("")){
//                    continue;
//                }

                String[] splitConfigLine = line.split(":");

                if(line.startsWith("POSTCODE")){

                    String postcode = splitConfigLine[1];
                    Postcode postcodeToAdd = new Postcode(postcode);
                    postcodes.add(postcodeToAdd);

                } else if (line.startsWith("RESTAURANT")){

                    String restaurantName = splitConfigLine[1];
                    String restaurantPostcode = splitConfigLine[2];
                    Postcode postcodeToAdd = new Postcode(restaurantPostcode);
                    postcodes.add(postcodeToAdd);
                    restaurant.setName(restaurantName);
                    restaurant.setLocation(postcodeToAdd);
//                    restaurant = new Restaurant(restaurantName, postcodeToAdd);

                } else if (line.startsWith("SUPPLIER")){

                    String supplierName = splitConfigLine[1];
                    String supplierPostcode = (splitConfigLine[2]);
                    Postcode postcodeToAdd = new Postcode(supplierPostcode);
                    Supplier supplier = new Supplier(supplierName, postcodeToAdd);
                    suppliers.add(supplier);

                } else if (line.startsWith("INGREDIENT")){

                    String name = splitConfigLine[1];
                    String unit = splitConfigLine[2];
                    String supplierName = splitConfigLine[3];
                    String restockThreshold = splitConfigLine[4];
                    String restockAmount = splitConfigLine[5];
                    String weight = splitConfigLine[6];
                    Supplier supplier = getSupplier(supplierName);
                    Ingredient ingredientToAdd = new Ingredient(name, unit, supplier, Integer.parseInt(restockThreshold), Integer.parseInt(restockAmount), Integer.parseInt(weight));
                    ingredients.add(ingredientToAdd);
                    stock.putIngredient(ingredientToAdd, 0);

                } else if (line.startsWith("DISH")){

                    String name = splitConfigLine[1];
                    String description = splitConfigLine[2];
                    Integer price = Integer.parseInt(splitConfigLine[3]);
                    Integer restockThreshold = Integer.parseInt(splitConfigLine[4]);
                    Integer restockAmount = Integer.parseInt(splitConfigLine[5]);

                    String recipeContent = splitConfigLine[6];

                    Map<Ingredient, Number> recipe = new HashMap<>();

                    String[] ingredients = recipeContent.split(",");

                    for (String currentIngredient : ingredients) {

                        String quantity = currentIngredient.split("\\*")[0].trim();
                        String ingredient = currentIngredient.split("\\*")[1].trim();

                        if(getIngredient(ingredient) != null){
                            Ingredient ingredientToAdd = getIngredient(ingredient);
                            recipe.put(ingredientToAdd, Integer.parseInt(quantity));
                        }
                    }

                    Dish dishToAdd = new Dish(name, description, price, restockThreshold, restockAmount);
                    dishToAdd.setRecipe(recipe);
                    dishes.add(dishToAdd);
                    stock.getDishes().put(dishToAdd, 0);

                } else if (line.startsWith("USER")){

                    String name = splitConfigLine[1];
                    String password = splitConfigLine[2];
                    String location = splitConfigLine[3];
                    String postcode = splitConfigLine[4];

                    Postcode userPostcode = null;

                    if (getPostcode(postcode) != null) {
                        userPostcode = getPostcode(postcode);
                    }

                    User userToAdd = new User(name, password, location, userPostcode);
                    users.add(userToAdd);

                } else if (line.startsWith("ORDER")){
                    String user = splitConfigLine[1];
                    String order = splitConfigLine[2];

                    User orderUser = null;


                    HashMap<Dish, Number> orderContents = new HashMap<>();
                    String[] ingredients = order.split(",");

                    // Getting the order contents
                    for (String currentDish : ingredients) {

                        String quantity = currentDish.split("\\*")[0].trim();
                        String dish = currentDish.split("\\*")[1].trim();

                        Dish dishToAdd = null;

                        if (getDish(dish) != null) {
                            dishToAdd = getDish(dish);
                        }

                        orderContents.put(dishToAdd, Integer.parseInt(quantity));
                    }

                    // Getting the order total
                    int sum = 0;
                    for(Map.Entry<Dish, Number> entry : orderContents.entrySet()){
                        sum = sum + (Integer) entry.getKey().getPrice() * (Integer) entry.getValue();
                    }

                    // Getting the order distance
                    Number distance;
                    try {
                        if (getUser(user) != null && getRestaurant() != null) {
                            orderUser = getUser(user);
                            distance = orderUser.getPostcode().calculateDistance(getRestaurant());
                        } else {
                            distance = 0;
                        }

                    } catch (NullPointerException e){
                        e.printStackTrace();
                        distance = 0;
                    }


                    Order orderToAdd = new Order(user, orderContents, sum, distance);
                    orderToAdd.setDescription(order);

                    try {
                        orderUser.addOrder(orderToAdd);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    orders.add(orderToAdd);

                } else if (line.startsWith("STOCK")){
                    String val = splitConfigLine[1];
                    String quantity = splitConfigLine[2];

                    if (getIngredient(val) != null) {
                        Ingredient ingredientToAdd = getIngredient(val);
                        stock.putIngredient(ingredientToAdd, Integer.parseInt(quantity));
                    } else if (getDish(val) != null) {
                        Dish dishToAdd = getDish(val);
                        stock.getDishes().put(dishToAdd, Integer.parseInt(quantity));
                    }

                } else if (line.startsWith("STAFF")){
                    String staffName = splitConfigLine[1];
                    Staff staffToAdd = new Staff(staffName);
                    staff.add(staffToAdd);
                    staffToAdd.start();

                } else if (line.startsWith("DRONE")){
                    Drone droneToAdd = new Drone(Integer.parseInt(splitConfigLine[1]));
//                    new Thread(droneToAdd).start();
                    drones.add(droneToAdd);
                    droneToAdd.setServer(server);
                    droneToAdd.start();
                }



            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private Supplier getSupplier(String supplier){
        for(Supplier s: suppliers){
            if(s.getName().equals(supplier)){
                return s;
            }
        }
        return null;
    }

    private Dish getDish(String dish){
        for(Dish d: dishes){
            if(d.getName().equals(dish)){
                return d;
            }
        }
        return null;
    }

    private Ingredient getIngredient(String ingredient){
        for(Ingredient i: ingredients){
            if(i.getName().equals(ingredient)){
                return i;
            }
        }
        return null;
    }

    private Stock getStock(){
        return stock;
    }

    private Postcode getPostcode(String postcode){
        for(Postcode p: postcodes){
            if(p.getName().equals(postcode)){
                return p;
            }
        }
        return null;
    }

    private User getUser(String user){
        for(User u: users){
            if(u.getName().equals(user)){
                return u;
            }
        }
        return null;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public List<Drone> getDrones() {
        return drones;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    public List<Staff> getStaff() {
        return staff;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public List<User> getUsers() {
        return users;
    }

    public HashMap<String, Integer> getOrderIDs() {
        return orderIDs;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }
}

