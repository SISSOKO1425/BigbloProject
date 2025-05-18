package com.chaine_commandement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GraphNode {
    int id;
    int x, y;
    String label;
    String RM;
    List<String> personnes;
}
