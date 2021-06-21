package Sem2FP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//provides functionality for reading from and writing to server
public class Server {
    Socket socket;
    DataInputStream din;
    DataOutputStream dout;

    public Server(String address, int portNum) throws IOException {
        this.socket = new Socket(address,portNum);
        dout = new DataOutputStream(socket.getOutputStream());
        din = new DataInputStream(socket.getInputStream());
    }

    public void writeInt(int toWrite)throws IOException{
        dout.writeInt(toWrite);
        dout.flush();
    }

    public int readInt() throws IOException{
        return din.readInt();
    }

    public void writeString(String toWrite) throws IOException{
        dout.writeUTF(toWrite);
        dout.flush();
    }

    public String readString() throws IOException{
        return din.readUTF();
    }

    public double readDouble() throws IOException{
        return  din.readDouble();
    }

    public void writeDouble(double toWrite) throws IOException{
        dout.writeDouble(toWrite);
        dout.flush();
    }
}
