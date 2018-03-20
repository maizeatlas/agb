package org.accretegbR.experimental;

public class AlphaDesign extends ExperimentDesign {

	@Override
	public StringBuffer buildRCode() {
		StringBuffer sb = new StringBuffer();
		sb.append("library(agricolae);");
		sb.append("trt<-c(" + convertListToString(getTreatments().get(0)) + ");");
		sb.append("outdesign<- design.alpha(trt, "+getBlockSize()+","+ getReps() + ", serie=2, " + getSeeds() + ", \"" + getMethodName() + "\");");
		sb.append("plots <- as.numeric(outdesign$book[,1]);");
		sb.append("cols <- as.numeric(outdesign$book[,2]);");
		sb.append("blocks <- as.numeric(outdesign$book[,3]);");
		sb.append("trts <- as.character(outdesign$book[,4]);");
		sb.append("reps <- as.character(outdesign$book[,5]);");
		sb.append("my.all <- list(my.plots=plots, my.cols=cols,my.blocks=blocks,my.trts=trts, my.reps=reps);");
		return sb;
	}

	
}
