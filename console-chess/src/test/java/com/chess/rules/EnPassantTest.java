package com.chess.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

public class EnPassantTest {

    @Test
    void testWhiteCapturesEnPassantAfterBlackDoubleStep() {
        GameState st = new GameState();
        Board b = st.getBoard();

        // Clear board
        for (int r = 0; r < 8; r++) for (int f = 0; f < 8; f++) b.setPiece(new Square(f, r), null);

        // Place kings to avoid edge cases
        b.setPiece(new Square(4,7), new Piece(PieceType.KING, Color.BLACK));
        b.setPiece(new Square(4,0), new Piece(PieceType.KING, Color.WHITE));

        // Black pawn on d7, White pawn on e5 (to capture EP)
        b.setPiece(new Square(3,6), new Piece(PieceType.PAWN, Color.BLACK)); // d7
        b.setPiece(new Square(4,4), new Piece(PieceType.PAWN, Color.WHITE)); // e5

        // Black to move: d7->d5 (double-step)
        st.setToMove(Color.BLACK);
        Move doubleStep = new Move(new Square(3,6), new Square(3,4)); // d7->d5
        assertTrue(RulesEngine.isLegalMove(st, doubleStep));
        com.chess.service.MoveService.apply(st, doubleStep);
        st.switchTurn(); // White to move, EP square should be d6

        assertNotNull(st.getEnPassantSquare(), "EP square should be set");
        assertEquals(3, st.getEnPassantSquare().getFile());
        assertEquals(5, st.getEnPassantSquare().getRank());

        // White plays e5xd6 EP: e5->d6
        Move epCapture = new Move(new Square(4,4), new Square(3,5));
        assertTrue(RulesEngine.isLegalMove(st, epCapture));
        com.chess.service.MoveService.apply(st, epCapture);

        // Captured black pawn should be removed from d5
        assertNull(b.getPiece(new Square(3,4)));
        // White pawn should be on d6
        Piece white = b.getPiece(new Square(3,5));
        assertNotNull(white);
        assertEquals(PieceType.PAWN, white.getType());
        assertEquals(Color.WHITE, white.getColor());
    }

    // @Test
    // void testEnPassantNotAllowedIfNotImmediate() {
    //     GameState st = new GameState();
    //     Board b = st.getBoard();
    //     for (int r = 0; r < 8; r++) for (int f = 0; f < 8; f++) b.setPiece(new Square(f, r), null);
    //     b.setPiece(new Square(4,7), new Piece(PieceType.KING, Color.BLACK));
    //     b.setPiece(new Square(4,0), new Piece(PieceType.KING, Color.WHITE));
    //     b.setPiece(new Square(3,6), new Piece(PieceType.PAWN, Color.BLACK)); // d7
    //     b.setPiece(new Square(4,4), new Piece(PieceType.PAWN, Color.WHITE)); // e5

    //     st.setToMove(Color.BLACK);
    //     Move doubleStep = new Move(new Square(3,6), new Square(3,4)); // d7->d5
    //     assertTrue(RulesEngine.isLegalMove(st, doubleStep));
    //     com.chess.service.MoveService.apply(st, doubleStep);
    //     st.switchTurn();

    //     // White makes a "waiting" move (not EP), e.g., king move
    //     Move wait = new Move(new Square(4,0), new Square(4,1));
    //     assertTrue(RulesEngine.isLegalMove(st, wait));
    //     com.chess.service.MoveService.apply(st, wait);
    //     st.switchTurn();

    //     // EP should now be cleared; White cannot capture EP anymore next turn
    //     assertNull(st.getEnPassantSquare());

    //     // Black to move now; (scenario suffices to prove EP is immediate-only)
    //     // No assertion beyond EP square cleared
    // }

    @Test
    void testEnPassantNotAllowedIfNotImmediate() {
        GameState st = new GameState();
        Board b = st.getBoard();

        // Clear board
        for (int r = 0; r < 8; r++) for (int f = 0; f < 8; f++) b.setPiece(new Square(f, r), null);

        // Kings (to avoid illegal positions)
        b.setPiece(new Square(4, 7), new Piece(PieceType.KING, Color.BLACK)); // e8
        b.setPiece(new Square(4, 0), new Piece(PieceType.KING, Color.WHITE)); // e1

        // Black pawn on d7, White pawn on e5 (to create EP opportunity)
        b.setPiece(new Square(3, 6), new Piece(PieceType.PAWN, Color.BLACK)); // d7
        b.setPiece(new Square(4, 4), new Piece(PieceType.PAWN, Color.WHITE)); // e5

        // Black to move: d7->d5 (double-step)
        st.setToMove(Color.BLACK);
        Move doubleStep = new Move(new Square(3, 6), new Square(3, 4)); // d7->d5
        assertTrue(RulesEngine.isLegalMove(st, doubleStep));
        com.chess.service.MoveService.apply(st, doubleStep);
        st.switchTurn(); // White to move (EP square is d6)

        assertNotNull(st.getEnPassantSquare(), "EP square should be set");
        assertEquals(3, st.getEnPassantSquare().getFile());
        assertEquals(5, st.getEnPassantSquare().getRank());

        // White makes a waiting (non-EP) legal move: king e1->e2
        Move wait = new Move(new Square(4, 0), new Square(4, 1));
        assertTrue(RulesEngine.isLegalMove(st, wait));
        com.chess.service.MoveService.apply(st, wait);
        st.switchTurn(); // Black to move

        // EP should be cleared after the waiting move
        assertNull(st.getEnPassantSquare(), "EP must be immediate-only and cleared after any non-EP move");
    }
}