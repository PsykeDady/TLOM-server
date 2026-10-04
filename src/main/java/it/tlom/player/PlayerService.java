package it.tlom.player;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PlayerService {
    @Inject PlayerRepository repository;
    private static final Player DEVELOPMENT_PLAYER = new Player(
            "player-psyke", "PsykeDady", "it",
            new Player.Avatar("body-default", "eyes-violet", "top-cyan", "hair-pink"), "1");

    public Player currentPlayer() { return repository.findOrCreate(DEVELOPMENT_PLAYER); }

    public void lockCurrentPlayer() { repository.lock(DEVELOPMENT_PLAYER.id()); }
}