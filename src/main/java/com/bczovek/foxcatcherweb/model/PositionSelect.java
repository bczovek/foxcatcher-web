package com.bczovek.foxcatcherweb.model;

import com.bczovek.foxcatcherweb.model.Position;
import com.bczovek.foxcatcherweb.model.Side;
import lombok.Data;

import java.util.List;

@Data
public class PositionSelect {

    List<Position> dogsPosition;
    Position foxPosition;
    Position selected;
    Side side;

}
