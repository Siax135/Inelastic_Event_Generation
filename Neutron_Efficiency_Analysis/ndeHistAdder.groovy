//= REQUIRED ADDER CODE ==========================================================================================
// All code enclosed by the equal signs is required for this adder to work and should not be changed if used 
// to add histograms from another analysis. 

import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;
import java.lang.Integer;
import java.io.FileNotFoundException;
import java.io.IOException;
import HistHandler;

//constants
final String FILE_SUFFIX = ".hipo"

//* Input Argument Stuff *****************************************************************************************
// variables for dealing with input arguments
String inputFileBase, numFilesString, numPrintString;
int numFiles, numPrint;

// if statement to deal with input arguments
if(args.length < 2){ // if there aren't enough arguments to run show usage
	System.out.println("Usage:> run-groovy ndeHistAdder.groovy <input_files_base> <number_of_files_to_sum> <optional: files_between_prints>");
	System.out.println();
	System.out.println("Example: If you want to sum 15 files called nde_histograms_0000.hipo through nde_histograms_0014.hipo and print");
	System.out.println("after every 3 files, then the command is as follows:");
	System.out.println();
	System.out.println("run-groovy ndeHistAdder.groovy nde_histograms_ 15 3");
	System.out.println();
	System.exit(1);
}else if(args.length == 2){ // if we have minimum required to run then print warning and continue with stated assumption
	System.out.println("Only 2 arguments specified! Assuming they are input files base name and number of files.");
	inputFileBase = args[0]; // Input files base name
	numFilesString = args[1]; // number of cases run
	numFiles = Integer.parseInt(numFilesString);
	numPrint = numFiles/5;
	if(numPrint == 0) numPrint = 1;
	System.out.println("Setting files between prints to " + numPrint);
}else{ // asumme we have all correct arguments and continue
	inputFileBase = args[0]; // Input files base name
	numFilesString = args[1]; // number of cases run
	numPrintString = args[2]; // number of cases between prints
	numFiles = Integer.parseInt(numFilesString);
	numPrint = Integer.parseInt(numPrintString);
} // end if for input arguments

System.out.println("File base name set to: " + inputFileBase);
System.out.println("Number of files to be added: " + numFiles);
System.out.println("Files between prints: " + numPrint);
System.out.println();

//****************************************************************************************************************

//* Set up hist file for summed histograms to be stored in *******************************************************
System.out.print("Initializing...");

HistHandler handler = new HistHandler();
TDirectory histFileFinal = handler.initializeHist();
H1F[] histograms1D = handler.get1DHist();
H2F[] histograms2D = handler.get2DHist();
GraphErrors[] graphs = handler.getGraphs();

System.out.println("Success");
System.out.println();
//****************************************************************************************************************

//* Addition of files ********************************************************************************************
// Create initial finial name
String fullFileName = inputFileBase + "0000" + FILE_SUFFIX;

System.out.println("Starting addition with file " + fullFileName);

// Use HistHandler to read first input file
HistHandler inputHandler = new HistHandler(fullFileName);

// Add histograms in input Handler to histograms in initial handler
handler.add(inputHandler);

// for loop to sum over files
for(int i = 1; i < numFiles; i++){
	if(i >= 1 && i <= 9){ // if statement to build file name
		fullFileName = inputFileBase + "000" + i + FILE_SUFFIX;
	}else if(i >= 10 && i <= 99){
		fullFileName = inputFileBase + "00" + i + FILE_SUFFIX;
	}else if(i >= 100 && i <= 999){
		fullFileName = inputFileBase + "0" + i + FILE_SUFFIX;
	}else if(i >= 1000 && i <= 9999){
		fullFileName = inputFileBase + i + FILE_SUFFIX;
	}else{
		System.out.println("Case number greater than maximum allowed additions!");
		System.out.println("Closing program and writing file with first 10,000 cases");
		break;
	} // end if statement to build file names

	if(i%numPrint == 0) System.out.println("Reading file " + fullFileName);

	// Read new file into the inputHandler and add its histogram data to the histograms in the initial handler
	inputHandler = new HistHandler(fullFileName);
	handler.add(inputHandler); 
}// end of for loop for summing files
//****************************************************************************************************************
//================================================================================================================


//* Calculate NDE from generated and reconstructed data **********************************************************
// The code contained in this section is specific to the analysis of the NDE. If this adder where used for another
// analysis this section can (and should) be deleted without breaking the scripts ability to add histogram data
// together.
final int BIN_NUM = 50;
final float BEAM_ENERGY = 11.0;  // GeV

