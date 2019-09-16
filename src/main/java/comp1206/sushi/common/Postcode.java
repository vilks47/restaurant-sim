package comp1206.sushi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Postcode extends Model implements Serializable {

	private String name;
	private Map<String,Double> latLong;
	private Double latCord;
	private Double longCord;
	private Number distance;
	private Restaurant restaurant;

	public Postcode(String code) {
		this.name = code;
		this.distance = 0;
		calculateLatLong();
//		.replaceAll("\\s+",""
	}
	
	public Postcode(String code, Restaurant restaurant) {
		this.name = code;
		calculateLatLong();
		calculateDistance(restaurant);
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Number getDistance() {
		return this.distance;
	}

	public void setDistance(Number distance) { this.distance = distance; }

	public Map<String,Double> getLatLong() {
		return this.latLong;
	}

	public void setLatLong(Map<String,Double> latLong){
		this.latLong = latLong;
	}

	public Double getLatCord() {
		return latCord;
	}

	public void setLatCord(Double latCord) {
		this.latCord = latCord;
	}

	public Double getLongCord() {
		return longCord;
	}

	public void setLongCord(Double longCord) {
		this.longCord = longCord;
	}

	public Number calculateDistance(Restaurant restaurant) {
        Double restaurantLat = restaurant.getLocation().getLatCord();
        Double restaurantLong = restaurant.getLocation().getLongCord();

        Double postcodeLat = this.latCord;
        Double postcodeLong = this.longCord;

		if ((restaurantLat == postcodeLat) && (restaurantLong == postcodeLong)) {
		    setDistance(0);
		    return 0;
		}
		else {
			double theta = restaurantLong - postcodeLong;
			double dist = Math.sin(Math.toRadians(restaurantLat)) * Math.sin(Math.toRadians(postcodeLat)) + Math.cos(Math.toRadians(restaurantLat)) * Math.cos(Math.toRadians(postcodeLat)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			dist = dist * 1.609344;
			setDistance(dist);
			return dist;
		}
	}
	
	private void calculateLatLong() {
		URL latLongAPI = null;
		BufferedReader in = null;
		String latLongJSON = null;

		try {
			latLongAPI = new URL("https://www.southampton.ac.uk/~ob1a12/postcode/postcode.php?postcode=" + getName().replaceAll("\\s+",""));
		} catch (MalformedURLException e){
			e.printStackTrace();
		}

		try {
			in = new BufferedReader(new InputStreamReader(latLongAPI.openStream()));
		} catch (IOException e){
			e.printStackTrace();
		} catch (NullPointerException n){
			n.printStackTrace();
		}

		try {
			latLongJSON = in.readLine();
		} catch (IOException e){
			e.printStackTrace();
		}

		Map<String, Double> map = new HashMap<>();
		for (String keyValue: latLongJSON.split(",")) {
			String[] data = keyValue.split(":");

			if(data[0].startsWith("{\"postcode\"")){
				continue;
			} else {
			    // Cleans up the JSON by removing loose characters and extra whitespace/double quotes and then parses to a double.
                String label = data[0].trim().replaceAll("^\"|\"$", "");
				Double coordinate = Double.parseDouble(data[1].replace('}', ' ').trim().replaceAll("^\"|\"$", ""));
				map.put(label, coordinate);
			}
		}

		setLatLong(map);

		for(Map.Entry<String,Double> entry : map.entrySet()){
			if(entry.getKey().equals("lat")){
				setLatCord(entry.getValue());
			} else if (entry.getKey().equals("long")){
				setLongCord(entry.getValue());
			}
		}

//		this.latLong = new HashMap<String,Double>();
//		latLong.put("lat", 0d);
//		latLong.put("lon", 0d);
//		this.distance = new Integer(0);
	}
	
}
