/**
 * Please dont grade this! This is what we tried and what did not work due to some smaller debug problems in the end that we couldt fix in time
 
 Assignment 01 - Task 03
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedMap;

public class EditTranscript {
    /**
     * Adjust your names here:
     */
    private static final String studentNameA = "Sophia Gaupp";
    private static final String studentNameB = "Niklas Gerbes";
    private static boolean verboseMode = false;

    /**
     * Interp. as direction from which the edit Distance at i, j is derived from!
     * Declare and initialize variables.
     */
    private enum tracebackDirection {
        Diagonal_Match,
        Diagonal_Missmatch,
        Left,
        Top;
        
        public boolean isDiagonal_Match() {
            return this == Diagonal_Match;
        }

        public boolean isDiagonal_Missmatch() {
            return this == Diagonal_Missmatch;
        }

        public boolean isLeft() {
            return this == Left;
        }

        public boolean isAbove() {
            return this == Top;
        }

        public char getOperationType() {
            switch(this) {
                case Diagonal_Match:
                    return 'M';
                
                case Diagonal_Missmatch:
                    return 'R';

                case Left:
                    return 'I';

                case Top:
                    return 'D';
            }
            return 'E';
        }
    }

    /**
     * Compute the edit transcript using Dynamic Programming
     * and Levenshtein-Algo (since we have full Sequence Alignment of two sequences):
     *
     * d_edit(X, Y) = min{R(X, Y), I(X, Y), D(X, Y)}
     *
     * @param sequence1 The first sequence
     * @param sequence2 The second sequence
     * @return The edit transcript
     */
    public static String computeEditTranscript(String sequence1, String sequence2){
        String editTranscript = "";
        int numberOfRows = sequence1.length() + 1;
        int numberOfCols = sequence2.length() + 1;

        int[][] scoreMatrix = initializeScoreMatrix(numberOfRows, numberOfCols);
        int[][][] tracebackMatrix = initializeTracebackMatrix(numberOfRows, numberOfCols); //warum hier drei mal []? --> erstes: i position, 2.: j Position ; 3.: woher kommt man als eigener array
        char[][] editTypeMatrix = initializeTypeMatrix(numberOfRows, numberOfCols);
        
        for (int i = 1; i < numberOfRows; i++) {
            for (int j = 1; j < numberOfCols; j++) {

                //TODO: Debug lines entfernen
                int delta_i_j = calculateDelta_i_j(i, sequence1, j, sequence2); // delta is hier erstmal entweder 0 (Identisch) oder 1 (nicht Identisch)
                if (verboseMode) System.out.println("\ndelta:" + delta_i_j);
                // first compute from which direction the score is derived (traceback)
                // then use this output as input for next function to calculate score!
                tracebackDirection direction = getTraceback(scoreMatrix, i, j, delta_i_j);
                if (verboseMode) System.out.println(direction);

                scoreMatrix[i][j] = computeScoreOfPrefix(scoreMatrix, i, j, direction);
                if (verboseMode) System.out.print("score to write in matrix: ");
                if (verboseMode) System.out.println(computeScoreOfPrefix(scoreMatrix, i, j, direction));
                tracebackMatrix[i][j] = computeTracebackCoordinate(direction, i, j);
                editTypeMatrix[i][j] = direction.getOperationType();
            }
        }

        printMatrices(scoreMatrix, tracebackMatrix, editTypeMatrix);

        return traceBack(tracebackMatrix, editTypeMatrix, numberOfRows, numberOfCols);
    }

    private static String traceBack(int[][][] traceBackMatrix, char[][] editTypeMatrix, int numberOfRows, int numberOfCols) {
        StringBuilder reverseEditTranscript = new StringBuilder();

        int[] nextIndex = new int[]{numberOfRows - 1, numberOfCols - 1};
        System.out.println(("init index:" + Arrays.toString(nextIndex)));
        System.out.println("inti append: " + editTypeMatrix[nextIndex[0]][nextIndex[1]]);
        reverseEditTranscript.append(editTypeMatrix[nextIndex[0]][nextIndex[1]]);
        System.out.println(Arrays.toString(traceBackMatrix[numberOfRows - 1][numberOfCols - 1]));
        do {
            nextIndex = traceBackMatrix[nextIndex[0]][nextIndex[1]];
            System.out.println("next index: " + Arrays.toString(nextIndex));
            System.out.println("append: " + editTypeMatrix[nextIndex[0]][nextIndex[1]]);
            reverseEditTranscript.append(editTypeMatrix[nextIndex[0]][nextIndex[1]]);
        } while (nextIndex[0] != 0 && nextIndex[1] != 0);

        return reverseEditTranscript.reverse().toString();
    }

    private static void printMatrices(int[][] matrix, int[][][] matrix2, char[][] matrix3) {
        //TODO: remove printouts
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }

        for (int i = 0; i < matrix2.length; i++) {
            for (int j = 0; j < matrix2[i].length; j++) {
                System.out.print(Arrays.toString(matrix2[i][j]) + " ");
            }
            System.out.println();
        }

        for (int i = 0; i < matrix3.length; i++) {
            for (int j = 0; j < matrix3[i].length; j++) {
                System.out.print(matrix3[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * This function calculates the delta i j based on the given chars at i in seq1 and j in seq2
     * @param current_i current index i
     * @param sequence1 String of sequence 1
     * @param current_j current index 2
     * @param sequence2 String of sequence 2
     * @return either 0 if char at i of seq1 and j of seq2 are identical else 1
     */
    private static int calculateDelta_i_j(int current_i, String sequence1, int current_j, String sequence2) {
        // we have to decrement the index by 1 since the matrix has one more row and col than the strings
        // (they are coming from the initialization of the scoreMatrix)
        char sequence1CharAt_i = sequence1.charAt(current_i - 1);
        char sequence2CharAt_j = sequence2.charAt(current_j - 1);
        int delta_i_j;
        if (sequence1CharAt_i == sequence2CharAt_j) delta_i_j = 0; else delta_i_j = 1;
        return delta_i_j;
    }

    /**
     * This functions initializes a new int[][] matrix with the first row and first column filled with 1, 2, 3, 4, ...
     *
     * @param numberOfRows length of sequence1 + 1
     * @param numberOfCols length of sequence2 + 1
     * @return int[][] an initialized score matrix
     */
    private static int[][] initializeScoreMatrix(int numberOfRows, int numberOfCols) {

        int[][] initializedMatrix = new int[numberOfRows][numberOfCols];

        // initialize first row from cell by cell starting with 0, incrementing by 1
        for (int j = 0; j < initializedMatrix[0].length; j++) {
            initializedMatrix[0][j] = j;
        }

        // initialize each first entry in each row starting with 0, incrementing by 1
        for (int i = 1; i < initializedMatrix.length; i++) {
            initializedMatrix[i][0] = i;
        }

        return initializedMatrix;      
    }

    /**
     * This function initializes a traceback matrix
     * @param numberOfRows length of sequence1 + 1
     * @param numberOfCols length of sequence2 + 1
     * @return an initialized tracebackMatrix with entries of pattern [i, j] as coordinates.
     */
    private static int[][][] initializeTracebackMatrix(int numberOfRows, int numberOfCols) {
        int[][][] traceBackMatrix = new int[numberOfRows][numberOfCols][2];     // +1 weil 0 eingefügt werden muss an Position 1 in row/column
                                                                                        // [2] weil array zwei Positionen für traceback abspeichert (woher kommt man). s. oben zusätzlich zu Position i und j
        traceBackMatrix[0][0] = new int[]{0, 0};

        // initialize first row from cell by cell starting with 0, incrementing by 1
        for (int j = 1; j < traceBackMatrix[0].length; j++) {
            int[] source = new int[]{0, j - 1};
            traceBackMatrix[0][j] = source;
        }

        // initialize each first entry in each row starting with 0, incrementing by 1
        for (int i = 1; i < traceBackMatrix.length; i++) {
            int[] source = new int[]{i - 1, 0};
            traceBackMatrix[i][0] = source;
        }

        return traceBackMatrix;
    }

    private static char[][] initializeTypeMatrix(int numberOfRows, int numberOfCols) {
        char[][] initializedMatrix = new char[numberOfRows][numberOfCols];

        // initialize first row from cell by cell starting with 0, incrementing by 1
        for (int j = 0; j < initializedMatrix[0].length; j++) {
            initializedMatrix[0][j] = 'I';
        }

        // initialize each first entry in each row starting with 0, incrementing by 1
        for (int i = 1; i < initializedMatrix.length; i++) {
            initializedMatrix[i][0] = 'D';
        }

        return initializedMatrix; 
    }

    /**
     * This function determines, from which possible traceback direction the edit distance is the lowest.
     * @param scoreMatrix the scoreMatrix
     * @param current_i the current index i
     * @param current_j the current index j
     * @param delta_i_j either 0 if char at i of seq1 and j of seq2 are identical else 1
     * @return the direction from which the edit distance is continued
     */
    private static tracebackDirection getTraceback(int[][] scoreMatrix, int current_i, int current_j, int delta_i_j) {
        int scoreAbove = scoreMatrix[current_i - 1][current_j] + 1;
        int scoreLeft = scoreMatrix[current_i][current_j - 1] + 1;
        int scoreDiagonal = scoreMatrix[current_i - 1][current_j - 1] + delta_i_j;

        //TODO: DEBUG lines entfernen
        if (verboseMode) {
            System.out.println("current index:");
            System.out.print(current_i);
            System.out.println(current_j);
            System.out.print("scoreAbove: ");
            System.out.println(scoreAbove);
            System.out.print("scoreLeft: ");
            System.out.println(scoreLeft);
            System.out.print("score diag: ");
            System.out.println(scoreDiagonal);
        }
        

        if (scoreDiagonal <= scoreAbove && scoreDiagonal <= scoreLeft && delta_i_j > 0) return tracebackDirection.Diagonal_Missmatch;
        else if (scoreDiagonal <= scoreAbove && scoreDiagonal <= scoreLeft && delta_i_j == 0) return tracebackDirection.Diagonal_Match;
        else if (scoreAbove <= scoreLeft && scoreAbove <= scoreDiagonal) return tracebackDirection.Top;
        else return tracebackDirection.Left;
        //if (scoreLeft < scoreAbove && scoreLeft < scoreDiagonal)
    }

    /**
     * This function computes the coordinate from which the edit distance was continued
     * @param direction from which the edit distance is continued 
     * @param current_i current index i
     * @param current_j current index j
     * @return the coordinates [i ,j] from which the edit distance was continued
     */
    private static int[] computeTracebackCoordinate(tracebackDirection direction, int current_i, int current_j) {
        int[] tracebackCoordinate = new int[2];

        switch (direction) {
            case tracebackDirection.Diagonal_Match:
            case tracebackDirection.Diagonal_Missmatch:

                tracebackCoordinate[0]= current_i -1;
                tracebackCoordinate[1]= current_j -1;
                break;

            case tracebackDirection.Left:

                tracebackCoordinate[0]= current_i;
                tracebackCoordinate[1]= current_j-1;
                break;
            
            case tracebackDirection.Top:

                tracebackCoordinate[0]= current_i-1;
                tracebackCoordinate[1]= current_j;
                break;
        }

        return tracebackCoordinate;
    }

    /**
     * this function returns the actual edit distance based on the inputted traceback direction
     * @param scoreMatrix the scoreMatrix to work on
     * @param current_i the current index of row
     * @param current_j the current index of col
     * @param direction the direction from which the edit distance should be computed
     * @return
     */
    private static int computeScoreOfPrefix(int[][] scoreMatrix, int current_i, int current_j, tracebackDirection direction) {
        if (direction.isAbove()) return scoreMatrix[current_i - 1][current_j] + 1;
        if (direction.isLeft()) return scoreMatrix[current_i][current_j - 1] + 1;
        if (direction.isDiagonal_Match()) return scoreMatrix[current_i - 1][current_j - 1];
        else return scoreMatrix[current_i - 1][current_j - 1] + 1;
    }

    public static void main(String[] args) {
        System.out.println("GBI - Assignment 1 Task 3 - " + studentNameA + ", " + studentNameB );

        String sequence1 = "";
        String sequence2 = "";

        try {
            sequence1 = args[0];
            sequence2 = args[1];


            // Optional verbose Mode (more like debug mode XD)
            try {
                if (args[2] != null) verboseMode = true;
            } catch (Exception e) {
                verboseMode = false;
            }
            
        } catch (Exception e) {
            System.out.println("<<<<<<! ERROR: please provide two sequences as arguments!");
        }

        // TODO: Call all functions from here and organise the output.
        System.out.println("edit transcirpt: " + computeEditTranscript(sequence1, sequence2));
    }

}
