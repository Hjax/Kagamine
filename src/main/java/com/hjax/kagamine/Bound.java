package com.hjax.kagamine;

public class Bound {
    public int frame = -1;
    public boolean exact = false;
    public Bound(int i, boolean b) {
        frame = i;
        exact = b;
    }

    public Bound update(int i, boolean b) {
        if (i < frame) {
            return new Bound(i, b);
        }
        return this;
    }
}
