package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/9/17.
 */
public class CardBalance {

    private int type ;
    private int card ;
    private int score ;
    private Set<Integer> targetId = new HashSet<>();
    private List<Integer> cards = new ArrayList<>();

    public CardBalance(){
    }

    public CardBalance(int type, int card, int score) {
        this.type = type;
        this.card = card;
        this.score = score;
    }

    public CardBalance(int type, List<Integer> cards, int score) {
        this.type = type;
        this.cards = cards;
        this.score = score;
    }

    public int getType() {
        return type;
    }

    public Set<Integer> getTargetId() {
        return targetId;
    }

    public void setTargetId(Set<Integer> targetId) {
        this.targetId = targetId;
    }

    public void addTargetId(int targetId) {
        this.targetId.add(targetId);
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();

        obj.putInt("t",type);
        obj.putInt("c",card);

        obj.putIntArray("cs",cards);
        obj.putInt("s",score);
        obj.putIntArray("tid",targetId);
        if(cards.size() > 0){
            obj.putIntArray("cds", cards);
        }

        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("t=").append(type).append(",");
        sb.append("c=").append(card).append(",");
        sb.append("s=").append(score).append(",");
        if(!targetId.isEmpty()){
            sb.append("tid={").append(StringUtils.join(targetId, ",")).append("}").append(",");
        }

        if(cards.size() > 0){
            sb.append("cds={").append(StringUtils.join(cards, ",")).append("}");
        }
        sb.append("}");
        return sb.toString();
    }

}