package ca.gc.crc.rnad.radiosimulation;
import java.util.Scanner;

public  class RxRadio extends Radio implements Rx{

    public RxRadio(Place location) {
this.location = location;
    }

    public Place getLocation() {
        return this.location;

    }


    public float setGain(float gain) {
        this.gain = gain;
        return gain;

    }
       // gainT and gainR are in dBi
       // transmitted power and received power are in  dBm
       // frequency in Ghz
       //
    public double powerReceived(double transmittedPower,float gainT, float gainR, double d, double f){
         double e = Math.pow(10,8);
         transmittedPower = transmittedPower/10;
         transmittedPower= (Math.pow(10,transmittedPower));
         transmittedPower = transmittedPower/1000;
          double pR= -10 * Math.log10(1000*transmittedPower)+10* Math.log10(gainT * gainR)+ 10* Math.log10 (((3)/(4*3.14*f*10*d))) + 10* Math.log10 (((3)/(4*3.14*10*f*d)));
        return pR;

    }

    public double getDistance(Place A, Place B) {

        double distance = Math.sqrt(((A.x-B.x) * (A.x - B.x) + (A.y-B.y) * (A.y-B.y) + (A.z- B.z) * (A.z-B.z)));
        if (distance < 0) {
            distance = -1 * distance;

        }
        distance = distance * Math.pow(10,-3);
        return distance;

    }

    }


