package com.efonian.cassandra.misc;

import java.awt.*;

public class ColouredVertex<T> {
    private final T vertex;
    private final Color color;
    
    public ColouredVertex(T vertex, Color color) {
        this.vertex = vertex;
        this.color = color;
    }
    
    public T getVertex() {
        return vertex;
    }
    
    public Color getColor() {
        return color;
    }
    
    @Override
    public String toString() {
        return vertex.toString();
    }
    
    @Override
    public int hashCode() {
        return this.vertex.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(!(obj instanceof ColouredVertex))
            return false;
    
        ColouredVertex<?> other = (ColouredVertex<?>) obj;
        return this.vertex.equals(other.vertex) && this.color.equals(other.color);
    }
}
