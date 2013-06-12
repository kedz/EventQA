package edu.columbia.cs.event.qa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jblas.DoubleMatrix;
import org.jblas.Singular;
import org.jblas.Solve;

class JavaLSA_v2
{
	public static DoubleMatrix TxD;
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
		DoubleMatrix first=Solve.pinv(Snew);
		System.out.println("first:"+first.rows+"-"+first.columns);
		DoubleMatrix mid=TxT.transpose();
		System.out.println("mid:"+mid.rows+"-"+mid.columns);
		space=first.mmul(mid);	
		System.out.println("space:"+space.rows+"-"+space.columns);		
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
		FileWriter fW=new FileWriter(outputFile);
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
	
	static void readMatrix(String fName, int termCount) throws Exception
	{		
		FileReader fR=new FileReader(fName);
		BufferedReader bR=new BufferedReader(fR);
		
		String line="";
		int i=0;
		
		while((line=bR.readLine())!=null)
		{
			String[] termFreq=line.split(",");
			if (i==0)
			{	
				TxD=DoubleMatrix.zeros(termCount, termFreq.length);
				System.out.println(termFreq.length);
			}
			
			for (int j=0;j<termFreq.length;j++)
			{
				TxD.put(i, j, Double.parseDouble(termFreq[j]));
			}	
			i++;
		}
		
		System.out.println("ROWS:"+TxD.rows);
		System.out.println("COLUMNS:"+TxD.columns);
	}
	
	public static void main(String args[]) throws Exception
	{
		String inputPath=args[0];
		String outputPath=args[1];
		int termCount=Integer.parseInt(args[2]);
		
		System.out.println("Build TxDMatrix:");
		readMatrix(inputPath, termCount);
		System.out.println("Starting Building Space");
		decomposeMatrix(TxD);
		System.out.println("Done Building Space");
		System.out.println("Text matrix");
		System.out.println(TxT.rows+"-"+TxT.columns);
		System.out.println("Singular space matrix");
		System.out.println(semantic.rows+"-"+semantic.columns);
		System.out.println("Eigen space matrix");
		System.out.println(space.rows+"-"+space.columns);
		System.out.println("Saving.. Eigenspace");
		saveSpace(outputPath);	
		System.out.println("Saved.. Eigenspace");
	}
}
