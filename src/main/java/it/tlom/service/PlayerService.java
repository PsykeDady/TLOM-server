package it.tlom.service;

import it.tlom.model.Player;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlayerService {
    private static final Player DEVELOPMENT_PLAYER = new Player(
            "player-psyke", "PsykeDady", "it",
            new Player.Avatar("body-default", "eyes-violet", "top-cyan", "hair-pink"), "1");

    public Player currentPlayer() { return DEVELOPMENT_PLAYER; }
}