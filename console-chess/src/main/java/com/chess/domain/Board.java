package com.chess.domain;

public class Board {
    private final Piece[][] squares = new Piece[8][8];

    public Board() {
        setupInitial();
    }

    private void setupInitial() {
        // Pawns
        for(int i=0;i<8;i++) {
            squares[1][i] = new Piece(PieceType.PAWN, Color.WHITE);
            squares[6][i] = new Piece(PieceType.PAWN, Color.BLACK);
        }
        // Rooks
        squares[0][0] = new Piece(PieceType.ROOK, Color.WHITE);
        squares[0][7] = new Piece(PieceType.ROOK, Color.WHITE);
        squares[7][0] = new Piece(PieceType.ROOK, Color.BLACK);
        squares[7][7] = new Piece(PieceType.ROOK, Color.BLACK);
        // Knights
        squares[0][1] = new Piece(PieceType.KNIGHT, Color.WHITE);
        squares[0][6] = new Piece(PieceType.KNIGHT, Color.WHITE);
        squares[7][1] = new Piece(PieceType.KNIGHT, Color.BLACK);
        squares[7][6] = new Piece(PieceType.KNIGHT, Color.BLACK);
        // Bishops
        squares[0][2] = new Piece(PieceType.BISHOP, Color.WHITE);
        squares[0][5] = new Piece(PieceType.BISHOP, Color.WHITE);
        squares[7][2] = new Piece(PieceType.BISHOP, Color.BLACK);
        squares[7][5] = new Piece(PieceType.BISHOP, Color.BLACK);
        // Queens
        squares[0][3] = new Piece(PieceType.QUEEN, Color.WHITE);
        squares[7][3] = new Piece(PieceType.QUEEN, Color.BLACK);
        // Kings
        squares[0][4] = new Piece(PieceType.KING, Color.WHITE);
        squares[7][4] = new Piece(PieceType.KING, Color.BLACK);
    }

    public Piece getPiece(Square square) {
        return squares[square.getRank()][square.getFile()];
    }

    public void setPiece(Square square, Piece piece) {
        squares[square.getRank()][square.getFile()] = piece;
    }

    public void printBoard() {
        for(int r=7;r>=0;r--) {
            System.out.print((r+1) + " ");
            for(int f=0;f<8;f++) {
                Piece p = squares[r][f];
                System.out.print(p == null ? "-- " : p + " ");
            }
            System.out.println();
        }
        System.out.println("   a  b  c  d  e  f  g  h");
    }
}