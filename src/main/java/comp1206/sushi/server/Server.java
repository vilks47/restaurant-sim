package comp1206.sushi.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
	
	public Restaurant restaurant;
	public List<Dish> dishes;
	public List<Drone> drones;
	public List<Ingredient> ingredients;
	public List<Order> orders;
	public List<Staff> staff;
	public List<Supplier> suppliers;
	public List<User> users;
	public List<Postcode> postcodes;
	private List<UpdateListener> listeners;
	private Stock stock;
	private Comms comms;
	private boolean restockingDishesEnabled;
	private boolean restockingIngredients;
	private boolean configLoaded;

	
	public Server() {
        logger.info("Starting up server...");
		System.out.println("Starting up server.");
        this.stock = Stock.getInstance();
        this.comms = new Comms(true);
        this.dishes = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.staff = new ArrayList<>();
		this.suppliers = new ArrayList<>();
		this.users = new ArrayList<>();
		this.postcodes = new ArrayList<>();
		this.listeners = new ArrayList<>();
		this.restaurant = new Restaurant(null, null);

		CommsThread commThread = new CommsThread(this, this.comms);
		commThread.start();



        Server server = this;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
			    DataPersistence saveData = new DataPersistence(server);
			    saveData.saveData();
			}
		}, "Shutdown-thread"));
	}

	@Override
	public List<Dish> getDishes() {
		return stock.getDishList();
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		Dish newDish = new Dish(name,description,price,restockThreshold,restockAmount);
		this.dishes.add(newDish);
		stock.getDishes().put(newDish, 0);
		this.notifyUpdate();
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) throws UnableToDeleteException {
	    stock.removeDish(dish);
		this.notifyUpdate();
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
		return stock.getDishStockLevels();
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
		this.restockingIngredients = enabled;
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		this.restockingDishesEnabled = enabled;
	}
	
	@Override
	public void setStock(Dish dish, Number stock) {
		this.stock.getDishes().put(dish, stock);
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
	    this.stock.getIngredients().put(ingredient, stock);
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public void setDishes(ArrayList<Dish> dishes) {
		this.dishes = dishes;
		this.notifyUpdate();
	}

	public void setDrones(ArrayList<Drone> drones) {
		this.drones = drones;
		this.notifyUpdate();
	}

	public void setUsers(ArrayList<User> users) {
		this.users = users;
		this.notifyUpdate();
	}

	@Override
	public List<Ingredient> getIngredients() {
		return stock.getIngredientList();
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier,
			Number restockThreshold, Number restockAmount, Number weight) {

	    Ingredient newIngredient = new Ingredient(name, unit, supplier, restockThreshold, restockAmount, weight);
	    stock.putIngredient(newIngredient, 0);
	    return newIngredient;
	}

	@Override
	public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
		stock.removeIngredient(ingredient);
		this.notifyUpdate();
	}

	@Override
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Postcode postcode) {
	    Supplier newSupplier = new Supplier(name, postcode);
	    suppliers.add(newSupplier);
	    return newSupplier;
	}


	@Override
	public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
	    if(suppliers.contains(supplier)){
	    	suppliers.remove(supplier);
		} else {
	    	throw new UnableToDeleteException("Cannot remove the supplier '" + supplier.getName() + "' as it is not in the system.");
		}
	}

	@Override
	public List<Drone> getDrones() {
		return this.drones;
	}

	@Override
	public Drone addDrone(Number speed) {
	    Drone drone = new Drone(speed);
	    drone.setServer(this);
	    drones.add(drone);
	    drone.start();
	    return drone;
	}

	@Override
	public void removeDrone(Drone drone) throws UnableToDeleteException {
		if(drones.contains(drone)){
			drones.remove(drone);
		} else {
			throw new UnableToDeleteException("Cannot remove the drone '" + drone.getName() + "' as it is not in the system.");
		}
	}

	@Override
	public List<Staff> getStaff() {
		return this.staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff staffMember = new Staff(name);
		staff.add(staffMember);
		staffMember.start();
		return staffMember;
	}

	@Override
	public void removeStaff(Staff staff) throws UnableToDeleteException {
		if(this.staff.contains(staff)){
			this.staff.remove(staff);
		} else {
			throw new UnableToDeleteException("Cannot remove the staff member '" + staff.getName() + "' as they are not in the system.");
		}
	}

	@Override
	public List<Order> getOrders() {
		return this.orders;
	}

	public void addOrder(Order order){
		orders.add(order);
	}


	@Override
	public void removeOrder(Order order) throws UnableToDeleteException {
		if(orders.contains(order)){
			orders.remove(order);
		} else {
			throw new UnableToDeleteException("Cannot remove the order '" + order.getName() + "' as it is not in the system.");
		}
	}
	
	@Override
	public Number getOrderCost(Order order) {
	    return order.getSumOfCosts();
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
	    return stock.getIngredientStockLevels();
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getPostcode().calculateDistance(restaurant);
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public Number getOrderDistance(Order order) {
	    return order.getDistance();
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		if(dish.getRecipe().containsKey(ingredient)){
		    dish.getRecipe().put(ingredient, quantity);
        }
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.getRecipe().remove(ingredient);
		this.notifyUpdate();
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
	    return dish.getRecipe();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return postcodes;
	}

	@Override
	public Postcode addPostcode(String code) {
	    Postcode newPostcode = new Postcode(code);
	    postcodes.add(newPostcode);
	    return newPostcode;
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		if(postcodes.contains(postcode)){
			postcodes.remove(postcode);
		} else {
			throw new UnableToDeleteException("Cannot remove the postcode '" + postcode.getName() + "' as it is not in the system.");
		}
	}

	@Override
	public List<User> getUsers() {
		return this.users;
	}

	public void addUser(User user){
	    users.add(user);
	}
	
	@Override
	public void removeUser(User user) throws UnableToDeleteException {
		if(users.contains(user)){
			users.remove(user);
		} else {
			throw new UnableToDeleteException("Cannot remove the user '" + user.getName() + "' as they are not in the system.");
		}
	}

	public void setStaff(ArrayList<Staff> staff) {
		this.staff = staff;
		this.notifyUpdate();
	}

	public void setPostcodes(ArrayList<Postcode> postcodes) {
		this.postcodes = postcodes;
		this.notifyUpdate();
	}

	public void setSuppliers(ArrayList<Supplier> suppliers) {
		this.suppliers = suppliers;
		this.notifyUpdate();
	}

	public void setOrders(ArrayList<Order> orders) {
		this.orders = orders;
		this.notifyUpdate();
	}



	@Override
	public void loadConfiguration(String filename) {
		Configuration config = new Configuration(filename, this);
	    try {
			config.loadConfig();
			setDishes((ArrayList<Dish>)config.getDishes());
			setDrones((ArrayList<Drone>)config.getDrones());
			setSuppliers((ArrayList<Supplier>)config.getSuppliers());
			setPostcodes((ArrayList<Postcode>)config.getPostcodes());
			setUsers((ArrayList<User>)config.getUsers());
			setStaff((ArrayList<Staff>)config.getStaff());
			setOrders((ArrayList<Order>)config.getOrders());
			System.out.println("Loaded configuration: " + filename);
		} catch (IOException e){
	        e.printStackTrace();;
		}

		postcodes = config.getPostcodes();
		users = config.getUsers();
		drones = config.getDrones();
		orders = config.getOrders();
		suppliers = config.getSuppliers();
		staff = config.getStaff();
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
	    try {
			for(Map.Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
				addIngredientToDish(dish,recipeItem.getKey(),recipeItem.getValue());
			}
		} catch (Exception e){
	    	e.printStackTrace();
		}
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return order.getOrderComplete();
	}

	@Override
	public String getOrderStatus(Order order) {
	    return order.getStatus();
	}
	
	@Override
	public String getDroneStatus(Drone drone) {
	    return drone.getStatus();
	}
	
	@Override
	public String getStaffStatus(Staff staff) {
	    return staff.getStatus();
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		dish.setRestockThreshold((Integer) restockThreshold);
		dish.setRestockAmount((Integer) restockAmount);
		this.notifyUpdate();
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestockThreshold(restockThreshold);
		ingredient.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestockAmount();
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestockAmount();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void notifyUpdate() {
//		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
		for(UpdateListener listener : listeners){
			listener.updated(new UpdateEvent());
		}
	}

	@Override
	public Postcode getDroneSource(Drone drone) {
		return drone.getSource();
	}

	@Override
	public Postcode getDroneDestination(Drone drone) {
		return drone.getDestination();
	}

	@Override
	public Number getDroneProgress(Drone drone) {
		return drone.getProgress();
	}

	@Override
	public String getRestaurantName() {
		return restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return restaurant.getLocation();
	}
	
	@Override
	public Restaurant getRestaurant() {
		return restaurant;
	}


}
