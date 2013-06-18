package edu.columbia.cs.event.qa.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.columbia.cs.event.qa.util.EventQAConfig;
import org.jblas.DoubleMatrix;
import org.jblas.Singular;
import org.jblas.Solve;

class LSA {

	private DoubleMatrix TxD;
	private DoubleMatrix TxT;
	private DoubleMatrix singular;
	private DoubleMatrix semanticSpace;

    static int numTerms;
    static int numDocs;

    public LSA () {}

    public void run () { run(EventQAConfig.getInstance().getProperty("term.doc.file")); }

    public void run (String matrixInputFile) {
        try {
            System.out.print("Loading TxD Matrix... ");
            this.TxD = loadTxDMatrix (matrixInputFile);
            System.out.println("Done\nTxD: [" + TxD.rows + " x " + TxD.columns + "]");
            System.out.print("Building Semantic Space Matrix... ");
            decompose(TxD);
            System.out.println("TxT: ["+TxT.rows+" x "+TxT.columns+"]");
            System.out.println("Semantic: ["+singular.rows+" x "+singular.columns+"]");
            System.out.println("Eigenspace: ["+semanticSpace.rows+" x "+ semanticSpace.columns);
            System.out.print("Saving... ");
            saveSemanticSpaceMatrix(EventQAConfig.getInstance().getProperty("semantic.space.file"));
            System.out.println("Done");

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public DoubleMatrix loadTxDMatrix (String matrixFileName) throws Exception {
        DoubleMatrix matrix = DoubleMatrix.zeros(numTerms, numDocs);
        String line; int i=0;
        BufferedReader reader =new BufferedReader(new FileReader(matrixFileName));
        while((line = reader.readLine())!=null) {
            String[] termFreq = line.split(",");
            for (int j=0; j<termFreq.length; j++) {
                TxD.put(i, j, Double.parseDouble(termFreq[j]));
            }
            i++;
        }
        return matrix;
    }
	
	public void decompose (DoubleMatrix matrix) throws IOException {

        // Singular.fullSVD(A)
        // Returns a DoubleMatrix[3] array of U, S, V such that M = U * diag(S) * V'
        DoubleMatrix[] svd = Singular.fullSVD(matrix);
        //System.out.println("Singular value decomposition complete");

        this.TxT = svd[0]; // U = M*M'
        this.singular = svd[1]; // singular values

        DoubleMatrix sigma = DoubleMatrix.zeros(matrix.rows, matrix.columns);

        for(int i=0; i<singular.length; i++) {
            sigma.put(i, i, singular.get(i));
        }
        //System.out.println("Snew: ["+sigma.rows+" x "+sigma.columns+"]");

        DoubleMatrix inverseSigma = Solve.pinv(sigma); // inverse
        //System.out.println("first: "+inverseSigma.rows+" x "+inverseSigma.columns+"]");

        DoubleMatrix TxTTranspose = TxT.transpose(); // transpose
        //System.out.println("mid: "+inverseSigmaTranspose.rows+" x "+inverseSigmaTranspose.columns);

        semanticSpace = inverseSigma.mmul(TxTTranspose); // matrix multiply
        //System.out.println("space: "+semanticSpace.rows+" x "+semanticSpace.columns+"]");
	}

    public void saveSemanticSpaceMatrix (String outputFile) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
        for (int i=0; i<semanticSpace.rows; i++) {
            for (int j=0; j<semanticSpace.columns; j++) {
                if (j == semanticSpace.columns-1) {
                    writer.print(semanticSpace.get(i, j));
                } else {
                    writer.print(semanticSpace.get(i, j)+",");
                }
            }
            writer.println();
            writer.flush();
        }
        writer.flush();
        writer.close();
    }
	
	public void printMatrix (DoubleMatrix matrix) {
		for(int i = 0; i <matrix.rows; i++) { System.out.println(matrix.getRow(i)); }
	}

    // pinv(S)*transpose(U)*sentence
	public DoubleMatrix foldingIn(DoubleMatrix matrix) throws Exception {
		return semanticSpace.mmul(matrix);
	}
	
	public static void main(String args[]) throws Exception {

        numTerms = Integer.parseInt(args[0]);
        numDocs = Integer.parseInt(args[1]);

        LSA droid = new LSA ();
        droid.run();

	}
}
