/*
 * MatrixUtil.java
 *
 * Created on August 13, 2003, 4:58 PM
 */

package prophyler.tool;

import Jama.*;
import prophyler.exception.*;

/** A class that facilitates easy matrix operations, using the Jama package.
 * @author  rkwok
 */
/* Compile command: javac -classpath (home)lib/Jama-1.0.1:(home) MatrixUtil.java */

public class MatrixUtil {
    
    /** Creates a new instance of MatrixUtil.
     */
    public MatrixUtil() {
    }
    
    /** Given a matrix, returns the diagonal of that matrix as a 1-D double array.  The input
     * matrix must have n x n dimensions.
     */
    public static double[] getDiagonal(Matrix m) throws ProphylerException {
        int numRows = m.getRowDimension();
        int numCols = m.getColumnDimension();
        
        if (numRows != numCols) {
            throw new ProphylerException("Cannot get diagonal of matrix where number of rows does not equal number of columns");
        }

        double[] diagonal = new double[numRows];        
        
        for (int i = 0; i < numRows; i++) {
            diagonal[i] = m.get(i, i);
        }
        
        return diagonal;
    }
    
    /** Takes an array of numbers and returns a matrix with those numbers in the diagonal and all
     * other elements set to 0.
     */
    public static Matrix getDiagonalMatrix(double[] diagonal) {
        int numElements = diagonal.length;
        double[][] array = new double[numElements][numElements];
        
        for (int i = 0; i < numElements; i++) {
            for (int j = 0; j < numElements; j++) {
                if (i == j) {
                    array[i][j] = diagonal[i];
                }
                else {
                    array[i][j] = 0;
                }
            }
        }
        
        return (new Matrix(array));
    }
    
    /** Creates an n x n matrix with the specified number in the diagonal, and all off-diagonal
     * entries set to 0.
     */
    public static Matrix getDiagonalMatrix(double number, int dimension) {
        double[][] array = new double[dimension][dimension];
        
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (i == j) {
                    array[i][j] = number;
                }
                else {
                    array[i][j] = 0;
                }
            }
        }
        
        return (new Matrix(array));
    }
    
    /** Given a matrix and row number, returns that row of numbers as a double array.
     */
    public static double[] getRow(Matrix m, int rowNum) throws ProphylerException {
        if (rowNum >= m.getRowDimension()) {
            throw new ProphylerException("Invalid row number " + rowNum + " for matrix");
        }
        
        double[][] array = m.getArray();
        double[] row = array[rowNum];
        
        return row;
    }
    
    /** Given a matrix and a column number, returns that column of numbers as a double array.
     */
    public static double[] getColumn(Matrix m, int colNum) throws ProphylerException {
        if (colNum >= m.getColumnDimension()) {
            throw new ProphylerException("Invalid column number " + colNum + " for matrix");
        }
        
        double[][] array = m.getArray();
        double[] col = new double[array.length];
        double num;
        
        for (int i = 0; i < array.length; i++) {
            num = array[i][colNum];
            col[i] = num;
        }
        
        return col;
    }

    /** Prints a tab-delimited matrix to the screen.
     */
    public static void print(Matrix m) {
        double[][] array = m.getArray();
        TextUtil.printTable(array);
    }
}
