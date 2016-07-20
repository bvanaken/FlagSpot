package de.beuth.bva.flagspot.model;

import java.io.Serializable;

/**
 * Created by betty on 07/07/16.
 */
public class Flag implements Serializable {

    static final long serialVersionUID = 1006920636116702075L;

    double[] vector;
    String name;

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
