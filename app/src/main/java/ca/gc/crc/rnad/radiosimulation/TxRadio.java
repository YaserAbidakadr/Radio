package ca.gc.crc.rnad.radiosimulation;
import java.util.Scanner;
import java.util.*;

public class TxRadio extends Radio implements Tx {

    public TxRadio(Place location) {
        this.location = location;

    }

    public Place getLocation() {
        return this.location;


    }

    public double setFrequency(double f) {
            this.frequency = f;
            return frequency;

        }

        public double setGain(float G) {

            this.gain = G;
            return gain;

        }

        public double setPower(double P) {
            this.power = P;
            return power;

        }



    }

