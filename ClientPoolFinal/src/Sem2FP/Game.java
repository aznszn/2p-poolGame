package Sem2FP;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Game {
    private final Server server;
    private final String name;
    private String opponentName;
    private boolean playerTurn;
    private final double bufferX;
    private final double bufferY;
    private final double width;
    private final double height;
    private final int numOfBalls;
    private final Ball[] balls;
    private Ball cueBall;
    private final double radius;
    private final double maxVel;
    private final Stage stage;
    private final Text turnText = new Text();
    private final Pane gamePane = new Pane();
    private final Circle guideCircle;
    private final Line cueBallTrajectory;
    private final Line line2;
    private final Line collisionBall2Trajectory;

    public Game(Server server, String name, Stage stage) {
        //menu sends server, player name, and stage where game will be staged
        this.server = server;
        this.name = name;
        this.stage = stage;
        //table dist from left
        bufferX = 200;
        //table dist from top
        bufferY = 150;
        //table dimensions
        width = 1200;
        height = 600;
        numOfBalls = 16;
        balls = new Ball[numOfBalls];
        //ball radius
        radius = 17.6;
        //cueball max velocity
        maxVel = 7;
        //shapes for guiding shot
        guideCircle = new Circle(radius);
        cueBallTrajectory = new Line(0,0,0,0);
        line2 = new Line(0,0,0,0);
        collisionBall2Trajectory = new Line(0,0,0,0);
    }

    public void start()throws Exception{
        //read name of opponent from server and write own name
        server.writeString(name);
        opponentName = server.readString();

        //setup table and add its elements to the pane
        Table table = new Table(bufferX,bufferY,width,height, Color.web("#56cc16"),Color.web("#4a260a"),radius);
        table.setupTable();
        gamePane.getChildren().add(table.getCloth());
        gamePane.getChildren().add(table.getBorder());
        table.addHoles(gamePane);
        //setup instruction text and create all the balls
        setupInstructions();
        createBalls();

        cueBall = balls[balls.length-1];
        cueBall.getCircle().setFill(Color.WHITE);

        table.placeBalls(balls);
        setupHeader();
        setupGuidingLines();
        setupTurnText();

        Scene scene = new Scene(gamePane, 1600, 900);
        gamePane.setStyle("-fx-background-color: linear-gradient(to right,#22e0f3,#f980fd)");
        stage.setScene(scene);
        stage.show();


        System.gc();

        cueBall.getCircle().setOnMouseReleased((event) -> {
            gamePane.getChildren().remove(cueBallTrajectory);
            gamePane.getChildren().remove(guideCircle);
            gamePane.getChildren().remove(line2);
            gamePane.getChildren().remove(collisionBall2Trajectory);

            //mouse position from cueball, scaled down. this is the shot velocity
            double deltaX = (cueBall.getXPos()-event.getSceneX())/20;
            double deltaY = (cueBall.getYPos()- event.getSceneY())/20;
            //magnitude
            double velMag = Math.sqrt((deltaX*deltaX)+(deltaY*deltaY));

            if(playerTurn) {
                if (velMag > maxVel) {
                    //if velocity is higher than max allowed velocity, scale it down to max velocity
                    cueBall.setVel(
                            (deltaX / velMag) * maxVel,
                            (deltaY / velMag) * maxVel
                    );
                } else
                    cueBall.setVel(deltaX, deltaY);
            }
            else
                cueBall.setVel(0,0);
        });

        cueBall.getCircle().setOnMouseDragged(event -> {
            //to show guiding lines
            if(playerTurn){
                gamePane.getChildren().remove(cueBallTrajectory);
                gamePane.getChildren().remove(line2);
                gamePane.getChildren().remove(collisionBall2Trajectory);
                gamePane.getChildren().remove(guideCircle);
                //shot is simulated by starting guidecircle from cueball position
                guideCircle.setCenterY(cueBall.getYPos());
                guideCircle.setCenterX(cueBall.getXPos());

                gamePane.getChildren().add(guideCircle);
                gamePane.getChildren().add(cueBallTrajectory);
                //this would be velocity of cueball
                double deltaX = (cueBall.getXPos()-event.getSceneX())/20;
                double deltaY = (cueBall.getYPos()- event.getSceneY())/20;
                double velMag = Math.sqrt((deltaX*deltaX)+(deltaY*deltaY));
                if (velMag > maxVel) {
                    deltaX = (deltaX / velMag)* maxVel;
                    deltaY = (deltaY/velMag)*maxVel;
                }

                outerLoop:
                while (true) {
                    //move the guidecircle
                    guideCircle.setCenterX(guideCircle.getCenterX()+deltaX);
                    guideCircle.setCenterY(guideCircle.getCenterY()+deltaY);

                    double newVelMag = Math.sqrt(deltaX*deltaX+deltaY*deltaY);
                    if(newVelMag <=0.1)
                        break;
                    //apply friction
                    deltaX-=(0.009*deltaX/newVelMag);
                    deltaY-=(0.009*deltaY/newVelMag);

                    for (Ball ball : balls) {
                        //check for collision against every ball except cueball
                        if (ball == cueBall)
                            continue;
                        double xDist = guideCircle.getCenterX()-ball.getXPos();
                        double yDist = guideCircle.getCenterY()-ball.getYPos();
                        double totalDistSqr = xDist*xDist+yDist*yDist;
                        //sum of radii squared = 4*radius^2
                        if(totalDistSqr <= (4*radius*radius)){
                            //if collision will happen, simulate collision and figure out trajectories
                            double dist = Math.sqrt(totalDistSqr);
                            double overlap = (dist - (2 * radius));
                            //resolve overlap
                            guideCircle.setCenterX(guideCircle.getCenterX() - (overlap*xDist/dist));
                            guideCircle.setCenterY(guideCircle.getCenterY() - (overlap*yDist/dist));

                            double normalX = xDist/dist;
                            double normalY = yDist/dist;

                            double tangentX = -normalY;
                            double tangentY = normalX;

                            //the striked ball's trajectory line
                            collisionBall2Trajectory.setStartX(ball.getXPos());
                            collisionBall2Trajectory.setStartY(ball.getYPos());

                            double dotProductTan1 = deltaX*tangentX + deltaY*tangentY;

                            double dotProductNorm1 = deltaX*normalX + deltaY*normalY;

                            line2.setEndX(guideCircle.getCenterX()+30*(tangentX*dotProductTan1));
                            line2.setEndY(guideCircle.getCenterY()+30*(tangentY*dotProductTan1));

                            collisionBall2Trajectory.setEndX(guideCircle.getCenterX()+30*(normalX*dotProductNorm1));
                            collisionBall2Trajectory.setEndY(guideCircle.getCenterY()+30*(normalY*dotProductNorm1));

                            gamePane.getChildren().add(collisionBall2Trajectory);
                            gamePane.getChildren().add(line2);
                            break outerLoop;
                        }
                    }
                    //right wall collision
                    if (guideCircle.getCenterX()+radius >= bufferX + width)
                        break;
                    //left wall collision
                    else if (guideCircle.getCenterX()-radius <= bufferX)
                        break;
                    //bottom wall collision
                    if (guideCircle.getCenterY() +radius>= bufferY + height)
                        break;
                    //top wall collision
                    else if (guideCircle.getCenterY() - radius<= bufferY)
                        break;

                }
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                String message;
                try {
                    for (Ball ball : balls) {
                        //for every ball, read its position
                        ball.setXPos(server.readDouble());
                        ball.setYPos(server.readDouble());
                        //calculate shadow based on position
                        calculateShadow(ball);
                        //if ball is at the origin it means it was removed
                        if (ball.getXPos() == 0 || ball.getYPos() == 0)
                            gamePane.getChildren().remove(ball.getCircle());
                    }
                    message = server.readString();
                    switch (message) {
                        case "turn" -> {
                            //this means player turn
                            turnText(true);
                            playerTurn = true;
                            //get cueball velocity and send
                            server.writeDouble(cueBall.getXVel());
                            server.writeDouble(cueBall.getYVel());
                            //reset velocity
                            cueBall.setVel(0, 0);
                        }
                        case "won", "lost" -> {
                            //show game result on screen
                            endScreen(message, stage);
                            this.stop();
                        }
                        default -> {
                            //if not player turn and game has not ended
                            turnText(false);
                            playerTurn = false;
                        }
                    }
                } catch (Exception e) {
                    this.stop();
                    //if here it means some error has occurred
                    endScreen("err", stage);
                }
            }
        };
        timer.start();
    }

    private void setupInstructions()throws IOException {
        //read which color balls is the player supposed to sink and display the information on the screen
        Text instructions = new Text("Sink all the "+server.readString()+" balls to win");
        instructions.setFont(Font.font(20));
        instructions.setFill(Color.RED);
        instructions.setX(bufferX);
        instructions.setY(bufferY+height+instructions.getLayoutBounds().getHeight()+50);
        gamePane.getChildren().add(instructions);
    }

    private void createBalls() {
        //create and initialize the objects in the balls array
        for(int i = 0; i<balls.length; i++){
            balls[i] = new Ball(radius);
            gamePane.getChildren().add(balls[i].getCircle());
            if(i%2==0) {
                balls[i].getCircle().setFill(Color.YELLOW);
            }
            else {
                balls[i].getCircle().setFill(Color.RED);
            }
            balls[i].getCircle().setStroke(Color.BLACK);
            balls[i].getCircle().setEffect(new DropShadow());
            ((DropShadow)balls[i].getCircle().getEffect()).setRadius(3);
        }
    }

    private void setupHeader(){
        //header displays player name and opponent name
        Font headerFont = Font.loadFont(getClass().getResource("Fonts/Odachi.ttf").toString(),40);
        Text vs = new Text("vs");
        vs.setFont(headerFont);
        vs.setStrokeWidth(5);
        vs.setX(bufferX+width/2-vs.getLayoutBounds().getWidth()/2);
        vs.setY(bufferY-vs.getLayoutBounds().getHeight()-10);
        gamePane.getChildren().add(vs);

        Text nameText = new Text(name);
        nameText.setFont(headerFont);
        nameText.setStrokeWidth(5);
        nameText.setX(bufferX);
        nameText.setY(bufferY-nameText.getLayoutBounds().getHeight()-10);
        gamePane.getChildren().add(nameText);

        Text opponentNameText = new Text(opponentName);
        opponentNameText.setStrokeWidth(5);
        opponentNameText.setFont(headerFont);
        opponentNameText.setX(bufferX+width-opponentNameText.getLayoutBounds().getWidth());
        opponentNameText.setY(bufferY-opponentNameText.getLayoutBounds().getHeight()-10);
        gamePane.getChildren().add(opponentNameText);
    }

    private void setupGuidingLines(){
        //setup the guiding lines and stylize them
        //cueball trajectory will always be from cueball to the guidecircle, so bind them
        cueBallTrajectory.endXProperty().bind(guideCircle.centerXProperty());
        cueBallTrajectory.endYProperty().bind(guideCircle.centerYProperty());
        cueBallTrajectory.startXProperty().bind(cueBall.getCircle().centerXProperty());
        cueBallTrajectory.startYProperty().bind(cueBall.getCircle().centerYProperty());
        cueBallTrajectory.setStrokeWidth(3);
        cueBallTrajectory.setStroke(Color.RED);
        line2.setStrokeWidth(3);
        line2.setStroke(Color.WHITE);
        line2.startXProperty().bind(guideCircle.centerXProperty());
        line2.startYProperty().bind(guideCircle.centerYProperty());
        guideCircle.setStrokeWidth(3);
        guideCircle.setFill(Color.TRANSPARENT);
        guideCircle.setStroke(Color.CYAN);
        collisionBall2Trajectory.setStrokeWidth(3);
        collisionBall2Trajectory.setStroke(Color.BLUE);
    }

    private void setupTurnText(){
        //setup and style turn indicator text
        gamePane.getChildren().add(turnText);
        turnText.setFont(Font.font(40));
        turnText.setFill(Color.RED);

    }

    private void turnText(boolean turn){
        //if player turn display it else dont
        if(turn) {
            turnText.setText("Your turn");
        }
        else {
            turnText.setText("");
        }
        turnText.setY(turnText.getLayoutBounds().getHeight()+4);
        turnText.setX(bufferX + width / 2 - turnText.getLayoutBounds().getWidth()/2);
    }

    private void calculateShadow(Ball ball) {
        //calculate shadow x and y offset based on distance from the center
        double lightHeight = 150;
        double xDist = width*0.5 + bufferX - ball.getXPos();
        double shadowOffsetX = -(xDist / lightHeight);
        double yDist = height*0.5 + bufferY - ball.getYPos();
        double shadowOffsetY = -(yDist / lightHeight);
        ((DropShadow) ball.getCircle().getEffect()).setOffsetX(shadowOffsetX);
        ((DropShadow) ball.getCircle().getEffect()).setOffsetY(shadowOffsetY);
    }

    private void endScreen(String result, Stage stage){
        //if player wins or loses display that and if an error has occurred display that
        Text text;
        //button to close game
        Button button = new Button("exit");
        gamePane.getChildren().add(button);
        button.setLayoutX(bufferX+width/2);
        button.setLayoutY(bufferY+height/2+60);
        System.out.println(result);
        if(result.equals("won")||result.equals("lost"))
            text = new Text("You "+result+" the game");
        else
            text = new Text("An unexpected error occurred");
        gamePane.getChildren().add(text);
        text.setFont(Font.font(50));
        text.setFill(Color.RED);
        text.setStroke(Color.RED);
        text.setX(bufferX+width/2-text.getLayoutBounds().getWidth()/2);
        text.setY(bufferY+height/2);
        button.setOnMouseClicked((event) ->{
            try {
                stage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
