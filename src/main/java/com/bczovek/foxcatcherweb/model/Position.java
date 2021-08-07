package com.bczovek.foxcatcherweb.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {

    private int row;
    private int col;


    public Position moveTo(Direction direction){
        return new Position(row+ direction.getRowChange(), col+direction.getColChange());
    }

    public static boolean isValidPosition(Position position){
        return position.getRow() >= 0 && position.getRow() <= 7 && position.getCol() >= 0 && position.getCol() <= 7;
    }

}
