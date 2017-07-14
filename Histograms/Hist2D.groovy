// The enum below contains the names of the 2D histograms for the NDE analysis 
// that should be tracked by the HistHandler class. If HistHandler is used for 
// another analysis this list should be edited to contain a comma seperated list 
// of the names of the 2D histograms.

public enum Hist2D{
	// list new 2D histograms as follows:
	// histName (numXBins, xMin, xMax, numYBins, yMin, yMax, xAxisTitle, yAxisTitle, histTitle)
	// The number of x and y bins and x and y min and max are required, all other arguments are optional
	// Colors to choose from are as follows: black, red, green, blue, yellow, magenta, light blue, purple, and dark green

	hacceptance (50, 0, 50, 100, 0, 10, "theta (degree)", "momentum (GeV/c)"),
	hhits (821, -410, 410, 961, -480, 480, "X", "Y");

	// No code below the point should be altered

	int xBins;
	int yBins;
	double xMin;
	double xMax;
	double yMin;
	double yMax;
	String title;
	String xTitle;
	String yTitle;
	String color;
	int settings;

	// Basic constructor that builds a 2D histogram with no titles
	public Hist2D(int numXBins, double xMinimum, double xMaximum, int numYBins, double yMinimum, double yMaximum){
		this.xBins = numXBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.yBins = numYBins;
		this.yMin = yMinimum;
		this.yMax = yMaximum;
		this.settings = 3;
	}

	// Constructor that builds a 2D histogram with an x axis title
	public Hist2D(int numXBins, double xMinimum, double xMaximum, int numYBins, double yMinimum, double yMaximum, String xHistTitle){
		this.xBins = numXBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.yBins = numYBins;
		this.yMin = yMinimum;
		this.yMax = yMaximum;
		this.xTitle = xHistTitle;
		this.settings = 2;
	}

	// Construtor that builds a 2D histogram with an x axis title and a y axis title
	public Hist2D(int numXBins, double xMinimum, double xMaximum, int numYBins, double yMinimum, double yMaximum, String xHistTitle, String yHistTitle){
		this.xBins = numXBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.yBins = numYBins;
		this.yMin = yMinimum;
		this.yMax = yMaximum;
		this.xTitle = xHistTitle;
		this.yTitle = yHistTitle;
		this.settings = 1;
	}

	// Construtor that builds a 2D histogram with an x axis title, y axis title, and a histogram title
	public Hist2D(int numXBins, double xMinimum, double xMaximum, int numYBins, double yMinimum, double yMaximum, String xHistTitle, String yHistTitle, String histTitle){
		this.xBins = numXBins;
		this.xMin = xMinimum;
		this.xMax = xMaximum;
		this.yBins = numYBins;
		this.yMin = yMinimum;
		this.yMax = yMaximum;
		this.xTitle = xHistTitle;
		this.yTitle = yHistTitle;
		this.title = histTitle;
		this.settings = 0;
	}
}