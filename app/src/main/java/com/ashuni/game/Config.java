package com.ashuni.game;

public final class Config {
    // Adresse de TON serveur (HTTPS obligatoire, sans "/" à la fin)
    public static final String API_URL = "https://TON-SERVEUR.com";

    // Mettre true seulement si tu arrives à appeler window.AshuniSocial.* depuis le jeu
    // (voir LISEZ-MOI.md). Active les succès liés aux pièces / au score.
    public static final boolean GAME_HOOKS = false;

    private Config() {}
}
