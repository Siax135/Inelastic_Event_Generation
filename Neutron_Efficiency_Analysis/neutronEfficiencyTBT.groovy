import org.jlab.evio.clas12.*;
import org.jlab.clas.physics.*;
import org.jlab.clas12.physics.*;
import org.root.histogram.*;
import org.root.pad.*;
import org.root.group.*;
import org.root.func.*;
import java.lang.Math;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.String;
import java.lang.Integer;
import java.lang.Double;
import org.jlab.geom.*;
import org.jlab.geom.detector.ec.*;
import org.jlab.geom.base.*;
import org.jlab.geom.detector.ec.*;
import org.jlab.clas12.dbdata.DataBaseLoader.*;
import org.jlab.clas12.dbdata.*;
import org.jlab.clasrec.utils.*
import org.jlab.geom.prim.*;
import org.jlab.geom.detector.ec.ECFactory.*;
import org.jlab.clas.detector.*;

String inputFile = args[0];
String caseNum = args[1];

// open file
EvioSource reader = new EvioSource();
//reader.open("/home/Siax/Linux_Shared/Pythia/e_pi_n3_Rec_NEW.0.evio");
reader.open(inputFile);

// create fitter to get data
GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);

//* Create variables for later use **************************************************
// variables for reading in data
EvioDataBank genBank;
EvioDataBank ecBank;
EvioDataBank TOFBank;
EvioDataEvent event;
PhysicsEvent genEvent;
PhysicsEvent recEvent;
Particle genElectron;
Particle recNeutron;

// various counters, indexes, and general information variables
int nevents, nentries, nentriesTotal, nrec, nless = 0;
int genRows, ecRows, TOFrows, electronIndex, piPlusIndex = 0;
int genNeutronCount, recNeutronCount, genElectronCount, recElectronCount, genPiPlusCount, recPiPlusCount, neutronCount, electronCount, piPlusCount = 0;
int chargeOne, chargeTwo = 0;
double electronPx, electronPy, electronPz, piPlusPx, piPlusPy, piPlusPz = 0;
double electronE, piPlusE = 0;
double energyTerm, momentumTerm = 0;
double scatteredElectronMassSq, scatteredPiPlusMassSq, missingMass = 0;
double hitX, hitY, hitZ = 0;
double neutronPmag, hitMag = 0;
double thetapq, theta, momentum = 0;
double nde, dnde = 0;

// variables for keeping track of relevant particles paths and hits
Vector3D scatteredElectron;
Vector3D scatteredPiPlus;
Vector3D scatteredNeutron;
Vector3D generatedNeutron;
Vector3D momentumTermVector;
Vector3D ECHit;

// variables for setting up and using the CLAS12 geometry
Line3D neutronPath;
ArrayList sectors, superlayes, layers;
ECSector sector;
ECSuperlayer superlayer;
ECLayer layer;
Shape3D[] ECfaces = new Plane3D[6];
boolean intersectionStatus = false;

 // keep track of skipped events
int skippedEvents = 0;

// used to look for reconstructed electron and pi+ info
Scanner electronSearch, piPlusSearch;
String recLund, electronInfo, piPlusInfo;

// used to tell if we found a reconstructed electron
boolean electronFound = false

// constants
final int BIN_NUM = 50;
final double BEAM_ENERGY = 11.0;  // GeV
final double ELECTRON_MASS = 0.00051; // GeV/c^2
final double PI_PLUS_MASS = 0.13957;  // GeV/c^2
final double ELECTRON_INITIAL_ENERGY = 11.0051;  // GeV
final double PROTON_INITIAL_ENERGY = 0.93827;    // GeV
//**********************************************************************************

//* Geometry set-up ****************************************************************
ConstantProvider constants = DataBaseLoader.getGeometryConstants(DetectorType.EC);
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
TDirectory histFile = new TDirectory();
histFile.mkdir("neutrons");

histFile.getDirectory("neutrons").add(new H1D("hthetapq", 100, 0, 10));
H1D hthetapq = (H1D)histFile.getDirectory("neutrons").getObject("hthetapq");
hthetapq.setXTitle("thetapq with cut (deg)");

histFile.getDirectory("neutrons").add(new H1D("htheta", 100, 0, 10));
H1D htheta = (H1D)histFile.getDirectory("neutrons").getObject("htheta");
htheta.setXTitle("thetapq (deg)");

histFile.getDirectory("neutrons").add(new H1D("hmomentumTotal", BIN_NUM, 0, BEAM_ENERGY));
H1D hmomentumTotal = (H1D)histFile.getDirectory("neutrons").getObject("hmomentumTotal");
hmomentumTotal.setXTitle("momentum (GeV/c)");

histFile.getDirectory("neutrons").add(new H1D("hmomentumRec", BIN_NUM, 0, BEAM_ENERGY));
H1D hmomentumRec = (H1D)histFile.getDirectory("neutrons").getObject("hmomentumRec");
hmomentumRec.setXTitle("momentum (GeV/c)");

