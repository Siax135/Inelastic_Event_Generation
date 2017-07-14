// The enum below contains the names of the GraphErrors for the NDE analysis 
// that should be tracked by the HistHandler class. If HistHandler is used for 
// another analysis this list should be edited to contain a comma seperated list 
// of the names of the GraphErrors.

public enum Graph{
	// list new GraphErrors as follows:
	// graphName (markerColor, xAxisTitle, yAxisTitle, GraphTitle)
	// There are no required arguments for a graph
	// Colors to choose from are as follows: black, red, green, blue, yellow, magenta, light blue, purple, and dark green

	hNDE1_3 ("red", "momentum (GeV/c)", "NDE"),
	hNDE1_2 ("purple", "momentum (GeV/c)", "NDE"),
	hNDE1_1 ("green", "momentum (GeV/c)", "NDE"),
	hNDE1_0 ("magenta", "momentum (GeV/c)", "NDE"),
	hNDE0_98 ("light blue", "momentum (GeV/c)", "NDE"),
	hNDE0_95 ("yellow", "momentum (GeV/c)", "NDE"),
	hNDE0_92 ("dark green", "momentum (GeV/c)", "NDE"),
	hNDE0_90 ("black", "momentum (GeV/c)", "NDE"),
	hNDEGen ("blue", "momentum (GeV/c)", "NDE");

	// No code below this point shoud be altered

	String title;
	String xTitle;
	String yTitle;
	String color;
	int settings;

	// Basic constructor to create a simple graph with no titles or color
	public Graph(){}

	// Constructor to create a graph with colored markers
	public Graph(String graphColor){
		this.color = graphColor;
		this.settings = 3;
	}

	// Constructor to create a graph with colored markers and an x axis title
	public Graph(String graphColor, String xGraphTitle){
		this.color = graphColor;
		this.xTitle = xGraphTitle;
		this.settings = 2;
	}

	// Constructor to create a graph with colored markers, an x axis title, and a y axis title
	public Graph(String graphColor, String xGraphTitle, String yGraphTitle){
		this.color = graphColor;
		this.xTitle = xGraphTitle;
		this.yTitle = yGraphTitle;
		this.settings = 1;
	}

	// Constructor to create a graph with colored markers, an xaxis title, a y axis title, and a graph title
	public Graph(String graphColor, String xGraphTitle, String yGraphTitle, String graphTitle){
		this.color = graphColor;
		this.xTitle = xGraphTitle;
		this.yTitle = yGraphTitle;
		this.title = graphTitle;
		this.settings = 0;
	}
}