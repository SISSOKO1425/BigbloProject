package com.chaine_commandement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Fonction {
    private int id;
    private String nom;
    private String RM;
    private List<Personne> personnes = new ArrayList<>();
    private List<Fonction> sous_fonctions = new ArrayList<>();
}
