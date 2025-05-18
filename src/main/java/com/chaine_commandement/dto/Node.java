package com.chaine_commandement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Node {
    int id;
    String nom;
    String RM;
    List<String> personnes;
    List<Node> sousFonctions;
}
