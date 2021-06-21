package Sem2FP;

import java.io.IOException;
import java.net.Socket;

public class GameThread implements Runnable{
    public static volatile int numOfThreadsRunning;
    private final Game game;

    public GameThread(Socket player1Socket, Socket player2Socket)throws IOException {
        numOfThreadsRunning++;
        //increase count of num of games running
        game = new Game(player1Socket,player2Socket);
    }

    @Override
    public void run() {
        game.start();
        numOfThreadsRunning--;
        //decrease count of num of games running after game finishes
    }
}
