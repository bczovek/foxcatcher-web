package com.bczovek.foxcatcherweb.controller;

import com.bczovek.foxcatcherweb.controller.dto.ConnectRequest;
import com.bczovek.foxcatcherweb.controller.dto.DisconnectRequest;
import com.bczovek.foxcatcherweb.exception.GameFinishedException;
import com.bczovek.foxcatcherweb.exception.GameFullException;
import com.bczovek.foxcatcherweb.exception.InvalidGameIdException;
import com.bczovek.foxcatcherweb.model.PositionSelect;
import com.bczovek.foxcatcherweb.model.Game;
import com.bczovek.foxcatcherweb.model.GamePlay;
import com.bczovek.foxcatcherweb.model.Player;
import com.bczovek.foxcatcherweb.model.Position;
import com.bczovek.foxcatcherweb.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/start")
    public ResponseEntity<Game> start(@RequestBody Player player){
        return ResponseEntity.ok(gameService.createGame(player));
    }

    @PostMapping("/connect")
    public ResponseEntity<Game> connect(@RequestBody ConnectRequest request){

        try {
            Game game = gameService.connectToGame(request.getName(), request.getGameId());
            simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);

            return ResponseEntity.ok(game);

        } catch (GameFullException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is full", e);
        } catch (InvalidGameIdException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game with this ID doesn't exist", e);
        }
    }

    @PostMapping("/gameplay")
    public ResponseEntity<Game> gamePlay(@RequestBody GamePlay request){

        try {
            Game game = gameService.gamePlay(request);
            simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
            return ResponseEntity.ok(game);
        } catch (GameFinishedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has been finished", e);
        } catch (InvalidGameIdException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game with this ID doesn't exist", e);
        }
    }

    @PostMapping("/select")
    public ResponseEntity<List<Position>> getValidMoves(@RequestBody PositionSelect positionSelect){

        switch (positionSelect.getSide()){
            case FOX -> {
                return ResponseEntity
                        .ok(gameService.getValidFoxMoves(positionSelect.getFoxPosition(),
                                positionSelect.getDogsPosition()));
            }
            case DOGS -> {
                return ResponseEntity.ok(gameService.getValidDogMoves(positionSelect.getFoxPosition(),
                        positionSelect.getDogsPosition(),positionSelect.getSelected()));
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Game> disconnect(@RequestBody DisconnectRequest request) {
        try {
            Game game = gameService.disconnect(request.getPlayer(), request.getGameId());
            simpMessagingTemplate.convertAndSend("/topic/game-progress/" + request.getGameId(), game);

            return ResponseEntity.ok(game);
        } catch (GameFinishedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has been finished", e);
        } catch (InvalidGameIdException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game with this ID doesn't exist", e);
        }
    }
}
