package comp1206.sushi.server;
import comp1206.sushi.common.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CommsThread extends Thread {
    private Server server;
    private Comms comms;

    public CommsThread(Server server, Comms comms){
        this.server = server;
        this.comms = comms;
    }

    @Override
    public void run(){
        while (true) {
            Object obj = comms.recieveMessage();
            if (obj.equals("register")) {
                comms.sendMessage("user");
                User newUser = (User) comms.recieveMessage();
                server.addUser(newUser);
            } else if (obj.equals("login")) {
                comms.sendMessage("username");
                String username = (String) comms.recieveMessage();
                comms.sendMessage("password");
                String password = (String) comms.recieveMessage();
                for (User user : server.getUsers()) {
                    if (user.getName().equals(username) && user.getPassword().equals(password)) {
                        comms.sendMessage(user);
                    }
                }
            } else if (obj.equals("postcodes")) {
//                server.printPostcodes();
                comms.sendMessage(server.getPostcodes());
            } else if (obj.equals("dishes")) {
                comms.sendMessage(server.getDishes());
            } else if (obj.equals("order")) {
                comms.sendMessage("send order");
                Order recievedOrder = (Order) comms.recieveMessage();
                server.addOrder(recievedOrder);
            } else if (obj.equals("restaurant")) {
                comms.sendMessage(server.getRestaurant());
            } else if (obj.equals("cancel order")) {
                comms.sendMessage("send order");
                Order ord = (Order) comms.recieveMessage();
                for (Order o : server.getOrders()) {
                    if (o.equals(ord)) {
                        o.setStatus("Cancelled");
                    }
                }
            }
        }
    }
}
