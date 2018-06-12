package com.myappcompany.isaac.dealday.Enums;

/**
 * Created by isaac on 15/04/18.
 */

public enum Platform {

    PELANDO("pelando"),
    PROMOBIT("promobit"),
    HARDMOB("hardmob"),
    ADRENALINE("adrenaline"),
    PROMOFORUM("promoforum"),
    MELHORES_DESTINOS("melhores_destinos"),
    PASSAGENS_IMPERDIVEIS("passagens_imperdiveis");


    private String nome;
    private int checked;

    Platform(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}
