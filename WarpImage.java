package com.example.morphimages;

import android.graphics.Bitmap;

/**
 * Warp class to warp the images and save them into arrays to be access out of the class
 */
public class WarpImage{
    /**
     * Line controller for the lines on the images
     */
    private LineController lc;

    /**
     * Original right and left bitmaps, as well as the final right and left bitmaps
     */
    private Bitmap rightBm, leftBm, finalBmRight, finalBmLeft;

    /**
     * Bitmaps of the right and left final images
     */
    public Bitmap[] rightFinals, leftFinals;

    /**
     * Constructor taking in the controller, right and left bitmap originals and the number of
     * frames specified by the user
     */
    public WarpImage(LineController controller, Bitmap left, Bitmap right, int frames){
        lc = controller;
        leftBm = left;
        rightBm = right;
        finalBmLeft = Bitmap.createBitmap(left.getWidth(), left.getHeight(), left.getConfig());
        finalBmRight = Bitmap.createBitmap(right.getWidth(), right.getHeight(), right.getConfig());
        rightFinals = new Bitmap[frames];
        leftFinals = new Bitmap[frames];
    }

    public void leftWarp(int i, int frames){
        finalBmLeft = Bitmap.createBitmap(leftBm.getWidth(), leftBm.getHeight(), leftBm.getConfig());
        for(int x = 0; x < leftBm.getWidth(); x++){
            for(int y = 0; y < leftBm.getHeight(); y++){

                Point Xprime = new Point(x, y);
                Point[] calculatedSrc = new Point[lc.leftCanvas.size()];
                double[] weights = new double[lc.leftCanvas.size()];

                for(int lines = 0; lines < lc.leftCanvas.size(); lines++){

                    Point starts = interPoint(lc.leftCanvas.get(lines).start, lc.rightCanvas.get(lines).start, i, frames),
                            ends = interPoint(lc.leftCanvas.get(lines).end,
                                    lc.rightCanvas.get(lines).end, i, frames);

                    // SOMETHING WRONG HERE, CHECK README
                    Vector PQ = new Vector((ends.getX() - starts.getX()) , (ends.getY() - starts.getY()));

                    Point Pprime = lc.rightCanvas.get(lines).start,
                            Qprime = lc.rightCanvas.get(lines).end,
                            P = starts,
                            Q = lc.leftCanvas.get(lines).end;
                    // Vector PQ == p---->q ((q.x - p.x), (q.y - p.y))
                    Vector PQprime = new Vector(Pprime, Qprime),
                            XPprime = new Vector(Xprime, Pprime),
                            PXprime = new Vector(Pprime, Xprime);

                    // project m onto n over n
                    double distance = project(PQprime.getNormal(), XPprime); // proj XP onto N |
                    double fraction = project(PQprime, PXprime); // | proj PX onto PQ |
                    double percent = fractionalPercentage(fraction, PQprime);
                    // get the "point" (correct right here if only 1 line, else use a weighted average)
                    calculatedSrc[lines] = calculateSourcePoint(P, percent, distance, PQ);
                    weights[lines] = weight(distance);


                }

                // Now get the ACTUAl point based on the sum of the average
                Point srcPoint = sumWeights(Xprime, weights, calculatedSrc);
                // Now get the data and put it to the empty bitmap
                int outX = (int)srcPoint.getX(), outY = (int)srcPoint.getY();

                if(outX >= leftBm.getWidth())
                    outX = (leftBm.getWidth() - 1); // -1 ???
                else if(outX < 0)
                    outX = 0;
                // else stay how it is
                if(outY >= leftBm.getHeight())
                    outY = (leftBm.getHeight() - 1); // -1 ???
                else if(outY < 0)
                    outY = 0;
                // else stay how it is

                finalBmLeft.setPixel(x, y, leftBm.getPixel(outX, outY)); // error here?
            }
        }
        leftFinals[i - 1] = finalBmLeft;
    }

