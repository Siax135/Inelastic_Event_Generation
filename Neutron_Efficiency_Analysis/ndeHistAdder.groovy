import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;
import java.lang.Integer;
import java.io.FileNotFoundException;
import java.io.IOException;
import HistInitializer;

//constants
final String FILE_SUFFIX = ".hipo"
final int BIN_NUM = 50;
final float BEAM_ENERGY = 11.0;  // GeV

//* Input Argument Stuff *****************************************************************************************

// variables for dealing with input arguments
String inputFileBase, numFilesString, numPrintString;
int numFiles, numPrint;
float ndeAtI, dndeAtI = 0;
float ndeGenAtI, dndeGenAtI = 0;
int nentriesTotal = 0;
int nentriesTotalGen = 0;

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


HistInitializer init = new HistInitializer();
TDirectory histFileFinal = init.initializeHist();
H1F[] histograms1D = init.get1DHist();
H2F[] histograms2D = init.get2DHist();
GraphErrors[] graphs = init.getGraphs();
// histFileFinal.mkdir("neutrons");

// histFileFinal.getDir("neutrons").add("hthetapq", new H1F("hthetapq", 100, 0, 10));
// H1F hthetapq = (H1F)histFileFinal.getObject("neutrons","hthetapq");
// hthetapq.setTitleX("thetapq with cut (deg)");

// histFileFinal.getDir("neutrons").add("htheta", new H1F("htheta", 100, 0, 10));
// H1F htheta = (H1F)histFileFinal.getObject("neutrons","htheta");
// htheta.setTitleX("thetapq (deg)");
// htheta.setLineColor(4);

// histFileFinal.getDir("neutrons").add("hthetaGen", new H1F("hthetaGen", 100, 0, 10));
// H1F hthetaGen = (H1F)histFileFinal.getObject("neutrons","hthetaGen");
// hthetaGen.setTitleX("thetapq (deg)");
// hthetaGen.setLineColor(2);

// histFileFinal.getDir("neutrons").add("hmomentumRec", new H1F("hmomentumRec", BIN_NUM, 0, BEAM_ENERGY));
// H1F hmomentumRec = (H1F)histFileFinal.getObject("neutrons","hmomentumRec");
// hmomentumRec.setTitleX("momentum reconstructed neutrons(GeV/c)");

// histFileFinal.getDir("neutrons").add("hmomentumFound", new H1F("hmomentumFound", BIN_NUM, 0, BEAM_ENERGY));
// H1F hmomentumFound = (H1F)histFileFinal.getObject("neutrons","hmomentumFound");
// hmomentumFound.setTitleX("momentum found neutrons(GeV/c)");

// histFileFinal.getDir("neutrons").add("hmissingMass", new H1F("hmissingMass", 150, 0, 1));
// H1F hmissingMass = (H1F)histFileFinal.getObject("neutrons","hmissingMass");
// hmissingMass.setTitleX("missing mass with hermicity cut (GeV/c^2)");

// histFileFinal.getDir("neutrons").add("hmissingMassHerm", new H1F("hmissingMassHerm", 300, 0, 6));
// H1F hmissingMassHerm = (H1F)histFileFinal.getObject("neutrons","hmissingMassHerm");
// hmissingMassHerm.setTitleX("Missing Mass (GeV/c^2)^2)");

// histFileFinal.getDir("neutrons").add("hecSectors", new H1F("hecSectors", 6, 1, 7));
// H1F hecSectors = (H1F)histFileFinal.getObject("neutrons","hecSectors");
// hecSectors.setTitleX("Hit Sector");

// histFileFinal.getDir("neutrons").add("hacceptance", new H2F("hacceptance", BIN_NUM, 0, 50, 100, 0, 10));
// H2F hacceptance = (H2F)histFileFinal.getObject("neutrons","hacceptance");
// hacceptance.setTitleX("theta (degree)");
// hacceptance.setTitleY("momentum (GeV/c)");

// histFileFinal.getDir("neutrons").add("hhits", new H2F("hhits", 821, -410, 410, 961, -480, 480));
// H2F hhits = (H2F)histFileFinal.getObject("neutrons","hhits");
// hhits.setTitleX("X");
// hhits.setTitleY("Y");

// histFileFinal.getDir("neutrons").add("hNDE", new GraphErrors("hNDE"));
// GraphErrors hNDE = (GraphErrors)histFileFinal.getObject("neutrons","hNDE");
// hNDE.setTitleX("momentum (GeV/c)");
// hNDE.setTitleY("NDE");

