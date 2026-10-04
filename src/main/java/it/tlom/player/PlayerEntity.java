package it.tlom.player;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class PlayerEntity {
    @Id
    String id;
    @Column(name = "display_name") String displayName;
    String locale;
    @Column(name = "avatar_body") String avatarBody;
    @Column(name = "avatar_eyes") String avatarEyes;
    @Column(name = "avatar_top") String avatarTop;
    @Column(name = "avatar_hair") String avatarHair;
    String revision;

    protected PlayerEntity() { }

    PlayerEntity(Player player) {
        id = player.id();
        displayName = player.displayName();
        locale = player.locale();
        avatarBody = player.avatar().body();
        avatarEyes = player.avatar().eyes();
        avatarTop = player.avatar().top();
        avatarHair = player.avatar().hair();
        revision = player.revision();
    }

    Player toModel() {
        return new Player(id, displayName, locale, new Player.Avatar(avatarBody, avatarEyes, avatarTop, avatarHair), revision);
    }
}