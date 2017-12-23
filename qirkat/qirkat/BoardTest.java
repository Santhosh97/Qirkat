package qirkat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests of the Board class.
 *
 * @author
 */
public class BoardTest {

    private static final String INIT_BOARD =
            "  b b b b b\n  b b b b b\n  b b - w w\n  w w w w w\n  w w w w w";

    private static final String INIT2_BOARD =
            "  - - - - -\n  - w - - -\n  - b b b -\n  - - - - -\n  - - - - -";

    private static final String[] GAME1 =
    {"c2-c3", "c4-c2",
     "c1-c3", "a3-c1",
     "c3-a3", "c5-c4",
     "a3-c5-c3",
    };

    private static final String[] GAME2 =
    {"b2-a2", "e5-e4",
     "a2-a3", "e4-e3",
     "a3-b3", "e3-e2",
     "b3-c3", "e2-e1",
     "c3-b4"
    };
    private static final String[] GAME4 =
    {
        "c2-c3"
    };
    private static final String[] GAME6 =
    {
        "a1-c1-a3-c3-a5-c5-c3-e1"
    };
    private static final String[] GAME5 =
    {
        "b1-b2"
    };
    private static final String[] GAME3 =
    {
        "b2-b4-d2-d4"
    };

    private static final String GAME3_BOARD =
        "  - - - - -\n  - - - w -\n  - - - - -\n  - - - - -\n  - - - - -";
    private static final String GAME2_BOARD =
        "  - - - - -\n  - w - - -\n  - - - - -\n  - - - - -\n  - - - - b";
    private static final String GAME6_BOARD =
        "  - - - - b\n  - - - - -\n  - - - - -\n  w - - - -\n  - - - - -";
    private static final String GAME1_BOARD =
        "  b b - b b\n  b - - b b\n  - - w w w\n  w - - w w\n  w w b w w";

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(Move.parseMove(s));
        }
    }

    @Test
    public void testInit1() {
        Board b0 = new Board();
        makeMoves(b0, GAME4);
    }

    @Test
    public void testMoves1() {
        Board b0 = new Board();
        b0.setPieces("------w----bbb-----------", PieceColor.WHITE);
        makeMoves(b0, GAME3);
        assertEquals(GAME3_BOARD, b0.toString());
    }

    @Test
    public void testMoves2() {
        Board b0 = new Board();
        b0.setPieces("------w-----------------b", PieceColor.WHITE);
        makeMoves(b0, GAME2);
        assertEquals(GAME2_BOARD, b0.toString());
    }

    @Test
    public void testMove3() {
        Board b0 = new Board();
        b0.setPieces("bbbbb-bbbb-w-www--wwwwbww", PieceColor.WHITE);
        makeMoves(b0, GAME5);
    }

    @Test
    public void testMoves4() {
        Board b0 = new Board();
        makeMoves(b0, GAME1);
        assertEquals(GAME1_BOARD, b0.toString());
    }

    @Test
    public void testmoves5() {
        Board b0 = new Board();
        b0.setPieces("wb----b-b--b----bb---b---", PieceColor.WHITE);
        makeMoves(b0, GAME6);

    }
    @Test
    public void testUndo() {
        Board b0 = new Board();
        Board b1 = b0;
        makeMoves(b0, GAME1);
        for (int i = 0; i < GAME1.length; i += 1) {
            b0.undo();
        }
        Board b2 = new Board(b0);
        assertEquals(b1, b2);

    }
}

