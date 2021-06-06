package com.example.triumph.Comparators;

import com.example.triumph.Card;

import java.util.Comparator;

public class CardsAscendingComprator implements Comparator<Card> {
  @Override
  public int compare(Card o1, Card o2) {
    return Integer.compare(o2.cardValue, o1.cardValue)*-1;
  }
}
