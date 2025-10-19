package com.chess.domain;

public class GameState {
    private final Board board;
    private Color toMove;

    //Rights + EP info
    private final CastlingRights rights;
    private Square enPassantSquare;        // null if none
    private Color enPassantFor;            // side eligible to capture EP/ opponent of last double-step(null if none)

    public GameState() {
        this(new Board(), Color.WHITE, new CastlingRights(), null, null);
    }

    public GameState(Board board, Color toMove) {
        this(board, toMove, new CastlingRights(), null, null);
    }

    public GameState(Board board, Color toMove, CastlingRights rights,
                     Square enPassantSquare, Color enPassantFor) {
        this.board = board;
        this.toMove = toMove;
        this.rights = rights;
        this.enPassantSquare = enPassantSquare;
        this.enPassantFor = enPassantFor;
    }

    public Board getBoard() { return board; }
    public Color getToMove() { return toMove; }
    public void setToMove(Color c) { this.toMove = c; }
    public void switchTurn() { this.toMove = (toMove == Color.WHITE) ? Color.BLACK : Color.WHITE; }

    public CastlingRights getRights() { return rights; }

    public Square getEnPassantSquare() { return enPassantSquare; }
    public void setEnPassantSquare(Square s) { this.enPassantSquare = s; }

    public Color getEnPassantFor() { return enPassantFor; }
    public void setEnPassantFor(Color c) { this.enPassantFor = c; }

    /** Deep copy helper used by MoveValidator simulation. */
    public static GameState deepCopy(GameState original) {
        Board ob = original.getBoard();
        Board nb = new Board();
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                nb.setPiece(new Square(f, r), ob.getPiece(new Square(f, r)));
            }
        }
        // copy castling rights
        CastlingRights nr = new CastlingRights();
        if (!original.getRights().canWhiteKingSide())  nr.revokeWhiteKingSide();
        if (!original.getRights().canWhiteQueenSide()) nr.revokeWhiteQueenSide();
        if (!original.getRights().canBlackKingSide())  nr.revokeBlackKingSide();
        if (!original.getRights().canBlackQueenSide()) nr.revokeBlackQueenSide();

        return new GameState(
                nb,
                original.getToMove(),
                nr,
                original.getEnPassantSquare(), // EP square can be reused (Square is immutable in your design)
                original.getEnPassantFor()
        );
    }
}