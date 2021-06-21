package Sem2FP;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Table {
    private final double bufferX;
    private final double bufferY;
    private final double width;
    private final double height;
    private final Color colorCloth;
    private final Color colorBoundary;
    private Rectangle cloth;
    private Rectangle border;
    private Circle[][] holes;
    private final double radius;

    public Table(double bufferX, double bufferY, double width, double height, Color colorCloth, Color colorBoundary, double radius) {
        this.bufferX = bufferX;
        this.bufferY = bufferY;
        this.width = width;
        this.height = height;
        this.colorCloth = colorCloth;
        this.colorBoundary = colorBoundary;
        this.radius = radius;
    }

    public void setupTable() {
        setupCloth();
        setupBorder();
        setupHoles();
    }

    private void setupCloth() {
        cloth = new Rectangle(width, height);
        cloth.setFill(colorCloth);
        cloth.setX(bufferX);
        cloth.setY(bufferY);
    }

    private void setupBorder() {
        border = new Rectangle(width + 30, height + 30);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(colorBoundary);
        border.setArcWidth(30);
        border.setArcHeight(20);
        border.setStrokeWidth(Math.sqrt(21 * 21 + 21 * 21));
        border.setX(bufferX - 15);
        border.setY(bufferY - 15);
    }

    private void setupHoles() {
        holes = new Circle[2][3];
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 3; j++)
                holes[i][j] = new Circle(radius + 7);
        double xPos = bufferX;
        double yPos = bufferY;
        for (int i = 0; i < 2; i++, yPos += height, xPos = bufferX)
            for (int j = 0; j < 3; j++, xPos += width / 2) {
                holes[i][j].setCenterX(xPos);
                holes[i][j].setCenterY(yPos);
            }
    }

    public void placeBalls(Ball[] balls){
        double radius = balls[0].getRadius();
        balls[0].setPos(bufferX+width*0.75,bufferY + height*0.5);

        for(int i =1;i<balls.length;i++) {
            double xDist = 2*radius * Math.round(Math.cos(Math.PI / 6.0));
            double yDist = 2*radius * Math.sin(Math.PI / 6.0);
            if(i<5) {
                balls[i].setPos(balls[i - 1].getXPos() + xDist, balls[i - 1].getYPos() - yDist);
            }
            else if(i<9) {
                balls[i].setPos(balls[i - 1].getXPos() , balls[i - 1].getYPos() + 2*radius);
            }
            else if(i<12) {
                balls[i].setPos(balls[i - 1].getXPos() - xDist, balls[i - 1].getYPos() - yDist);
            }
            else if(i==12) {
                balls[i].setPos(balls[3].getXPos(),balls[3].getYPos()+2*radius);
            }
            else if(i==13) {
                balls[i].setPos(balls[12].getXPos(),balls[12].getYPos()+2*radius);
            }
            else if(i==14) {
                balls[i].setPos(balls[2].getXPos(),balls[2].getYPos()+2*radius);
            }
        }
        balls[balls.length-1].setPos(bufferX+(width/4),bufferY+(height/2.0));
    }

    public void addHoles(Pane pane){
        for(Circle[] holes: holes)
            for (Circle hole: holes){
                pane.getChildren().add(hole);
            }
    }

    public Rectangle getCloth() {
        return cloth;
    }

    public Rectangle getBorder() {
        return border;
    }
}
