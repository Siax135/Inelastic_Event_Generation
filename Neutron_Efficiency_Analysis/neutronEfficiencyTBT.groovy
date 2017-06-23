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
int ecRows, TOFrows = 0
int chargeOne, chargeTwo = 0;
int reconstructedSector, foundSector = -1;
float electronPx, electronPy, electronPz, piPlusPx, piPlusPy, piPlusPz = 0;
float electronE, piPlusE = 0;
float energyTerm, momentumTerm = 0;
float missingMass = 0;
float hitX, hitY, hitZ = 0;
float thetapq, theta, momentum = 0;
float nde, dnde = 0;

// variables for keeping track of relevant particles paths and hits
Vector3D scatteredElectron;
Vector3D scatteredPiPlus;
Vector3D scatteredNeutron;
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
TDirectory histFile = new TDirectory();
histFile.mkdir("neutrons");

histFile.getDir("neutrons").add("hthetapq", new H1F("hthetapq", 100, 0, 10));
H1F hthetapq = (H1F)histFile.getObject("neutrons","hthetapq");
hthetapq.setTitleX("thetapq with cut (deg)");

histFile.getDir("neutrons").add("htheta", new H1F("htheta", 100, 0, 10));
H1F htheta = (H1F)histFile.getObject("neutrons","htheta");
htheta.setTitleX("thetapq (deg)");

histFile.getDir("neutrons").add("hmomentumTotal", new H1F("hmomentumTotal", BIN_NUM, 0, BEAM_ENERGY));
H1F hmomentumTotal = (H1F)histFile.getObject("neutrons","hmomentumTotal");
hmomentumTotal.setTitleX("momentum (GeV/c)");

histFile.getDir("neutrons").add("hmomentumRec", new H1F("hmomentumRec", BIN_NUM, 0, BEAM_ENERGY));
H1F hmomentumRec = (H1F)histFile.getObject("neutrons","hmomentumRec");
hmomentumRec.setTitleX("momentum (GeV/c)");

histFile.getDir("neutrons").add("hmissingMass", new H1F("hmissingMass", 150, 0, 6));
H1F hmissingMass = (H1F)histFile.getObject("neutrons","hmissingMass");
hmissingMass.setTitleX("mass (GeV/c^2)");

histFile.getDir("neutrons").add("hmissingMassHerm", new H1F("hmissingMassHerm", 300, 0, 6));
H1F hmissingMassHerm = (H1F)histFile.getObject("neutrons","hmissingMassHerm");
hmissingMassHerm.setTitleX("Missing Mass with cut ((GeV/c^2)^2)");

histFile.getDir("neutrons").add("hecSectors", new H1F("hecSectors", 6, 1, 6));
H1F hecSectors = (H1F)histFile.getObject("neutrons","hecSectors");
hecSectors.setTitleX("Hit Sector");

histFile.getDir("neutrons").add("hacceptance", new H2F("hacceptance", BIN_NUM, 0, 50, 100, 0, 10));
H2F hacceptance = (H2F)histFile.getObject("neutrons","hacceptance");
hacceptance.setTitleX("theta (degree)");
hacceptance.setTitleY("momentum (GeV/c)");

histFile.getDir("neutrons").add("hhits", new H2F("hhits", 401, 0, 400, 401, 0, 400));
H2F hhits = (H2F)histFile.getObject("neutrons","hhits");
hhits.setTitleX("X");
hhits.setTitleY("Y");

