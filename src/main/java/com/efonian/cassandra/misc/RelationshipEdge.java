package com.efonian.cassandra.misc;

import org.jgrapht.graph.DefaultEdge;

// Source: https://jgrapht.org/guide/LabeledEdges
//         https://github.com/jgrapht/jgrapht/wiki/Users%3A-EqualsAndHashCode
public class RelationshipEdge extends DefaultEdge {
    private static final long serialVersionUID = 3258408452177932855L;
    
    private String label;
    
    /**
     * Constructs a relationship edge
     *
     * @param label the label of the new edge.
     *
     */
    public RelationshipEdge(String label) {
        this.label = label;
    }
    
    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     */
    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return getLabel();
    }
}
