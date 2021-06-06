package com.example.triumph;

import java.util.ArrayList;
import java.util.List;

public class Player {

    String playerUsername;
    String playerImage;
    String playerEmail;
    int playerScore = 0;
    List<Card> cards = new ArrayList<>();
    Card playedCard;

    public Player(String playerUsername, String playerImage) {
        this.playerUsername = playerUsername;
        this.playerImage = playerImage;
    }

    public Player(String playerUsername, int playerScore, String playerImage, List<Card> cards, Card playedCard) {
        this.playerUsername = playerUsername;
        this.playerScore = playerScore;
        this.playerImage = playerImage;
        this.cards = cards;
        this.playedCard = playedCard;
    }

    public void removeFromDeck(Card card) {
        cards.remove(card);
    }

    @Override
    public String toString() {
        return playerUsername+":"+playerScore+":"+ playerImage + ":"+cards.toString() + ":"+playedCard;
    }

}
