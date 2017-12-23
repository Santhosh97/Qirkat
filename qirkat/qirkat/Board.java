package qirkat;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import static qirkat.Move.*;
import static qirkat.PieceColor.*;

/**
 * A Qirkat board.   The squares are labeled by column (a char value between
 * 'a' and 'e') and row (a char value between '1' and '5'.
 * <p>
 * For some purposes, it is useful to refer to squares using a single
 * integer, which we call its "linearized index".  This is simply the
 * number of the square in row-major order (with row 0 being the bottom row)
 * counting from 0).
 * <p>
 * Moves on this board are denoted by Moves.
 *
 * @author Santhosh Subramanian
 */
class Board extends Observable {
    /**
     * Convenience value giving values of pieces at each ordinal position.
     */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

   /** Setup Board. **/
    private final PieceColor[] setup = {
        WHITE, WHITE, WHITE, WHITE, WHITE,
        WHITE, WHITE, WHITE, WHITE, WHITE,
        BLACK, BLACK, EMPTY, WHITE, WHITE,
        BLACK, BLACK, BLACK, BLACK, BLACK,
        BLACK, BLACK, BLACK, BLACK, BLACK,
    };
    /**
     * Creates _board.
     */
    private PieceColor[] _board = new PieceColor[MAX_INDEX + 1];

    /** New Horizontal board _h. **/
    private char[][] _h = new char[MAX_INDEX + 1][2];

    /**
     * A copy of B.
     */
    private Stack<PieceColor[]> firstBoard = new Stack<>();
    /** NEWBOARD.**/
    private PieceColor[] newBoard = new PieceColor[MAX_INDEX + 1];
    /**
     * Player that is on move.
     */
    private PieceColor _whoseMove;
    /**
     * Set true when game ends.
     */
    private boolean _gameOver;


    /**
     * A new, cleared board at the start of the game.
     */
    Board() {
        clear();
    }
    /** New Copy of B. **/
    Board(Board b) {
        internalCopy(b);
    }

