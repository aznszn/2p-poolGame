package Sem2FP;

import javafx.scene.shape.Circle;

public class Ball {
    private Circle circle;
    private double xVel;
    private double yVel;
    private double velMag;

    private double radius;

    public Ball(double radius){
        this.radius=radius;
        circle = new Circle(radius);
        xVel=0;
        yVel=0;
    }

    public void setVel(double xVel, double yVel){
        this.xVel=xVel;
        this.yVel=yVel;
        calculateVelMag();
    }


    private void calculateVelMag(){
        velMag = Math.sqrt((xVel*xVel)+(yVel*yVel));
    }

    public void setPos(double xPos, double yPos){
        circle.setCenterX(xPos);
        circle.setCenterY(yPos);
    }

    public double getXPos(){
        return circle.getCenterX();
    }

    public double getYPos(){
        return circle.getCenterY();
    }

    public double getXVel(){ return xVel; }

    public double getYVel(){
        return yVel;
    }

    public Circle getCircle(){
        return circle;
    }

    public void setXPos(double xPos){
        this.circle.setCenterX(xPos);
    }

    public void setYPos(double yPos){
        this.circle.setCenterY(yPos);
    }
    public double getVelMag() {
        return velMag;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

}
