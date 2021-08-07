package com.bczovek.foxcatcherweb.model;

public enum Side {

    FOX,
    DOGS;

    public Side alter() {
        return switch(this){
            case DOGS -> FOX;
            case FOX -> DOGS;
        };
    }
}
