package com.chess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class BoardTest {
    @Test
    void initialSetupHasKings() {
        Board board = new Board();
        assertEquals(PieceType.KING, board.getPiece(new Square(4, 0)).getType());
        assertEquals(PieceType.KING, board.getPiece(new Square(4, 7)).getType());
    }
}