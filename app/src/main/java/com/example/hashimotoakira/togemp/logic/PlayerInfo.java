package com.example.hashimotoakira.togemp.logic;

import java.util.ArrayList;
import java.util.List;

// 親が管理する、各プレイヤーの情報
public class PlayerInfo {

    public String id; // プレイヤーのID、親ならparent
    public int position; // プレイヤーの位置（＝順番）、1始まり
    public int cardCount; // プレイヤーの手札枚数
    public List<Card> initialHands; // プレイヤーの初期手札

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public void createEmptyHands(){
        this.initialHands = new ArrayList<>();
    }

    public List<Card> getInitialHands() {
        return initialHands;
    }

    public void draw(Deck deck){
        initialHands.add(deck.draw());
    }

    PlayerInfo(String id) {
        this.id = id;
    }
}
