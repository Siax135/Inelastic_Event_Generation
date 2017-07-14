import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;
import HistHandler;

String inputFile;
boolean allOptions;

//* Input Argument Stuff *****************************************************************************************
// If to check running arguments
if(args.length == 0){ // If there are no arguments supplied then show usage and exit
	System.out.println("Usage: >run-groovy NDEHistViewer.groovy <inputFile.hipo> <optional_flags>");
	System.out.println();
	System.out.println("You can also use the -h flag to get a list of the optional flags");
	System.out.println("Usage: >run-groovy NDEHistViewer.groovy -h");
	System.out.println();
	System.exit(0);
}else if(args.length == 1){ // If there is only one option then check to see which one possibility it is
	if(args[0] == "-h"){ // If the one option is the help option then show help message
		System.out.println();
		System.out.println("-theta      Show theta plots");
		System.out.println("-P          Show momentum plots");
		System.out.println("-MM         Show missing mass plots");
		System.out.println("-accep      Show acceptance plot");
		System.out.println("-NDE        Show NDE plot");
		System.out.println("-EC         Show EC hit plots");
		System.out.println();
		System.exit(0);
	}else{ // If the one option isn't the help option then assume its the name of a histogram file and show all histograms
		inputFile = args[0];
		allOptions = true;
		System.out.println("Showing all histograms and graphs in file " + inputFile);
		System.out.println("For a list of flags to only show specific histograms re-run with the -h option as below.");
		System.out.println(">run-groovy NDEHistViewer.groovy -h");
	}
}else{ // If more than one options is supplied then assume the first one is the input file and the others are options to display specific plots
	allOptions = false;
} // end argument if
//****************************************************************************************************************

//* Read input file and get plots ********************************************************************************
inputFile = args[0]

// Create a HistHandler to read the file and add all of the histograms and graphs to the correct arrays.
HistHandler handler = new HistHandler(inputFile);
H1F[] histograms1D = handler.get1DHist();
H2F[] histograms2D = handler.get2DHist();
GraphErrors[] graphs = handler.getGraphs();
//****************************************************************************************************************

//* Display appropriate plots ************************************************************************************
// Set up a TCanvas's to display plots as needed.
for(int i = 0; i < args.length; i++){ // loop over arguments
	switch(args[i]){ // switch to check which plots should be displayed
		case inputFile:
			if(!allOptions){ // check to see if all plots should be shown or not
				continue;
			} 
		case "-theta": // Show theta plots
			TCanvas c1 = new TCanvas("Theta Data",800,600);
			c1.divide(2,1);
			c1.cd(0);
			c1.draw(histograms1D[Hist1D.hthetapq.ordinal()]);
			c1.cd(1);
			c1.draw(histograms1D[Hist1D.hthetaGen2.ordinal()]);
			c1.draw(histograms1D[Hist1D.hthetaGen.ordinal()],"same");
			c1.draw(histograms1D[Hist1D.htheta.ordinal()],"same");
			if(!allOptions){
				continue;
			}
		case "-P": // Show momentum plots
			TCanvas c2 = new TCanvas("Momentum Plots", 800, 600);
			c2.divide(2,1);
			c2.cd(0);
			c2.draw(histograms1D[Hist1D.hmomentumGen.ordinal()]);
			c2.draw(histograms1D[Hist1D.hmomentumRec1_3.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumRec1_2.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumRec1_1.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumRec1_0.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumRec0_98.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumRec0_95.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumRec0_92.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumRec0_90.ordinal()],"same");
			c2.cd(1);
			c2.draw(histograms1D[Hist1D.hmomentumGenFound.ordinal()]);
			c2.draw(histograms1D[Hist1D.hmomentumFound1_3.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumFound1_2.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumFound1_1.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumFound1_0.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumFound0_98.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumFound0_95.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumFound0_92.ordinal()],"same");
			c2.draw(histograms1D[Hist1D.hmomentumFound0_90.ordinal()],"same");
			if(!allOptions){
				continue;
			}
		case "-MM": // Show missing mass plots
			TCanvas c3 = new TCanvas("Missing Mass Plots", 800, 600);
			c3.divide(2,1);
			c3.cd(0);
			c3.draw(histograms1D[Hist1D.hmissingMass.ordinal()]);
			c3.cd(1);
			c3.draw(histograms1D[Hist1D.hmissingMassHerm1_3.ordinal()]);
			c3.draw(histograms1D[Hist1D.hmissingMassHerm1_2.ordinal()],"same");
			c3.draw(histograms1D[Hist1D.hmissingMassHerm1_1.ordinal()],"same");
			c3.draw(histograms1D[Hist1D.hmissingMassHerm1_0.ordinal()],"same");
			c3.draw(histograms1D[Hist1D.hmissingMassHerm0_98.ordinal()],"same");
			c3.draw(histograms1D[Hist1D.hmissingMassHerm0_95.ordinal()],"same");
			c3.draw(histograms1D[Hist1D.hmissingMassHerm0_92.ordinal()],"same");
			c3.draw(histograms1D[Hist1D.hmissingMassHerm0_90.ordinal()],"same");
			if(!allOptions){
				continue;
			}
		case "-accep": // Show acceptance plot
			TCanvas c4 = new TCanvas("Acceptance", 400, 600);
			c4.divide(1,1);
			c4.cd(0);
			c4.draw(histograms2D[Hist2D.hacceptance.ordinal()]);
			if(!allOptions){
				continue;
			}
		case "-NDE": // Show NDE plots
			TCanvas c5 = new TCanvas("NDE", 400, 600);
			c5.divide(1,1);
			c5.cd(0);
			c5.draw(graphs[Graph.hNDE1_3.ordinal()]);
			c5.draw(graphs[Graph.hNDE1_2.ordinal()],"same");
			c5.draw(graphs[Graph.hNDE1_1.ordinal()],"same");
			c5.draw(graphs[Graph.hNDE1_0.ordinal()],"same");
			c5.draw(graphs[Graph.hNDE0_98.ordinal()],"same");
			c5.draw(graphs[Graph.hNDE0_95.ordinal()],"same");
			c5.draw(graphs[Graph.hNDE0_92.ordinal()],"same");
			c5.draw(graphs[Graph.hNDE0_90.ordinal()],"same");
			c5.draw(graphs[Graph.hNDEGen.ordinal()],"same");
			if(!allOptions){
				continue;
			}
		case "-EC": // Show EC hits plots
			TCanvas c6 = new TCanvas("EC Hits", 800, 600);
			c6.divide(2,1);
			c6.cd(0);
			c6.draw(histograms1D[Hist1D.hecSectors.ordinal()]);
			c6.cd(1);
			c6.draw(histograms2D[Hist2D.hhits.ordinal()]);
			if(!allOptions){
				continue;
			}
	} // end switch checking which plots should be shown
	if(!allOptions){ // if not all plots are shown and a bad flag is given then say so
		System.out.println("Flag " + args[i] + " not recognized!");
		System.out.println("Continuing  to see if there are other recognized flags.");
	}
} // End loop over arguments
System.out.println("Finished parsing flags");
//****************************************************************************************************************