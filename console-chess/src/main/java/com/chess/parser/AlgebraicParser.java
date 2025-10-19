package com.chess.parser;

import com.chess.domain.Move;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

public class AlgebraicParser {

    public static Move parse(String input) {
        if (input == null) throw new IllegalArgumentException("Empty input");
        input = input.trim().toLowerCase();

        if (input.length() != 4 && input.length() != 5) {
            throw new IllegalArgumentException("Invalid move format. Use e.g., e2e4 or e7e8q for promotion.");
        }

        Square from = Square.fromNotation(input.substring(0, 2));
        Square to   = Square.fromNotation(input.substring(2, 4));

        PieceType promo = null;
        if (input.length() == 5) {
            char p = input.charAt(4);
            promo = switch (p) {
                case 'q' -> PieceType.QUEEN;
                case 'r' -> PieceType.ROOK;
                case 'b' -> PieceType.BISHOP;
                case 'n' -> PieceType.KNIGHT;
                default  -> throw new IllegalArgumentException("Unknown promotion piece: " + p + " (use q/r/b/n)");
            };
        }

        return new Move(from, to, promo);
    }
}