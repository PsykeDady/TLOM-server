package it.tlom.model;

public record Player(String id, String displayName, String locale, Avatar avatar, String revision) {
    public record Avatar(String body, String eyes, String top, String hair) { }
}