    /**
     * right image pixels are being copied to the position based on the lines drawn on the left image
     * Warping right (second) to left (first) lines
     */
    public void rightWarp(int i, int frames){
        finalBmRight = Bitmap.createBitmap(rightBm.getWidth(), rightBm.getHeight(), rightBm.getConfig());
        for(int x = 0; x < rightBm.getWidth(); x++){
            for(int y = 0; y < rightBm.getHeight(); y++){
                Point Xprime = new Point(x, y);
                Point[] calculatedSrc = new Point[lc.rightCanvas.size()];
                double[] weights = new double[lc.rightCanvas.size()];

                for(int lines = 0; lines < lc.rightCanvas.size(); lines++){

                    Point starts = interPoint(lc.rightCanvas.get(lines).start, lc.leftCanvas.get(lines).start, i, frames);
                    Point ends = interPoint(lc.rightCanvas.get(lines).end, lc.leftCanvas.get(lines).end, i, frames);
                    Vector PQ = new Vector((ends.getX() - starts.getX()) , (ends.getY() - starts.getY()));

                    Point Pprime = lc.leftCanvas.get(lines).start,
                            Qprime = lc.leftCanvas.get(lines).end,
                            P = starts,
                            Q = lc.rightCanvas.get(lines).end;
                    // Vector PQ == p---->q ((q.x - p.x), (q.y - p.y))
                    Vector PQprime = new Vector(Pprime, Qprime),
//                            PQ = new Vector(P, Q),
                            XPprime = new Vector(Xprime, Pprime),
                            PXprime = new Vector(Pprime, Xprime);

                    // project m onto n over n
                    double distance = project(PQprime.getNormal(), XPprime); // proj XP onto N |
                    double fraction = project(PQprime, PXprime); // | proj PX onto PQ |
                    double percent = fractionalPercentage(fraction, PQprime);
                    // get the "point" (correct right here if only 1 line, else use a weighted average)
                    calculatedSrc[lines] = calculateSourcePoint(P, percent, distance, PQ);
                    weights[lines] = weight(distance);
                }

                Point srcPoint = sumWeights(Xprime, weights, calculatedSrc);
                int outX = (int)srcPoint.getX(), outY = (int)srcPoint.getY();

                if(outX >= rightBm.getWidth())
                    outX = (rightBm.getWidth() - 1); // -1 ???
                else if(outX < 0)
                    outX = 0;
                if(outY >= rightBm.getHeight())
                    outY = (rightBm.getHeight() - 1); // -1 ???
                else if(outY < 0)
                    outY = 0;
                finalBmRight.setPixel(x, y, rightBm.getPixel(outX, outY)); // error here?
            }
        }
        rightFinals[i - 1] = finalBmRight;
    }

    /**
     * Projects the vector of M onto the vector of N
     */
    private double project(Vector n, Vector m){
        double top, bottom, d;
        top = calculateDot(n, m);
        bottom = Math.abs(calculateMagnitude(n));
        d = (top / bottom);
        return d;
    }

    /**
     * Calculates dot notation
     */
    private double calculateDot(Vector n, Vector m){
        return ((n.getX() * m.getX()) + (n.getY() * m.getY()));
    }

    /**
     * Calculates magnitude of passed in vector
     */
    private double calculateMagnitude(Vector v){
        return Math.sqrt((v.getX() * v.getX()) + (v.getY() * v.getY()));
    }

    // frac from fractionOnLine, x from line vector, y from line vector
    /**
     * Cacluates the fracitonal percentage of the fraction on the vector of n
     */
    private double fractionalPercentage(double frac, Vector n){
        double bottom, perc;
        bottom = Math.abs(calculateMagnitude(n));
        perc = frac / bottom;
        return perc;
    }

    /**
     * Calculates the source point of the x,y to be displayed
     */
    private Point calculateSourcePoint(Point P, double percent, double distance, Vector PQ){
        float Px, Py, tempPQx, tempPQy, tempNx, tempNy;
        double normalMagnitude = Math.abs(calculateMagnitude(PQ.getNormal()));
        tempPQx = (float)percent * PQ.getX();
        tempPQy = (float)percent * PQ.getY();
        tempNx = (float)distance * (PQ.getNormal().getX() / (float)normalMagnitude);
        tempNy = (float)distance * (PQ.getNormal().getY() / (float)normalMagnitude);
        Px = P.getX() + tempPQx;
        Py = P.getY() + tempPQy;
        Px = Px - tempNx;
        Py = Py - tempNy;
        return new Point(Px, Py);
    }

    /**
     * Calculates the weight of the distance
     */
    private double weight(double d){

        double weight, a = 0.01, b = 2;

        weight = Math.pow(((1) / (a + d)), b);
        return weight;
    }

    /**
     * Returns the delta point of the source - dest
     */
    private Point deltaPoint(Point src, Point dest){
        return new Point((src.getX() - dest.getX()), (src.getY() - dest.getY()));
    }

    /**
     * Returns the point over the sum of the weights
     */
    private Point sumWeights(Point Xprime, double[] weight, Point newPositions[]){
        // list of all the weights
        double totalWeight = 0, topX = 0, topY = 0;
        float outX, outY;
        for(int i = 0; i < newPositions.length; i++){
            Point changePoint = deltaPoint(Xprime, newPositions[i]);
            topX += changePoint.getX() * weight[i];
            topY += changePoint.getY() * weight[i];
            totalWeight += weight[i];
        }
        outX = (float)(topX / totalWeight);
        outY =  (float)(topY / totalWeight);

        outX += Xprime.getX();
        outY += Xprime.getY();

        return new Point( outX, outY );
    }

    /**
     * Returns the point of the intermediate frame's calculated point.
     */
    private Point interPoint(Point src, Point dest, int i, int max){
        int tempX, tempY;
        tempX = (int)(src.getX() + (((i + 1)/ (float)(max + 1)) * (dest.getX() - src.getX())));
        tempY = (int)(src.getY() + (((i + 1)/ (float)(max + 1)) * (dest.getY() - src.getY())));
        return new Point(tempX, tempY); // point on the line to create a vector
    }
}