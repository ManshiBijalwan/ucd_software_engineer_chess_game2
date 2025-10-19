package com.chess.domain;

public class Piece {
    private final PieceType type;
    private final Color color;

    public Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    public PieceType getType() { return type; }
    public Color getColor() { return color; }

    @Override
    public String toString() {
        return color.toString().charAt(0) + type.toString().substring(0,1);
    }
}