System.out.println("Success");
System.out.println();
//****************************************************************************************************************

//* Addition of files ********************************************************************************************
// Create initial finial name
String fullFileName = inputFileBase + "0000" + FILE_SUFFIX;

System.out.println("Starting addition with file " + fullFileName);

// open initial file
TDirectory inputHistFile = new TDirectory();
inputHistFile.readFile(fullFileName);

// declare histograms objects that will be used to store data from files that need to be summed
// and initialize them to the data in the first file. 
H1F inputHthetapq = (H1F)inputHistFile.getObject("neutrons","hthetapq");
H1F inputHtheta = (H1F)inputHistFile.getObject("neutrons","htheta");
H1F inputHthetaGen = (H1F)inputHistFile.getObject("neutrons","hthetaGen");
H1F inputHthetaGen2 = (H1F)inputHistFile.getObject("neutrons","hthetaGen2");
H1F inputHmomentumRec = (H1F)inputHistFile.getObject("neutrons","hmomentumRec");
H1F inputHmomentumGen = (H1F)inputHistFile.getObject("neutrons","hmomentumGen");
H1F inputHmomentumFound = (H1F)inputHistFile.getObject("neutrons","hmomentumFound");
H1F inputHmomentumGenFound = (H1F)inputHistFile.getObject("neutrons","hmomentumGenFound");
H1F inputHmissingMass = (H1F)inputHistFile.getObject("neutrons","hmissingMass");
H1F inputHmissingMassHerm = (H1F)inputHistFile.getObject("neutrons","hmissingMassHerm");
H1F inputHecSectors = (H1F)inputHistFile.getObject("neutrons","hecSectors");
H2F inputHacceptance = (H2F)inputHistFile.getObject("neutrons","hacceptance");
H2F inputHhits = (H2F)inputHistFile.getObject("neutrons","hhits");

System.out.println("Before add: " + histograms1D[Hist1D.hmomentumRec.ordinal()].getEntries());

// add histogram data to total
histograms1D[Hist1D.hthetapq.ordinal()].add(inputHthetapq); 
histograms1D[Hist1D.htheta.ordinal()].add(inputHtheta); 
histograms1D[Hist1D.hthetaGen.ordinal()].add(inputHthetaGen);
histograms1D[Hist1D.hthetaGen2.ordinal()].add(inputHthetaGen2);
histograms1D[Hist1D.hmomentumRec.ordinal()].add(inputHmomentumRec);
histograms1D[Hist1D.hmomentumGen.ordinal()].add(inputHmomentumGen); 
histograms1D[Hist1D.hmomentumFound.ordinal()].add(inputHmomentumFound);
histograms1D[Hist1D.hmomentumGenFound.ordinal()].add(inputHmomentumGenFound); 
histograms1D[Hist1D.hmissingMass.ordinal()].add(inputHmissingMass); 
histograms1D[Hist1D.hmissingMassHerm.ordinal()].add(inputHmissingMassHerm); 
histograms1D[Hist1D.hecSectors.ordinal()].add(inputHecSectors); 
histograms2D[Hist2D.hacceptance.ordinal()].add(inputHacceptance); 
histograms2D[Hist2D.hhits.ordinal()].add(inputHhits);  

