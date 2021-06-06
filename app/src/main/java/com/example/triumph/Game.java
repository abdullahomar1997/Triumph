package com.example.triumph;

import android.util.Log;

import com.example.triumph.Comparators.CardsAscendingComprator;
import com.example.triumph.Comparators.CardsDescendingCompartor;

import java.util.*;

import static android.content.ContentValues.TAG;

public class Game {

    List<Player> players = new ArrayList<>();
    static Stack<Card> deck = new Stack<>();
    int numPlayers;
    static String triumphCard = "Spades";
    public boolean youWin = true;

    public Game(String Players,String game) {

        String[] player = Players.split(";");

        Player player1 = convertStringPlayerToPlayer(player[0]);
        players.add(player1);

        Player player2 = convertStringPlayerToPlayer(player[1]);
        players.add(player2);

        setGame(game);
    }

    public static List<Player> getTwoDecks() {

        List<Player> players = new ArrayList<>();

        createAndShuffleDeck();

        for (int noPlayersIndex = 0; noPlayersIndex < 2; ++noPlayersIndex) {
            Player player = new Player("Player " + noPlayersIndex, "playerAvatar");
            players.add(player);
        }

        for (int i = 0; i < 2; ++i) {
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
        }
        return players;
    }

    private Player convertStringPlayerToPlayer(String players) {

        String[] playerInfo = players.split(":");

        String playerUsername = playerInfo[0];
        int playerScore = Integer.parseInt(playerInfo[1]);
        String playerAvatar = playerInfo[2];
        List<Card> cards = converstStringToList(playerInfo[3]);
        Card playedCard = getCard(playerInfo[4]);

        return new Player(playerUsername,playerScore,playerAvatar,cards,playedCard);
    }

    public static Card getCard(String cardName) {

        if(cardName.equals("")){
            return null;
        }

        List<Card> deckc = new ArrayList<>();

        deckc.add(new Card("Ace of Spades", 11, "a s"));
        deckc.add(new Card("King of Spades", 4, "k s"));
        deckc.add(new Card("Queen of Spades", 2, "q s"));
        deckc.add(new Card("Jack of Spades", 3, "j s"));
        deckc.add(new Card("Seven of Spades", 10, "7 s"));
        deckc.add(new Card("Six of Spades", 0, "6 s"));

        deckc.add(new Card("Ace of Hearts", 11, "a h"));
        deckc.add(new Card("King of Hearts", 4, "k h"));
        deckc.add(new Card("Queen of Hearts", 2, "q h"));
        deckc.add(new Card("Jack of Hearts", 3, "j h"));
        deckc.add(new Card("Seven of Hearts", 10, "7 h"));
        deckc.add(new Card("Six of Hearts", 0, "6 h"));

        deckc.add(new Card("Ace of Flowers", 11, "a f"));
        deckc.add(new Card("King of Flowers", 4, "k f"));
        deckc.add(new Card("Queen of Flowers", 2, "q f"));
        deckc.add(new Card("Jack of Flowers", 3, "j f"));
        deckc.add(new Card("Seven of Flowers", 10, "7 f"));
        deckc.add(new Card("Six of Flowers", 0, "6 f"));

        deckc.add(new Card("Ace of Diamonds", 11, "a d"));
        deckc.add(new Card("King of Diamonds", 4, "k d"));
        deckc.add(new Card("Queen of Diamonds", 2, "q d"));
        deckc.add(new Card("Jack of Diamonds", 3, "j d"));
        deckc.add(new Card("Seven of Diamonds", 10, "7 d"));
        deckc.add(new Card("Six of Diamonds", 0, "6 d"));

        for(int i = 0;i < deckc.size();++i){
            if(deckc.get(i).cardName.trim().equals(cardName.trim())){
                return deckc.get(i);
            }
        }
        return null;
    }

    public static List<Card> converstStringToList(String s) {

        List<Card> deck = new ArrayList<>();

        String[] cc = s.split(",");

        for (String value : cc) {
            deck.add(getCard(value));
        }
        return deck;
    }

    private void setGame(String game) {

        String[] gg = game.split(":");


        //System.out.println(gg.length);

        Log.d(TAG," adding data " + gg.length);


        if(gg.length == 3) {
            converstStringToStack(gg[2]);
        }

        this.triumphCard = gg[0];

        if(gg[1].equals("true")){
            youWin = true;
        }
        else if(gg[1].equals("false")){
            youWin = false;
        }
    }

