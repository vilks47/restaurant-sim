package comp1206.sushi.common;

import comp1206.sushi.common.Drone;
import comp1206.sushi.common.*;
import comp1206.sushi.server.*;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class Drone extends Model implements Runnable {

	private Number speed;
	private Number progress;
	
	private Number capacity;
	private Number battery;
	
	private String status;
	private String droneName;
	
	private Postcode source;
	private Postcode destination;

	private Stock stock;

	private Thread thread;
	private Server server;

	private Restaurant droneHome;

	public Drone(Number speed) {
		this.setSpeed(speed);
		this.droneName = getName();
		this.setCapacity(1);
		this.setBattery(100);
		this.stock = Stock.getInstance();
		this.server = null;
		this.destination = null;
		this.source = null;
		this.progress = 0;
	}

	public void setServer(Server server){
		this.server = server;
		droneHome = server.getRestaurant();
	}


	@Override
	public void run(){
		while(true){
		    this.setStatus("Idle");
		    monitorStocks();
		    this.setStatus("Idle");
		    monitorOrders();
		    this.setStatus("Idle");
		}
	}

	public void start() {
		if(thread == null){
			thread = new Thread(this);
			thread.start();
		}
	}

	private void monitorStocks() {
		try {
			for(Ingredient currentIngredient : stock.getIngredients().keySet()){
				this.setStatus("Checking ingredient stocks");
				Integer currentIngredientStock = (Integer) stock.getIngredients().get(currentIngredient);
				try {
					this.setStatus("Checking stock level for " + currentIngredient.getName());
					if((Integer) currentIngredientStock < (Integer) currentIngredient.getRestockThreshold()){
						this.setStatus("Collecting ingredient: " + currentIngredient.getName() + " from supplier: " + currentIngredient.getSupplier().getName());
						source = server.getRestaurant().getLocation();
						destination = currentIngredient.getSupplier().getPostcode();

						try {
							Double droneTravelDistance = currentIngredient.getSupplier().getPostcode().calculateDistance(droneHome).doubleValue();
							double originalTravelTime = 1000 * (droneTravelDistance * 1) / speed.intValue();
							double travelTimeRemaining = originalTravelTime;
							while(travelTimeRemaining > 0){
								Thread.sleep((int) travelTimeRemaining);
								travelTimeRemaining = travelTimeRemaining - (originalTravelTime / 100);
								if(travelTimeRemaining < 0){
									break;
								}
								Number newProgress = (int) getProgress() + 1;
								setProgress(newProgress);
								if((int) progress == 100){
									break;
								}
							}
							setProgress(0);

						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}


						int newIngredientQuantity = (Integer) stock.getIngredients().get(currentIngredient) + (Integer) currentIngredient.getRestockThreshold() + (Integer) currentIngredient.getRestockAmount();
						stock.putIngredient(currentIngredient, newIngredientQuantity);
						break;
					}
					this.setStatus("Idle");
					monitorOrders();
//					lock.unlock();
				} catch (ConcurrentModificationException e) {
//				    lock.unlock();
					monitorOrders();
				}
			}
		} catch (ConcurrentModificationException e) {
		    monitorOrders();
		}
	}

	private void monitorOrders() {
		try {
			for (Order order : server.getOrders()) {
				try {
					if (!order.getOrderComplete()) {
						this.setStatus("Checking orders");
						boolean enoughPreparedDishes = true;
						for (Map.Entry<Dish, Number> orderDish : order.getOrderContents().entrySet()) {
							for(Dish dish : server.getDishes()){
								if(dish.equals(orderDish.getKey())){
									if((int) stock.getDishes().get(dish) < (int) orderDish.getValue()){
										enoughPreparedDishes = false;
									}
								}
							}
						}

						if(enoughPreparedDishes){
							order.setStatus("Ready");
						} else {
							order.setStatus("Not ready");
						}

						if (order.getStatus().equals("Ready")) {
							this.setStatus("Delivering order");

							source = server.getRestaurant().getLocation();
							destination = null;

							for(User u : server.getUsers()){
								for (Order o : server.getOrders()){
									if(o.equals(order)){
										destination = u.getPostcode();
									}
								}
							}

							try {
								Double droneTravelDistance = order.getDistance().doubleValue();
								double originalTravelTime = 1000 * (droneTravelDistance * 1) / speed.intValue();
								double travelTimeRemaining = originalTravelTime;
								while(travelTimeRemaining > 0){
									Thread.sleep((int) travelTimeRemaining);
									travelTimeRemaining = travelTimeRemaining - (originalTravelTime / 100);
									if(travelTimeRemaining < 0){
										break;
									}
									Number newProgress = (int) getProgress() + 1;
									setProgress(newProgress);
									if((int) progress == 100){
										break;
									}
								}
								setProgress(0);

							} catch (InterruptedException ex) {
								ex.printStackTrace();
							} catch (NullPointerException e){
								e.printStackTrace();
							}

							for(Dish dish : order.getOrderContents().keySet()){
								Integer dishesInOrder;
								try{
									dishesInOrder = order.getOrderContents().get(dish).intValue();
								} catch (NullPointerException e){
									dishesInOrder = 0;
								}

								Dish dishToUpdate = null;
								Integer dishesInStock = 0;
								for(Map.Entry<Dish, Number> stockDish : stock.getDishes().entrySet()){
									if(stockDish.getKey().getName().equals(dish.getName())){
										dishesInStock = (Integer) stockDish.getValue();
										dishToUpdate = stockDish.getKey();
									}
								}

								stock.getDishStockLevels().put(dishToUpdate, dishesInStock - dishesInOrder);
							}

	//						lock.unlock();
							order.setStatus("Complete");
							notifyUpdate("Delivered order");
							this.setStatus("Finished order.");
							}
					}
				} catch (ConcurrentModificationException e) {
//				    lock.unlock();
				    break;
				}
			}
		} catch (ConcurrentModificationException e) {
		    monitorStocks();
		}


	}



	public Number getSpeed() {
		return speed;
	}

	
	public Number getProgress() {
		return progress;
	}
	
	public void setProgress(Number progress) {
		this.progress = progress;
	}
	
	public void setSpeed(Number speed) {
		notifyUpdate("speed", this.speed, speed);
		this.speed = speed;
	}
	
	@Override
	public String getName() {
		return "Drone (" + getSpeed() + " speed)";
	}

	public Postcode getSource() {
		return source;
	}

	public void setSource(Postcode source) {
		this.source = source;
	}

	public Postcode getDestination() {
		return destination;
	}

	public void setDestination(Postcode destination) {
		this.destination = destination;
	}

	public Number getCapacity() {
		return capacity;
	}

	public void setCapacity(Number capacity) {
		this.capacity = capacity;
	}

	public Number getBattery() {
		return battery;
	}

	public void setBattery(Number battery) {
		this.battery = battery;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

	private void notifyUpdate(String status) {
		this.status = status;
		server.notifyUpdate();
	}
}