histFile.getDirectory("neutrons").add(new H1D("hmissingMass", 150, 0, 6));
H1D hmissingMass = (H1D)histFile.getDirectory("neutrons").getObject("hmissingMass");
hmissingMass.setXTitle("mass (GeV/c^2)");

histFile.getDirectory("neutrons").add(new H1D("hmissingMassHerm", 300, 0, 6));
H1D hmissingMassHerm = (H1D)histFile.getDirectory("neutrons").getObject("hmissingMassHerm");
hmissingMassHerm.setXTitle("Missing Mass with cut ((GeV/c^2)^2)");

histFile.getDirectory("neutrons").add(new H2D("hacceptance", BIN_NUM, 0, 50, 100, 0, 10));
H2D hacceptance = (H2D)histFile.getDirectory("neutrons").getObject("hacceptance");
hacceptance.setXTitle("theta (degree)");
hacceptance.setYTitle("momentum (GeV/c)");

histFile.getDirectory("neutrons").add(new H2D("hNDE", BIN_NUM, 0, BEAM_ENERGY, 100, 0, 1));
H2D hNDE = (H2D)histFile.getDirectory("neutrons").getObject("hNDE");
hNDE.setXTitle("momentum (GeV/c)");
hNDE.setYTitle("NDE");
//**********************************************************************************

//loop over events
while(reader.hasEvent()){
    // get generated and reconstructed events
    //System.out.println("Grabbed event: " + nevents);
    event = reader.getNextEvent();
    genEvent = fitter.getGeneratedEvent(event);
    recEvent = fitter.getPhysicsEvent(event);

    // get generated and reconstructed electron counts
    genElectronCount = genEvent.countByPid(11);
    recElectronCount = recEvent.countByPid(11);
    electronCount = Math.max(genElectronCount, recElectronCount);

    // get generated and reconstructed electron counts
    genPiPlusCount = genEvent.countByPid(211);
    recPiPlusCount = recEvent.countByPid(211);
    piPlusCount = Math.max(genPiPlusCount, recPiPlusCount);

    // get generated and reconstructed electron counts
    genNeutronCount = genEvent.countByPid(2112);
    recNeutronCount = recEvent.countByPid(2112);
    //if(recNeutronCount > 0) System.out.println(nevents + ": " + recNeutronCount);
    neutronCount = Math.max(genNeutronCount, recNeutronCount);

    TOFrows = 0;
    //System.out.println("Before if, event: " + nevents + " reset rows: " + TOFrows);

    // make sure we have electrons and neutrons to analyze
    if(electronCount > 0 && neutronCount > 0 && piPlusCount > 0){
        //System.out.println("After if, event: " + nevents + " reset rows: " + TOFrows);

        if(event.hasBank("TimeBasedTrkg::TBTracks")){
            TOFBank = event.getBank("TimeBasedTrkg::TBTracks");
            TOFrows = TOFBank.rows();
            //System.out.println("Event: " + nevents + " rows: " + TOFBank.rows() + " what I think: " + TOFrows);
            if(TOFrows != 2) {
                nevents++;
                continue;
            }
        } else {
            nevents++;
            continue;
        }

        chargeOne = TOFBank.getInt("q",0);
        chargeTwo = TOFBank.getInt("q",1);

        if(chargeOne == 1 && chargeTwo == -1){
            electronPx = TOFBank.getDouble("p0_x",1);
            electronPy = TOFBank.getDouble("p0_y",1);
            electronPz = TOFBank.getDouble("p0_z",1);

            piPlusPx = TOFBank.getDouble("p0_x",0);
            piPlusPy = TOFBank.getDouble("p0_y",0);
            piPlusPz = TOFBank.getDouble("p0_z",0);
        } else if(chargeOne == -1 && chargeTwo == 1){
            electronPx = TOFBank.getDouble("p0_x",0);
            electronPy = TOFBank.getDouble("p0_y",0);
            electronPz = TOFBank.getDouble("p0_z",0);

            piPlusPx = TOFBank.getDouble("p0_x",1);
            piPlusPy = TOFBank.getDouble("p0_y",1);
            piPlusPz = TOFBank.getDouble("p0_z",1);
        } else{
            nevents++;
            continue;
        }

        scatteredElectron = new Vector3D(electronPx, electronPy, electronPz);
        scatteredPiPlus = new Vector3D(piPlusPx, piPlusPy, piPlusPz);

        electronE = Math.sqrt(scatteredElectron.dot(scatteredElectron) + Math.pow(ELECTRON_MASS,2));
        piPlusE = Math.sqrt(scatteredPiPlus.dot(scatteredPiPlus) + Math.pow(PI_PLUS_MASS,2));

        energyTerm = Math.pow(ELECTRON_INITIAL_ENERGY + PROTON_INITIAL_ENERGY - electronE - piPlusE, 2);

        momentumTermVector = new Vector3D(0,0,11).sub(scatteredElectron).sub(scatteredPiPlus);
        momentumTerm = momentumTermVector.dot(momentumTermVector);

        // calculate missing mass
        missingMass = energyTerm - momentumTerm;

        hmissingMassHerm.fill(missingMass);

        if( missingMass > 0.87 && missingMass < 0.89 ) {
            hmissingMass.fill(missingMass);

            if(event.hasBank("ECDetector::clusters")){ // make sure we have the ECRec::clusters bank
                ecBank = event.getBank("ECDetector::clusters");
                ecRows = ecBank.rows();
            }else{ // if we don't have the bank then move on to the next event
                nevents++;
                continue;
            } // end if for the ECRec::clusters bank

            scatteredNeutron = new Vector3D(-1*(electronPx+piPlusPx), -1*(electronPy+piPlusPy), (BEAM_ENERGY-electronPz-piPlusPz));
            momentum = scatteredNeutron.mag(); // store magnitude in GeV
            scatteredNeutron.scale(1000); // scale scatter neutron vector to MeV



            neutronPath = new Line3D( new Point3D(0,0,0), scatteredNeutron );
            for(int j = 0; j < 6 && !intersectionStatus; j++){
                intersectionStatus = ECfaces[j].hasIntersection(neutronPath);
            } // end loop over sectors to see if neutrons hit


            if(!intersectionStatus){ // if neutron wouldn't hit then skip event
                nevents++;
                continue;
            } // end if checking that the neutron doesn't miss



            hmomentumTotal.fill(momentum);

            // loop over neutron candidates
            for(int j = 0; j < ecRows; j++){
                // get hit coordinates and make a vector
                hitX = ecBank.getDouble("X",j);
                hitY = ecBank.getDouble("Y",j);
                hitZ = ecBank.getDouble("Z",j);
                ECHit = new Vector3D(hitX, hitY, hitZ);

                scatteredNeutron.unit(); // make scattered neutron momentum vector a unit vector
                ECHit.unit(); // make hit vector a unit vector

                thetapq = Math.acos(scatteredNeutron.dot(ECHit))*(180/Math.PI); // calculate angle between scattered neutron and neutron hit candidate
                htheta.fill(thetapq);
                if(thetapq < 1.5) { // if angle is less than 1.5 degrees then call it a hit and move on to the next event
                    hthetapq.fill(thetapq);
                    hmomentumRec.fill(momentum);

                    theta = Math.acos(scatteredNeutron.z()/scatteredNeutron.mag())*(180/Math.PI);
                    hacceptance.fill(theta,momentum);

                    System.out.println("Event: " + nevents + " P: " + momentum + " Theta: " + theta);

                    break;
                } // end if checking thetapq


            }

            intersectionStatus = false;
        }

//        if(missingMass < 0.80){
//            nless++;
//            System.out.println("Event: " + nevents);
//            System.out.println("row 1 q: " + TOFBank.getInt("q",0) + " row 2 q: " + TOFBank.getInt("q",1))
//            System.out.println("Electron: " + scatteredElectron.toString() + " E: " + electronE);
//            System.out.println("Pi+: " + scatteredPiPlus.toString() + " E: " + piPlusE);
//            System.out.println("MM: " + missingMass);
//        }

        //System.out.println("Event: " + nevents);
        //System.out.println("row 1 q: " + TOFBank.getInt("q",0) + " row 2 q: " + TOFBank.getInt("q",1))
        //System.out.println("Electron: " + scatteredElectron.toString() + " E: " + electronE);
        //System.out.println("Pi+: " + scatteredPiPlus.toString() + " E: " + piPlusE);
        //System.out.println("MM: " + missingMass);
    }
    nevents++;
}