    private void converstStringToStack(String s) {

        Stack<Card> cards = new Stack<>();

        String[] gg = s.split(",");

        for (String value : gg) {
            deck.add(getCard(value));
        }

    }

    public Game(int numPlayers,String triumphCard, String difficultyLevel) {

        this.numPlayers = numPlayers;
        this.triumphCard = triumphCard;

        createPlayers();
        createAndShuffleDeck();
        distributeCardsToPlayers();

    }

    public void createPlayers() {
        for (int noPlayersIndex = 0; noPlayersIndex < numPlayers; ++noPlayersIndex) {
            Player player = new Player("Player " + noPlayersIndex, "playerAvatar");
            players.add(player);
        }
    }

    public static void createAndShuffleDeck() {

        deck.add(new Card("Ace of Spades", 11, "a s"));
        deck.add(new Card("King of Spades", 4, "k s"));
        deck.add(new Card("Queen of Spades", 2, "q s"));
        deck.add(new Card("Jack of Spades", 3, "j s"));
        deck.add(new Card("Seven of Spades", 10, "7 s"));
        deck.add(new Card("Six of Spades", 0, "6 s"));

        deck.add(new Card("Ace of Hearts", 11, "a h"));
        deck.add(new Card("King of Hearts", 4, "k h"));
        deck.add(new Card("Queen of Hearts", 2, "q h"));
        deck.add(new Card("Jack of Hearts", 3, "j h"));
        deck.add(new Card("Seven of Hearts", 10, "7 h"));
        deck.add(new Card("Six of Hearts", 0, "6 h"));

        deck.add(new Card("Ace of Flowers", 11, "a f"));
        deck.add(new Card("King of Flowers", 4, "k f"));
        deck.add(new Card("Queen of Flowers", 2, "q f"));
        deck.add(new Card("Jack of Flowers", 3, "j f"));
        deck.add(new Card("Seven of Flowers", 10, "7 f"));
        deck.add(new Card("Six of Flowers", 0, "6 f"));

        deck.add(new Card("Ace of Diamonds", 11, "a d"));
        deck.add(new Card("King of Diamonds", 4, "k d"));
        deck.add(new Card("Queen of Diamonds", 2, "q d"));
        deck.add(new Card("Jack of Diamonds", 3, "j d"));
        deck.add(new Card("Seven of Diamonds", 10, "7 d"));
        deck.add(new Card("Six of Diamonds", 0, "6 d"));

        Collections.shuffle(deck);
    }

    public void distributeCardsToPlayers() {

        for (int i = 0; i < 2; ++i) {
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
            players.get(i).cards.add(giveOutCard());
        }
    }

    public static Card giveOutCard() {
        return deck.pop();
    }

    public boolean isGameOver() {
        return players.get(0).cards.isEmpty() && players.get(1).cards.isEmpty() ;
    }

    public Card cpuPlayFirstAutomatically(Player player) {

        System.out.println("player 2 cards = " + player.cards);

        ArrayList<Card> triumphCardsInDescendingOrder = getTriumphCardsInDescendingOrder(player);

        ArrayList<Card> nonTriumphCardsInAscendingOrder = getNonTriumphCardsInAscendingOrder(player);

        if (!nonTriumphCardsInAscendingOrder.isEmpty()) {
            return Play(player,nonTriumphCardsInAscendingOrder.get(0));
        }

        return Play(player,triumphCardsInDescendingOrder.get(0));
    }

    public ArrayList<Card> getCardsThatMatchCardInAscendingOrder(Player player, Card playedCard) {

        ArrayList<Card> cardsList = new ArrayList<>();

        for (Card card : player.cards) {
            if (card.getCardSign().equals(playedCard.getCardSign())) {
                cardsList.add(card);
            }
        }

        Collections.sort(cardsList, new CardsAscendingComprator());

        return cardsList;
    }

    public ArrayList<Card> getCardsThatMatchCardInDescendingOrder(Player player, Card playedCard) {

        ArrayList<Card> cardsList = new ArrayList<>();

        for (Card card : player.cards) {
            if (card.getCardSign().equals(playedCard.getCardSign())) {
                cardsList.add(card);
            }
        }

        Collections.sort(cardsList, new CardsDescendingCompartor());

        return cardsList;
    }

