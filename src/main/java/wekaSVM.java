import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;

class wekaSVM
{
	static ArrayList<String> getPredictedClass(String trainName, String testName) throws Exception
	{
		BufferedReader bR=new BufferedReader(new FileReader(trainName));
		Instances train=new Instances(bR);	
		train.setClassIndex(train.numAttributes()-1);
		
		bR=new BufferedReader(new FileReader(testName));
		Instances test=new Instances(bR);
		test.setClassIndex(test.numAttributes()-1);
		
		Classifier svm= new SMO();
		svm.buildClassifier(train);		
		
		ArrayList<String> predClasses=new ArrayList<String>();
		for (int i=0;i<test.numInstances();i++)
		{
			double pred=svm.classifyInstance(test.instance(i));
			//System.out.println(test.instance(i)+"="+test.classAttribute().value((int) pred));
			predClasses.add(test.classAttribute().value((int) pred));			
		}		
		
		return predClasses;
	}
	
	public static void main(String args[]) throws Exception
	{
		BufferedReader bR=new BufferedReader(new FileReader("/proj/fluke/users/shreya2k7/newsblaster/train.arff"));
		Instances train=new Instances(bR);	
		train.setClassIndex(train.numAttributes()-1);
		
		bR=new BufferedReader(new FileReader("/proj/fluke/users/shreya2k7/newsblaster/test.arff"));
		Instances test=new Instances(bR);
		test.setClassIndex(test.numAttributes()-1);
		
		Classifier svm= new SMO();
		svm.buildClassifier(train);
		
		Evaluation eval=new Evaluation(train);
		eval.crossValidateModel(svm, train, 10, new Random(1));
		System.out.println(eval.toSummaryString("\nResults\n*******\n", true));
		System.out.println(eval.fMeasure(1)+" "+eval.precision(1)+" "+eval.recall(1));
		
		for (int i=0;i<test.numInstances();i++)
		{
			double pred=svm.classifyInstance(test.instance(i));
			System.out.print("ID: " + test.instance(i).value(0));
			System.out.print(", actual: " + test.classAttribute().value((int) test.instance(i).classValue()));
			System.out.println(", predicted: " + test.classAttribute().value((int) pred));
		}		
	}
}