System.out.println("After add: " + histograms1D[Hist1D.hmomentumRec.ordinal()].getEntries());

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

	// read in file
	inputHistFile.readFile(fullFileName);

	// get histograms from file
	inputHthetapq = (H1F)inputHistFile.getObject("neutrons","hthetapq");
	inputHtheta = (H1F)inputHistFile.getObject("neutrons","htheta");
	inputHthetaGen = (H1F)inputHistFile.getObject("neutrons","hthetaGen");
	inputHthetaGen2 = (H1F)inputHistFile.getObject("neutrons","hthetaGen2");
	inputHmomentumRec = (H1F)inputHistFile.getObject("neutrons","hmomentumRec");
	inputHmomentumGen = (H1F)inputHistFile.getObject("neutrons","hmomentumGen");
	inputHmomentumFound = (H1F)inputHistFile.getObject("neutrons","hmomentumFound");
	inputHmomentumGenFound = (H1F)inputHistFile.getObject("neutrons","hmomentumGenFound");
	inputHmissingMass = (H1F)inputHistFile.getObject("neutrons","hmissingMass");
	inputHmissingMassHerm = (H1F)inputHistFile.getObject("neutrons","hmissingMassHerm");
	inputHecSectors = (H1F)inputHistFile.getObject("neutrons","hecSectors");
	inputHacceptance = (H2F)inputHistFile.getObject("neutrons","hacceptance");
	inputHhits = (H2F)inputHistFile.getObject("neutrons","hhits");

	// add histogram data to total
	histograms1D[Hist1D.hthetapq.ordinal()].add(inputHthetapq); 
	histograms1D[Hist1D.htheta.ordinal()].add(inputHtheta); 
	histograms1D[Hist1D.hthetaGen.ordinal()].add(inputHthetaGen);
	histograms1D[Hist1D.hthetaGen2.ordinal()].add(inputHthetaGen2);
	histograms1D[Hist1D.hmomentumRec.ordinal()].add(inputHmomentumRec);
	histograms1D[Hist1D.hmomentumGen.ordinal()].add(inputHmomentumGen); 
	histograms1D[Hist1D.hmomentumFound.ordinal()].add(inputHmomentumFound);
	histograms1D[Hist1D.hmomentumGenFound.ordinal()].add(inputHmomentumGenFound); 
	histograms1D[Hist1D.hmissingMass.ordinal()].add(inputHmissingMass); 
	histograms1D[Hist1D.hmissingMassHerm.ordinal()].add(inputHmissingMassHerm); 
	histograms1D[Hist1D.hecSectors.ordinal()].add(inputHecSectors); 
	histograms2D[Hist2D.hacceptance.ordinal()].add(inputHacceptance); 
	histograms2D[Hist2D.hhits.ordinal()].add(inputHhits);  
}// end of for loop for summing files

for(int i = 0; i < BIN_NUM; i++){
	nentriesTotal += histograms1D[Hist1D.hmomentumRec.ordinal()].getDataY(i);
	nentriesTotalGen += histograms1D[Hist1D.hmomentumGen.ordinal()].getDataY(i);
}

System.out.println("Total: " + nentriesTotal + " Total Gen: " + nentriesTotalGen);
System.out.println("At rec 3: " + histograms1D[Hist1D.hmomentumRec.ordinal()].getDataY(3));
System.out.println("At found 3: " + histograms1D[Hist1D.hmomentumFound.ordinal()].getDataY(3));

// get data from momentum histograms
float[] hmomentumRecData = histograms1D[Hist1D.hmomentumRec.ordinal()].getData();
float[] hmomentumFoundData = histograms1D[Hist1D.hmomentumFound.ordinal()].getData();

float[] hmomentumGenData = histograms1D[Hist1D.hmomentumGen.ordinal()].getData();
float[] hmomentumGenFoundData = histograms1D[Hist1D.hmomentumGenFound.ordinal()].getData();

// calculate steps between bins
float step = BEAM_ENERGY/BIN_NUM;
float currentP = step/2;

// loop over data from histograms and create NDE histogram
for(int i = 0; i < BIN_NUM; i++){ 
    if(hmomentumRecData[i] != 0){
        ndeAtI = hmomentumFoundData[i]/hmomentumRecData[i];
        dndeAtI = Math.sqrt(ndeAtI*(1-ndeAtI)*nentriesTotal)/nentriesTotal;
        graphs[Graph.hNDE.ordinal()].addPoint(currentP, ndeAtI, 0, dndeAtI);
        System.out.println("i: " + i + " P: " + currentP + " NDE: " + ndeAtI + " +/- " + dndeAtI);
    }
    if(hmomentumGenData[i] != 0){
        ndeGenAtI = hmomentumGenFoundData[i]/hmomentumGenData[i];
        dndeGenAtI = Math.sqrt(ndeGenAtI*(1-ndeGenAtI)*nentriesTotalGen)/nentriesTotalGen;
        graphs[Graph.hNDEGen.ordinal()].addPoint(currentP, ndeGenAtI, 0, dndeGenAtI);
        System.out.println("P Gen: " + currentP + " NDE Gen: " + ndeGenAtI + " +/- " + dndeGenAtI);
    }
    currentP += step;
} // end loop over data

System.out.println("File summation complete!");
System.out.println("Summed histograms stored in " + inputFileBase + "_Total" + FILE_SUFFIX);
//****************************************************************************************************************

//write summed histograms to file
histFileFinal.writeFile(inputFileBase + "_Total" + FILE_SUFFIX);