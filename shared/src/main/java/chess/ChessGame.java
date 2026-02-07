package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove;
    private boolean whiteKingMoved;
    private boolean whiteRookAMoved;
    private boolean whiteRookHMoved;
    private boolean blackKingMoved;
    private boolean blackRookAMoved;
    private boolean blackRookHMoved;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
        lastMove = null;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        ArrayList<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            if (!moveLeavesKingInCheck(move, piece.getTeamColor())) {
                validMoves.add(move);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            addEnPassantMoves(startPosition, piece, validMoves);
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            addCastlingMoves(startPosition, piece, validMoves);
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        boolean isEnPassant = piece.getPieceType() == ChessPiece.PieceType.PAWN
                && Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 1
                && board.getPiece(move.getEndPosition()) == null;

        boolean isCastling = piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2;

        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }

        if (isEnPassant) {
            ChessPosition capturedPawnPos = new ChessPosition(
                    move.getStartPosition().getRow(), move.getEndPosition().getColumn());
            board.addPiece(capturedPawnPos, null);
        }

        if (isCastling) {
            int row = move.getStartPosition().getRow();
            int direction = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            if (direction > 0) {
                board.addPiece(new ChessPosition(row, 8), null);
                board.addPiece(new ChessPosition(row, 6), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK));
            } else {
                board.addPiece(new ChessPosition(row, 1), null);
                board.addPiece(new ChessPosition(row, 4), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK));
            }
        }

        updateMovedFlags(move.getStartPosition(), move.getEndPosition());
        lastMove = move;
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isKingInCheck(board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return !hasAnyValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return !hasAnyValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        this.lastMove = null;
        this.whiteKingMoved = false;
        this.whiteRookAMoved = false;
        this.whiteRookHMoved = false;
        this.blackKingMoved = false;
        this.blackRookAMoved = false;
        this.blackRookHMoved = false;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /** Checks if making a move would leave the team's king in check */
    private boolean moveLeavesKingInCheck(ChessMove move, TeamColor teamColor) {
        ChessBoard testBoard = board.copy();

        ChessPiece piece = testBoard.getPiece(move.getStartPosition());
        testBoard.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            testBoard.addPiece(move.getEndPosition(), new ChessPiece(teamColor, move.getPromotionPiece()));
        } else {
            testBoard.addPiece(move.getEndPosition(), piece);
        }

        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int colDiff = Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn());
            if (colDiff == 1 && board.getPiece(move.getEndPosition()) == null) {
                ChessPosition capturedPawnPos = new ChessPosition(
                        move.getStartPosition().getRow(), move.getEndPosition().getColumn());
                testBoard.addPiece(capturedPawnPos, null);
            }
        }

        return isKingInCheck(testBoard, teamColor);
    }

    /** Checks if the king of the given team is in check on the given board */
    private boolean isKingInCheck(ChessBoard testBoard, TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(testBoard, teamColor);
        if (kingPosition == null) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = testBoard.getPiece(pos);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(testBoard, pos);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Finds the position of the king for the given team */
    private ChessPosition findKingPosition(ChessBoard testBoard, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = testBoard.getPiece(pos);

                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING
                        && piece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }
        return null;
    }

    /** Adds castling moves if its available */
    private void addCastlingMoves(ChessPosition position, ChessPiece piece, ArrayList<ChessMove> validMoves) {
        TeamColor color = piece.getTeamColor();

        if (isInCheck(color)) {
            return;
        }

        int row = position.getRow();
        boolean kingMoved;
        boolean rookAMoved;
        boolean rookHMoved;

        if (color == TeamColor.WHITE) {
            kingMoved = whiteKingMoved;
            rookAMoved = whiteRookAMoved;
            rookHMoved = whiteRookHMoved;
        } else {
            kingMoved = blackKingMoved;
            rookAMoved = blackRookAMoved;
            rookHMoved = blackRookHMoved;
        }

        if (kingMoved) {
            return;
        }

        if (!rookHMoved) {
            ChessPosition rookPos = new ChessPosition(row, 8);
            ChessPiece rook = board.getPiece(rookPos);
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK && rook.getTeamColor() == color) {
                if (board.getPiece(new ChessPosition(row, 6)) == null
                        && board.getPiece(new ChessPosition(row, 7)) == null) {
                    ChessMove throughMove = new ChessMove(position, new ChessPosition(row, 6), null);
                    ChessMove destMove = new ChessMove(position, new ChessPosition(row, 7), null);
                    if (!moveLeavesKingInCheck(throughMove, color) && !moveLeavesKingInCheck(destMove, color)) {
                        validMoves.add(destMove);
                    }
                }
            }
        }

        if (!rookAMoved) {
            ChessPosition rookPos = new ChessPosition(row, 1);
            ChessPiece rook = board.getPiece(rookPos);
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK && rook.getTeamColor() == color) {
                if (board.getPiece(new ChessPosition(row, 2)) == null
                        && board.getPiece(new ChessPosition(row, 3)) == null
                        && board.getPiece(new ChessPosition(row, 4)) == null) {
                    ChessMove throughMove = new ChessMove(position, new ChessPosition(row, 4), null);
                    ChessMove destMove = new ChessMove(position, new ChessPosition(row, 3), null);
                    if (!moveLeavesKingInCheck(throughMove, color) && !moveLeavesKingInCheck(destMove, color)) {
                        validMoves.add(destMove);
                    }
                }
            }
        }
    }

    /** Updates flags tracking if kings and rooks have moved */
    private void updateMovedFlags(ChessPosition startPosition, ChessPosition endPosition) {
        int startRow = startPosition.getRow();
        int startCol = startPosition.getColumn();
        int endRow = endPosition.getRow();
        int endCol = endPosition.getColumn();

        if (startRow == 1 && startCol == 5) whiteKingMoved = true;
        if (startRow == 1 && startCol == 1) whiteRookAMoved = true;
        if (startRow == 1 && startCol == 8) whiteRookHMoved = true;
        if (startRow == 8 && startCol == 5) blackKingMoved = true;
        if (startRow == 8 && startCol == 1) blackRookAMoved = true;
        if (startRow == 8 && startCol == 8) blackRookHMoved = true;

        if (endRow == 1 && endCol == 1) whiteRookAMoved = true;
        if (endRow == 1 && endCol == 8) whiteRookHMoved = true;
        if (endRow == 8 && endCol == 1) blackRookAMoved = true;
        if (endRow == 8 && endCol == 8) blackRookHMoved = true;
    }

    /** en passant moves if available */
    private void addEnPassantMoves(ChessPosition position, ChessPiece piece, ArrayList<ChessMove> validMoves) {
        if (lastMove == null) {
            return;
        }

        ChessPiece lastPiece = board.getPiece(lastMove.getEndPosition());
        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return;
        }

        int lastRowDiff = Math.abs(lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow());
        if (lastRowDiff != 2) {
            return;
        }

        int ourRow = position.getRow();
        int ourCol = position.getColumn();
        int theirRow = lastMove.getEndPosition().getRow();
        int theirCol = lastMove.getEndPosition().getColumn();

        if (ourRow != theirRow || Math.abs(ourCol - theirCol) != 1) {
            return;
        }

        int direction = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
        ChessPosition enPassantTarget = new ChessPosition(ourRow + direction, theirCol);
        ChessMove enPassantMove = new ChessMove(position, enPassantTarget, null);

        if (!moveLeavesKingInCheck(enPassantMove, piece.getTeamColor())) {
            validMoves.add(enPassantMove);
        }
    }

    /** Checks if the team has any valid moves */
    private boolean hasAnyValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (moves != null && !moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    @Override
    public String toString() {
        return "ChessGame{" + "teamTurn=" + teamTurn + ", board=\n" + board + '}';
    }
}
