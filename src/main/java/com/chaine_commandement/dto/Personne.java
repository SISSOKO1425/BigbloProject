package com.chaine_commandement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Personne {
    private String mle;
    private String prenoms;
    private String nom;
    private String grade;
    private String unite;
    private byte[] photo;
    private String reference;
    private String date;
}
