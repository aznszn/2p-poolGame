package Sem2FP;

import javafx.geometry.Point2D;

import java.io.IOException;
import java.net.Socket;

public class Game {

    private boolean player1Turn = true;
    private final double bufferX;
    private final double bufferY;
    private final double width;
    private final double height;
    private final double friction;
    private final Circle[][] holes;
    private final int numOfBalls;
    private final int numOfRedBalls;
    private final int numOfYellowBalls;
    private final Ball[] balls;
    private Ball cueBall;
    private int numOfRedBallsRemoved;
    private int numOfYellowBallsRemoved;
    private final double radius;
    private Player player1;
    private Player player2;
    private final Socket player1Socket;
    private final Socket player2Socket;

    public Game(Socket player1Socket, Socket player2Socket) {
        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;
        bufferX = 200;
        //dist of table from the left
        bufferY = 150;
        //dist of table from the top
        width = 1200;
        height = 600;
        //dimensions of table
        friction = 0.009;
        holes = new Circle[2][3];
        numOfBalls=16;
        numOfRedBalls=(numOfBalls-1)/2;
        numOfYellowBalls=numOfBalls/2;
        balls = new Ball[numOfBalls];
        numOfRedBallsRemoved=0;
        numOfYellowBallsRemoved=0;
        radius = 17.6;
        //radius of balls
    }

