package comp1206.sushi.client;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client implements ClientInterface {

    private static final Logger logger = LogManager.getLogger("Client");
    private Comms clientComms;
	private List<UpdateListener> updateListeners;

	public Client() {
	    this.clientComms = new Comms(false);
	    this.updateListeners = new ArrayList<>();
        logger.info("Starting up client...");
	}
	
	@Override
	public Restaurant getRestaurant() {
	    clientComms.sendMessage("restaurant");
		Restaurant recievedRestaurant = (Restaurant) clientComms.recieveMessage();
		return recievedRestaurant;
	}
	
	@Override
	public String getRestaurantName() {
		clientComms.sendMessage("restaurant");
		Restaurant recievedRestaurant = (Restaurant) clientComms.recieveMessage();
		return recievedRestaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		clientComms.sendMessage("restaurant");
		Restaurant recievedRestaurant = (Restaurant) clientComms.recieveMessage();
		return recievedRestaurant.getLocation();
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
		User newUser = new User(username, password, address, postcode);
		clientComms.sendMessage("register");
		if (clientComms.recieveMessage().equals("user")) {
			clientComms.sendMessage(newUser);
		}

		return newUser;
	}

	@Override
	public User login(String username, String password) {
		User user = null;
		clientComms.sendMessage("login");
		if (clientComms.recieveMessage().equals("username")) {
			clientComms.sendMessage(username);
			if (clientComms.recieveMessage().equals("password")) {
				clientComms.sendMessage(password);
				user = (User) clientComms.recieveMessage();
			}
		}
		return user;
	}

	@Override
	public List<Postcode> getPostcodes() {
		clientComms.sendMessage("postcodes");
		return (List<Postcode>) clientComms.recieveMessage();
	}

	@Override
	public List<Dish> getDishes() {
		clientComms.sendMessage("dishes");
		return (List<Dish>) clientComms.recieveMessage();
	}

	@Override
	public String getDishDescription(Dish dish) {
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
		return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) {
		return user.getBasketCost();
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
		user.addToBasket(dish, quantity);
		notifyUpdate();

	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
		user.getBasket().put(dish, quantity);
		notifyUpdate();
	}

	@Override
	public Order checkoutBasket(User user) {
		clientComms.sendMessage("restaurant");
		Restaurant recievedRestaurant = (Restaurant) clientComms.recieveMessage();

		Order newOrder = new Order(user.getName(), user.getBasket(), user.getBasketCost(), user.getPostcode().calculateDistance(recievedRestaurant));

		String description = "";
		for (Map.Entry<Dish, Number> entry : user.getBasket().entrySet()) {
			description += entry.getValue() + " * " + entry.getKey().getName() + ",";
		}

		// Removes comma at the end and adds the description to the order.
		newOrder.setDescription(description.substring(0, description.length() - 1));

		clientComms.sendMessage("order");
		if (clientComms.recieveMessage().equals("send order")) {
			clientComms.sendMessage(newOrder);
			user.addOrder(newOrder);
		}
		return newOrder;
	}

	@Override
	public void clearBasket(User user) {
		user.clearBasket();
	}

	@Override
	public List<Order> getOrders(User user) {
	    return user.getOrders();
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
	public Number getOrderCost(Order order) {
	    return order.getSumOfCosts();
	}

	@Override
	public void cancelOrder(Order order) {
		clientComms.sendMessage("cancel order");
		if (clientComms.recieveMessage().equals("send order")) {
			clientComms.sendMessage(order);
		}
		notifyUpdate();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		updateListeners.add(listener);

	}

	@Override
	public void notifyUpdate() {
		for (UpdateListener listener : updateListeners) {
			listener.updated(new UpdateEvent());
		}
	}

}
