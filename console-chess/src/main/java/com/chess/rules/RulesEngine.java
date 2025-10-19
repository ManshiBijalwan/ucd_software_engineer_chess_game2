package com.chess.rules;

import java.util.ArrayList;
import java.util.List;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

/**
 * RulesEngine provides high-level chess rule operations.
 * <p>
 * It validates moves and generates all legal moves for the current player.
 * This class delegates detailed checks to {@link MoveValidator} and uses
 * {@link CheckDetector} for king safety.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Check if a move is legal according to chess rules.</li>
 *   <li>Generate all legal moves for the side to move.</li>
 * </ul>
 *
 * Example usage:
 * <pre>
 * GameState state = new GameState();
 * Move move = AlgebraicParser.parse("e2e4");
 * boolean legal = RulesEngine.isLegalMove(state, move);
 * </pre>
 */

public class RulesEngine {
    
    /**
     * Determines if a given move is legal in the current game state.
     *
     * @param state the current game state
     * @param move  the move to validate
     * @return true if the move is legal, false otherwise
     */
    public static boolean isLegalMove(GameState state, Move move) {
        return MoveValidator.isLegal(state, move);
    }

    public static List<Move> generateLegalMoves(GameState state) {
        List<Move> legal = new ArrayList<>();
        Board board = state.getBoard();
        Color side = state.getToMove();

        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                Square from = new Square(f, r);
                Piece p = board.getPiece(from);
                if (p == null || p.getColor() != side) continue;

                for (int rr = 0; rr < 8; rr++) {
                    for (int ff = 0; ff < 8; ff++) {
                        Square to = new Square(ff, rr);
                        // try normal move
                        Move m = new Move(from, to);
                        if (MoveValidator.isLegal(state, m)) legal.add(m);

                        // If pawn and moving to last rank, also try promotions (q/r/b/n)
                        if (p.getType() == PieceType.PAWN) {
                            boolean lastRank = (side == Color.WHITE && rr == 7) || (side == Color.BLACK && rr == 0);
                            if (lastRank) {
                                for (PieceType pt : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}) {
                                    Move pm = new Move(from, to, pt);
                                    if (MoveValidator.isLegal(state, pm)) legal.add(pm);
                                }
                            }
                        }
                    }
                }
            }
        }
        return legal;
    }
}