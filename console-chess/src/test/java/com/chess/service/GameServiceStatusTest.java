package com.chess.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.GameStatus;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;
import com.chess.rules.RulesEngine;

public class GameServiceStatusTest {

    /** Utility: clear the board quickly. */
    private static void clear(Board b) {
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                b.setPiece(new Square(f, r), null);
            }
        }
    }

    @Test
    void testSimpleCheckmatePattern() {
        GameState state = new GameState();
        Board b = state.getBoard();
        clear(b);

        // Black king on h8, White queen on h7, White king on g6, Black to move → checkmate
        b.setPiece(new Square(7, 7), new Piece(PieceType.KING, Color.BLACK));   // h8
        b.setPiece(new Square(7, 6), new Piece(PieceType.QUEEN, Color.WHITE));  // h7
        b.setPiece(new Square(6, 5), new Piece(PieceType.KING, Color.WHITE));   // g6

        state.setToMove(Color.BLACK);

        GameStatus status = GameService.assessStatus(state);
        assertEquals(GameStatus.CHECKMATE, status, "Expected checkmate for Black");
    }

    @Test
    void testClassicStalematePattern() {
        GameState state = new GameState();
        Board b = state.getBoard();
        clear(b);

        // Stalemate known setup: BK a8, WQ c7, WK b6; Black to move, not in check, no legal moves
        b.setPiece(new Square(0, 7), new Piece(PieceType.KING, Color.BLACK));   // a8
        b.setPiece(new Square(2, 6), new Piece(PieceType.QUEEN, Color.WHITE));  // c7
        b.setPiece(new Square(1, 5), new Piece(PieceType.KING, Color.WHITE));   // b6

        state.setToMove(Color.BLACK);

        GameStatus status = GameService.assessStatus(state);
        assertEquals(GameStatus.STALEMATE, status, "Expected stalemate for Black");
    }

    @Test
    void testStatusInProgressOrCheck() {
        GameState state = new GameState();
        // From initial position: White to move, should be IN_PROGRESS; after e2e4 and switch, Black may be IN_PROGRESS or CHECK depending on responses
        assertEquals(GameStatus.IN_PROGRESS, GameService.assessStatus(state));
        // Make a legal move to ensure no crash in status evaluation pipeline
        Move m = new Move(new Square(4,1), new Square(4,3)); // e2e4
        assertTrue(RulesEngine.isLegalMove(state, m));
        com.chess.service.MoveService.apply(state, m);
        state.switchTurn();
        GameStatus status = GameService.assessStatus(state);
        assertTrue(status == GameStatus.IN_PROGRESS || status == GameStatus.CHECK);
    }
}