float ndeAtI1_3, ndeAtI1_2, ndeAtI1_1, ndeAtI1_0, ndeAtI0_98, ndeAtI0_95, ndeAtI0_92, ndeAtI0_90 = 0;
float dndeAtI1_3, dndeAtI1_2, dndeAtI1_1, dndeAtI1_0, dndeAtI0_98, dndeAtI0_95, dndeAtI0_92, dndeAtI0_90 = 0;
float ndeGenAtI, dndeGenAtI = 0;
int nentriesTotal1_3, nentriesTotal1_2, nentriesTotal1_1, nentriesTotal1_0, nentriesTotal0_98, nentriesTotal0_95, nentriesTotal0_92, nentriesTotal0_90 = 0;
int nentriesTotalGen = 0;

// Count number of entries in the reconstructed and generated momentum histograms
for(int i = 0; i < BIN_NUM; i++){
	nentriesTotal1_3 += histograms1D[Hist1D.hmomentumRec1_3.ordinal()].getDataY(i);
	nentriesTotal1_2 += histograms1D[Hist1D.hmomentumRec1_2.ordinal()].getDataY(i);
	nentriesTotal1_1 += histograms1D[Hist1D.hmomentumRec1_1.ordinal()].getDataY(i);
	nentriesTotal1_0 += histograms1D[Hist1D.hmomentumRec1_0.ordinal()].getDataY(i);
	nentriesTotal0_98 += histograms1D[Hist1D.hmomentumRec0_98.ordinal()].getDataY(i);
	nentriesTotal0_95 += histograms1D[Hist1D.hmomentumRec0_95.ordinal()].getDataY(i);
	nentriesTotal0_92 += histograms1D[Hist1D.hmomentumRec0_92.ordinal()].getDataY(i);
	nentriesTotal0_90 += histograms1D[Hist1D.hmomentumRec0_90.ordinal()].getDataY(i);
	nentriesTotalGen += histograms1D[Hist1D.hmomentumGen.ordinal()].getDataY(i);
}// end loop to count entries

System.out.println("Total: " + nentriesTotal1_3 + " Total Gen: " + nentriesTotalGen);

// get data from momentum histograms
float[] hmomentumRecData1_3 = histograms1D[Hist1D.hmomentumRec1_3.ordinal()].getData();
float[] hmomentumFoundData1_3 = histograms1D[Hist1D.hmomentumFound1_3.ordinal()].getData();
float[] hmomentumRecData1_2 = histograms1D[Hist1D.hmomentumRec1_2.ordinal()].getData();
float[] hmomentumFoundData1_2 = histograms1D[Hist1D.hmomentumFound1_2.ordinal()].getData();
float[] hmomentumRecData1_1 = histograms1D[Hist1D.hmomentumRec1_1.ordinal()].getData();
float[] hmomentumFoundData1_1 = histograms1D[Hist1D.hmomentumFound1_1.ordinal()].getData();
float[] hmomentumRecData1_0 = histograms1D[Hist1D.hmomentumRec1_0.ordinal()].getData();
float[] hmomentumFoundData1_0 = histograms1D[Hist1D.hmomentumFound1_0.ordinal()].getData();
float[] hmomentumRecData0_98 = histograms1D[Hist1D.hmomentumRec0_98.ordinal()].getData();
float[] hmomentumFoundData0_98 = histograms1D[Hist1D.hmomentumFound0_98.ordinal()].getData();
float[] hmomentumRecData0_95 = histograms1D[Hist1D.hmomentumRec0_95.ordinal()].getData();
float[] hmomentumFoundData0_95 = histograms1D[Hist1D.hmomentumFound0_95.ordinal()].getData();
float[] hmomentumRecData0_92 = histograms1D[Hist1D.hmomentumRec0_92.ordinal()].getData();
float[] hmomentumFoundData0_92 = histograms1D[Hist1D.hmomentumFound0_92.ordinal()].getData();
float[] hmomentumRecData0_90 = histograms1D[Hist1D.hmomentumRec0_90.ordinal()].getData();
float[] hmomentumFoundData0_90 = histograms1D[Hist1D.hmomentumFound0_90.ordinal()].getData();

float[] hmomentumGenData = histograms1D[Hist1D.hmomentumGen.ordinal()].getData();
float[] hmomentumGenFoundData = histograms1D[Hist1D.hmomentumGenFound.ordinal()].getData();

// calculate steps between bins
float step = BEAM_ENERGY/BIN_NUM;
float currentP = step/2;

