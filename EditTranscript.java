import java.util.Arrays;
import java.util.SortedMap;

public class EditTranscript {
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

        public String getArrowType() {
            switch(this) {
                case Diagonal_Match:
                    return " ↖︎ ";

                case Diagonal_Missmatch:
                    return "(↖︎)";

                case Left:
                    return " ← ";

                case Top:
                    return " ↑ ";
            }
            return "E";
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
        String[][] arrowMatrix = initializeArrowMatrix(numberOfRows, numberOfCols);
        
        for (int i = 1; i < numberOfRows; i++) {
            for (int j = 1; j < numberOfCols; j++) {

                //TODO: Debug lines entfernen
                int delta_i_j = calculateDelta_i_j(i, sequence1, j, sequence2);
                // first compute from which direction the score is derived (traceback)
                // then use this output as input for next function to calculate score!
                tracebackDirection direction = getTraceback(scoreMatrix, i, j, delta_i_j);

                scoreMatrix[i][j] = computeScoreOfPrefix(scoreMatrix, i, j, direction);
                tracebackMatrix[i][j] = computeTracebackCoordinate(direction, i, j);
                editTypeMatrix[i][j] = direction.getOperationType();
                arrowMatrix[i][j] = direction.getArrowType();
            }
        }

        if (verboseMode) printMatrices(scoreMatrix, tracebackMatrix, editTypeMatrix, arrowMatrix);

        return traceBack(tracebackMatrix, editTypeMatrix, numberOfRows, numberOfCols);
    }

    /**
     * This Function uses the Traceback matrix to read through the operation type matrix which kind of operation should
     * be added to the growing edit transcript string
     * @param traceBackMatrix matrix which holds the traceback coordinates
     * @param editTypeMatrix matrix which holds the edit type at position i, j
     * @param numberOfRows length of sequence1 + 1
     * @param numberOfCols length of sequence2 + 1
     * @return edit transcript String
     */
    private static String traceBack(int[][][] traceBackMatrix, char[][] editTypeMatrix, int numberOfRows, int numberOfCols) {
        StringBuilder reverseEditTranscript = new StringBuilder();

        int[] nextIndex = new int[]{numberOfRows - 1, numberOfCols - 1};

        while (! (nextIndex[0] == 0 && nextIndex[1] == 0)) {
            reverseEditTranscript.append(editTypeMatrix[nextIndex[0]][nextIndex[1]]);
            nextIndex = traceBackMatrix[nextIndex[0]][nextIndex[1]];
        }

        return reverseEditTranscript.reverse().toString();
    }

    /**
     * simple sout function to print all matrices to the console
     * @param scoreMatrix the scoreMatrix
     * @param tracebackMatrix the traceBackMatrix
     * @param editTypeMatrix the editTypeMatrix
     * @param arrowMatrix the arrowMatrix
     */
    private static void printMatrices(int[][] scoreMatrix, int[][][] tracebackMatrix, char[][] editTypeMatrix, String[][] arrowMatrix) {

        System.out.println("\n#### scoreMatrix:");
        for (int i = 0; i < scoreMatrix.length; i++) {
            for (int j = 0; j < scoreMatrix[i].length; j++) {
                System.out.print(scoreMatrix[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("\n#### traceBackMatrix:");
        for (int i = 0; i < tracebackMatrix.length; i++) {
            for (int j = 0; j < tracebackMatrix[i].length; j++) {
                System.out.print(Arrays.toString(tracebackMatrix[i][j]) + " ");
            }
            System.out.println();
        }

        System.out.println("\n#### editTypeMatrix:");
        for (int i = 0; i < editTypeMatrix.length; i++) {
            for (int j = 0; j < editTypeMatrix[i].length; j++) {
                System.out.print(editTypeMatrix[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("\n#### arrowMatrix:");
        for (int i = 0; i < arrowMatrix.length; i++) {
            for (int j = 0; j < arrowMatrix[i].length; j++) {
                System.out.print(arrowMatrix[i][j] + " ");
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
        int[][][] traceBackMatrix = new int[numberOfRows][numberOfCols][2];

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

    /**
     * This function initializes a TypeMatrix
     * @param numberOfRows length of sequence1 + 1
     * @param numberOfCols length of sequence2 + 1
     * @return initialized matrix
     */
    private static char[][] initializeTypeMatrix(int numberOfRows, int numberOfCols) {
        char[][] initializedMatrix = new char[numberOfRows][numberOfCols];

        // initialize first row from cell by cell starting with 0, incrementing by 1
        Arrays.fill(initializedMatrix[0], 'I');

        // initialize each first entry in each row starting with 0, incrementing by 1
        for (int i = 1; i < initializedMatrix.length; i++) {
            initializedMatrix[i][0] = 'D';
        }

        return initializedMatrix; 
    }

    private static String[][] initializeArrowMatrix(int numberOfRows, int numberOfCols) {
        String[][] initializedMatrix = new String[numberOfRows][numberOfCols];

        // initialize first row from cell by cell starting with 0, incrementing by 1
        Arrays.fill(initializedMatrix[0], " ← ");

        // initialize each first entry in each row starting with 0, incrementing by 1
        for (int i = 1; i < initializedMatrix.length; i++) {
            initializedMatrix[i][0] = " ↑ ";
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
            System.out.print("\ncurrent index: ");
            System.out.print(current_i);
            System.out.print(" ");
            System.out.println(current_j);
            System.out.print("scoreAbove: ");
            System.out.println(scoreAbove);
            System.out.print("scoreLeft: ");
            System.out.println(scoreLeft);
            System.out.print("score diag: ");
            System.out.println(scoreDiagonal);
        }

        tracebackDirection direction;

        if (scoreDiagonal <= scoreAbove && scoreDiagonal <= scoreLeft && delta_i_j > 0) direction = tracebackDirection.Diagonal_Missmatch;
        else if (scoreDiagonal <= scoreAbove && scoreDiagonal <= scoreLeft && delta_i_j == 0) direction = tracebackDirection.Diagonal_Match;
        else if (scoreAbove <= scoreLeft && scoreAbove <= scoreDiagonal) direction = tracebackDirection.Top;
        else direction = tracebackDirection.Left;

        if (verboseMode) System.out.println("traceback to write: " + direction.getOperationType());
        return direction;
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
        int editDistance;
        if (direction.isAbove()) editDistance = scoreMatrix[current_i - 1][current_j] + 1;
        else if (direction.isLeft()) editDistance = scoreMatrix[current_i][current_j - 1] + 1;
        else if (direction.isDiagonal_Match()) editDistance = scoreMatrix[current_i - 1][current_j - 1];
        else editDistance = scoreMatrix[current_i - 1][current_j - 1] + 1;

        if(verboseMode) System.out.println("edit Distance to write: " + editDistance);
        return editDistance;
    }

    public static void main(String[] args) {
        String sequence1 = "";
        String sequence2 = "";

        try {
            sequence1 = args[0];
            sequence2 = args[1];
            
        } catch (Exception e) {
            System.out.println("<<<<<<! ERROR: please provide two sequences as arguments!");
        }

        // Optional verbose Mode (more like debug mode XD)
        try {
            if (args[2].equals("-v") || args[2].equals("--verbose")) verboseMode = true;
        } catch (Exception e) {
            verboseMode = false;
        }

        System.out.println("\n#### edit transcirpt:\n" + computeEditTranscript(sequence1, sequence2));
    }

}
