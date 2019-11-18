package util;

import java.util.ArrayList;
import java.util.List;

public class Common {
    public static char[][] rotateMatrixBy90Degree(char[][] matrix) {
        int totalColsOfRotatedMatrix = matrix[0].length; //Total columns of Original Matrix
        int totalRowsOfRotatedMatrix = matrix.length; //Total rows of Original Matrix

        char[][] rotatedMatrix = new char[totalColsOfRotatedMatrix][totalRowsOfRotatedMatrix];

        for (int i = 0; i < totalRowsOfRotatedMatrix; i++) {
            for (int j = 0; j < totalColsOfRotatedMatrix; j++) {
                rotatedMatrix[j][totalRowsOfRotatedMatrix - 1 - i] = matrix[i][j];
            }
        }
        return rotatedMatrix;
    }

    public static void setRotatedMatrixList(List<char[][]> matrixList) {
        List<char[][]> RotatedMatrixList = new ArrayList<>();
        for (char[][] matrix : matrixList) {
            char[][] currMatrix = matrix;
            for (int i=0; i<3; i++){
                char[][] newMatrix = rotateMatrixBy90Degree(currMatrix);
                // 检查所有的matrix
                boolean isDuplicated = false;
                for (char[][] checkMatrix : matrixList) {
                    if (isMatrixDuplicated(checkMatrix, newMatrix)) {
                        isDuplicated = true;
                        break;
                    }
                }
                for (char[][] checkMatrix : RotatedMatrixList) {
                    if (isMatrixDuplicated(checkMatrix, newMatrix)) {
                        isDuplicated = true;
                        break;
                    }
                }
                if (!isDuplicated) {
                    RotatedMatrixList.add(newMatrix);
                }
                currMatrix = newMatrix;
            }
        }
        matrixList.addAll(RotatedMatrixList);
    }

    public static char[][] reflectMatrix(char[][] matrix) {
        int totalColsOfReflectedMatrix = matrix[0].length; //Total columns of Original Matrix
        int totalRowsOfReflectedMatrix = matrix.length; //Total rows of Original Matrix

        char[][] reflectedMatrix = new char[totalRowsOfReflectedMatrix][totalColsOfReflectedMatrix];

        for (int i = 0; i < totalRowsOfReflectedMatrix; i++) {
            for (int j = 0; j < totalColsOfReflectedMatrix; j++) {
                reflectedMatrix[i][ (totalColsOfReflectedMatrix-1)- j] = matrix[i][j];
            }
        }
        return reflectedMatrix;
    }

    public static void setReflectedMatrixList(List<char[][]> matrixList) {
        List<char[][]> ReflectedMatrixList = new ArrayList<>();
        for (char[][] matrix : matrixList) {
            char[][] newMatrix = reflectMatrix(matrix);
            // 检查所有的matrix
            boolean isDuplicated = false;
            for (char[][] checkMatrix : matrixList) {
                if (isMatrixDuplicated(checkMatrix, newMatrix)) {
                    isDuplicated = true;
                    break;
                }
            }
            for (char[][] checkMatrix : ReflectedMatrixList) {
                if (isMatrixDuplicated(checkMatrix, newMatrix)) {
                    isDuplicated = true;
                    break;
                }
            }
            if (!isDuplicated) {
                ReflectedMatrixList.add(newMatrix);
            }
        }
        matrixList.addAll(ReflectedMatrixList);
    }

    public static boolean isMatrixDuplicated(char[][] matrixA, char[][] matrixB) {
        // char矩阵判重
        int rowLength = matrixA.length;
        int colLength = matrixA[0].length;
        if (rowLength != matrixB.length) {
            return false;
        }
        if (colLength != matrixB[0].length) {
            return false;
        }
        for (int i=0; i<rowLength; i++) {
            for (int j=0; j<colLength; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[][] rotateMatrixBy90Degree(int[][] matrix) {
        int totalColsOfRotatedMatrix = matrix[0].length; //Total columns of Original Matrix
        int totalRowsOfRotatedMatrix = matrix.length; //Total rows of Original Matrix

        int[][] rotatedMatrix = new int[totalColsOfRotatedMatrix][totalRowsOfRotatedMatrix];

        for (int i = 0; i < totalRowsOfRotatedMatrix; i++) {
            for (int j = 0; j < totalColsOfRotatedMatrix; j++) {
                rotatedMatrix[j][totalRowsOfRotatedMatrix - 1 - i] = matrix[i][j];
            }
        }
        return rotatedMatrix;
    }

    public static int[][] reflectMatrix(int[][] matrix) {
        int totalColsOfReflectedMatrix = matrix[0].length; //Total columns of Original Matrix
        int totalRowsOfReflectedMatrix = matrix.length; //Total rows of Original Matrix

        int[][] reflectedMatrix = new int[totalRowsOfReflectedMatrix][totalColsOfReflectedMatrix];

        for (int i = 0; i < totalRowsOfReflectedMatrix; i++) {
            for (int j = 0; j < totalColsOfReflectedMatrix; j++) {
                reflectedMatrix[i][ (totalColsOfReflectedMatrix-1)- j] = matrix[i][j];
            }
        }
        return reflectedMatrix;
    }

    public static boolean isMatrixDuplicated(int[][] matrixA, int[][] matrixB) {
        // int 矩阵判重
        if (matrixA.length != matrixB.length) {
            return false;
        }
        if (matrixA[0].length != matrixB[0].length) {
            return false;
        }
        for (int i=0; i<matrixA.length; i++) {
            for (int j=0; j<matrixA[0].length; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        char[][] matrix = new char[4][2];
        for (int i=0; i<4; i++) {
            for (int j=0; j<2; j++) {
                matrix[i][j] = (char)('a' + i*2 + j);
            }
        }
        char[][] res1 = rotateMatrixBy90Degree(matrix);
        char[][] res2 = reflectMatrix(matrix);
        System.out.println();

        List<char[][]> matrixList = new ArrayList<>();
        matrixList.add(matrix);
        setRotatedMatrixList(matrixList);
        setReflectedMatrixList(matrixList);
        System.out.println();
    }

}
