package comp1206.sushi.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class used to represent an order and its details.
 */
public class Order extends Model implements Serializable {
	private String status;
	private Number sumOfCosts;
	private String name;
	private HashMap<Dish, Number> orderContents;
	private Number distance;
	private String description;
	private Lock lock = new ReentrantLock();

	public Order(String name, HashMap<Dish, Number> orderContents, Number sumOfCosts, Number distance) {

		super.setName(name);

		this.orderContents = orderContents;
		this.sumOfCosts = sumOfCosts;
		this.distance = distance;
		this.description = "";

		status = "Not ready";
	}

	public Lock getLock() {
		return lock;
	}

	public Order() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		this.name = dtf.format(now);
	}

	public void setDistance(Number distance){
		this.distance = distance;
	}

	public Number getDistance() {
		return distance;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public Number getSumOfCosts() {
		return sumOfCosts;
	}

	public void setSumOfCosts(Integer sumOfCosts) {
		notifyUpdate("cost", this.sumOfCosts, sumOfCosts);
		this.sumOfCosts = sumOfCosts;
	}

	public synchronized Boolean getOrderComplete() {
		return this.getStatus().equals("Complete");
	}

	public String getDescription() {
	    return description;
	}

	public void setDescription(String description){
		this.description = description;
	}

//	public synchronized void setOrderComplete(Boolean orderComplete) {
//		this.orderComplete = orderComplete;
//	}


	public synchronized HashMap<Dish, Number> getOrderContents() {
		return orderContents;
	}

	public void setOrderContents(HashMap<Dish, Number> orderContents){
		notifyUpdate("dishes", this.orderContents, orderContents);
		this.orderContents = orderContents;
	}

	public synchronized String getStatus() {
		return status;
	}

	public synchronized void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

}
