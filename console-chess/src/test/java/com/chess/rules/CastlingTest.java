package com.chess.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

public class CastlingTest {

    private void clear(Board b) {
        for (int r = 0; r < 8; r++) for (int f = 0; f < 8; f++) b.setPiece(new Square(f, r), null);
    }

    @Test
    void testWhiteKingSideCastlingLegalWhenClearAndSafe() {
        GameState st = new GameState();
        Board b = st.getBoard(); clear(b);

        // Setup: King e1, Rook h1, no attacks on e1,f1,g1
        b.setPiece(new Square(4,0), new Piece(PieceType.KING, Color.WHITE)); // e1
        b.setPiece(new Square(7,0), new Piece(PieceType.ROOK, Color.WHITE)); // h1
        // Opponent piece far away (no attack)
        b.setPiece(new Square(7,7), new Piece(PieceType.KING, Color.BLACK)); // h8

        st.setToMove(Color.WHITE);

        Move castle = new Move(new Square(4,0), new Square(6,0)); // e1->g1
        assertTrue(MoveValidator.isLegal(st, castle), "Castling should be legal");
    }

    @Test
    void testWhiteQueenSideCastlingBlockedPathIsIllegal() {
        GameState st = new GameState();
        Board b = st.getBoard(); clear(b);

        b.setPiece(new Square(4,0), new Piece(PieceType.KING, Color.WHITE)); // e1
        b.setPiece(new Square(0,0), new Piece(PieceType.ROOK, Color.WHITE)); // a1
        // Block path at d1
        b.setPiece(new Square(3,0), new Piece(PieceType.KNIGHT, Color.WHITE)); // d1
        b.setPiece(new Square(7,7), new Piece(PieceType.KING, Color.BLACK));

        st.setToMove(Color.WHITE);

        Move castle = new Move(new Square(4,0), new Square(2,0)); // e1->c1
        assertFalse(MoveValidator.isLegal(st, castle), "Blocked path should make castling illegal");
    }

//     @Test
//     void testCastlingRevokesRightsAfterKingMoves() {
//         GameState st = new GameState();
//         // From initial position, move white king one square (illegal for castling later)
//         Move m = new Move(new Square(4,0), new Square(5,0)); // e1->f1
//         assertTrue(RulesEngine.isLegalMove(st, m));
//         com.chess.service.MoveService.apply(st, m);
//         // rights should be revoked
//         assertFalse(st.getRights().canWhiteKingSide());
//         assertFalse(st.getRights().canWhiteQueenSide());
//     }
// }

    @Test
    void testCastlingRevokesRightsAfterKingMoves() {
        GameState st = new GameState();
        Board b = st.getBoard();

        // Clear pieces that block the king's adjacent square (f1)
        b.setPiece(new Square(5, 0), null); // remove any piece at f1 (white bishop in the initial setup)

        // Now a simple king move e1->f1 should be legal
        Move m = new Move(new Square(4, 0), new Square(5, 0)); // e1->f1
        assertTrue(RulesEngine.isLegalMove(st, m), "King should be able to move to f1 after clearing f1");
        com.chess.service.MoveService.apply(st, m);

        // Rights should be revoked because the king moved
        assertFalse(st.getRights().canWhiteKingSide());
        assertFalse(st.getRights().canWhiteQueenSide());
    }
}