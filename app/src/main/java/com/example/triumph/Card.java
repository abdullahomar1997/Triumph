package com.example.triumph;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class Card {

    public int cardValue;
    public String cardName;
    public String cardImageCode;

    public Card(String cardName, int cardValue, String cardImageCode) {
        this.cardValue = cardValue;
        this.cardName = cardName;
        this.cardImageCode = cardImageCode;
    }

    public String getCardSign(){
        String[] card = cardName.split(" ");
        return card[2];
    }

    public String getCardNumber(){
        String[] card = cardName.split(" ");
        return card[0];
    }

    @Override
    public String toString() {
        return cardName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return cardName.equals(card.cardName);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(cardName);
    }

}
