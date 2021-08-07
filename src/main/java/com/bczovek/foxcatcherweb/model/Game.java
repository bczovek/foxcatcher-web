package com.bczovek.foxcatcherweb.model;

import lombok.Data;

import java.util.List;

@Data
public class Game {

    private String gameId;
    private Player player1;
    private Player player2;
    private GameStatus status;
    private List<Position> dogsPosition;
    private Position foxPosition;
    private Side playingSide;
    private Player winner;
}
