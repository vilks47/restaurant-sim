package comp1206.sushi.common;

import java.io.Serializable;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.Socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Comms implements Serializable {

    private ServerSocket serverSocket;
    private int serverPort;
    private boolean isServer;

    public Comms(boolean isServer) {
        this.serverPort = 60010;
        this.isServer = isServer;

        if (this.isServer) {
            try {
                this.serverSocket = new ServerSocket(this.serverPort);
            } catch (IOException e) {
                throw new RuntimeException("Port can't be opened on " + serverPort, e);
            }
        }
    }

    public void sendMessage(Object message){
        try {
            Socket socket;
            if (isServer) {
                socket = this.serverSocket.accept();
            } else {
                socket = new Socket("localhost", this.serverPort);
            }
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object recieveMessage(){
        try {
            Socket socket;
            Object obj = null;
            if (isServer) {
                socket = this.serverSocket.accept();
            } else {
                socket = new Socket("localhost", this.serverPort);
            }
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            try {
                obj = in.readObject();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            in.close();
            return obj;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