    /**
     * Return a constant view of me (allows any access method, but no
     * method that modifies it).
     */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /**
     * Clear me to my starting state, with pieces in their initial
     * positions.
     */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;
        setPieces("wwwwwwwwwwbb-wwbbbbbbbbbb", _whoseMove);
        setChanged();
        notifyObservers();

    }
    /** Undo. **/
    void undo() {
        nextMove();
        if (firstBoard.size() > 0) {
            this._board = copyBoard(firstBoard.pop());
            nextMove();
        }
        setChanged();
        notifyObservers();
    }
    /** Nextmove. **/
    void nextMove() {
        if (whoseMove().equals(WHITE)) {
            _whoseMove = BLACK;
            return;
        }
        if (whoseMove().equals(BLACK)) {
            _whoseMove = WHITE;
            return;
        }
    }
    /** RETURNS array based on BEGIN. **/
    private PieceColor[] copyBoard(PieceColor[] begin) {
        if (begin != null) {
            PieceColor[] output = newboard(begin);
            return output;
        }
        return setup;
    }

    /**
     * Copy B into me.
     */
    void copy(Board b) {
        internalCopy(b);
    }

    /**
     * Copy B into me.
     */
    private void internalCopy(Board b) {
        _whoseMove = b._whoseMove;
        if (_board != null) {
            for (int i = 0; i < MAX_INDEX + 1; i++) {
                set(i, b.get(i));
            }
        }
        _gameOver = b._gameOver;
    }

    /**
     * Set my contents as defined by STR.  STR consists of 25 characters,
     * each of which is b, w, or -, optionally interspersed with whitespace.
     * These give the contents of the Board in row-major order, starting
     * with the bottom row (row 1) and left column (column a). All squares
     * are initialized to allow horizontal movement in either direction.
     * NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }
        for (int k = 0; k < str.length(); k += 1) {
            switch (str.charAt(k)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b':
            case 'B':
                set(k, BLACK);
                break;
            case 'w':
            case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }
        _whoseMove = nextMove;

        setChanged();
        notifyObservers();
    }

    /**
     * Return true iff the game is over: i.e., if the current player has
     * no moves.
     */
    boolean gameOver() {
        return _gameOver;
    }

    /**
     * Return the current contents of square C R, where 'a' <= C <= 'e',
     * and '1' <= R <= '5'.
     */
    PieceColor get(char c, char r) {
        if (validSquare(c, r)) {
            return get(index(c, r));
        } else {
            return null;
        }
    }

    /**
     * Return the current contents of the square at linearized index K.
     */
    PieceColor get(int k) {
        if (validSquare(k)) {
            return this._board[k];
        } else {
            return null;
        }
    }

    /**
     * for (PieceColor piece : _board) Checks COLOR.
     * returns int pieces
     */
    int number(PieceColor color) {
        int count = 0;
        for (int i = 0; i < _board.length; i++) {
            if (color == get(i)) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Set get(C, R) to V, where 'a' <= C <= 'e', and
     * '1' <= R <= '5'.
     */
    private void set(char c, char r, PieceColor v) {
        if (validSquare(c, r)) {
            set(index(c, r), v);
        }
    }

    /**
     * Set get(K) to V, where K is the linearized index of a square.
     */
    private void set(int k, PieceColor v) {
        if (validSquare(k)) {
            _board[k] = v;
        }
    }

    /** Returns whether or not MOV is
     * legal. */
    boolean legalMove(Move mov) {
        return getMoves().contains(mov);
    }

    /**
     * Return a list of all legal moves from the current position.
     */
    public ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        if (result.isEmpty()) {
            _gameOver = true;
        }
        return result;
    }
    /** Add all legal moves from the
     * current position to MOVES.*/
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                if (get(k) == whoseMove()) {
                    getJumps(moves, k);
                }
            }
            if (moves.size() > 0) {
                ArrayList<Move> total = combiner(moves);
                moves.clear();
                moves.addAll(total);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                if (get(k) == whoseMove()) {
                    getMoves(moves, k);
                }
            }
        }
    }

    /** Checks whether or not values match based on
     * I, COL, ROW given by MOVES. RETURNS true or false
     **/
    private boolean check(ArrayList<Move> moves, int i,
                          char col, char row) {
        char col1 = moves.get(i).col0();
        char row2 = moves.get(i).row0();
        return col == col1 && row == row2;
    }
    /** RETURNS arraylist of combined MOVES. **/
    private ArrayList<Move> combiner(ArrayList<Move> moves) {
        ArrayList<Move> total = new ArrayList<>();
        total.add(moves.get(0)); char col = moves.get(0).col1();
        char row = moves.get(0).row1();
        int a = 0; int b = 0;
        for (int i = 1; i < moves.size(); i++) {
            if (!check(moves, i, col, row)) {
                total.add(moves.get(a)); b++;
                char startingCol = moves.get(a).col0();
                char startingRow = moves.get(a).row0();
                col = moves.get(a).col1();
                row = moves.get(a).row1();
                int j = a + 1;
                while (!check(moves, i, col, row) && j < i) {
                    if (check(moves, j, col, row)) {
                        total.set(b, movegenerator(total, moves, b, j));
                    } else {
                        total.set(b, movegenerator(moves, moves, a, j));
                    }
                    col = moves.get(j).col1();
                    row = moves.get(j).row1();
                    j++;
                }
                if (j != i && !check(moves, j, startingCol, startingRow)) {
                    total.set(b, Move.move(total.get(b), moves.get(i)));
                } else {
                    total.set(b, moves.get(i));
                    a = i;
                }
                col = moves.get(i).col1();
                row = moves.get(i).row1();
            } else {
                total.set(b, movegenerator(total, moves, b, i));
                col = moves.get(i).col1();
                row = moves.get(i).row1();
            }
        }
        return total;
    }
    /** RETURNS moves based on MOVE, NEWMOVE, F, I. **/
    private Move movegenerator(ArrayList<Move> move,
        ArrayList<Move> newmove, int f, int i) {
        return Move.move(move.get(f), newmove.get(i));
    }
    /**
     * Add all legal non-capturing moves from the position
     * with linearized index K to MOVES.
     */
    private void getMoves(ArrayList<Move> moves, int k) {
        PieceColor mypiece = get(k);
        if (validSquare(k)) {
            if (mypiece.equals(BLACK) && whoseMove().equals(BLACK)) {
                blackpieces(moves, k);
            }
            if (mypiece.equals(WHITE) && whoseMove().equals(WHITE)) {
                whitepieces(moves, k);
            }
        }
    }
    /** Given COL, ROW, K, RETURNS true or false. **/
    private boolean checkhorizontal(char col, char row, int k) {
        return (col != _h[k][0]) || (row != _h[k][1]);
    }
    /** Given MOVES, K RETURNS arraylist moves of blackpieces. **/
    private ArrayList<Move> blackpieces(ArrayList<Move> moves, int k) {
        char col = col(k);
        char row = row(k);
        char south = (char) (row(k) - 1);
        char west = (char) (col(k) - 1);
        char east = (char) (col(k) + 1);
        int west2 = k - 1;
        int east2 = k + 1;
        if (checker(west, row) && !(row == '1')
                && checkhorizontal(col, row, west2)) {
            moves.add(get("west", col, row, k, 1));
        }
        if (checker(east, row) && !(row == '1')
                && checkhorizontal(col, row, east2)) {
            moves.add(get("east", col, row, k, 1));
        }
        if (checker(col, south)) {
            moves.add(get("south", col, row, k, 1));
        }
        if (k % 2 == 0) {
            if (checker(west, south)) {
                moves.add(get("southwest", col, row, k, 1));
            }
            if (checker(east, south)) {
                moves.add(get("southeast", col, row, k, 1));
            }
        }
        return moves;
    }

    /**
     * Gets all MOVES for white pieces using index K.
     * Return arraylist moves.
     */
    private ArrayList<Move> whitepieces(ArrayList<Move> moves, int k) {
        char col = col(k);
        char row = row(k);
        char north = (char) (row(k) + 1);
        char west = (char) (col(k) - 1);
        char east = (char) (col(k) + 1);
        int west2 = k - 1;
        int east2 = k + 1;
        if (checker(west, row) && !(row == '5')
                && checkhorizontal(col, row, west2)) {
            moves.add(get("west", col, row, k, 1));
        }
        if (checker(east, row) && !(row == '5')
                && checkhorizontal(col, row, east2)) {
            moves.add(get("east", col, row, k, 1));
        }
        if (checker(col, north)) {
            moves.add(get("north", col, row, k, 1));
        }
        if (k % 2 == 0) {
            if (checker(west, north)) {
                moves.add(get("northwest", col, row, k, 1));
            }
            if (checker(east, north)) {
                moves.add(get("northeast", col, row, k, 1));
            }
        }
        return moves;
    }

    /**
     * Given COL and ROW returns whether the tile is empty.
     */
    private boolean checker(char col, char row) {
        return get(col, row) == EMPTY;
    }

    /**
     * Given COL, MYPIECE, BOARD, and ROW returns whether the tile is empty.
     */
    private boolean firstjumpchecker(char col, char row,
        PieceColor mypiece, PieceColor[] board) {
        return validSquare(col, row)
                && board[index(col, row)]
                != mypiece && board[index(col, row)] != EMPTY;
    }

    /**
     * Given COL, BOARD, and ROW returns whether the tile is empty.
     */
    private boolean secondjumpchecker(char col,
        char row, PieceColor[] board) {
        return validSquare(col, row)
              && board[index(col, row)] == EMPTY;
    }

    /**
     * Given K, COL, ROW, J, and STEP returns a move.
     */
    private Move get(String k, char col, char row, int j, int step) {
        char west = (char) (col(j) - step);
        char east = (char) (col(j) + step);
        char north = (char) (row(j) + step);
        char south = (char) (row(j) - step);
        if (k.equals("west")) {
            return Move.move(col, row, west, row);
        } else if (k.equals("east")) {
            return Move.move(col, row, east, row);
        } else if (k.equals("north")) {
            return Move.move(col, row, col, north);
        } else if (k.equals("south")) {
            return Move.move(col, row, col, south);
        } else if (k.equals("southwest")) {
            return Move.move(col, row, west, south);
        } else if (k.equals("northwest")) {
            return Move.move(col, row, west, north);
        } else if (k.equals("southeast")) {
            return Move.move(col, row, east, south);
        } else {
            return Move.move(col, row, east, north);
        }
    }

    /**
     * Add all legal captures from the position with linearized index K
     * to MOVES.
     */
    private void getJumps(ArrayList<Move> moves, int k) {
        PieceColor mypiece = get(k);
        PieceColor[] boardCopy = newboard(_board);
        if (validSquare(k) && mypiece != EMPTY && whoseMove().equals(mypiece)) {
            piecesjump(moves, k, boardCopy);
        }
    }

    /** Add all moves from the north given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
        northjump(ArrayList<Move> moves,
                  int k, PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char north2 = (char) (row(k) + 2);
        char north = (char) (row(k) + 1);
        char col = col(k);
        char row = row(k);
        if (firstjumpchecker(col, north, mypiece, copyboard)
                && secondjumpchecker(col, north2, copyboard)) {
            Move created = get("north", col, row, k, 2);
            moves.add(created);
            int i = index(col, north2);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }
    }
    /** Add all moves from the south given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
        southjump(ArrayList<Move> moves,
                  int k, PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char col = col(k);
        char row = row(k);
        char south = (char) (row(k) - 1);
        char south2 = (char) (row(k) - 2);
        if (firstjumpchecker(col, south, mypiece, copyboard)
                && secondjumpchecker(col, south2, copyboard)) {
            Move created = get("south", col, row, k, 2);
            moves.add(created);
            int i = index(col, south2);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }
    }
    /** Add all moves from the west given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
        westjump(ArrayList<Move> moves,
                 int k, PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char west = (char) (col(k) - 1);
        char west2 = (char) (col(k) - 2);
        char col = col(k);
        char row = row(k);
        if (firstjumpchecker(west, row, mypiece, copyboard)
                && secondjumpchecker(west2, row, copyboard)
                && validSquare(west, row)
                && validSquare(west2, row)) {
            Move created = get("west", col, row, k, 2);
            moves.add(created);
            int i = index(west2, row);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }
    }
    /** Add all moves from the east given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
        eastjump(ArrayList<Move> moves,
                 int k, PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char col = col(k);
        char row = row(k);
        char east = (char) (col(k) + 1);
        char east2 = (char) (col(k) + 2);
        if (firstjumpchecker(east, row, mypiece, copyboard)
                && secondjumpchecker(east2, row, copyboard)
                && validSquare(east, row)
                && validSquare(east2, row)) {
            Move created = get("east", col, row, k, 2);
            moves.add(created);
            int i = index(east2, row);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }
    }
    /** Add all moves from the southwest given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
        southwestjump(ArrayList<Move> moves,
                      int k, PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char col = col(k);
        char row = row(k);
        char south = (char) (row(k) - 1);
        char south2 = (char) (row(k) - 2);
        char west = (char) (col(k) - 1);
        char west2 = (char) (col(k) - 2);
        if (firstjumpchecker(west, south, mypiece, copyboard)
                && secondjumpchecker(west2, south2, copyboard)
                && validSquare(west, south)
                && validSquare(west2, south2)) {
            Move created = get("southwest", col, row, k, 2);
            moves.add(created);
            int i = index(west2, south2);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }

    }
    /** Add all moves from the southeast given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
         southeastjump(ArrayList<Move> moves,
                       int k, PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char col = col(k);
        char row = row(k);
        char south = (char) (row(k) - 1);
        char south2 = (char) (row(k) - 2);
        char east = (char) (col(k) + 1);
        char east2 = (char) (col(k) + 2);
        if (firstjumpchecker(east, south, mypiece, copyboard)
                && secondjumpchecker(east2, south2, copyboard)
                && validSquare(east, south)
                && validSquare(east2, south2)) {
            Move created = get("southeast", col, row, k, 2);
            moves.add(created);
            int i = index(east2, south2);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }
    }
    /** Add all moves from the northeast given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
        northeastjump(ArrayList<Move> moves,
                      int k, PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char col = col(k);
        char row = row(k);
        char north2 = (char) (row(k) + 2);
        char north = (char) (row(k) + 1);
        char east = (char) (col(k) + 1);
        char east2 = (char) (col(k) + 2);
        if (firstjumpchecker(east, north, mypiece, copyboard)
                && secondjumpchecker(east2, north2, copyboard)
                && validSquare(east, north)
                && validSquare(east2, north2)) {
            Move created = get("northeast", col, row, k, 2);
            moves.add(created);
            int i = index(east2, north2);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }
    }
    /** Add all moves from the northwest given MOVES, K, MYPIECE,
     * COPYBOARD. */
    private void
        northwestjump(ArrayList<Move> moves, int k,
                      PieceColor[] copyboard) {
        PieceColor mypiece = whoseMove();
        char col = col(k);
        char row = row(k);
        char west = (char) (col(k) - 1);
        char west2 = (char) (col(k) - 2);
        char north2 = (char) (row(k) + 2);
        char north = (char) (row(k) + 1);
        if (firstjumpchecker(west, north, mypiece, copyboard)
                && secondjumpchecker(west2, north2, copyboard)
                && validSquare(west2, north2)
                && validSquare(west, north)) {
            Move created = get("northwest", col, row, k, 2);
            moves.add(created);
            int i = index(west2, north2);
            piecesjump(moves, i, boardcopy(created, copyboard));
        }
    }

    /**
     * Gets all MOVES for whitepieces using index K, COPYBOARD
     * and MYPIECE. Return arraylist MOVES.
     */
    private ArrayList<Move>
        piecesjump(ArrayList<Move> moves, int k, PieceColor[] copyboard) {
        northjump(moves, k, copyboard);
        southjump(moves, k, copyboard);
        westjump(moves, k, copyboard);
        eastjump(moves, k, copyboard);
        if (k % 2 == 0) {
            southeastjump(moves, k, copyboard);
            northwestjump(moves, k, copyboard);
            southwestjump(moves, k, copyboard);
            northeastjump(moves, k, copyboard);
        }
        return moves;
    }

    /** Copys board using MOV and PREVBOARD, RETURNS
     * a BOARD.*/
    private PieceColor[] boardcopy(Move mov,
                                   PieceColor[] prevBoard) {

        PieceColor[] createdBoard = new PieceColor[MAX_INDEX + 1];
        for (int i = 0; i <= MAX_INDEX; i++) {
            createdBoard[i] = prevBoard[i];
        }
        editBoard(createdBoard, mov);
        createdBoard[mov.jumpedIndex()] = EMPTY;
        return createdBoard;
    }

    /**
     * Return true iff MOV is a valid jump sequence on the current board.
     * MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     * could be continued and are valid as far as they go.
     */
    boolean checkJump(Move mov, boolean allowPartial) {
        if (mov == null) {
            return true;
        } else if (mov.isJump()) {
            return checkJump(mov.jumpTail(), allowPartial);
        } else {
            return false;
        }
    }

    /**
     * Return true iff a jump is possible for a piece at position C R.
     */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /**
     * Return true iff a jump is possible for a piece at position with
     * linearized index K.
     */
    boolean jumpPossible(int k) {
        ArrayList<Move> check = new ArrayList<>();
        getJumps(check, k);
        if (check.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Return true iff a jump is possible from the current board.
     */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the color of the player who has the next move.  The
     * value is arbitrary if gameOver().
     */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Resets horizontal array based on
     * MOV.*/
    private void reset(Move mov) {
        int index = index(mov.col0(), mov.row0());
        _h[index][0] = mov.col1(); _h[index][1] = mov.row1();
        char col = mov.col0(); char left = (char) (mov.col0() + 1);
        int left2 = index + 1; int right2 = index - 1;
        char right = (char) (mov.col0() - 1); char row = mov.row0();
        char up = (char) (mov.row0() + 1); char down = (char) (mov.row0() - 1);
        if (mov.isLeftMove()) {
            if (validSquare(left, row)) {
                if (_h[left2][0] == col && _h[left2][1] == row) {
                    _h[left2][0] = 'g'; _h[left2][1] = '8';
                }
            }
        }
        if (mov.isRightMove()) {
            if (validSquare(right, row)) {
                if (_h[right2][0] == col && _h[right2][1] == row) {
                    _h[right2][0] = 'g'; _h[right2][1] = '8';
                }
            }
        }
        if (mov.isJump()) {
            if (validSquare(left, row)) {
                if (_h[left2][0] == col && _h[left2][1] == row) {
                    _h[left2][0] = 'g'; _h[left2][1] = '8';
                }
            }
            if (validSquare(right, row)) {
                if (_h[right2][0] == col && _h[right2][1] == row) {
                    _h[right2][0] = 'g'; _h[right2][1] = '8';
                }
            }
            if (validSquare(right, up)) {
                int upright = index(right, up) - 1;
                if (_h[upright][0] == right && _h[upright][1] == up) {
                    _h[upright][0] = 'g'; _h[upright][1] = '8';
                }
            }
            if (validSquare(left, up)) {
                int upleft = index(left, up) - 1;
                if (_h[upleft][0] == left && _h[upleft][1] == up) {
                    _h[upleft][0] = 'g'; _h[upleft][1] = '8';
                }
            }
            if (validSquare(right, down)) {
                int downright = index(right, down) + 1;
                if (_h[downright][0] == right && _h[downright][1] == down) {
                    _h[downright][0] = 'g'; _h[downright][1] = '8';
                }
            }
            if (validSquare(left, down)) {
                int downleft = index(left, down) + 1;
                if (_h[downleft][0] == left && _h[downleft][1] == down) {
                    _h[downleft][0] = 'g'; _h[downleft][1] = '8';
                }
            }
        }
    }

    /**
     * Make the Move MOV on this Board, assuming it is legal.
     */
    void makeMove(Move mov) {
        try {
            assert legalMove(mov);
            if (mov.jumpTail() == null) {
                firstBoard.push(copyBoard(_board));
            }
            reset(mov);
            if (!mov.isJump() && mov.jumpTail() == null) {
                editBoard(_board, mov);
            } else if (mov.isJump() && mov.jumpTail() == null) {
                _board[mov.jumpedIndex()] = EMPTY;
                editBoard(_board, mov);
            } else {
                _board[mov.jumpedIndex()] = EMPTY;
                editBoard(_board, mov);
                makeMove(mov.jumpTail());
            }
            if (mov.jumpTail() == null) {
                nextMove();
            }
            setChanged();
            notifyObservers();
        } catch (AssertionError e) {
            System.out.println("Illegal move");
        }
    }

    /** Edits Board based on BOARD and MOV. **/
    void editBoard(PieceColor[] board, Move mov) {
        board[mov.toIndex()] = board[mov.fromIndex()];
        board[mov.fromIndex()] = EMPTY;
    }

    /** Returns a board extracted from ARRAY. **/
    private PieceColor[] newboard(PieceColor[] array) {
        for (int i = 0; i < MAX_INDEX + 1; i++) {
            newBoard[i] = array[i];
        }
        return newBoard;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Return a text depiction of the board.  If LEGEND, supply row and
     * column numbers around the edges.
     */
    String toString(boolean legend) {
        String output = "  ";
        for (char r = '5'; r >= '0'; r--) {
            if (legend && r >= '1') {
                output += String.valueOf(r) + "  ";
            }
            for (char c = 'a'; c < 'f'; c++) {
                if (validSquare(c, r)) {
                    int k = index(c, r);
                    if (get(k) == WHITE) {
                        output += "w";
                    } else if (get(k) == BLACK) {
                        output += "b";
                    } else {
                        output += "-";
                    }
                    if (k % 5 == 4 && k != 4) {
                        output += "\n  ";
                    } else if (k != 4) {
                        output += " ";
                    } else {
                        output += "";
                    }
                }
            }
        }
        if (legend) {
            output += "\n    ";
            for (char c = 'a'; c <= 'e'; c++) {
                output +=  String.valueOf(c) + " ";
            }
        }
        return output;
    }

    /**
     * Return true iff there is a move for the current player.
     */
    private boolean isMove() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Board) {
            Board other = (Board) o;
            return (toString().equals(other.toString())
                    && whoseMove() == other.whoseMove()
                    && gameOver() == other.gameOver());
        }
        return false;
    }

    /** HASHCODE to override
     * RETURNS int. **/
    public int hashCode() {
        return super.hashCode();
    }

    /** Sets Game over to true. **/
    public void setgameOver() {
        _gameOver = true;
    }
    /**
     * One cannot create arrays of ArrayList<Move>, so we introduce
     * a specialized private list type for this purpose.
     */
    private static class MoveList extends ArrayList<Move> {
    }

    /**
     * A read-only view of a Board.
     */
    private class ConstantBoard extends Board implements Observer {
        /**
         * A constant view of this Board.
         */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /**
         * Undo the last move.
         */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}
