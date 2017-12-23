package qirkat;

/**
 * A Player that receives its moves from its Game's getMoveCmnd method.
 *
 * @author Santhosh Subramanian
 */
class Manual extends Player {

    /**
     * Identifies the player serving as a source of input commands.
     */
    private String _prompt;

    /**
     * A Player that will play MYCOLOR on GAME, taking its moves from
     * GAME.
     */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
        _prompt = myColor + ": ";
    }

    @Override
    Move myMove() {
        return Move.parseMove(game().getMoveCmnd(_prompt).operands()[0]);
    }
}

