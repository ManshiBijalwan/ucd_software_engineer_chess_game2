package com.chess.rules;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

public class CheckDetector {

    public static boolean isKingInCheck(GameState state, Color color) {
        Square kingSquare = findKing(state.getBoard(), color);
        if (kingSquare == null) return false; // King captured (game over)
        
        // Check if any opponent piece can attack kingSquare
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
        Board board = state.getBoard();

        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                Piece piece = board.getPiece(new Square(f, r));
                if (piece != null && piece.getColor() == opponent) {
                    Move pseudoMove = new Move(new Square(f, r), kingSquare);
                    if (MoveValidator.validatePattern(piece, pseudoMove, board)) {
                        // For sliding pieces, ensure path is clear
                        if (piece.getType() == PieceType.ROOK || piece.getType() == PieceType.BISHOP || piece.getType() == PieceType.QUEEN) {
                            if (!MoveValidator.isPathClear(pseudoMove.getFrom(), pseudoMove.getTo(), board)) continue;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Square findKing(Board board, Color color) {
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                Piece piece = board.getPiece(new Square(f, r));
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    return new Square(f, r);
                }
            }
        }
        return null;
    }
}