package com.chess.rules;

import com.chess.domain.Board;
import com.chess.domain.CastlingRights;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Move;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

public class MoveValidator {

    public static boolean isLegal(GameState state, Move move) {
        Board board = state.getBoard();
        Piece piece = board.getPiece(move.getFrom());
        if (piece == null || piece.getColor() != state.getToMove()) return false;
        if (move.getFrom().equals(move.getTo())) return false;

        // cannot capture own piece
        Piece destPiece = board.getPiece(move.getTo());
        if (destPiece != null && destPiece.getColor() == piece.getColor()) return false;

        // Special-cases: Castling / En Passant
        if (piece.getType() == PieceType.KING && isCastlingAttempt(piece.getColor(), move)) {
            if (!validateCastling(state, move)) return false;
            // simulate castling steps for king safety
            GameState copy = GameState.deepCopy(state);
            applyCastling(copy, move);
            return !CheckDetector.isKingInCheck(copy, piece.getColor());
        }

        boolean isEp = piece.getType() == PieceType.PAWN && isEnPassantMove(state, piece.getColor(), move);
        if (isEp) {
            GameState copy = GameState.deepCopy(state);
            applyEnPassant(copy, move, piece.getColor());
            return !CheckDetector.isKingInCheck(copy, piece.getColor());
        }

        // Normal pattern validation
        if (!validatePattern(piece, move, board)) return false;

        // King safety simulation (normal move)
        GameState copy = GameState.deepCopy(state);
        applyNormal(copy, move);
        return !CheckDetector.isKingInCheck(copy, piece.getColor());
    }

    /** Existing method kept public for CheckDetector reuse. */
    public static boolean validatePattern(Piece piece, Move move, Board board) {
        int dx = move.getTo().getFile() - move.getFrom().getFile();
        int dy = move.getTo().getRank() - move.getFrom().getRank();

        switch (piece.getType()) {
            case PAWN:   return validatePawn(piece.getColor(), dx, dy, board, move);
            case ROOK:   if (dx == 0 || dy == 0) return isPathClear(move.getFrom(), move.getTo(), board); return false;
            case BISHOP: if (Math.abs(dx) == Math.abs(dy)) return isPathClear(move.getFrom(), move.getTo(), board); return false;
            case QUEEN:  if (dx == 0 || dy == 0 || Math.abs(dx) == Math.abs(dy)) return isPathClear(move.getFrom(), move.getTo(), board); return false;
            case KNIGHT: return (Math.abs(dx) == 2 && Math.abs(dy) == 1) || (Math.abs(dx) == 1 && Math.abs(dy) == 2);
            case KING:   return Math.abs(dx) <= 1 && Math.abs(dy) <= 1; // castling handled separately
            default:     return false;
        }
    }

    public static boolean isPathClear(Square from, Square to, Board board) {
        int fileStep = Integer.compare(to.getFile(), from.getFile());
        int rankStep = Integer.compare(to.getRank(), from.getRank());
        int f = from.getFile() + fileStep;
        int r = from.getRank() + rankStep;
        while (f != to.getFile() || r != to.getRank()) {
            if (board.getPiece(new Square(f, r)) != null) return false;
            f += fileStep; r += rankStep;
        }
        return true;
    }

    //Castling code strts below, additionl feature

    private static boolean isCastlingAttempt(Color color, Move move) {
        int dx = move.getTo().getFile() - move.getFrom().getFile();
        int dy = move.getTo().getRank() - move.getFrom().getRank();
        // King moves two files horizontally on its home rank
        int homeRank = (color == Color.WHITE) ? 0 : 7;
        return dy == 0 && Math.abs(dx) == 2 && move.getFrom().getRank() == homeRank;
    }

    private static boolean validateCastling(GameState state, Move move) {
        Color color = state.getToMove();
        CastlingRights rights = state.getRights();
        Board board = state.getBoard();

        boolean kingSide = move.getTo().getFile() > move.getFrom().getFile(); // e1->g1 or e8->g8
        int homeRank = (color == Color.WHITE) ? 0 : 7;
        Square kingFrom = move.getFrom();
        Square kingTo   = move.getTo();

        // Check rights exist and rook present
        Square rookFrom = kingSide ? new Square(7, homeRank) : new Square(0, homeRank);
        Piece rook = board.getPiece(rookFrom);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != color) return false;

        if (color == Color.WHITE) {
            if (kingSide && !rights.canWhiteKingSide()) return false;
            if (!kingSide && !rights.canWhiteQueenSide()) return false;
        } else {
            if (kingSide && !rights.canBlackKingSide()) return false;
            if (!kingSide && !rights.canBlackQueenSide()) return false;
        }