    public void start() {
        setupPlayers();
        addHoles();
        placeBalls();
        System.gc();

        //Main game loop
        while(true){
            try {
                //cycle through every ball
                for (int i = 0; i < balls.length; i++) {
                    detectBorderCollision(balls[i]);
                    move(balls[i]);

                    //sending new location of every ball to both players
                    player1.writeDouble(balls[i].getXPos());
                    player2.writeDouble(balls[i].getXPos());

                    player1.writeDouble(balls[i].getYPos());
                    player2.writeDouble(balls[i].getYPos());

                    if (balls[i] != cueBall && detectFall(balls[i])) {
                        if (i % 2 == 0) {
                            numOfYellowBallsRemoved++;
                            if(player1Turn)
                                player1Turn=false;
                            }
                        else {
                            numOfRedBallsRemoved++;
                            if(!player1Turn)
                                player1Turn=true;
                        }
                        //balls that have been sunk are placed in the corner, indicating to the clients to remove them
                        balls[i].setPos(0, 0);
                        balls[i].setVel(0, 0);
                    }

                    for (int j = 0; j < balls.length; j++) {
                        if (i != j) {
                            //detect collision of current ball with every other ball except itself
                            detectCollision(balls[i], balls[j]);
                        }
                    }
                }
                //checking if a player has won
                if (player1Win()) {
                    player1.writeString("won");
                    player2.writeString("lost");
                    //closing game
                    player2.close();
                    player1.close();
                    break;
                } else if (player2Win()) {
                    player1.writeString("lost");
                    player2.writeString("won");
                    //closing game
                    player2.close();
                    player1.close();
                    break;
                } else if (playerTurn()) {
                    //if all balls are static, check which player's turn it else
                    if (player1Turn) {
                        player1.writeString("turn");
                        player2.writeString("wait");
                        cueBall.setVel(player1.readDouble(), player1.readDouble());
                        //read cue ball velocity from player, if player hasnt played yet continue and wait until player responds
                        if (cueBall.getVelMag() != 0)
                            player1Turn = false;
                    } else {
                        player2.writeString("turn");
                        player1.writeString("wait");
                        cueBall.setVel(player2.readDouble(), player2.readDouble());
                        //read cue ball velocity from player, if player hasnt played yet continue and wait until player responds
                        if (cueBall.getVelMag() != 0)
                            player1Turn = true;
                    }
                } else {
                    //if it isnt either player's turn
                    player1.writeString("wait");
                    player2.writeString("wait");
                }
            } catch (IOException e) {
                //if a communication error occurs
                player1.close();
                player2.close();
                break;
            }
        }
    }
    private void setupPlayers() {
        try {
            player1 = new Player(player1Socket);
            player2 = new Player(player2Socket);

            //read names of both players
            player1.setName(player1.readString());
            player2.setName(player2.readString());

            //send opponent name to both players
            player1.writeString(player2.getName());
            player2.writeString(player1.getName());

            //tell each player what color balls they have to sink
            player1.writeString("red");
            player2.writeString("yellow");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addHoles(){
        //adding holes to the table
        for(int i =0;i<2;i++)
            for(int j =0;j<3;j++)
                holes[i][j] = new Circle(radius+7);
                //holes are bigger than the balls to facilitate sinking

        double xPos=bufferX;
        double yPos=bufferY;

        //algorithm to place holes in the correct places
        for(int i = 0;i < 2;i++,yPos+=height,xPos=bufferX)
            for (int j = 0; j<3; j++,xPos+=width/2) {
                holes[i][j].setXPos(xPos);
                holes[i][j].setYPos(yPos);
            }
    }

    private void placeBalls(){
        //create balls and place them in their initial positions

        balls[0] = new Ball(radius);
        balls[0].setPos(bufferX+(width*0.75),bufferY + height*0.5);

        //programmatically placing balls in their initial positions
        for(int i =1;i<balls.length;i++) {
            balls[i] = new Ball(radius);
            double xDist = 2*radius * Math.round(Math.cos(Math.PI / 6.0));
            double yDist = 2*radius * Math.sin(Math.PI / 6.0);
            if(i<5)
                //going up the triangle to top vertex
                balls[i].setPos(balls[i - 1].getXPos() + xDist, balls[i - 1].getYPos() - yDist);
            else if(i<9)
                //going straight down
                balls[i].setPos(balls[i - 1].getXPos() , balls[i - 1].getYPos() + 2*radius);
            else if(i<12)
                //going up the triangle from bottom vertex
                balls[i].setPos(balls[i - 1].getXPos() - xDist, balls[i - 1].getYPos() - yDist);
            //middle three balls in triangle
            else if(i==12)
                balls[i].setPos(balls[3].getXPos(),balls[3].getYPos()+2*radius);
            else if(i==13)
                balls[i].setPos(balls[12].getXPos(),balls[12].getYPos()+2*radius);
            else if(i==14)
                balls[i].setPos(balls[2].getXPos(),balls[2].getYPos()+2*radius);
        }
        //cueball is the last ball in the array
        cueBall = balls[balls.length-1];
        //cueball initial position
        cueBall.setPos(bufferX+(width*0.25),bufferY+(height*0.5));
    }

    private void detectCollision(Ball ball1, Ball ball2)throws IOException{
        //basically calls the colliding function on the given balls if the distance between them is less than the sum of
        //their radii
        double xDist = ball1.getXPos()-ball2.getXPos();
        double yDist = ball1.getYPos()-ball2.getYPos();
        double totalDistSqr = xDist*xDist+yDist*yDist;
        if(totalDistSqr <= (4*radius*radius))
            collide(ball1, ball2);
    }

    private void collide(Ball ball1, Ball ball2){
        //vector between the two ball's centers
        Point2D collisionVector = new Point2D(ball1.getXPos(),ball1.getYPos()).subtract(ball2.getXPos(),ball2.getYPos());

        double dist = collisionVector.magnitude();
        //normalizing
        collisionVector = collisionVector.normalize();

        //these dot products determine how direct the collisions were
        double vA = collisionVector.dotProduct(ball1.getXVel(),ball1.getYVel());
        double vB = collisionVector.dotProduct(ball2.getXVel(),ball2.getYVel());

        if (vB <= 0 && vA >= 0) {
            //detects if the balls have already collided and doesnt do further calculations
            return;
        }

        //move the first ball back to eliminate the overlap
        double overlap = (dist-(2*radius));
        ball1.setXPos(ball1.getXPos() - (overlap*collisionVector.getX()));
        ball1.setYPos(ball1.getYPos() - (overlap*collisionVector.getY()));

        //normal to the collision
        double normalX = collisionVector.getX();
        double normalY = collisionVector.getY();

        //tangent to the collision
        double tangentX = -normalY;
        double tangentY = normalX;

        //calculate the tangential response of both balls
        double dotProductTan1 = ball1.getXVel()*tangentX + ball1.getYVel()*tangentY;
        double dotProductTan2 = ball2.getXVel()*tangentX + ball2.getYVel()*tangentY;

        //calculate the normal response of both balls
        double dotProductNorm1 = ball1.getXVel()*normalX + ball1.getYVel()*normalY;
        double dotProductNorm2 = ball2.getXVel()*normalX + ball2.getYVel()*normalY;

        //the tangential and normal responses are scaled proportionate to the length of the tangent
        //this is the new velocity of each ball
        ball1.setVel(tangentX*dotProductTan1 + normalX*dotProductNorm2,tangentY*dotProductTan1 + normalY*dotProductNorm2);
        ball2.setVel(tangentX*dotProductTan2 + normalX*dotProductNorm1,tangentY*dotProductTan2 + normalY*dotProductNorm1);
    }

    private void move(Ball ball){
        //move balls according to the velocity
        ball.setPos(ball.getXPos()+(ball.getXVel()), ball.getYPos()+(ball.getYVel()));

        applyFriction(ball);

        //clamp ball velocity to zero if it is close to zero
        if(ball.getVelMag()<0.1)
            ball.setVel(0,0);
    }

    private void applyFriction(Ball ball){
        //reduce ball velocity according to the friction value
        if(ball.getVelMag()!=0)
            ball.setVel(
                    ball.getXVel()-(friction*ball.getXVel()/ball.getVelMag()),
                    ball.getYVel()-(friction*ball.getYVel()/ball.getVelMag())
            );
    }

    private void detectBorderCollision(Ball ball){
        if(ball.getXPos()!=0&&ball.getYPos()!=0) {
            //top
            if (ball.getYPos() - bufferY <= radius) {
                //restore position to top border
                ball.setPos(ball.getXPos(),bufferY+radius);
                ball.setVel(ball.getXVel(), -ball.getYVel());
            }

            //left
            if (ball.getXPos() - bufferX <= radius) {
                //restore position to left border
                ball.setPos(bufferX+radius,ball.getYPos());
                ball.setVel(-ball.getXVel(), ball.getYVel());
            }

            //bottom
            if (ball.getYPos() - height - bufferY >= -radius) {
                //restore position to bottom border
                ball.setPos(ball.getXPos(),bufferY+height-radius);
                ball.setVel(ball.getXVel(), -ball.getYVel());
            }

            //right
            if (ball.getXPos() - width - bufferX >= -radius) {
                //restore position to  right border
                ball.setPos(bufferX+width-radius,ball.getYPos());
                ball.setVel(-ball.getXVel(), ball.getYVel());
            }
        }
    }

    private boolean detectFall(Ball ball){
        for(Circle[] holeRow: holes)
            for(Circle hole: holeRow){
                //checking ball position against every hole
                double xDist = ball.getXPos()-hole.getXPos();
                double yDist = ball.getYPos()-hole.getYPos();
                double dist =  Math.sqrt((xDist * xDist) + (yDist * yDist));
                if(dist<=(radius+hole.getRadius()-10)) {
                    return true;
                }
            }
        return false;
    }

    private boolean playerTurn(){
        for(Ball ball: balls){
            //if any ball is moving it isnt player turn
            if(ball.getXVel()!=0||ball.getYVel()!=0)
                return false;
        }
        return true;
    }

    private boolean player1Win(){
        return numOfRedBallsRemoved == numOfRedBalls;
    }

    private boolean player2Win(){
        return numOfYellowBallsRemoved == numOfYellowBalls;
    }
}
