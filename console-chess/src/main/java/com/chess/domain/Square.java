package com.chess.domain;

public class Square {
    private final int file; // 0-7 for a-h
    private final int rank; // 0-7 for 1-8

    public Square(int file, int rank) {
        if(file < 0 || file > 7 || rank < 0 || rank > 7)
            throw new IllegalArgumentException("Invalid square");
        this.file = file;
        this.rank = rank;
    }

    public int getFile() { return file; }
    public int getRank() { return rank; }

    public static Square fromNotation(String notation) {
        if(notation.length() != 2) throw new IllegalArgumentException("Bad notation");
        int file = notation.charAt(0) - 'a';
        int rank = notation.charAt(1) - '1';
        return new Square(file, rank);
    }

    @Override
    public String toString() {
        return "" + (char)('a' + file) + (rank + 1);
    }
}