/**
 * 
 */
package logic;

/**
 * @author thayumaanavan
 *
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import classifier.DTW;


import datastructure.LabelledTimeSeriesClassificationData;
import datastructure.Matrix;



public class ProcessingUnit {
	
	
	LabelledTimeSeriesClassificationData current;
	public List<LabelledTimeSeriesClassificationData> trainsequence;
	Boolean analyzing,learning;
	Matrix  trainingSample;
	DTW recognition;
	double testAccuracy;
	Vector<Double> testFMeasure,testPrecision,testRecall;
	Matrix testConfusionMatrix;
	Boolean trained;
	int inputVectorDim,outputVectorDim;
	double testRejectionPrecision,testRejectionRecall;
	

   
	/**
	 * 
	 */
	public ProcessingUnit(boolean autofilter) 
	{
		this.learning = false;
        this.analyzing = false;
        this.trainsequence = new ArrayList<LabelledTimeSeriesClassificationData>();
		
		trainingSample=new Matrix();
		recognition=new DTW();
		
		
		current=new LabelledTimeSeriesClassificationData(9, null, null);
		//current.setNumDim(9);
		
	}
	
	 
	
	 public void addData(Vector<Double> vector)
     {
		 
        /* for(Filter i:this.dataFilters)
         {
             vector = i.filter(vector);
         }*/
		 if (this.learning || this.analyzing)
		 {
			 //System.out.println(vector.get(2));
			// Boolean filter1=this.dataFilters.get(0).filter(vector);
			// Boolean filter2=this.dataFilters.get(1).filter(vector);
			// Boolean filter3=this.dataFilters.get(2).filter(vector);
			// if(filter1 && filter2 && filter3)
				 trainingSample.push_back(vector);
			// else
			//	 System.out.println("frame filtered out");
			// System.out.println("row="+trainingSample.getNumRows());
			// System.out.println("col="+trainingSample.getNumCols());
			 
		 }
			 
		 
		 
     }
	 
	 public void startTraining()
	 {
		 if (!this.analyzing && !this.learning)
         {
             this.learning = true;
             System.out.println("Started...");
         }
	 }
	 
	 public void stopTraining()
	 {
		 if (this.learning)
         {
			 
             /*if (this.current.getCountOfData() > 0)
             {
            	 
                // LabelledTimeSeriesClassificationData gesture = new LabelledTimeSeriesClassificationData(this.current);
                 //this.trainsequence.add(gesture);
                 //this.current = new LabelledTimeSeriesClassificationData();
             }*/

             this.learning = false;
             System.out.println("stopped...");
         }
	 }
	 
	 public void finishTraining(int n)
	 {
		 if (!this.analyzing && !this.learning)
         {
			 current.addSample(n,trainingSample);
			// System.out.println("row="+trainingSample.getNumRows());
			 //System.out.println("col="+trainingSample.getNumCols());
			 this.learning=false;
			 /*for(int i=0;i<trainingSample.getNumRows();i++)
				 for(int j=0;j<trainingSample.getNumCols();j++)
					 System.out.print(trainingSample.dataptr[i][j]);*/
			 trainingSample=new Matrix();
			 System.out.println("sample added...");
             /*if (!this.trainsequence.isEmpty())
             {
            	 this.learning = true;
            	 
            	 
            	 this.trainsequence = new ArrayList<LabelledTimeSeriesClassificationData>();
                 this.learning = false;
             }*/
         }
	 }

	/**
	 * @param next
	 * @throws IOException 
	 */
	public void Save(String next) throws IOException 
	{
		// TODO Auto-generated method stub
		current.saveDatasetToFile(next);
		
	}
	
	public void train()
	{
		
		trained=recognition.train(current);
		if(! trained)
			System.out.println("training failed");
		System.out.println("Training successful");
		
	}
	
	public void test(String fname) throws IOException
	{
		LabelledTimeSeriesClassificationData test=new LabelledTimeSeriesClassificationData(9,null,null);
		test.loadDatasetToFile(fname);
		if(!test(test))
			System.out.println("test failed");
		System.out.println("ACCURACY:"+testAccuracy);
		System.out.println("FMeasure:"+testFMeasure);
		System.out.println("Precision:"+testPrecision);
		System.out.println("Recall:"+testRecall);
        
        
	}
	
	public Boolean test(LabelledTimeSeriesClassificationData testData)
	{
		testAccuracy=0;
		testFMeasure=new Vector<Double>(recognition.getNumClasses());
		testPrecision=new Vector<Double>(recognition.getNumClasses());
		testRecall=new Vector<Double>(recognition.getNumClasses());
		for(int i=0;i<recognition.getNumClasses();i++)
		{
			testFMeasure.add(i, 0.0);
			testPrecision.add(i, 0.0);
			testRecall.add(i, 0.0);
		}
		testConfusionMatrix=new Matrix();
		testConfusionMatrix.clear();
		
		if(! trained)
			return false;
		inputVectorDim=9;
		if(testData.getNumDim() != inputVectorDim)
			return false;
		
		double rejectionPrecisionCounter=0;
		double rejectionRecallCounter=0;
		int confusionMatrixSize=recognition.getNumClasses()+1;
		Vector<Double> precisionCounter=new Vector<Double>(recognition.getNumClasses());
		for(int i=0;i<precisionCounter.capacity();i++)
			precisionCounter.add(i, 0.0);
		Vector<Double> recallCounter=new Vector<Double>(recognition.getNumClasses());
		for(int i=0;i<recallCounter.capacity();i++)
			recallCounter.add(i, 0.0);
		Vector<Double> confusionMatrixCounter=new Vector<Double>(recognition.getNumClasses()+1);
		for(int i=0;i<confusionMatrixCounter.capacity();i++)
			confusionMatrixCounter.add(i, 0.0);
		
		testConfusionMatrix.resize(confusionMatrixSize, confusionMatrixSize);
		for(int i=0;i<testConfusionMatrix.getNumRows();i++)
		{
			for(int j=0;j<testConfusionMatrix.getNumCols();j++)
				testConfusionMatrix.dataptr[i][j]=0;
		}
		System.out.println("samples:"+testData.getNumSamples());
		for(int i=0;i<testData.getNumSamples();i++)
		{
			int classLabel=testData.getDataVector().get(i).getClassLabel();
			Matrix testMatrix=testData.getDataVector().get(i).getData();
			//System.out.println("classLabel:"+classLabel);
			//System.out.println("predicted:"+testMatrix);
			//for(int x=0;x<testMatrix.getNumRows();x++)
			//{
				//Vector<Double> testSample=testMatrix.getRowVector(x);
				
				if(!recognition.predict(testMatrix))
				{
					return false;
				}
				
				int predictedClassLabel=recognition.getPredictedClassLabel();
				//System.out.println("predicted label:"+predictedClassLabel);
				if(!updateTestMetrics(classLabel,predictedClassLabel,precisionCounter,recallCounter,rejectionPrecisionCounter,rejectionRecallCounter, confusionMatrixCounter))
					return false;
				//System.out.println("Accuracy"+testAccuracy);
			//}
		}
		
		if(! computeTestMetrics(precisionCounter,recallCounter,rejectionPrecisionCounter,rejectionRecallCounter, confusionMatrixCounter, testData.getNumSamples()))
			return false;
		/*System.out.println("precisionCounter:"+precisionCounter);
		System.out.println("recallCounter:"+recallCounter);
		System.out.println("confusionMatrixCounter:"+confusionMatrixCounter);
		System.out.println("rejectionRecallCounter:"+rejectionRecallCounter);
		*/
		return true;
	}
		
		
		
	
	
	/**
	 * @param precisionCounter
	 * @param recallCounter
	 * @param rejectionPrecisionCounter
	 * @param rejectionRecallCounter
	 * @param confusionMatrixCounter
	 * @param numSamples
	 * @return
	 */
	private boolean computeTestMetrics(Vector<Double> precisionCounter,
			Vector<Double> recallCounter, double rejectionPrecisionCounter,
			double rejectionRecallCounter,
			Vector<Double> confusionMatrixCounter, int numTestSamples) {
		// TODO Auto-generated method stub
		
		testAccuracy=(testAccuracy/(numTestSamples))*100.0;
		/*System.out.println("Before:");
		System.out.println("confusionMatrix:"+this.testConfusionMatrix);
		for(int i=0;i<testConfusionMatrix.getNumRows();i++)
		{
			for(int j=0;j<testConfusionMatrix.getNumCols();j++)
				System.out.print(testConfusionMatrix.dataptr[i][j]+"  ");
			System.out.println();
		}
		*/
		/*System.out.println("FMeasure:"+testFMeasure);
		System.out.println("Precision:"+testPrecision);
		System.out.println("Recall:"+testRecall);
		System.out.println("precisionCounter:"+precisionCounter);
		System.out.println("recallCounter:"+recallCounter);*/
		for(int k=0;k<recognition.getNumClasses();k++)
		{
			if(precisionCounter.get(k)>0)
				testPrecision.set(k, testPrecision.get(k)/precisionCounter.get(k));
			else
				testPrecision.set(k, 0.0);
			
			if(recallCounter.get(k)>0)
				testRecall.set(k, testRecall.get(k)/recallCounter.get(k));
			else
				testRecall.set(k, 0.0);
			
			if(precisionCounter.get(k)+recallCounter.get(k)>0)
				testFMeasure.set(k, 2*((testPrecision.get(k)*testRecall.get(k))/(testPrecision.get(k)+testRecall.get(k))));
			else
				testFMeasure.set(k, 0.0);
			
			
		}
		
		if( rejectionPrecisionCounter > 0 ) 
			testRejectionPrecision /= rejectionPrecisionCounter;
	    if( rejectionRecallCounter > 0 ) 
	    	testRejectionRecall /= rejectionRecallCounter;

	    for(int r=0;r<confusionMatrixCounter.size();r++)
	    {
	    	if(confusionMatrixCounter.get(r)>0)
	    	{
	    		for(int c=0;c<testConfusionMatrix.getNumCols();c++)
	    			testConfusionMatrix.dataptr[r][c]/=confusionMatrixCounter.get(r);
	    		
	    	}
	    }
	    
		return true;
	}

	/**
	 * @param classLabel
	 * @param predictedClassLabel
	 * @param precisionCounter
	 * @param recallCounter
	 * @param rejectionPrecisionCounter
	 * @param rejectionRecallCounter
	 * @param confusionMatrixCounter
	 * @return
	 */
	private boolean updateTestMetrics(int classLabel, int predictedClassLabel,
			Vector<Double> precisionCounter, Vector<Double> recallCounter,
			double rejectionPrecisionCounter, double rejectionRecallCounter,
			Vector<Double> confusionMatrixCounter) {
		// TODO Auto-generated method stub
		
		int predictedClassLabelIndex=0;
		Boolean predictedClassLabelIndexFound=false;
		//if(predictedClassLabel!=0)
		//{
			//System.out.println("num classes:"+recognition.getNumClasses());
			for(int k=0;k<recognition.getNumClasses();k++)
			{
				//System.out.println("class:"+recognition.getClassLabel(k));
				if(predictedClassLabel==recognition.getClassLabel(k))
				{
					predictedClassLabelIndex=k;
					predictedClassLabelIndexFound=true;
					break;
				}
			}
			
			if(!predictedClassLabelIndexFound)
				return false;
			
		//}
		
		int actualClassLabelIndex=0;
		//if(classLabel!=0)
		//{
			for(int k=0;k<recognition.getNumClasses();k++)
			{
				if(classLabel==recognition.getClassLabel(k))
				{
					actualClassLabelIndex=k;
					break;
					
				}
			}
		//}
		
		//System.out.println("predicted Index:"+predictedClassLabelIndex);
		//System.out.println("actual Index:"+actualClassLabelIndex);
		if(classLabel==predictedClassLabel)
		{
			testAccuracy++;
		}
		
		//if(predictedClassLabel !=0)
		//{
			if(classLabel== predictedClassLabel)
				testPrecision.set(predictedClassLabelIndex, (testPrecision.get(predictedClassLabelIndex))+1);
			//if(predictedClassLabel!=classLabel)
				//precisionCounter.set(actualClassLabelIndex, (precisionCounter.get(actualClassLabelIndex))+1);
			//else
				precisionCounter.set(predictedClassLabelIndex, (precisionCounter.get(predictedClassLabelIndex))+1);
		//}
		
		//if(classLabel !=0)
		//{
			if(classLabel==predictedClassLabel)
				testRecall.set(predictedClassLabelIndex, (testRecall.get(predictedClassLabelIndex))+1);
			recallCounter.set(actualClassLabelIndex, (recallCounter.get(actualClassLabelIndex))+1);
		//}
		
	/*	if( predictedClassLabel == 0 ){
	        if( classLabel == 0 ) testRejectionPrecision++;
	        rejectionPrecisionCounter++;
	    }

		if( classLabel == 0 ){
	        if( predictedClassLabel == 0 ) testRejectionRecall++;
	        rejectionRecallCounter++;
	    }*/

		/*if( classLabel == 0 ) 
			actualClassLabelIndex = 0;
        else actualClassLabelIndex++;
        if( predictedClassLabel == 0 ) 
        	predictedClassLabelIndex = 0;
        else predictedClassLabelIndex++;
        */
        testConfusionMatrix.dataptr[actualClassLabelIndex][predictedClassLabelIndex]+=1;
        confusionMatrixCounter.set(actualClassLabelIndex,( confusionMatrixCounter.get(actualClassLabelIndex))+1);
        
        
        
        return true;

	}

	public void startRecognition()
	{
		if(!this.analyzing && !this.learning)
		{
			this.analyzing=true;
		}
	}
	
	public void stopRecognition()
	{
		if(this.analyzing)
		{
			//for(int i=0;i<trainingSample.getNumRows();i++)
			//	for(int j=0;j<trainingSample.getNumCols();j++)
			//System.out.println(trainingSample.dataptr[i][j]);
			//System.out.println("input matrix rows:"+trainingSample.getNumRows());
			Boolean predict=recognition.predict(this.trainingSample);
			if(!predict)
				System.out.println("prediction failed");
			else{
			this.analyzing=false;
			trainingSample=new Matrix();
			printText(recognition.getPredictedClassLabel());
			}
		}
	}

	/**
	 * @param gesture
	 */
	private void printText(int id) {
		// TODO Auto-generated method stub
            
		
		//String[] sign={"Student","Super","North","South","Four","Olympic Ring","Victory","Warning","Circle","Kill","Five"};
		String[] signs={"Student","North","South","West","East","Four","Olympic Ring","House","Swap","Scissor"};
		System.out.println(signs[id]);
		
		/*switch(id)
		{
		case 0:
			//System.out.println("North");
           // obj.doSpeak("North","kevin16");
            System.out.println("Student");
            Chec obj=new Chec();
            obj.doSpeak("Student","kevin16");
			break;
		case 1:
			System.out.println("Super");
			Chec obj1=new Chec();
            obj1.doSpeak("Super","kevin16");
			break;
		case 2:
			System.out.println("North");
			Chec obj2=new Chec();
            obj2.doSpeak("North","kevin16");

			//System.out.println("Student");
           // obj.doSpeak("Student","kevin16");
			break;
		case 3:
			System.out.println("South");
			Chec obj3=new Chec();
            obj3.doSpeak("South","kevin16");
			break;
		case 4:
			System.out.println("Bird");
			Chec obj4=new Chec();
            obj4.doSpeak("Bird","kevin16");
			break;
		case 5:
			System.out.println("Olympic ring");
			Chec obj5=new Chec();
			obj5.doSpeak("Olympic ring","kevin16");
			break;
		case 6:
			System.out.println("Victory");
			Chec obj6=new Chec();
            obj6.doSpeak("Victory","kevin16");
            break;
		case 7:
			System.out.println("Warning");
			Chec obj7=new Chec();
            obj7.doSpeak("warning","kevin16");
            break;
		case 8:
			System.out.println("Circle");
			Chec obj8=new Chec();
            obj8.doSpeak("Circle","kevin16");
            break;
		case 9:
			System.out.println("Kill");
			Chec obj9=new Chec();
            obj9.doSpeak("Kill","kevin16");
            break;
		case 10:
			System.out.println();
		default:
			System.out.println("prediction class failed");
		*/	
		
	}

	/**
	 * @param next
	 * @throws IOException 
	 */
	public void load(String next) throws IOException {
		// TODO Auto-generated method stub
		current.loadDatasetToFile(next);
	}

	/**
	 * @param next
	 * @throws FileNotFoundException 
	 */
	public void saveModel(String next) throws FileNotFoundException {
		// TODO Auto-generated method stub
		recognition.saveModelToFile(next);
	}

	/**
	 * @param next
	 */
	public void loadModel(String next) {
		// TODO Auto-generated method stub
		recognition.loadModelToFile(next);
	}
}
