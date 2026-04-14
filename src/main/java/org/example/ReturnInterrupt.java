package org.example;

public class ReturnInterrupt extends RuntimeException {

    private final int value;

    public ReturnInterrupt(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