System.out.println("Events less than 0.88:" + nless);

//TCanvas c1 = new TCanvas("c1","PhysicsAnalysis",1200,640,1,3);
//c1.cd(0);
//c1.draw(hmissingMassHerm);
//c1.cd(1);
//c1.draw(hmissingMass);
//c1.cd(2);
//c1.draw(hacceptance);

 
 
 
 
 
 
 
 
 

 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 

 
 // calculate neutron detection efficiency and print run info to screen
System.out.println("Case: " + caseNum);
System.out.println("Num events: " + nevents + " Skipped events: " + skippedEvents);
nentries = hmomentumRec.getEntries();
nentriesTotal = hmomentumTotal.getEntries();
nde = (double) nentries/nentriesTotal;
dnde = nde*Math.sqrt(nde*(1-nde)/nentries);
System.out.println("nde= " + nde + " +/- " + dnde);

// get data from momentum histograms
double[] hmomentumTotalData = hmomentumTotal.getData();
double[] hmomentumRecData = hmomentumRec.getData();

// calculate steps between bins
double step = BEAM_ENERGY/BIN_NUM;
double currentP = step/2;

// loop over data from histograms and create NDE histogram
for(int i = 0; i < BIN_NUM; i++){
    hNDE.fill(currentP, hmomentumRecData[i]/hmomentumTotalData[i], hmomentumRecData[i]);
    currentP += step;
} // end loop over data

// write histograms to file
histFile.write("e_pi_n_NDE_"+caseNum+".hipo");