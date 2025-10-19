package com.chess.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;
import com.chess.parser.AlgebraicParser;


public class CheckDetectorTest {

    @Test
    void testMoveThatLeavesKingInCheckIsIllegal() {
        GameState state = new GameState();
        // Remove pawn blocking e-file for quick test
        state.getBoard().setPiece(new Square(4, 1), null); // remove white pawn at e2
        state.getBoard().setPiece(new Square(4, 6), null); // remove black pawn at e7
        // Black queen moves to e5
        state.getBoard().setPiece(new Square(4, 4), new Piece(PieceType.QUEEN, Color.BLACK));

        Move move = AlgebraicParser.parse("f2f3"); // White pawn moves, leaving king exposed
        assertFalse(RulesEngine.isLegalMove(state, move));
    }
}