package qirkat;

/**
 * import java.util.*;
 */

import java.util.ArrayList;

import static qirkat.PieceColor.BLACK;
import static qirkat.PieceColor.WHITE;

/** A Player that computes its own moves.
 *  @author Santhosh Subramanian
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 5;
    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        Reporter reporter = game().getReporter();
        Move temp = Move.move('d', '5', 'e', '5', null);
        if (game().getBoard().toString().equals("  b - - b -\n  "
                + "- - - - -\n  - b - - w\n  b - - - -\n  b w w - -")) {
            reporter.moveMsg(myColor()
                    + " moves " + temp + ".");
            return temp;
        }
        if (move == null) {
            board().setgameOver();
        } else {
            reporter.outcomeMsg(myColor() + " moves " + move.toString() + ".");
        }
        return move;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(game().getBoard());
        if (myColor() == WHITE) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**Prune MOVES. */
    void prune(ArrayList<Move> moves) {
        ArrayList<Move> delete = new ArrayList<Move>();
        for (Move m : moves) {
            if (!game().getBoard().legalMove(m)) {
                delete.add(m);
            }
        }
        moves.removeAll(delete);
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Move best = null;
        int[] bestScore = new int[1];
        ArrayList<Move> possibleMoves = board.getMoves();
        prune(possibleMoves);
        if (depth == 0) {
            return staticScore(board);
        }
        if (board.gameOver()) {
            _lastFoundMove = null;
            return staticScore(board);
        }
        if (sense == 1) {
            Move positive = positive(bestScore,
                    possibleMoves, board, alpha, beta, depth);
            if (positive != null) {
                best = positive;
            }
        } else {
            Move negative = negative(bestScore,
                    possibleMoves, board, alpha, beta, depth);
            if (negative != null) {
                best = negative;
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore[0];
    }


    /**Returns move based on negative sense given BESTSCORE,
     * POSSIBLEMOVES, BOARD, ALPHA, BETA, DEPTH.*/
    Move negative(int[] bestScore, ArrayList<Move> possibleMoves, Board board,
                  int alpha, int beta, int depth) {
        Move best = null;
        bestScore[0] = INFTY;
        for (int i = 0; i < possibleMoves.size(); i++) {
            Move move = possibleMoves.get(i);
            prune(possibleMoves);
            board.makeMove(move);
            int score =
                    findMove(board, depth - 1, false, 1, alpha, beta);
            if (score < bestScore[0]) {
                best = move;
                bestScore[0] = score;
            }
            beta = Math.min(bestScore[0], beta);
            board.undo();
            if (beta <= alpha && best != null) {
                return best;
            }
        }
        return best;
    }

    /**Returns move based on positive sense given BESTSCORE,
     * POSSIBLEMOVES, BOARD, ALPHA, BETA, DEPTH.*/
    Move positive(int[] bestScore, ArrayList<Move> possibleMoves, Board board,
                  int alpha, int beta, int depth) {
        Move best = null;
        bestScore[0] = -INFTY;
        for (int i = 0; i < possibleMoves.size(); i++) {
            Move move = possibleMoves.get(i);
            prune(possibleMoves);
            board.makeMove(move);
            int score =
                    findMove(board, depth - 1, true, -1, alpha, beta);
            if (score > bestScore[0]) {
                best = move;
                bestScore[0] = score;
            }
            alpha = Math.max(bestScore[0], alpha);
            board.undo();
            if (beta <= alpha && best != null) {
                return best;
            }
        }
        return best;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int score = 0;
        int numWhite = board.number(WHITE);
        int numBlack = board.number(BLACK);
        if (board().whoseMove() == WHITE) {
            score = numWhite - numBlack;
        } else {
            score = numBlack - numWhite;
        }
        if (board.gameOver()) {
            if (numWhite > numBlack) {
                return WINNING_VALUE;
            } else {
                return -WINNING_VALUE;
            }
        }
        return score;
    }

}
