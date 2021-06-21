package Sem2FP;

import java.io.IOException;
import java.net.ServerSocket;

//deals with clients trying to play a game
public class GameRequestHandler {
    private static ServerSocket serverSocket;
    private static int maxGames = 2; //arbitrary
    public static void main(String[] args){
        try{
            serverSocket = new ServerSocket(69);
        }catch (IOException e){
            e.printStackTrace();
        }
        while (true){
            try {
                if (GameThread.numOfThreadsRunning < maxGames) {
                    //start game if the number of games running is less than maximum allowed games
                    GameThread gameThread = new GameThread(serverSocket.accept(), serverSocket.accept());
                    Thread thread = new Thread(gameThread);
                    thread.start();
                } else {
                    //close socket and wait until the number of games running is less than max allowed num of games
                    serverSocket.close();
                    while(GameThread.numOfThreadsRunning>=maxGames)
                        Thread.onSpinWait();
                    serverSocket = new ServerSocket(69);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