        // Path between king and rook must be clear
        if (!isPathClear(kingFrom, rookFrom, board)) return false;

        // King not in check and doesn’t pass through attacked squares
        // simulate step-by-step: from -> intermediate -> to
        int stepFile = kingSide ? kingFrom.getFile() + 1 : kingFrom.getFile() - 1;
        Square intermediate = new Square(stepFile, homeRank);

        // Check current, intermediate, destination are safe
        GameState copy = GameState.deepCopy(state);
        // current position safety
        if (CheckDetector.isKingInCheck(copy, color)) return false;
        // move king one step to intermediate
        applyKingStep(copy, kingFrom, intermediate);
        if (CheckDetector.isKingInCheck(copy, color)) return false;
        // move king to final
        applyKingStep(copy, intermediate, kingTo);
        if (CheckDetector.isKingInCheck(copy, color)) return false;

        return true;
    }

    private static void applyKingStep(GameState st, Square from, Square to) {
        Board b = st.getBoard();
        Piece k = b.getPiece(from);
        b.setPiece(to, k);
        b.setPiece(from, null);
    }

    private static void applyCastling(GameState st, Move move) {
        Color color = st.getToMove();
        Board b = st.getBoard();
        int homeRank = (color == Color.WHITE) ? 0 : 7;

        // move king (already validated)
        Piece king = b.getPiece(move.getFrom());
        b.setPiece(move.getTo(), king);
        b.setPiece(move.getFrom(), null);

        boolean kingSide = move.getTo().getFile() > move.getFrom().getFile();
        Square rookFrom = kingSide ? new Square(7, homeRank) : new Square(0, homeRank);
        Square rookTo   = kingSide ? new Square(5, homeRank) : new Square(3, homeRank);

        Piece rook = b.getPiece(rookFrom);
        b.setPiece(rookTo, rook);
        b.setPiece(rookFrom, null);

        // Revoke rights after castling
        st.getRights().revokeBoth(color);

        // EP is not affected directly
    }

    // ---------- En Passant ----------

    private static boolean isEnPassantMove(GameState state, Color color, Move move) {
        Square ep = state.getEnPassantSquare();
        if (ep == null || state.getEnPassantFor() != color) return false;

        int dx = move.getTo().getFile() - move.getFrom().getFile();
        int dy = move.getTo().getRank() - move.getFrom().getRank();
        int direction = (color == Color.WHITE) ? 1 : -1;

        // Diagonal move into EP square; destination must equal EP square; destination is empty by definition
        return Math.abs(dx) == 1 && dy == direction && move.getTo().getFile() == ep.getFile() && move.getTo().getRank() == ep.getRank();
    }

    private static void applyEnPassant(GameState st, Move move, Color mover) {
        Board b = st.getBoard();
        Piece pawn = b.getPiece(move.getFrom());
        // Move the capturing pawn
        b.setPiece(move.getTo(), pawn);
        b.setPiece(move.getFrom(), null);

        // Remove the bypassed pawn (on the file of destination, on the rank the pawn came from)
        Square capturedSquare = new Square(move.getTo().getFile(), move.getFrom().getRank());
        b.setPiece(capturedSquare, null);

        // Clear EP availability after use
        st.setEnPassantSquare(null);
        st.setEnPassantFor(null);
    }

    private static void applyNormal(GameState st, Move move) {
        Board b = st.getBoard();
        Piece p = b.getPiece(move.getFrom());
        b.setPiece(move.getTo(), p);
        b.setPiece(move.getFrom(), null);
    }

    // Existing pawn validator, extended (double-step + promotion reach already added earlier)
    private static boolean validatePawn(Color color, int dx, int dy, Board board, Move move) {
        Piece dest = board.getPiece(move.getTo());
        int direction = (color == Color.WHITE) ? 1 : -1;

        // one-step forward (empty)
        boolean one = (dx == 0 && dy == direction && dest == null);

        // two-step forward from starting rank (both empty)
        int startRank = (color == Color.WHITE) ? 1 : 6;
        boolean two = false;
        if (dx == 0 && dy == 2 * direction && dest == null && move.getFrom().getRank() == startRank) {
            Square mid = new Square(move.getFrom().getFile(), move.getFrom().getRank() + direction);
            two = (board.getPiece(mid) == null);
        }

        // diagonal capture (occupied by opponent)
        boolean cap = (Math.abs(dx) == 1 && dy == direction && dest != null && dest.getColor() != color);

        // promotion reach: only one-step or capture can land on last rank
        int toRank = move.getTo().getRank();
        boolean last = (color == Color.WHITE && toRank == 7) || (color == Color.BLACK && toRank == 0);
        if (last) return one || cap;

        return one || two || cap;
    }
}