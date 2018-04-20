package com.niksoftware.snapseed.util;

public class Rational {
    public Integer denominator;
    public Integer numerator;

    public Rational() {
        this.numerator = Integer.valueOf(0);
        this.denominator = Integer.valueOf(1);
    }

    public Rational(Integer numerator, Integer denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public String toString() {
        int i = 0;
        String str = "%d/%d";
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(this.numerator != null ? this.numerator.intValue() : 0);
        if (this.denominator != null) {
            i = this.denominator.intValue();
        }
        objArr[1] = Integer.valueOf(i);
        return String.format(str, objArr);
    }

    public double toDouble() {
        if (this.denominator.intValue() != 0) {
            return ((double) this.numerator.intValue()) / ((double) this.denominator.intValue());
        }
        return 0.0d;
    }

    public float toFloat() {
        if (this.denominator.intValue() != 0) {
            return ((float) this.numerator.intValue()) / ((float) this.denominator.intValue());
        }
        return 0.0f;
    }
}
