package com.nobass.server.enums;

/**
 * Enumération représentant les types de listes de films d'un utilisateur.
 * <ul>
 *   <li>{@link #WATCHED} : Liste des films vus</li>
 *   <li>{@link #WATCHLIST} : Liste des films à voir</li>
 * </ul>
 */
public enum ListType {
    /** Liste des films vus par l'utilisateur */
    WATCHED,
    /** Liste des films à voir (watchlist) */
    WATCHLIST
}
