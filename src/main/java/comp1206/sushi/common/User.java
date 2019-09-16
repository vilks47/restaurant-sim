package comp1206.sushi.common;

import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class User extends Model implements Serializable {
	
	private String name;

	private String password;
	private String address;
	private Postcode postcode;
	private HashMap<Dish,Number> basket;
	private List<Order> orders;

	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
		this.orders = new ArrayList<>();
		basket=new HashMap<>();
	}

	public List<Order> getOrders() {
		System.out.println("Order list size of user:" +  orders.size());
		return orders;
	}

	public void setOrders(List<Order> orders) {
		notifyUpdate("orders", this.orders, orders);
		this.orders = orders;
	}

	public void addOrder(Order orderToAdd){
		orders.add(orderToAdd);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		notifyUpdate("Name", this.name, name);
		this.name = name;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		notifyUpdate("Postcode", this.postcode, postcode);
		this.postcode = postcode;
	}

	public void setBasket(HashMap<Dish, Number> basket) {
		notifyUpdate("Dishes",this.basket,basket);
		this.basket = basket;
	}

	public void setPassword(String password) {
		notifyUpdate("User", this.password, password);
		this.password = password;
	}


	public void setAddress(String address) {
		notifyUpdate("Address", this.address, address);
		this.address = address;
	}

	public String getPassword() {
		return password;
	}


	public String getAddress() {
		return address;
	}

	public HashMap<Dish, Number> getBasket() {
		return basket;
	}

	public Integer getBasketCost(){
		int sum = 0;
		for(Map.Entry<Dish, Number> entry : basket.entrySet()){
			sum = sum + (Integer) entry.getKey().getPrice() * (Integer) entry.getValue();
		}

		return sum;
	}

	public void addToBasket(Dish dish,Number quantity){
		basket.put(dish, quantity);
	}

	public void clearBasket(){
		basket = new HashMap<>();
	}
}
