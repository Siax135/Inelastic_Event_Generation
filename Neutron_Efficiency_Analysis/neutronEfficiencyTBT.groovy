import org.jlab.groot.data.*;
import java.lang.Math;
import java.util.ArrayList;
import java.lang.Float;
import org.jlab.geom.*;
import org.jlab.geom.detector.ec.*;
import org.jlab.geom.base.*;
import org.jlab.geom.detector.base.*;
import org.jlab.geom.prim.*;
import org.jlab.detector.base.*;
import org.jlab.io.hipo.*;
import HistInitializer;

String inputFile = args[0];

// open file
HipoDataSource reader = new HipoDataSource();
reader.open(inputFile);

//* Create variables for later use **************************************************
// variables for reading in data
HipoDataBank genBank;
HipoDataBank ecBank;
HipoDataBank TOFBank;
HipoDataEvent event;

// various counters, indexes, and general information variables
int nevents, nentries, nentriesTotal = 0;
int genRecDiff = 0;
int ecRows, TOFrows = 0
int chargeOne, chargeTwo = 0;
int genSector = -1;
int reconstructedSector = -1;
int foundSector = -1;
float electronPx, electronPy, electronPz, piPlusPx, piPlusPy, piPlusPz = 0;
float genNeutronPx, genNeutronPy, genNeutronPz = 0;
float electronE, piPlusE = 0;
float energyTerm, momentumTerm = 0;
float missingMass = 0;
float hitX, hitY, hitZ = 0;
float thetapq, genThetapq, genThetapq2, theta
float momentum, genMomentum = 0;
float nde, dnde = 0;
float ndeAtI, dndeAtI = 0;

// variables for keeping track of relevant particles paths and hits
Vector3D scatteredElectron;
Vector3D scatteredPiPlus;
Vector3D scatteredNeutron;
Vector3D genNeutron;
Vector3D momentumTermVector;
Vector3D ECHit;

// variables for setting up and using the CLAS12 geometry
Line3D neutronPath, genNeutronPath;
ArrayList sectors, superlayes, layers;
ECSector sector;
ECSuperlayer superlayer;
ECLayer layer;
Shape3D[] ECfaces = new Plane3D[6];
boolean intersectionStatus, genIntersectionStatus = false;

 // keep track of skipped events
int skippedEvents = 0;

// constants
final int BIN_NUM = 50;
final float BEAM_ENERGY = 11.0;  // GeV
final float ELECTRON_MASS = 0.00051; // GeV/c^2
final float PI_PLUS_MASS = 0.13957;  // GeV/c^2
final float ELECTRON_INITIAL_ENERGY = 11.0051;  // GeV
final float PROTON_INITIAL_ENERGY = 0.93827;    // GeV
//**********************************************************************************

System.out.println("Loading EC Geometry");

//* Geometry set-up ****************************************************************
ConstantProvider constants = GeometryFactory.getConstants(DetectorType.EC,11,"default");
ECDetector detector = new ECFactory().createDetectorCLAS(constants);

sectors = detector.getAllSectors();

// loop over each sector and move down the hierarchy to get a plane in place of the front face of each sector of the EC
for(int i = 0; i < 6; i++){
    sector = sectors.get(i);
    superlayers = sector.getAllSuperlayers();
    superlayer = superlayers.get(0);
    layers = superlayer.getAllLayers();
    layer = layers.get(0);
    ECfaces[i] = layer.getBoundary();
} // end loop over sectors
//**********************************************************************************

//* Create histograms **************************************************************
HistInitializer init = new HistInitializer();
TDirectory histFile = init.initializeHist(); //new TDirectory();
H1F[] histograms1D = init.get1DHist();
H2F[] histograms2D = init.get2DHist();
GraphErrors[] graphs = init.getGraphs();
//**********************************************************************************