    public ArrayList<Card> getNonTriumphCardsInAscendingOrder(Player player) {

        ArrayList<Card> cardsList = new ArrayList<>();

        for (Card card : player.cards) {
            if (!card.getCardSign().equals(triumphCard)) {
                cardsList.add(card);
            }
        }

        Collections.sort(cardsList, new CardsAscendingComprator());

        return cardsList;
    }

    public ArrayList<Card> getTriumphCardsInAscendingOrder(Player player) {

        ArrayList<Card> cardsList = new ArrayList<>();

        for (Card card : player.cards) {
            if (card.getCardSign().equals(triumphCard)) {
                cardsList.add(card);
            }
        }

        Collections.sort(cardsList, new CardsAscendingComprator());

        return cardsList;
    }

    public ArrayList<Card> getTriumphCardsInDescendingOrder(Player player) {

        ArrayList<Card> cardsList = new ArrayList<>();

        for (Card card : player.cards) {
            if (card.getCardSign().equals(triumphCard)) {
                cardsList.add(card);
            }
        }

        Collections.sort(cardsList, new CardsDescendingCompartor());

        return cardsList;
    }

    private boolean sevenPlayed(Card playedCard) {
        return playedCard.getCardNumber().equals("Seven");
    }

    private boolean acePlayed(Card playedCard) {
        return playedCard.getCardNumber().equals("Ace");
    }

    public Card Play(Player player, Card card) {
        player.removeFromDeck(card);
        return card;
    }

    private boolean isTriumphCard(Card playedCard) {
        return playedCard.getCardSign().equals(triumphCard);
    }

    public Card cpuPlaySecondAutomatically(Player player, Card playedCard) {

        System.out.println("player 2 cards = " + player.cards);

        ArrayList<Card> triumphCardsInAscendingOrder = getTriumphCardsInAscendingOrder(player);

        ArrayList<Card> triumphCardsInDescendingOrder = getTriumphCardsInDescendingOrder(player);

        ArrayList<Card> cardsThatMatchCardInAscendingOrder = getCardsThatMatchCardInAscendingOrder(player, playedCard);

        ArrayList<Card> cardsThatMatchCardInDescendingOrder = getCardsThatMatchCardInDescendingOrder(player, playedCard);

        ArrayList<Card> nonTriumphCardsInAscendingOrder = getNonTriumphCardsInAscendingOrder(player);

        if (!isTriumphCard(playedCard)) {

            if (acePlayed(playedCard)) {

                if (!triumphCardsInAscendingOrder.isEmpty()) {
                    return Play(player, triumphCardsInAscendingOrder.get(0));
                }
            }

            if (sevenPlayed(playedCard)) {

                if (!cardsThatMatchCardInDescendingOrder.isEmpty()) {
                    if (cardsThatMatchCardInDescendingOrder.get(0).cardValue > playedCard.cardValue) {
                        return Play(player, cardsThatMatchCardInDescendingOrder.get(0));
                    }
                }

                if (!triumphCardsInDescendingOrder.isEmpty()) {
                    return Play(player, triumphCardsInDescendingOrder.get(0));
                }
            }

            if (!cardsThatMatchCardInAscendingOrder.isEmpty()) {
                return Play(player, cardsThatMatchCardInAscendingOrder.get(0));
            }

            else{
                return Play(player, player.cards.get(0));
            }
        }

        else {
            if (!nonTriumphCardsInAscendingOrder.isEmpty()) {
                return Play(player, nonTriumphCardsInAscendingOrder.get(0));
            }

            if (!triumphCardsInAscendingOrder.isEmpty()) {
                return Play(player, triumphCardsInAscendingOrder.get(0));
            }
        }
        return Play(player,player.cards.get(0));
    }

