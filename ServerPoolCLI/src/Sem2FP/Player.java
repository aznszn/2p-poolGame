package Sem2FP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//Player class contains all methods and fields for interacting with clients
public class Player{
    private String name;
    private Socket playerSocket;
    private DataInputStream input;
    private DataOutputStream output;

    public Player(Socket playerSocket){
        this.playerSocket = playerSocket;
        this.name = "temp";
        try {
            input = new DataInputStream(playerSocket.getInputStream());
            output = new DataOutputStream(playerSocket.getOutputStream());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void writeString (String message) throws IOException{
        output.writeUTF(message);
        output.flush();
    }

    public void writeDouble (double value) throws IOException{
        output.writeDouble(value);
        output.flush();
    }

    public void writeInt (int value) throws IOException{
        output.writeInt(value);
        output.flush();
    }

    public String readString () throws IOException{
        return input.readUTF();
    }

    public double readDouble () throws IOException{
        return input.readDouble();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void close(){
        try {
            playerSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