//loop over events
while(reader.hasEvent()){
    // get generated and reconstructed events

    event = reader.getNextEvent();

    TOFrows = 0;
    if(event.hasBank("TimeBasedTrkg::TBTracks")){ // check to see if the event has the time based tracking bank I need
        TOFBank = event.getBank("TimeBasedTrkg::TBTracks");
        TOFrows = TOFBank.rows();
        // Since I am only interested in the 3 particle event e pi+ n and n won't be reconstructed 
        // I only want to look at events that have exactly 2 reconstructed particles ( e and pi+ )
        if(TOFrows != 2) { 
            skippedEvents++;
            nevents++;
            continue;
        } // end of if to check TOFrows
    } else {
        skippedEvents++;
        nevents++;
        continue;
    } // end of if to check for time based tracking bank.

    chargeOne = TOFBank.getInt("q",0);
    chargeTwo = TOFBank.getInt("q",1);

    // check which charge is the electron and which is the pi+
    if(chargeOne == 1 && chargeTwo == -1){ // chargeOne is the pi+ and chargeTwo is the electron
        electronPx = TOFBank.getFloat("p0_x",1);
        electronPy = TOFBank.getFloat("p0_y",1);
        electronPz = TOFBank.getFloat("p0_z",1);

        piPlusPx = TOFBank.getFloat("p0_x",0);
        piPlusPy = TOFBank.getFloat("p0_y",0);
        piPlusPz = TOFBank.getFloat("p0_z",0);
    } else if(chargeOne == -1 && chargeTwo == 1){ // chargeOne is the electron and chargeTwo is the pi+ 
        electronPx = TOFBank.getFloat("p0_x",0);
        electronPy = TOFBank.getFloat("p0_y",0);
        electronPz = TOFBank.getFloat("p0_z",0);

        piPlusPx = TOFBank.getFloat("p0_x",1);
        piPlusPy = TOFBank.getFloat("p0_y",1);
        piPlusPz = TOFBank.getFloat("p0_z",1);
    } else{ // either both charges are the same or one (or both) are 0
        skippedEvents++;
        nevents++;
        continue;
    } // end if to check which charge is which

    // calculate scattered neutron 3-momentum
    scatteredNeutron = new Vector3D(-1*(electronPx+piPlusPx), -1*(electronPy+piPlusPy), (BEAM_ENERGY-electronPz-piPlusPz));
    momentum = scatteredNeutron.mag(); // store magnitude in GeV
    //scatteredNeutron.scale(1000); // scale scatter neutron vector to MeV
    scatteredNeutron.unit();

    //System.out.println("Scattered neutron: " + scatteredNeutron.toString());

    genBank = event.getBank("MC::Particle");
    for(int j = 0; j < genBank.rows(); j++){
        if(genBank.getInt("pid",j) == 2112){
            genNeutronPx = genBank.getFloat("px",j);
            genNeutronPy = genBank.getFloat("py",j);
            genNeutronPz = genBank.getFloat("pz",j);
            genNeutron = new Vector3D(genNeutronPx,genNeutronPy,genNeutronPz);
            genMomentum = genNeutron.mag();
            genNeutron.unit();
            break;
        }
    }

    genThetapq = Math.acos(genNeutron.dot(scatteredNeutron))*(180/Math.PI);
    histograms1D[Hist1D.hthetaGen2.ordinal()].fill(genThetapq);

    // 3-momentum of scattered charged particles
    scatteredElectron = new Vector3D(electronPx, electronPy, electronPz);
    scatteredPiPlus = new Vector3D(piPlusPx, piPlusPy, piPlusPz);

    // Energy of scattered charged particles
    electronE = Math.sqrt(scatteredElectron.dot(scatteredElectron) + Math.pow(ELECTRON_MASS,2));
    piPlusE = Math.sqrt(scatteredPiPlus.dot(scatteredPiPlus) + Math.pow(PI_PLUS_MASS,2));

    // missing mass squared (m^2): m^2 = (E_in - E_out)^2 - (P_in - P_out)^2

    // energy term of the equation to solve for m^2
    energyTerm = Math.pow(ELECTRON_INITIAL_ENERGY + PROTON_INITIAL_ENERGY - electronE - piPlusE, 2);

    // momentum term of the equation to solve for m^2
    momentumTermVector = new Vector3D(0,0,11).sub(scatteredElectron).sub(scatteredPiPlus);
    momentumTerm = momentumTermVector.dot(momentumTermVector);

    // calculate m^2
    missingMass = energyTerm - momentumTerm;

    histograms1D[Hist1D.hmissingMassHerm.ordinal()].fill(missingMass);

    // If m^2 isn't around what it should be for a neutron then skip the event
    if( missingMass < 0.92 ) {
        histograms1D[Hist1D.hmissingMass.ordinal()].fill(missingMass);

        if(event.hasBank("ECAL::clusters")){ // make sure we have the ECRec::clusters bank
            ecBank = event.getBank("ECAL::clusters");
            ecRows = ecBank.rows();
        }else{ // if we don't have the bank then move on to the next event
            skippedEvents++;
            nevents++;
            continue;
        } // end if for the ECRec::clusters bank

        neutronPath = new Line3D( new Point3D(0,0,0), scatteredNeutron );
        genNeutronPath = new Line3D( new Point3D(0,0,0), genNeutron);
       // System.out.println("Event: " + nevents);
        for(int j = 0; j < 6 && !intersectionStatus; j++){ // loop over EC sectors to see if neutron hits
            //System.out.println("Reconstructed Status: " + intersectionStatus + " Sector: " + reconstructedSector);
            intersectionStatus = ECfaces[j].hasIntersection(neutronPath);
            if(intersectionStatus) reconstructedSector = j+1;
        } // end loop over sectors to see if neutrons hit

        for(int j = 0; j < 6 && !genIntersectionStatus; j++){ // loop over EC sectors to see if neutron hits
            //System.out.println("Generated Status: " + genIntersectionStatus + " Sector: " + genSector);
            genIntersectionStatus = ECfaces[j].hasIntersection(genNeutronPath);
            if(genIntersectionStatus) genSector = j+1;
        } // end loop over sectors to see if neutrons hit

        if(reconstructedSector != genSector){
            if(intersectionStatus == false && genIntersectionStatus == true){
                System.out.println("Generated neutron in event " + nevents + " hit sector " + genSector + " but reconstructed neutron didn't!");
            }else if(intersectionStatus == true && genIntersectionStatus == false){
                System.out.println("Reconstructed neutron in event " + nevents + " hit sector " + reconstructedSector + " but generated neutron didn't!");
            }else{
                System.out.println("Generated neutron in event " + nevents + " hit sector " + genSector + " but reconstructed neutron hit sector " + reconstructedSector);
            }
            genRecDiff++;
        }
        //System.out.println();

        if(!intersectionStatus && !genIntersectionStatus){ // if neutron wouldn't hit then skip event
            intersectionStatus = false;
            genIntersectionStatus = false;
            reconstructedSector = -1;
            genSector = -1;
            skippedEvents++;
            nevents++;
            continue;
        } // end if checking that the neutron doesn't miss

        if(intersectionStatus) histograms1D[Hist1D.hmomentumRec.ordinal()].fill(momentum);
        if(genIntersectionStatus) histograms1D[Hist1D.hmomentumGen.ordinal()].fill(genMomentum);

        //scatteredNeutron.unit(); // make scattered neutron momentum vector a unit vector
        //genThetapq2 = Math.acos(genNeutron.dot(scatteredNeutron))*(180/Math.PI);  
        histograms1D[Hist1D.hthetaGen.ordinal()].fill(genThetapq);

        // loop over neutron candidates
        for(int j = 0; j < ecRows; j++){
            // get hit coordinates and make a vector
            hitX = ecBank.getFloat("x",j);
            hitY = ecBank.getFloat("y",j);
            hitZ = ecBank.getFloat("z",j);
            foundSector = (int)ecBank.getByte("sector",j);
            ECHit = new Vector3D(hitX, hitY, hitZ);

            ECHit.unit(); // make hit vector a unit vector

            if(foundSector == genSector){
                genThetapq2 = Math.acos(genNeutron.dot(ECHit))*(180/Math.PI); // calculate angle between scattered neutron and neutron hit candidate 
                if(genThetapq < 1.5){ 
                    histograms1D[Hist1D.hmomentumGenFound.ordinal()].fill(genMomentum);
                    genSector = -1;
                }
            }

            if(foundSector == reconstructedSector){
                thetapq = Math.acos(scatteredNeutron.dot(ECHit))*(180/Math.PI); // calculate angle between scattered neutron and neutron hit candidate 
                histograms1D[Hist1D.htheta.ordinal()].fill(thetapq);
                if(thetapq < 1.5) { // if angle is less than 1.5 degrees then call it a hit and move on to the next event
                    histograms1D[Hist1D.hthetapq.ordinal()].fill(thetapq);
                    histograms1D[Hist1D.hmomentumFound.ordinal()].fill(momentum);
                    histograms2D[Hist2D.hhits.ordinal()].fill(hitX,hitY);
                    histograms1D[Hist1D.hecSectors.ordinal()].fill(foundSector);

                    theta = Math.acos(scatteredNeutron.z()/scatteredNeutron.mag())*(180/Math.PI);
                    histograms2D[Hist2D.hacceptance.ordinal()].fill(theta,momentum);
                    reconstructedSector = -1;
                } // end if checking thetapq
            }
        }
        intersectionStatus = false;
        genIntersectionStatus = false;
        reconstructedSector = -1;
        genSector = -1;
    } else {
        skippedEvents++;
    }

    nevents++;
}
 
 // calculate neutron detection efficiency and print run info to screen
