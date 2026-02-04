package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        if (type == PieceType.KING) {
            addKingMoves(board, myPosition, moves);
        } else if (type == PieceType.QUEEN) {
            addQueenMoves(board, myPosition, moves);
        } else if (type == PieceType.BISHOP) {
            addBishopMoves(board, myPosition, moves);
        } else if (type == PieceType.KNIGHT) {
            addKnightMoves(board, myPosition, moves);
        } else if (type == PieceType.ROOK) {
            addRookMoves(board, myPosition, moves);
        } else if (type == PieceType.PAWN) {
            addPawnMoves(board, myPosition, moves);
        }

        return moves;
    }

    private void addRookMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        addSlidingMoves(board, myPosition, moves, directions);
    }

    private void addBishopMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves) {
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        addSlidingMoves(board, myPosition, moves, directions);
    }

    private void addQueenMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        addSlidingMoves(board, myPosition, moves, directions);
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves, int[][] directions) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (int i = 0; i < directions.length; i++) {
            int rowDir = directions[i][0];
            int colDir = directions[i][1];
            int newRow = row + rowDir;
            int newCol = col + colDir;

            while (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPosition = board.getPiece(newPosition);

                if (pieceAtPosition == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else if (pieceAtPosition.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    break;
                }

                newRow = newRow + rowDir;
                newCol = newCol + colDir;
            }
        }
    }

    private void addKnightMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int[][] knightMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int i = 0; i < knightMoves.length; i++) {
            int newRow = row + knightMoves[i][0];
            int newCol = col + knightMoves[i][1];

            if (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPosition = board.getPiece(newPosition);

                if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }

    private void addKingMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int[][] kingMoves = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int i = 0; i < kingMoves.length; i++) {
            int newRow = row + kingMoves[i][0];
            int newCol = col + kingMoves[i][1];

            if (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPosition = board.getPiece(newPosition);

                if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }

    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int direction;
        int startRow;
        int promotionRow;

        if (pieceColor == ChessGame.TeamColor.WHITE) {
            direction = 1;
            startRow = 2;
            promotionRow = 8;
        } else {
            direction = -1;
            startRow = 7;
            promotionRow = 1;
        }

        int newRow = row + direction;

        if (isValidPosition(newRow, col)) {
            ChessPosition newPosition = new ChessPosition(newRow, col);
            if (board.getPiece(newPosition) == null) {
                if (newRow == promotionRow) {
                    addPromotionMoves(myPosition, newPosition, moves);
                } else {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }

                if (row == startRow) {
                    int doubleRow = row + 2 * direction;
                    ChessPosition doublePosition = new ChessPosition(doubleRow, col);
                    if (board.getPiece(doublePosition) == null) {
                        moves.add(new ChessMove(myPosition, doublePosition, null));
                    }
                }
            }
        }

        int[] captureCols = {col - 1, col + 1};
        for (int i = 0; i < captureCols.length; i++) {
            int captureCol = captureCols[i];
            if (isValidPosition(newRow, captureCol)) {
                ChessPosition capturePosition = new ChessPosition(newRow, captureCol);
                ChessPiece pieceAtPosition = board.getPiece(capturePosition);

                if (pieceAtPosition != null && pieceAtPosition.getTeamColor() != this.pieceColor) {
                    if (newRow == promotionRow) {
                        addPromotionMoves(myPosition, capturePosition, moves);
                    } else {
                        moves.add(new ChessMove(myPosition, capturePosition, null));
                    }
                }
            }
        }
    }

    private void addPromotionMoves(ChessPosition start, ChessPosition end, ArrayList<ChessMove> moves) {
        moves.add(new ChessMove(start, end, PieceType.QUEEN));
        moves.add(new ChessMove(start, end, PieceType.ROOK));
        moves.add(new ChessMove(start, end, PieceType.BISHOP));
        moves.add(new ChessMove(start, end, PieceType.KNIGHT));
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" + "pieceColor=" + pieceColor + ", type=" + type + '}';
    }
}
