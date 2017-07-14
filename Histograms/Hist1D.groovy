// The enum below contains the names of the 1D histograms for the NDE analysis 
// that should be tracked by the HistHandler class. If HistHandler is used for 
// another analysis this list should be edited to contain a comma seperated list 
// of the names of the 1D histograms.

public enum Hist1D{
	// list new 1D histograms as follows:
	// histName (numBins, xMin, xMax, xAxisTitle, lineColor, histTitle, yAxisTitle)
	// The number of bins and x min and max are required, all other arguments are optional
	// Colors to choose from are as follows: black, red, green, blue, yellow, magenta, light blue, purple, and dark green

	hthetapq (100, 0, 10, "thetapq with cut (deg)"),
	htheta (100, 0, 10, "thetapq (deg)", "red"),
	hthetaGen (100, 0, 10, "thetapq (deg)", "blue"),
	hthetaGen2 (100, 0, 10, "thetapq (deg)", "green"),

	hmomentumRec1_3 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "red"),
	hmomentumRec1_2 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "purple"),
	hmomentumRec1_1 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "green"),
	hmomentumRec1_0 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "magenta"),
	hmomentumRec0_98 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "light blue"),
	hmomentumRec0_95 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "yellow"),
	hmomentumRec0_92 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "dark green"),
	hmomentumRec0_90 (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "black"),
	hmomentumGen (50, 0, 11, "momentum reconstructed neutrons(GeV/c)", "blue"),

	hmomentumFound1_3 (50, 0, 11, "momentum found neutrons(GeV/c)", "red"),
	hmomentumFound1_2 (50, 0, 11, "momentum found neutrons(GeV/c)", "purple"),
	hmomentumFound1_1 (50, 0, 11, "momentum found neutrons(GeV/c)", "green"),
	hmomentumFound1_0 (50, 0, 11, "momentum found neutrons(GeV/c)", "magenta"),
	hmomentumFound0_98 (50, 0, 11, "momentum found neutrons(GeV/c)", "light blue"),
	hmomentumFound0_95 (50, 0, 11, "momentum found neutrons(GeV/c)", "yellow"),
	hmomentumFound0_92 (50, 0, 11, "momentum found neutrons(GeV/c)", "dark green"),
	hmomentumFound0_90 (50, 0, 11, "momentum found neutrons(GeV/c)", "black"),
	hmomentumGenFound (50, 0, 11, "momentum found neutrons(GeV/c)", "blue"),

	hmissingMassHerm1_3 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "red"),
	hmissingMassHerm1_2 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "purple"),
	hmissingMassHerm1_1 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "green"),
	hmissingMassHerm1_0 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "magenta"),
	hmissingMassHerm0_98 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "light blue"),
	hmissingMassHerm0_95 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "yellow"),
	hmissingMassHerm0_92 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "dark green"),
	hmissingMassHerm0_90 (150, 0, 1.4, "missing mass with hermicity cut (GeV/c^2)^2", "black"),
	hmissingMass (300, 0, 6, "missing mass (GeV/c^2)^2"),

	hecSectors (6, 1, 7, "Hit Sector");

	// No code below this point should be altered

	// Internal enum variables for building histograms
	int bins;
	double xMin;
	double xMax;
	String title;
	String xTitle;
	String yTitle;
	String color;
	int settings;

	// Basic constructor that builds a 1D histogram with no titles or color
	Hist1D(int numBins, double xMinimum, double xMaximum){
		this.bins = numBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.settings = 4;
	}

	// Constructor that builds a 1D histogram with an x axis title
	Hist1D(int numBins, double xMinimum, double xMaximum, String xHistTitle){
		this.bins = numBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.xTitle = xHistTitle;
		this.settings = 3;
	}

	// Constructor that builds a 1D histogram with an x axis title and color
	Hist1D(int numBins, double xMinimum, double xMaximum, String xHistTitle, String histColor){
		this.bins = numBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.xTitle = xHistTitle;
		this.color = histColor;
		this.settings = 2;
	}

	// Constructor that builds a 1D histogram with an x axis title, color, and a histogram title
	Hist1D(int numBins, double xMinimum, double xMaximum, String xHistTitle, String histColor, String histTitle){
		this.bins = numBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.xTitle = xHistTitle;
		this.color = histColor;
		this.title = histTitle;
		this.settings = 1;
	}

	// Constructor that builds a 1D histogram with an x axis title, color, a histogram title, and a y axis title
	Hist1D(int numBins, double xMinimum, double xMaximum, String xHistTitle, String histColor, String histTitle, String yHistTitle){
		this.bins = numBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.xTitle = xHistTitle;
		this.color = histColor;
		this.title = histTitle;
		this.yTitle = yHistTitle;
		this.settings = 0;
	}
}