package com.bczovek.foxcatcherweb.model;

import lombok.Data;

@Data
public class GamePlay {

    private String gameId;
    private Position from;
    private Position to;
    private Side side;

}
