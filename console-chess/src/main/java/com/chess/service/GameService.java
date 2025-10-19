package com.chess.service;

import java.util.List;

import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.GameStatus;
import com.chess.rules.CheckDetector;
import com.chess.rules.RulesEngine;

public final class GameService {
    private GameService() {}

    public static Color opponentOf(Color c) {
        return (c == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    /** Returns IN_PROGRESS, CHECK, CHECKMATE or STALEMATE for the side to move. */
    public static GameStatus assessStatus(GameState state) {
        boolean inCheck = CheckDetector.isKingInCheck(state, state.getToMove());
        List<com.chess.domain.Move> moves = RulesEngine.generateLegalMoves(state);
        if (moves.isEmpty()) {
            return inCheck ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
        }
        return inCheck ? GameStatus.CHECK : GameStatus.IN_PROGRESS;
    }

    /** Helper used previously to announce "Check!" after a turn switch. */
    public static boolean sideToMoveIsInCheck(GameState state) {
        return CheckDetector.isKingInCheck(state, state.getToMove());
    }
}