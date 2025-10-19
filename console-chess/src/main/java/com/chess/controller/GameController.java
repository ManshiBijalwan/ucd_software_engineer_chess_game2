package com.chess.controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.chess.domain.Color;
import com.chess.domain.GameState;
import com.chess.domain.GameStatus;
import com.chess.domain.Move;
import com.chess.parser.AlgebraicParser;
import com.chess.persist.SaveLoadService;
import com.chess.rules.RulesEngine;
import com.chess.service.GameService;
import com.chess.service.MoveService;

public class GameController {

    public static void main(String[] args) {
        GameState state = new GameState();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Console Chess!");
        System.out.print("Enter White player's name: ");
        String whiteName = safeRead(scanner);
        System.out.print("Enter Black player's name: ");
        String blackName = safeRead(scanner);

        printHelp();

        while (true) {
            state.getBoard().printBoard();
            String currentName = (state.getToMove() == Color.WHITE) ? whiteName : blackName;
            System.out.println(currentName + " (" + state.getToMove() + ") to move.");
            System.out.print("Enter move (e.g., e2e4 or e7e8q), 'pip' for legal moves, 'hint' for help, 'save <file>', 'load <file>', or 'q' to quit: ");
            String input = safeRead(scanner).trim();

            // Quit
            if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit")) {
                System.out.println("Game ended. Goodbye!");
                break;
            }

            // Help
            if (input.equalsIgnoreCase("hint")) {
                printHelp();
                continue;
            }

            // pip: list legal moves
            if (input.equalsIgnoreCase("pip")) {
                List<Move> moves = RulesEngine.generateLegalMoves(state);
                String msg = moves.isEmpty()
                        ? "No legal moves."
                        : moves.stream().map(Move::toString).sorted().collect(Collectors.joining(" "));
                System.out.println(msg);
                continue;
            }

            // --- NEW: Save / Load commands ---
            if (input.startsWith("save ")) {
                String fn = input.substring(5).trim();
                if (fn.isEmpty()) {
                    System.out.println("Usage: save <filename>");
                    continue;
                }
                try (FileOutputStream fos = new FileOutputStream(fn)) {
                    SaveLoadService.save(state, fos);
                    System.out.println("Saved to " + fn);
                } catch (Exception e) {
                    System.out.println("Save failed: " + e.getMessage());
                }
                continue;
            }

            if (input.startsWith("load ")) {
                String fn = input.substring(5).trim();
                if (fn.isEmpty()) {
                    System.out.println("Usage: load <filename>");
                    continue;
                }
                try (FileInputStream fis = new FileInputStream(fn)) {
                    GameState loaded = SaveLoadService.load(fis);
                    state = loaded;   // keep player names, but replace the game state
                    System.out.println("Loaded from " + fn);
                } catch (Exception e) {
                    System.out.println("Load failed: " + e.getMessage());
                }
                continue;
            }
            // --- END NEW ---

            // Move input and play
            try {
                Move move = AlgebraicParser.parse(input);

                if (!RulesEngine.isLegalMove(state, move)) {
                    System.out.println("Illegal move. Try again.");
                    continue;
                }

                boolean kingCaptured = MoveService.apply(state, move);
                if (kingCaptured) {
                    System.out.println("King captured! Winner: " + currentName + " (" + state.getToMove() + ")");
                    state.getBoard().printBoard();
                    break;
                }

                // Switch turn, then assess endgame for the side NOW to move
                state.switchTurn();
                GameStatus status = GameService.assessStatus(state);

                if (status == GameStatus.CHECKMATE) {
                    String winnerName = (state.getToMove() == Color.WHITE) ? blackName : whiteName;
                    Color winnerColor = GameService.opponentOf(state.getToMove());
                    state.getBoard().printBoard();
                    System.out.println("Checkmate! Winner: " + winnerName + " (" + winnerColor + ")");
                    break;
                } else if (status == GameStatus.STALEMATE) {
                    state.getBoard().printBoard();
                    System.out.println("Stalemate! The game is a draw.");
                    break;
                } else if (status == GameStatus.CHECK) {
                    System.out.println("Check!");
                }

            } catch (Exception e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    private static String safeRead(Scanner sc) {
        String s = sc.nextLine();
        return (s == null) ? "" : s;
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  - Move: long algebraic like e2e4, e7e8q (promotion to q/r/b/n).");
        System.out.println("  - pip : list all legal moves for the side to move.");
        System.out.println("  - hint: show this help.");
        System.out.println("  - save <file> : save the current game to a text file.");
        System.out.println("  - load <file> : load a previously saved game from a text file.");
        System.out.println("  - q   : quit the game.");
    }
}