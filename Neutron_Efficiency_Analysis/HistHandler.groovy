import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;

public enum Hist1D{
	hthetapq,
	htheta,
	hthetaGen,
	hthetaGen2,
	hmomentumRec,
	hmomentumGen,
	hmomentumFound,
	hmomentumGenFound,
	hmissingMass,
	hmissingMassHerm,
	hecSectors;
}

public enum Hist2D{
	hacceptance,
	hhits
}

public enum Graph{
	hNDE,
	hNDEGen;
}

public class HistInitializer{
	final int BIN_NUM = 50;
	final float BEAM_ENERGY = 11.0;  // GeV

	TDirectory histFile;
	H1F[] histograms1D;
	H2F[] histograms2D;
	GraphErrors[] graphs;

	public HistInitializer(){
		histFile = new TDirectory();
		histograms1D = new H1F[Hist1D.values().length];
		histograms2D = new H2F[Hist2D.values().length];
		graphs = new GraphErrors[Graph.values().length];
	}

	public TDirectory initializeHist(){
		histFile.mkdir("neutrons");

		histFile.getDir("neutrons").add("hthetapq", new H1F("hthetapq", 100, 0, 10));
		histograms1D[Hist1D.hthetapq.ordinal()] = (H1F)histFile.getObject("neutrons","hthetapq");
		histograms1D[Hist1D.hthetapq.ordinal()].setTitleX("thetapq with cut (deg)");

		histFile.getDir("neutrons").add("htheta", new H1F("htheta", 100, 0, 10));
		histograms1D[Hist1D.htheta.ordinal()] = (H1F)histFile.getObject("neutrons","htheta");
		histograms1D[Hist1D.htheta.ordinal()].setTitleX("thetapq (deg)");
		histograms1D[Hist1D.htheta.ordinal()].setLineColor(2);

		histFile.getDir("neutrons").add("hthetaGen", new H1F("hthetaGen", 100, 0, 10));
		histograms1D[Hist1D.hthetaGen.ordinal()] = (H1F)histFile.getObject("neutrons","hthetaGen");
		histograms1D[Hist1D.hthetaGen.ordinal()].setTitleX("thetapq (deg)");
		histograms1D[Hist1D.hthetaGen.ordinal()].setLineColor(4);

		histFile.getDir("neutrons").add("hthetaGen2", new H1F("hthetaGen2", 100, 0, 10));
		histograms1D[Hist1D.hthetaGen2.ordinal()] = (H1F)histFile.getObject("neutrons","hthetaGen2");
		histograms1D[Hist1D.hthetaGen2.ordinal()].setTitleX("thetapq (deg)");
		histograms1D[Hist1D.hthetaGen2.ordinal()].setLineColor(3);

		histFile.getDir("neutrons").add("hmomentumRec", new H1F("hmomentumRec", BIN_NUM, 0, BEAM_ENERGY));
		histograms1D[Hist1D.hmomentumRec.ordinal()] = (H1F)histFile.getObject("neutrons","hmomentumRec");
		histograms1D[Hist1D.hmomentumRec.ordinal()].setTitleX("momentum reconstructed neutrons(GeV/c)");
		histograms1D[Hist1D.hmomentumRec.ordinal()].setLineColor(2);

		histFile.getDir("neutrons").add("hmomentumGen", new H1F("hmomentumGen", BIN_NUM, 0, BEAM_ENERGY));
		histograms1D[Hist1D.hmomentumGen.ordinal()] = (H1F)histFile.getObject("neutrons","hmomentumGen");
		histograms1D[Hist1D.hmomentumGen.ordinal()].setTitleX("momentum reconstructed neutrons(GeV/c)");
		histograms1D[Hist1D.hmomentumGen.ordinal()].setLineColor(4);

		histFile.getDir("neutrons").add("hmomentumFound", new H1F("hmomentumFound", BIN_NUM, 0, BEAM_ENERGY));
		histograms1D[Hist1D.hmomentumFound.ordinal()] = (H1F)histFile.getObject("neutrons","hmomentumFound");
		histograms1D[Hist1D.hmomentumFound.ordinal()].setTitleX("momentum found neutrons(GeV/c)");
		histograms1D[Hist1D.hmomentumFound.ordinal()].setLineColor(2);

		histFile.getDir("neutrons").add("hmomentumGenFound", new H1F("hmomentumGenFound", BIN_NUM, 0, BEAM_ENERGY));
		histograms1D[Hist1D.hmomentumGenFound.ordinal()] = (H1F)histFile.getObject("neutrons","hmomentumGenFound");
		histograms1D[Hist1D.hmomentumGenFound.ordinal()].setTitleX("momentum reconstructed neutrons(GeV/c)");
		histograms1D[Hist1D.hmomentumGenFound.ordinal()].setLineColor(4);

		histFile.getDir("neutrons").add("hmissingMass", new H1F("hmissingMass", 150, 0, 1));
		histograms1D[Hist1D.hmissingMass.ordinal()] = (H1F)histFile.getObject("neutrons","hmissingMass");
		histograms1D[Hist1D.hmissingMass.ordinal()].setTitleX("missing mass with hermicity cut (GeV/c^2)");

		histFile.getDir("neutrons").add("hmissingMassHerm", new H1F("hmissingMassHerm", 300, 0, 6));
		histograms1D[Hist1D.hmissingMassHerm.ordinal()] = (H1F)histFile.getObject("neutrons","hmissingMassHerm");
		histograms1D[Hist1D.hmissingMassHerm.ordinal()].setTitleX("Missing Mass ((GeV/c^2)^2)");

		histFile.getDir("neutrons").add("hecSectors", new H1F("hecSectors", 6, 1, 7));
		histograms1D[Hist1D.hecSectors.ordinal()] = (H1F)histFile.getObject("neutrons","hecSectors");
		histograms1D[Hist1D.hecSectors.ordinal()].setTitleX("Hit Sector");

		histFile.getDir("neutrons").add("hacceptance", new H2F("hacceptance", BIN_NUM, 0, 50, 100, 0, 10));
		histograms2D[Hist2D.hacceptance.ordinal()] = (H2F)histFile.getObject("neutrons","hacceptance");
		histograms2D[Hist2D.hacceptance.ordinal()].setTitleX("theta (degree)");
		histograms2D[Hist2D.hacceptance.ordinal()].setTitleY("momentum (GeV/c)");

		histFile.getDir("neutrons").add("hhits", new H2F("hhits", 821, -410, 410, 961, -480, 480));
		histograms2D[Hist2D.hhits.ordinal()] = (H2F)histFile.getObject("neutrons","hhits");
		histograms2D[Hist2D.hhits.ordinal()].setTitleX("X");
		histograms2D[Hist2D.hhits.ordinal()].setTitleY("Y");

		histFile.getDir("neutrons").add("hNDE", new GraphErrors("hNDE"));
		graphs[Graph.hNDE.ordinal()] = (GraphErrors)histFile.getObject("neutrons","hNDE");
		graphs[Graph.hNDE.ordinal()].setTitleX("momentum (GeV/c)");
		graphs[Graph.hNDE.ordinal()].setTitleY("NDE");
		graphs[Graph.hNDE.ordinal()].setMarkerColor(2);

		histFile.getDir("neutrons").add("hNDEGen", new GraphErrors("hNDE"));
		graphs[Graph.hNDEGen.ordinal()] = (GraphErrors)histFile.getObject("neutrons","hNDEGen");
		graphs[Graph.hNDEGen.ordinal()].setTitleX("momentum (GeV/c)");
		graphs[Graph.hNDEGen.ordinal()].setTitleY("NDE");
		graphs[Graph.hNDEGen.ordinal()].setMarkerColor(4);

		return histFile;
	}

	public H1F[] get1DHist(){
		return histograms1D;
	}

	public H2F[] get2DHist(){
		return histograms2D;
	}

	public GraphErrors[] getGraphs(){
		return graphs;
	}
}