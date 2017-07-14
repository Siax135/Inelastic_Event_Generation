import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;
import Hist1D;
import Hist2D;
import Graph;

// This class is used to handle histograms for analysis use. It will take care of initializing histograms and putting them in a TDirectory
// so they can be written to file. It also has the capability to add the histograms from one handler to another.
public class HistHandler{
	// DIR_NAME should be set to the name of the directory to be created in the TDirectory histFile.
	final String DIR_NAME = "neutrons";

	// Instance variable for the HistHandler class
	TDirectory histFile;
	H1F[] histograms1D;
	H2F[] histograms2D;
	GraphErrors[] graphs;

	// Constructor to create the TDirectory and set aside space in memory for each histogram
	public HistHandler(){
		histFile = new TDirectory();
		histograms1D = new H1F[Hist1D.values().length];
		histograms2D = new H2F[Hist2D.values().length];
		graphs = new GraphErrors[Graph.values().length];
	}

	// Constructor to read a histogram file and get all of the stored histograms for easy access
	public HistHandler(String filename){
		histFile = new TDirectory();
		histFile.readFile(filename);
		histograms1D = new H1F[Hist1D.values().length];
		histograms2D = new H2F[Hist2D.values().length];
		graphs = new GraphErrors[Graph.values().length];

		for(Hist1D h : Hist1D.values()){ // loop through 1D histograms and add them to the 1D hist array
			histograms1D[h.ordinal()] = (H1F)histFile.getObject(DIR_NAME, (String)h);
		} // end loop over 1D histograms

		for(Hist2D h : Hist2D.values()){ // loop through 2D histograms and add them to the 2D hist array
			histograms2D[h.ordinal()] = (H2F)histFile.getObject(DIR_NAME, (String)h);
		} // end loop over 2D histograms

		for(Graph g : Graph.values()){ // loop through graphs and add them to the graph array
			graphs[g.ordinal()] = (GraphErrors)histFile.getObject(DIR_NAME, (String)g);
		}// end loop over graphs
	}

	// This method sets up a new histogram file by making the main directory, setting up each histogram, 
	// and adding them to the correct array for quick access.
	public TDirectory initializeHist(){
		histFile.mkdir(DIR_NAME);

		for(Hist1D h : Hist1D.values()){ // loop through 1D histograms to add them to the hist file and array and set appropriate settings
			histFile.getDir(DIR_NAME).add((String)h, new H1F((String)h, h.bins, h.xMin, h.xMax));
			histograms1D[h.ordinal()] = (H1F)histFile.getObject(DIR_NAME,(String)h);
			switch(h.settings){ // switch to check which settings need to be set
				case 0:
					histograms1D[h.ordinal()].setTitleY(h.yTitle);
				case 1:
					histograms1D[h.ordinal()].setTitle(h.title);
				case 2:
					histograms1D[h.ordinal()].setLineColor(this.getColor(h.color));
				case 3:
					histograms1D[h.ordinal()].setTitleX(h.xTitle);
			} // end settings switch
		} // end loop over 1D histograms

		for(Hist2D h : Hist2D.values()){ // loop though 2D histograms to add them to the hist file and array and set appropriate settings
			histFile.getDir(DIR_NAME).add((String)h, new H2F((String)h, h.xBins, h.xMin, h.xMax, h.yBins, h.yMin, h.yMax));
			histograms2D[h.ordinal()] = (H2F)histFile.getObject(DIR_NAME,(String)h);
			switch(h.settings){ // switch to check which settings need to be set
				case 0:
					histograms2D[h.ordinal()].setTitle(h.title);
				case 1:
					histograms2D[h.ordinal()].setTitleY(h.yTitle);
				case 2:
					histograms2D[h.ordinal()].setTitleX(h.xTitle);
			} // end settings switch
		} // end loop over 2D histograms

		for(Graph g : Graph.values()){ // loop over graphs to add them to the hist file and array and set appropriate settings
			histFile.getDir(DIR_NAME).add((String)g, new GraphErrors((String)g));
			graphs[g.ordinal()] = (GraphErrors)histFile.getObject(DIR_NAME,(String)g);
			switch(g.settings){ // switch to check which settings need to be set
				case 0:
					graphs[g.ordinal()].setTitle(g.title);
				case 1:
					graphs[g.ordinal()].setTitleY(g.yTitle);
				case 2:
					graphs[g.ordinal()].setTitleX(g.xTitle);
				case 3:
					graphs[g.ordinal()].setMarkerColor(this.getColor(g.color));
			} // end settings switch
		} // end loop over graphs

		return histFile;
	}

	// Method to add the histogram data in another handler to the histograms in the calling handler.
	// This method does not do any addition of GraphErrors as those are non-trivial and should be 
	// handled on a case-by-case basis.
	public void add(HistHandler other){
		for(Hist1D h : Hist1D.values()){ // loop over and add 1D histogram data
			this.histograms1D[h.ordinal()].add(other.histograms1D[h.ordinal()]);
		} // end loop for 1D histograms

		for(Hist2D h : Hist2D.values()){ // loop over and add 2D histogram data
			this.histograms2D[h.ordinal()].add(other.histograms2D[h.ordinal()]);
		} // end loop for 2D histograms
	}

	// Returns a handle to the TDirectory file is needed.
	public TDirectory getFile(){
		return histFile;
	}

	// Returns a handle to the 1D histogram array.
	public H1F[] get1DHist(){
		return histograms1D;
	}

	// Returns a handle to the 2D histogram array.
	public H2F[] get2DHist(){
		return histograms2D;
	}

	// returns a handle to the GraphErrors array.
	public GraphErrors[] getGraphs(){
		return graphs;
	}

	// private method used to convert color names into the ROOT color-int system
	private int getColor(String color){
		switch(color){ // switch to check desired color, if desired color is not found black is returned
			case "red":
				return 2;
			case "green":
				return 3;
			case "blue":
				return 4;
			case "yellow":
				return 5;
			case "magenta":
				return 6;
			case "light blue":
				return 7;
			case "purple":
				return 9;
			case "dark green":
				return 8;
		} // end color switch
		return 1; // return black if given color isn't in switch
	}
}