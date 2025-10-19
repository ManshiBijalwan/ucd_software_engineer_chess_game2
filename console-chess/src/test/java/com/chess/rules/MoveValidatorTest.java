package com.chess.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;
import com.chess.parser.AlgebraicParser;

public class MoveValidatorTest {

    @Test
    void testPawnForwardMove() {
        GameState state = new GameState();
        Move move = AlgebraicParser.parse("e2e3");
        assertTrue(RulesEngine.isLegalMove(state, move));
    }

    @Test
    void testIllegalKnightMove() {
        GameState state = new GameState();
        Move move = AlgebraicParser.parse("b1b3");
        assertFalse(RulesEngine.isLegalMove(state, move));
    }

    @Test
    void testRookBlockedMove() {
        GameState state = new GameState();
        Move move = AlgebraicParser.parse("a1a3");
        assertFalse(RulesEngine.isLegalMove(state, move));
    }

    @Test
    void testCannotCaptureOwnPiece() {
        GameState state = new GameState();
        Move move = AlgebraicParser.parse("a1a2");
        assertFalse(RulesEngine.isLegalMove(state, move));
    }

    @Test
    void testPromotionDefaultsToQueenWhenNotSpecified() {
        GameState state = new GameState();
        Board b = state.getBoard();

        // Clear path and set a white pawn on e7
        b.setPiece(new Square(4, 1), null); // remove white pawn at e2 for clarity (optional)
        b.setPiece(new Square(4, 6), null); // remove black pawn at e7
        b.setPiece(new Square(4, 7), null); // remove black king from e8
        b.setPiece(new Square(7, 7), new Piece(PieceType.KING, Color.BLACK)); // black king at h8
        b.setPiece(new Square(4, 6), new Piece(PieceType.PAWN, Color.WHITE)); // white pawn on e7
        state.setToMove(Color.WHITE);

        Move m = AlgebraicParser.parse("e7e8");
        assertTrue(RulesEngine.isLegalMove(state, m), "Promotion move should be legal");

        com.chess.service.MoveService.apply(state, m);
        Piece promoted = b.getPiece(new Square(4, 7));
        assertNotNull(promoted);
        assertEquals(PieceType.QUEEN, promoted.getType(), "Default promotion should be to Queen");
        assertEquals(Color.WHITE, promoted.getColor());
    }

    @Test
    void testPromotionExplicitToKnight() {
        GameState state = new GameState();
        Board b = state.getBoard();

        // Place white pawn on a7, black king somewhere safe
        b.setPiece(new Square(0, 6), new Piece(PieceType.PAWN, Color.WHITE));
        b.setPiece(new Square(0, 7), null); // remove rook from a8
        b.setPiece(new Square(7, 7), new Piece(PieceType.KING, Color.BLACK)); // black king at h8
        state.setToMove(Color.WHITE);

        Move m = AlgebraicParser.parse("a7a8n");
        assertTrue(RulesEngine.isLegalMove(state, m));
        com.chess.service.MoveService.apply(state, m);
        Piece promoted = b.getPiece(new Square(0, 7));
        assertEquals(PieceType.KNIGHT, promoted.getType());
        assertEquals(Color.WHITE, promoted.getColor());
    }

    @Test
    void testPawnDoubleStepFromStartIsLegal() {
        GameState state = new GameState();
        Move move = AlgebraicParser.parse("e2e4");
        assertTrue(RulesEngine.isLegalMove(state, move), "White should be able to play e2e4 from the initial position");
    }

    @Test
    void testPawnDoubleStepBlockedByPieceIsIllegal() {
        GameState state = new GameState();
        // Block e3 with a white piece so e2e4 is not allowed
        state.getBoard().setPiece(new Square(4, 2), new Piece(PieceType.KNIGHT, Color.WHITE)); // e3
        Move move = AlgebraicParser.parse("e2e4");
        assertFalse(RulesEngine.isLegalMove(state, move), "Two-step push must fail if the intermediate square is occupied");
    }

    @Test
    void testPawnDoubleStepFromNonStartIsIllegal() {
        GameState state = new GameState();
        // Move the pawn one step to e3 first (simulate on board)
        state.getBoard().setPiece(new Square(4, 1), null); // clear e2
        state.getBoard().setPiece(new Square(4, 2), new Piece(PieceType.PAWN, Color.WHITE)); // place pawn on e3
        Move move = AlgebraicParser.parse("e3e5");
        assertFalse(RulesEngine.isLegalMove(state, move), "Two-step is only from the starting rank");
    }

}