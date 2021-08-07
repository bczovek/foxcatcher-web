package com.bczovek.foxcatcherweb.controller.dto;

import com.bczovek.foxcatcherweb.model.Player;
import lombok.Data;

@Data
public class DisconnectRequest {

    private Player player;
    private String gameId;
}
