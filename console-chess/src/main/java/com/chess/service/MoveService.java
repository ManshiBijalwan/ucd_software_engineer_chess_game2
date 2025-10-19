package com.chess.service;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

public final class MoveService {
    private MoveService() {}

    /** Applies a validated move to the given state and returns true iff the move captured the opponent's king. */
    public static boolean apply(GameState state, Move move) {
        Board board = state.getBoard();
        Piece moving = board.getPiece(move.getFrom());
        Piece target = board.getPiece(move.getTo());
        boolean kingCaptured = (target != null && target.getType() == PieceType.KING);

        // ---------- 1) Handle CASTLING first ----------
        if (moving.getType() == PieceType.KING
                && Math.abs(move.getTo().getFile() - move.getFrom().getFile()) == 2
                && move.getTo().getRank() == move.getFrom().getRank()) {
            applyCastling(state, move, moving.getColor());
            state.getRights().revokeBoth(moving.getColor());
            // EP becomes irrelevant after castling; clear it.
            state.setEnPassantSquare(null);
            state.setEnPassantFor(null);
            return kingCaptured;
        }

        // ---------- 2) Handle EN PASSANT (must check BEFORE clearing EP) ----------
        boolean isEpCandidate = (moving.getType() == PieceType.PAWN
                && target == null
                && state.getEnPassantSquare() != null
                && move.getTo().getFile() == state.getEnPassantSquare().getFile()
                && move.getTo().getRank() == state.getEnPassantSquare().getRank()
                && state.getEnPassantFor() == moving.getColor());

        if (isEpCandidate) {
            // Move the capturing pawn to the EP square
            board.setPiece(move.getTo(), moving);
            board.setPiece(move.getFrom(), null);

            // Remove the bypassed pawn (same file as destination, rank from which the pawn came)
            Square capturedSquare = new Square(move.getTo().getFile(), move.getFrom().getRank());
            Piece capturedPiece = board.getPiece(capturedSquare);
            if (capturedPiece != null && capturedPiece.getType() == PieceType.PAWN) {
                board.setPiece(capturedSquare, null);
            }

            // Clear EP after use
            state.setEnPassantSquare(null);
            state.setEnPassantFor(null);
            return kingCaptured; // kingCaptured will be false in EP (as expected)
        }

        // ---------- 3) Normal move (including promotion) ----------
        // Clear any previous EP by default (non-EP moves end the EP opportunity).
        state.setEnPassantSquare(null);
        state.setEnPassantFor(null);

        // Move piece
        board.setPiece(move.getTo(), moving);
        board.setPiece(move.getFrom(), null);

        // Revoke rook rights if rook moved from original square
        if (moving.getType() == PieceType.ROOK) {
            int rank = (moving.getColor() == Color.WHITE) ? 0 : 7;
            if (move.getFrom().equals(new Square(0, rank))) {
                if (moving.getColor() == Color.WHITE) state.getRights().revokeWhiteQueenSide();
                else state.getRights().revokeBlackQueenSide();
            } else if (move.getFrom().equals(new Square(7, rank))) {
                if (moving.getColor() == Color.WHITE) state.getRights().revokeWhiteKingSide();
                else state.getRights().revokeBlackKingSide();
            }
        }

        // If king moves, revoke both castling rights
        if (moving.getType() == PieceType.KING) {
            state.getRights().revokeBoth(moving.getColor());
        }

        // Promotion handling
        if (moving.getType() == PieceType.PAWN) {
            int rank = move.getTo().getRank();
            boolean onLast = (moving.getColor() == Color.WHITE && rank == 7) || (moving.getColor() == Color.BLACK && rank == 0);
            if (onLast) {
                PieceType promoteTo = (move.getPromotion() != null) ? move.getPromotion() : PieceType.QUEEN;
                board.setPiece(move.getTo(), new Piece(promoteTo, moving.getColor()));
            } else {
                // Double-step → set EP square for opponent
                int startRank = (moving.getColor() == Color.WHITE) ? 1 : 6;
                int direction = (moving.getColor() == Color.WHITE) ? 1 : -1;
                if (move.getFrom().getRank() == startRank && move.getTo().getRank() == startRank + 2 * direction) {
                    Square ep = new Square(move.getFrom().getFile(), move.getFrom().getRank() + direction);
                    state.setEnPassantSquare(ep);
                    state.setEnPassantFor(opponentOf(moving.getColor()));
                }
            }
        }

        return kingCaptured;
    }

    private static void applyCastling(GameState st, Move move, Color color) {
        Board b = st.getBoard();
        Piece king = b.getPiece(move.getFrom());
        b.setPiece(move.getTo(), king);
        b.setPiece(move.getFrom(), null);

        int homeRank = (color == Color.WHITE) ? 0 : 7;
        boolean kingSide = move.getTo().getFile() > move.getFrom().getFile();
        Square rookFrom = kingSide ? new Square(7, homeRank) : new Square(0, homeRank);
        Square rookTo   = kingSide ? new Square(5, homeRank) : new Square(3, homeRank);
        Piece rook = b.getPiece(rookFrom);
        b.setPiece(rookTo, rook);
        b.setPiece(rookFrom, null);
    }

    private static Color opponentOf(Color c) {
        return (c == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
}