// loop over data from histograms and create NDE histogram
for(int i = 0; i < BIN_NUM; i++){ 
    if(hmomentumRecData1_3[i] != 0){
        ndeAtI1_3 = hmomentumFoundData1_3[i]/hmomentumRecData1_3[i];
        dndeAtI1_3 = Math.sqrt(ndeAtI1_3*(1-ndeAtI1_3)*nentriesTotal1_3)/nentriesTotal1_3;
        graphs[Graph.hNDE1_3.ordinal()].addPoint(currentP, ndeAtI1_3, 0, dndeAtI1_3);
    }
    if(hmomentumRecData1_2[i] != 0){
        ndeAtI1_2 = hmomentumFoundData1_2[i]/hmomentumRecData1_2[i];
        dndeAtI1_2 = Math.sqrt(ndeAtI1_2*(1-ndeAtI1_2)*nentriesTotal1_2)/nentriesTotal1_2;
        graphs[Graph.hNDE1_2.ordinal()].addPoint(currentP, ndeAtI1_2, 0, dndeAtI1_2);
    }
    if(hmomentumRecData1_1[i] != 0){
        ndeAtI1_1 = hmomentumFoundData1_1[i]/hmomentumRecData1_1[i];
        dndeAtI1_1 = Math.sqrt(ndeAtI1_1*(1-ndeAtI1_1)*nentriesTotal1_1)/nentriesTotal1_1;
        graphs[Graph.hNDE1_1.ordinal()].addPoint(currentP, ndeAtI1_1, 0, dndeAtI1_1);
    }
    if(hmomentumRecData1_0[i] != 0){
        ndeAtI1_0 = hmomentumFoundData1_0[i]/hmomentumRecData1_0[i];
        dndeAtI1_0 = Math.sqrt(ndeAtI1_0*(1-ndeAtI1_0)*nentriesTotal1_0)/nentriesTotal1_0;
        graphs[Graph.hNDE1_0.ordinal()].addPoint(currentP, ndeAtI1_0, 0, dndeAtI1_0);
    }
    if(hmomentumRecData0_98[i] != 0){
        ndeAtI0_98 = hmomentumFoundData0_98[i]/hmomentumRecData0_98[i];
        dndeAtI0_98 = Math.sqrt(ndeAtI0_98*(1-ndeAtI0_98)*nentriesTotal0_98)/nentriesTotal0_98;
        graphs[Graph.hNDE0_98.ordinal()].addPoint(currentP, ndeAtI0_98, 0, dndeAtI0_98);
    }
    if(hmomentumRecData0_95[i] != 0){
        ndeAtI0_95 = hmomentumFoundData0_95[i]/hmomentumRecData0_95[i];
        dndeAtI0_95 = Math.sqrt(ndeAtI0_95*(1-ndeAtI0_95)*nentriesTotal0_95)/nentriesTotal0_95;
        graphs[Graph.hNDE0_95.ordinal()].addPoint(currentP, ndeAtI0_95, 0, dndeAtI0_95);
    }
    if(hmomentumRecData0_92[i] != 0){
        ndeAtI0_92 = hmomentumFoundData0_92[i]/hmomentumRecData0_92[i];
        dndeAtI0_92 = Math.sqrt(ndeAtI0_92*(1-ndeAtI0_92)*nentriesTotal0_92)/nentriesTotal0_92;
        graphs[Graph.hNDE0_92.ordinal()].addPoint(currentP, ndeAtI0_92, 0, dndeAtI0_92);
    }
    if(hmomentumRecData0_90[i] != 0){
        ndeAtI0_90 = hmomentumFoundData0_90[i]/hmomentumRecData0_90[i];
        dndeAtI0_90 = Math.sqrt(ndeAtI0_90*(1-ndeAtI0_90)*nentriesTotal0_90)/nentriesTotal0_90;
        graphs[Graph.hNDE0_90.ordinal()].addPoint(currentP, ndeAtI0_90, 0, dndeAtI0_90);
    }
    if(hmomentumGenData[i] != 0){
        ndeGenAtI = hmomentumGenFoundData[i]/hmomentumGenData[i];
        dndeGenAtI = Math.sqrt(ndeGenAtI*(1-ndeGenAtI)*nentriesTotalGen)/nentriesTotalGen;
        graphs[Graph.hNDEGen.ordinal()].addPoint(currentP, ndeGenAtI, 0, dndeGenAtI);
    }
    currentP += step;
} // end loop over data
//****************************************************************************************************************

//= Print success and write summed histograms to file ============================================================
System.out.println("File summation complete!");
System.out.println("Summed histograms stored in " + inputFileBase + "_Total" + FILE_SUFFIX);
//write summed histograms to file
histFileFinal.writeFile(inputFileBase + "_Total" + FILE_SUFFIX);
//================================================================================================================