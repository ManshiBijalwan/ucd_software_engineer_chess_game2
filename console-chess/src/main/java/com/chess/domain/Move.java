package com.chess.domain;

import java.util.Objects;

public class Move {
    private final Square from;
    private final Square to;
    private final PieceType promotion; // null if not a promotion

    public Move(Square from, Square to) {
        this(from, to, null);
    }

    public Move(Square from, Square to, PieceType promotion) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.promotion = promotion; // may be null
    }

    public Square getFrom() { return from; }
    public Square getTo() { return to; }
    public PieceType getPromotion() { return promotion; }
    public boolean isPromotion() { return promotion != null; }

    @Override
    public String toString() {
        String base = from.toString() + to.toString();
        if (promotion != null) {
            // print a single letter in lowercase per input convention
            char c = switch (promotion) {
                case QUEEN -> 'q';
                case ROOK  -> 'r';
                case BISHOP-> 'b';
                case KNIGHT-> 'n';
                default    -> 'q';
            };
            return base + c;
        }
        return base;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) return false;
        Move m = (Move)o;
        return from.equals(m.from) && to.equals(m.to) &&
               Objects.equals(promotion, m.promotion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, promotion);
    }
}