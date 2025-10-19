package com.chess.domain;

/** Tracks whether each side can still castle on either wing. */
public final class CastlingRights {
    private boolean whiteKingSide = true;
    private boolean whiteQueenSide = true;
    private boolean blackKingSide = true;
    private boolean blackQueenSide = true;

    public boolean canWhiteKingSide() { return whiteKingSide; }
    public boolean canWhiteQueenSide() { return whiteQueenSide; }
    public boolean canBlackKingSide() { return blackKingSide; }
    public boolean canBlackQueenSide() { return blackQueenSide; }

    public void revokeWhiteKingSide()  { whiteKingSide = false; }
    public void revokeWhiteQueenSide() { whiteQueenSide = false; }
    public void revokeBlackKingSide()  { blackKingSide = false; }
    public void revokeBlackQueenSide() { blackQueenSide = false; }

    /** Revoke both rights for a color when its king moves. */
    public void revokeBoth(Color color) {
        if (color == Color.WHITE) { whiteKingSide = false; whiteQueenSide = false; }
        else { blackKingSide = false; blackQueenSide = false; }
    }
}