histFile.getDir("neutrons").add("hNDE", new H2F("hNDE", BIN_NUM, 0, BEAM_ENERGY, 100, 0, 1));
H2F hNDE = (H2F)histFile.getObject("neutrons","hNDE");
hNDE.setTitleX("momentum (GeV/c)");
hNDE.setTitleY("NDE");
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

    hmissingMassHerm.fill(missingMass);

    // If m^2 isn't around what it should be for a neutron then skip the event
    if( missingMass > 0.87 && missingMass < 0.89 ) {
        hmissingMass.fill(missingMass);

        if(event.hasBank("ECAL::clusters")){ // make sure we have the ECRec::clusters bank
            ecBank = event.getBank("ECAL::clusters");
            ecRows = ecBank.rows();
        }else{ // if we don't have the bank then move on to the next event
            skippedEvents++;
            nevents++;
            continue;
        } // end if for the ECRec::clusters bank

        // calculate scattered neutron 3-momentum
        scatteredNeutron = new Vector3D(-1*(electronPx+piPlusPx), -1*(electronPy+piPlusPy), (BEAM_ENERGY-electronPz-piPlusPz));
        momentum = scatteredNeutron.mag(); // store magnitude in GeV
        scatteredNeutron.scale(1000); // scale scatter neutron vector to MeV

        neutronPath = new Line3D( new Point3D(0,0,0), scatteredNeutron );
        for(int j = 0; j < 6 && !intersectionStatus; j++){ // loop over EC sectors to see if neutron hits
            intersectionStatus = ECfaces[j].hasIntersection(neutronPath);
            if(intersectionStatus) reconstructedSector = j+1;
        } // end loop over sectors to see if neutrons hit


        if(!intersectionStatus){ // if neutron wouldn't hit then skip event
            skippedEvents++;
            nevents++;
            continue;
        } // end if checking that the neutron doesn't miss



        hmomentumTotal.fill(momentum);

        // loop over neutron candidates
        for(int j = 0; j < ecRows; j++){
            // get hit coordinates and make a vector
            hitX = ecBank.getFloat("x",j);
            hitY = ecBank.getFloat("y",j);
            hitZ = ecBank.getFloat("z",j);
            foundSector = (int)ecBank.getByte("sector",j);
            ECHit = new Vector3D(hitX, hitY, hitZ);

            scatteredNeutron.unit(); // make scattered neutron momentum vector a unit vector
            ECHit.unit(); // make hit vector a unit vector

            thetapq = Math.acos(scatteredNeutron.dot(ECHit))*(180/Math.PI); // calculate angle between scattered neutron and neutron hit candidate
            htheta.fill(thetapq);
            if(thetapq < 1.5) { // if angle is less than 1.5 degrees then call it a hit and move on to the next event
                hthetapq.fill(thetapq);
                hmomentumRec.fill(momentum);
                hhits.fill(hitX, hitY);

                if(foundSector==reconstructedSector){
                    hecSectors.fill(foundSector);
                } else {
                    System.out.println("Found Sector and Reconstruced Sector not the same!");
                }

                theta = Math.acos(scatteredNeutron.z()/scatteredNeutron.mag())*(180/Math.PI);
                hacceptance.fill(theta,momentum);

                break;
            } // end if checking thetapq


        }

        intersectionStatus = false;
    } else {
        skippedEvents++;
    }

    nevents++;
}
 
 // calculate neutron detection efficiency and print run info to screen
System.out.println("Num events: " + nevents + " Skipped events: " + skippedEvents);
nentries = hmomentumRec.getEntries();
nentriesTotal = hmomentumTotal.getEntries();
nde = (float) nentries/nentriesTotal;
dnde = nde*Math.sqrt(nde*(1-nde)/nentries);
System.out.println("Reconstructed: " + nentriesTotal);
System.out.println("Found: " + nentries);
System.out.println("nde= " + nde + " +/- " + dnde);

// get data from momentum histograms
float[] hmomentumTotalData = hmomentumTotal.getData();
float[] hmomentumRecData = hmomentumRec.getData();

// calculate steps between bins
float step = BEAM_ENERGY/BIN_NUM;
float currentP = step/2;

// loop over data from histograms and create NDE histogram
for(int i = 0; i < BIN_NUM; i++){
    hNDE.fill(currentP, hmomentumRecData[i]/hmomentumTotalData[i], hmomentumRecData[i]);
    currentP += step;
} // end loop over data

// write histograms to file
histFile.writeFile("nde_histograms.hipo");