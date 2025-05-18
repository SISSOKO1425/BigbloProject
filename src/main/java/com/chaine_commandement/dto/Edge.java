package com.chaine_commandement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Edge {
    String id;
    int source;
    int target;
    public Edge(int source, int target) {
        this.id = "e" + source + "-" + target;
        this.source = source;
        this.target = target;
    }
}
