package com.bczovek.foxcatcherweb.service;

import com.bczovek.foxcatcherweb.exception.GameFinishedException;
import com.bczovek.foxcatcherweb.exception.GameFullException;
import com.bczovek.foxcatcherweb.exception.InvalidGameIdException;
import com.bczovek.foxcatcherweb.model.*;
import com.bczovek.foxcatcherweb.storage.GameStorage;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class GameService {

    private final int ID_LENGTH = 8;

    public Game createGame(Player player) {
        Game game = new Game();
        game.setGameId(RandomStringUtils.randomAlphanumeric(ID_LENGTH));
        game.setPlayer1(player);
        game.setFoxPosition(new Position(0, 2));
        ArrayList<Position> dogPositions = new ArrayList<>();
        for (int i = 1; i <= 7; i += 2) {
            dogPositions.add(new Position(7, i));
        }
        game.setDogsPosition(dogPositions);
        game.setStatus(GameStatus.STARTING);
        game.setPlayingSide(Side.DOGS);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game connectToGame(String playerName, String gameId) throws InvalidGameIdException, GameFullException {
        if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new InvalidGameIdException("Invalid Game ID");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);

        if (game.getPlayer2() != null) {
            throw new GameFullException("Game is full");
        }
        Player player2 = new Player();
        player2.setName(playerName);
        player2.setSide(game.getPlayer1().getSide().alter());
        game.setPlayer2(player2);
        game.setStatus(GameStatus.IN_PROGRESS);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game gamePlay(GamePlay gamePlay) throws InvalidGameIdException, GameFinishedException {
        if (!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
            throw new InvalidGameIdException("Invalid Game ID");
        }

        Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());
        if (game.getStatus().equals(GameStatus.FINISHED)) {
            throw new GameFinishedException("Game has been finished");
        }

        if(gamePlay.getSide().equals(game.getPlayingSide())) {
            switch (gamePlay.getSide()) {
                case DOGS -> {
                    int index = game.getDogsPosition().indexOf(gamePlay.getFrom());

                    game.getDogsPosition().set(index, gamePlay.getTo());
                    game.setPlayingSide(Side.DOGS.alter());

                }
                case FOX -> {
                    game.setFoxPosition(gamePlay.getTo());
                    game.setPlayingSide(Side.FOX.alter());
                }
            }

            checkForWinner(game);
            GameStorage.getInstance().setGame(game);
        }

        return game;
    }

    public List<Position> getValidFoxMoves(Position foxPosition, List<Position> dogsPosition){
        List<Position> validMoves = new ArrayList<>();
        for(var direction : FoxDirection.values()){
            if(Position.isValidPosition(foxPosition.moveTo(direction)) &&
                    !dogsPosition.contains(foxPosition.moveTo(direction))){
                validMoves.add(foxPosition.moveTo(direction));
            }
        }
        return validMoves;
    }

    public List<Position> getValidDogMoves(Position foxPosition, List<Position> dogsPosition, Position dogToMove){
        List<Position> validMoves = new ArrayList<>();
        for(var direction : DogDirection.values()){
            if(Position.isValidPosition(dogToMove.moveTo(direction)) &&
                    !dogsPosition.contains(dogToMove.moveTo(direction)) &&
                    !dogToMove.moveTo(direction).equals(foxPosition)){
                validMoves.add(dogToMove.moveTo(direction));
            }
        }
        return validMoves;
    }

    private void checkForWinner(Game game) {
        if (getValidFoxMoves(game.getFoxPosition(), game.getDogsPosition()).isEmpty()) {
            game.setWinner(game.getPlayer1().getSide().equals(Side.DOGS) ? game.getPlayer1() : game.getPlayer2());
            game.setStatus(GameStatus.FINISHED);
        }
        int lastDogRow = game.getDogsPosition().stream()
                .map(Position::getRow)
                .max(Integer::compareTo)
                .orElseThrow();
        if (game.getFoxPosition().getRow() >= lastDogRow) {
            game.setWinner(game.getPlayer1().getSide().equals(Side.FOX) ? game.getPlayer1() : game.getPlayer2());
            game.setStatus(GameStatus.FINISHED);
        }
    }

    public Game disconnect(Player player, String gameId) throws InvalidGameIdException, GameFinishedException {
        if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new InvalidGameIdException("Invalid Game ID");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);

        if (game.getStatus().equals(GameStatus.FINISHED)) {
            throw new GameFinishedException("Game has been finished");
        }

        if(game.getPlayer2() == null){
            GameStorage.getInstance().deleteGame(gameId);
            game.setStatus(GameStatus.FINISHED);
            return game;
        }

        if(game.getPlayer1().equals(player)){
            game.setWinner(game.getPlayer2());
            game.setStatus(GameStatus.FINISHED);
        }
        if(game.getPlayer2().equals(player)){
            game.setWinner(game.getPlayer1());
            game.setStatus(GameStatus.FINISHED);
        }

        GameStorage.getInstance().setGame(game);

        return game;
    }
}
