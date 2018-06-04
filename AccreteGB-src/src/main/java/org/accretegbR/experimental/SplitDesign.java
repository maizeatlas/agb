package org.accretegbR.experimental;

public class SplitDesign extends ExperimentDesign {

	@Override
	public StringBuffer buildRCode() {
		StringBuffer sb = new StringBuffer();
		sb.append("library(agricolae);");
		//sb.append("require(\"Runiversal\");");
		sb.append("trt1<-c(" + convertListToString(getTreatments().get(0)) + ");");
		sb.append("trt2<-c(" + convertListToString(getTreatments().get(1)) + ");");
		sb.append("outdesign<- design.split(trt1,trt2, "+ getReps() + ", serie=2, seed=" + getSeeds() + ", kinds=\"" + getMethodName() + "\");");
		sb.append("plots <- as.numeric(outdesign$book[,1]);");
		sb.append("splots <- as.numeric(outdesign$book[,2]);");
		sb.append("reps <- as.numeric(outdesign$book[,3]);");
		sb.append("trt1 <- as.character(outdesign$book[,4]);");
		sb.append("trt2 <- as.character(outdesign$book[,5]);");
		sb.append("my.all <- list(my.plots=plots, my.splots=splots,my.reps=reps,my.trt1=trt1, my.trt2=trt2);");
		return sb;
	}

	
}
