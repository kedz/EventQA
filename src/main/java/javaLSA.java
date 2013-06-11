import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jblas.DoubleMatrix;
import org.jblas.Singular;
import org.jblas.Solve;

class javaLSA
{
	public static DoubleMatrix TxT;
	public static DoubleMatrix semantic;
	public static DoubleMatrix space;
	
	public static void decomposeMatrix(DoubleMatrix Matrix) throws IOException
	{
		DoubleMatrix[] svd=Singular.fullSVD(Matrix);
		System.out.println("Decomposition complete");
		
		TxT=svd[0];
		System.out.println("TxT:"+TxT.rows+"-"+TxT.columns);
		semantic=svd[1];
		System.out.println("semantic:"+semantic.rows+"-"+semantic.columns);
		DoubleMatrix Snew=DoubleMatrix.zeros(Matrix.rows, Matrix.columns);
		for(int i=0;i<semantic.length;i++)
		{
			Snew.put(i, i, semantic.get(i));
		}
		System.out.println("Snew:"+Snew.rows+"-"+Snew.columns);
		DoubleMatrix first= Solve.pinv(Snew);
		System.out.println("first:"+first.rows+"-"+first.columns);
		DoubleMatrix mid=TxT.transpose();
		System.out.println("mid:"+mid.rows+"-"+mid.columns);
		space=first.mmul(mid);	
		System.out.println("space:"+space.rows+"-"+space.columns);
		
		FileWriter fW=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/SpaceDM.txt");
		PrintWriter pW=new PrintWriter(fW);
		for (int i=0;i<space.rows;i++)
		{
			for (int j=0;j<space.columns;j++)
			{
				if (j==space.columns-1)
					pW.print(space.get(i, j));
				else
					pW.print(space.get(i, j)+",");
			}
			pW.println();
			pW.flush();
		}
		
		pW.flush();
		pW.close();
		fW.close();
	}
	
	static void printDM(DoubleMatrix x)
	{
		for(int i = 0; i <x.rows; i++)
		{
			System.out.println(x.getRow(i));
		}
	}
	
	static DoubleMatrix foldingIn(DoubleMatrix sentence) throws Exception  //pinv(S)*transpose(U)*sent
	{			
		return space.mmul(sentence);
	}
	
	static void saveSpace(String outputFile) throws IOException
	{
		FileWriter fR=new FileWriter(outputFile);
		PrintWriter pW=new PrintWriter(fR);
		
		//pW.println("Space: ");		
		for(int i = 0; i < space.rows; i++)
		{
			pW.println(space.getRow(i));
		}
		pW.flush();

		/*pW.println("DxD: ");		
		for(int i = 0; i < DxD.length; i++)
		{
			pW.println(DxD.getRow(i));
		}
		pW.flush();*/
		
		pW.close();
	}
	
	/*static void testing()
	{
		double[][] mat={{1,2,5,5},{3,4,5,8},{7,2,5,1}};
		
		DoubleMatrix matDM=new DoubleMatrix(mat);
		printDM(matDM);
		decomposeMatrix(matDM);
		
		printDM(TxT);
		printDM(semantic);
		printDM(DxD);
		
		System.out.println("*** Snew");
		DoubleMatrix Snew=DoubleMatrix.zeros(TxT.rows, DxD.rows);
		
		for(int i=0;i<semantic.length;i++)
		{
			Snew.put(i, i, semantic.get(i));
		}
		printDM(Snew);
		
		DoubleMatrix first=Solve.pinv(Snew);
		System.out.println("*** pinv");
		printDM(first);
		
		DoubleMatrix mid=TxT.transpose();
		System.out.println("*** transpose");
		printDM(mid);
		
		DoubleMatrix getone=first.mmul(mid);
		
		System.out.println("*** mul");
		
		printDM(getone);
	}*/
	
	public static void main(String args[]) throws Exception
	{
		System.out.println("Reading Corpus");
		String trainFile="/proj/fluke/users/shreya2k7/newsblaster/training2007.txt";
		try{
			train.readTrainingData(trainFile);
		} catch(IOException e)
		{
			e.printStackTrace();
		}	
		System.out.println("Building Matrix");
		train.buildTxDMatrixDM();
		System.out.println("Printing Stats");
		train.saveTxDAsFile();	
		System.out.println("Starting Building Space");
		decomposeMatrix(train.TxDMatrix);
		System.out.println("Done Building Space");
		System.out.println("Text matrix");
		System.out.println(TxT.rows+"-"+TxT.columns);
		System.out.println("Singular space matrix");
		System.out.println(semantic.rows+"-"+semantic.columns);
		System.out.println("Eigen space matrix");
		System.out.println(space.rows+"-"+space.columns);
		System.out.println("Saving.. Eigenspace");
		saveSpace("/proj/fluke/users/shreya2k7/newsblaster/eigs.txt");	
		System.out.println("Saved.. Eigenspace");
	}
}