System.out.println("Num events: " + nevents + " Skipped events: " + skippedEvents);
System.out.println("Num differences between generated and reconstructed neutrons: " + genRecDiff);
nentries = histograms1D[Hist1D.hmomentumFound.ordinal()].getEntries();
nentriesTotal = histograms1D[Hist1D.hmomentumRec.ordinal()].getEntries();
nde = (float) nentries/nentriesTotal;
dnde = nde*Math.sqrt(nde*(1-nde)/nentriesTotal);
System.out.println("Reconstructed: " + nentriesTotal);
System.out.println("Found: " + nentries);
System.out.println("Overall nde= " + nde + " +/- " + dnde);

nentriesGen = histograms1D[Hist1D.hmomentumGenFound.ordinal()].getEntries();
nentriesTotalGen = histograms1D[Hist1D.hmomentumGen.ordinal()].getEntries();
ndeGen = (float) nentriesGen/nentriesTotalGen;
dndeGen = Math.sqrt(ndeGen*(1-ndeGen)*nentriesTotalGen)/nentriesTotalGen;
System.out.println("Gen Rec: " + nentriesGen);
System.out.println("Gen Found: " + nentriesTotalGen);
System.out.println("Overall gen nde= " + ndeGen + " +/- " + dndeGen);

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
        System.out.println("P: " + currentP + " NDE: " + ndeAtI + " +/- " + dndeAtI);
    }
    if(hmomentumGenData[i] != 0){
        ndeGenAtI = hmomentumGenFoundData[i]/hmomentumGenData[i];
        dndeGenAtI = Math.sqrt(ndeGenAtI*(1-ndeGenAtI)*nentriesTotalGen)/nentriesTotalGen;
        graphs[Graph.hNDEGen.ordinal()].addPoint(currentP, ndeGenAtI, 0, dndeGenAtI);
        System.out.println("P Gen: " + currentP + " NDE Gen: " + ndeGenAtI + " +/- " + dndeGenAtI);
    }
    currentP += step;
} // end loop over data

// write histograms to file
histFile.writeFile("nde_histograms.hipo");