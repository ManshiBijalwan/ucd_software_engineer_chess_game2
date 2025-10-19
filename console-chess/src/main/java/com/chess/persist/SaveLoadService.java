package com.chess.persist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.chess.domain.Board;
import com.chess.domain.CastlingRights;
import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Square;

/** Simple console-friendly save/load for GameState (board + toMove + rights + EP). */
public final class SaveLoadService {
    private SaveLoadService() {}

    public static void save(GameState state, OutputStream os) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"))) {
            Board b = state.getBoard();
            // write ranks 8..1
            for (int r = 7; r >= 0; r--) {
                pw.print((r + 1) + ":");
                for (int f = 0; f < 8; f++) {
                    Piece p = b.getPiece(new Square(f, r));
                    String cell = (p == null) ? "--" : ((p.getColor() == Color.WHITE ? "w" : "b")
                            + pieceLetter(p.getType()));
                    pw.print(" " + cell);
                }
                pw.println();
            }
            pw.println("toMove: " + (state.getToMove() == Color.WHITE ? "WHITE" : "BLACK"));

            CastlingRights cr = state.getRights();
            String rights = (cr.canWhiteKingSide() ? "K" : "") + (cr.canWhiteQueenSide() ? "Q" : "")
                    + (cr.canBlackKingSide() ? "k" : "") + (cr.canBlackQueenSide() ? "q" : "");
            if (rights.isEmpty()) rights = "-";
            pw.println("rights: " + rights);

            // ---- FIX #1: Write ep as text "a1..h8" (NOT numeric addition) ----
            Square ep = state.getEnPassantSquare();
            String epStr = "-";
            if (ep != null) {
                char fileChar = (char) ('a' + ep.getFile());
                int rankNum = ep.getRank() + 1;
                epStr = String.valueOf(fileChar) + rankNum;  // e.g., "d6"
            }
            pw.println("ep: " + epStr);
        }
    }

    public static GameState load(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        Board b = new Board();
        // clear the board first
        for (int r = 0; r < 8; r++) for (int f = 0; f < 8; f++) b.setPiece(new Square(f, r), null);

        Color toMove = Color.WHITE;
        CastlingRights cr = new CastlingRights();
        Square epSquare = null;

        String line;
        int rowsParsed = 0;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("toMove:")) {
                toMove = line.contains("BLACK") ? Color.BLACK : Color.WHITE;

            } else if (line.startsWith("rights:")) {
                String val = line.substring("rights:".length()).trim();
                if (!val.contains("K")) cr.revokeWhiteKingSide();
                if (!val.contains("Q")) cr.revokeWhiteQueenSide();
                if (!val.contains("k")) cr.revokeBlackKingSide();
                if (!val.contains("q")) cr.revokeBlackQueenSide();

            } else if (line.startsWith("ep:")) {
                // ---- FIX #2: Robust EP parsing ----
                String v = line.substring("ep:".length()).trim();
                if (!v.equals("-")) {
                    if (v.matches("^[a-h][1-8]$")) {
                        int file = v.charAt(0) - 'a';
                        int rank = v.charAt(1) - '1';
                        epSquare = new Square(file, rank);
                    } else if (v.matches("^\\d+$")) {
                        // Legacy incorrect numeric format like "103" -> ignore (no EP)
                        epSquare = null;
                    } else {
                        throw new IOException("Invalid ep square in save file: " + v);
                    }
                }

            } else if (Character.isDigit(line.charAt(0)) && line.contains(":")) {
                // rank row e.g., "8: br bn ...", "1: wr ..."
                StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(':') + 1));
                int rowLabel = line.charAt(0) - '1'; // '8'->7, '1'->0
                int r = rowLabel;                    // our rank index (0..7 bottom->top)
                for (int f = 0; f < 8; f++) {
                    String cell = st.hasMoreTokens() ? st.nextToken() : "--";
                    if (!cell.equals("--")) {
                        Color c = (cell.charAt(0) == 'w') ? Color.WHITE : Color.BLACK;
                        PieceType pt = letterToType(cell.charAt(1));
                        b.setPiece(new Square(f, r), new Piece(pt, c));
                    }
                }
                rowsParsed++;
            }
        }
        if (rowsParsed != 8) throw new IOException("Malformed board rows in save file.");

        // EP is allowed only for the opponent next move; we keep 'enPassantFor' neutral (controller/next move will clear).
        Color enPassantFor = null;

        return new GameState(b, toMove, cr, epSquare, enPassantFor);
    }

    private static char pieceLetter(PieceType t) {
        switch (t) {
            case KING: return 'k';
            case QUEEN: return 'q';
            case ROOK: return 'r';
            case BISHOP: return 'b';
            case KNIGHT: return 'n';
            case PAWN: return 'p';
            default: return '?';
        }
    }

    private static PieceType letterToType(char c) {
        switch (c) {
            case 'k': return PieceType.KING;
            case 'q': return PieceType.QUEEN;
            case 'r': return PieceType.ROOK;
            case 'b': return PieceType.BISHOP;
            case 'n': return PieceType.KNIGHT;
            case 'p': return PieceType.PAWN;
            default: throw new IllegalArgumentException("Unknown piece letter: " + c);
        }
    }
}