    public static boolean determineWinnerOnline(Card playedCard, Card cpuPlayedCard, int turn) {

        boolean youWin = false;

        if (playedCard.getCardSign().equals(triumphCard) && cpuPlayedCard.getCardSign().equals(triumphCard)) {
            if (playedCard.cardValue > cpuPlayedCard.cardValue) {
                youWin = true;
            }
        }

        else if (playedCard.getCardSign().equals(triumphCard) && !cpuPlayedCard.getCardSign().equals(triumphCard)) {
            youWin = true;
        }

        else if (!playedCard.getCardSign().equals(triumphCard) && cpuPlayedCard.getCardSign().equals(triumphCard)) {
            youWin = false;
        }

        else if (turn == 1) {

            if (playedCard.getCardSign().equals(cpuPlayedCard.getCardSign())) {

                if (playedCard.cardValue > cpuPlayedCard.cardValue) {
                    youWin = true;
                }
            }

            if (!playedCard.getCardSign().equals(cpuPlayedCard.getCardSign())) {
                youWin = true;
            }
        }

        else if (turn == 2) {

            if (playedCard.getCardSign().equals(cpuPlayedCard.getCardSign())) {

                if (playedCard.cardValue > cpuPlayedCard.cardValue) {
                    youWin = true;
                }
            }
        }
        return youWin;
    }


    public void determineWinner(Player player, Player cpuPlayer, int turn) {

        Card playedCard = player.playedCard;
        Card cpuPlayedCard = cpuPlayer.playedCard;

        youWin = false;

        if (playedCard.getCardSign().equals(triumphCard) && cpuPlayedCard.getCardSign().equals(triumphCard)) {
            if (playedCard.cardValue > cpuPlayedCard.cardValue) {
                youWin = true;
            }
        }

        else if (playedCard.getCardSign().equals(triumphCard) && !cpuPlayedCard.getCardSign().equals(triumphCard)) {
            youWin = true;
        }

        else if (!playedCard.getCardSign().equals(triumphCard) && cpuPlayedCard.getCardSign().equals(triumphCard)) {
            youWin = false;
        }

        else if (turn == 1) {

            if (playedCard.getCardSign().equals(cpuPlayedCard.getCardSign())) {

                if (playedCard.cardValue > cpuPlayedCard.cardValue) {
                    youWin = true;
                }
            }

            if (!playedCard.getCardSign().equals(cpuPlayedCard.getCardSign())) {
                youWin = true;
            }
        }

        else if (turn == 2) {

            if (playedCard.getCardSign().equals(cpuPlayedCard.getCardSign())) {

                if (playedCard.cardValue > cpuPlayedCard.cardValue) {
                    youWin = true;
                }
            }
        }

        if(youWin){

            System.out.println("You Wins");
            player.playerScore += (playedCard.cardValue + cpuPlayedCard.cardValue);

            if(deck.size() != 0) {

                player.cards.add(giveOutCard());
                cpuPlayer.cards.add(giveOutCard());
            }
        }
        else{

            cpuPlayer.playerScore += (playedCard.cardValue + cpuPlayedCard.cardValue);
            System.out.println("Cpu Wins");

            if(deck.size() != 0) {
                cpuPlayer.cards.add(giveOutCard());
                player.cards.add(giveOutCard());
            }
        }
    }

    public void PlayFirst(Player player1, Player player2, Card playedCard) {

        player2.playedCard = cpuPlaySecondAutomatically(player2,playedCard);

        determineWinner(player1,player2,1);

        Play(player1, playedCard);
    }

    public void PlaySecond(Player player1, Player player2, Card playedCard) {

        determineWinner(player1,player2,2);

        Play(player1, playedCard);
    }

    public void PlayFirstOnline(Player player1, Player player2, Card playedCard) {
 
        //player2.playedCard = cpuPlaySecondAutomatically(player2,playedCard);

        determineWinner(player1,player2,1);

        Play(player1, playedCard);
    }

    public void PlaySecondOnline(Player player1, Player player2, Card playedCard) {

        determineWinner(player1,player2,2);

        Play(player1, playedCard);
    }

    public String getTriumphCardImage(){

        switch (triumphCard) {
            case "Hearts":
                return "t h";
            case "Spades":
                return "t s";
            case "Flowers":
                return "t f";
            default:
                return "t d";
        }
    }

    public void switchTriumphCard() {

        switch (triumphCard) {
            case "Spades":
                triumphCard = "Hearts";
                break;
            case "Hearts":
                triumphCard = "Flowers";
                break;
            case "Flowers":
                triumphCard = "Diamonds";
                break;
            case "Diamonds":
                triumphCard = "Spades";
                break;
        }
    }

    public String getMatchOutcome(){

        if (players.get(0).playerScore > 60){
            return "You Win";
        }
        else if (players.get(0).playerScore == 60){
            return "Draw";
        }
        else {
            return "You Lose";
        }
    }

    @Override
    public String toString() {
        return triumphCard+":"+youWin+":"+deck.toString();
    }

}