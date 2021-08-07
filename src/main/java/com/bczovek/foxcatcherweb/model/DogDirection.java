package com.bczovek.foxcatcherweb.model;

public enum DogDirection implements Direction{

    LEFT_UP(-1,-1),
    RIGHT_UP(-1,1);

    private final int rowChange;
    private final int colChange;

    DogDirection(int rowChange, int colChange){
        this.rowChange = rowChange;
        this.colChange = colChange;
    }

    @Override
    public int getRowChange() {
        return rowChange;
    }

    @Override
    public int getColChange() {
        return colChange;
    }

    public static DogDirection of(int rowChange, int colChange) {
        for (var direction : values()) {
            if (direction.rowChange == rowChange && direction.colChange == colChange) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Illegal DogDirection: ("+String.valueOf(rowChange)+", "+String.valueOf(colChange)+")");
    }

}
