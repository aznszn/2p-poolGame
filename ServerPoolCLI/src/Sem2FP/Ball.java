package Sem2FP;

public class Ball extends Circle {
    //ball will have a velocity in addition to all properties of a circle
    private double xVel;
    private double yVel;
    private double velMag;

    public Ball(double radius){
        super(radius);
        xVel=0;
        yVel=0;
    }

    public void setVel(double xVel, double yVel){
        this.xVel=xVel;
        this.yVel=yVel;
        //calculate new velocity magnitude whenever velocity is changed
        calculateVelMag();
    }


    public void calculateVelMag(){
        velMag = Math.sqrt((xVel*xVel)+(yVel*yVel));
    }

    public double getXVel(){
        return xVel;
    }

    public double getYVel(){
        return yVel;
    }

    public double getVelMag() {
        return velMag;
    }
}

