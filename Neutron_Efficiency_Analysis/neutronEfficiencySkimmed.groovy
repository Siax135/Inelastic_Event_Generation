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
import HistHandler;

String inputFile = args[0];

// open file
HipoDataSource reader = new HipoDataSource();
reader.open(inputFile);

//* Create variables for later use *******************************************************************************
// variables for reading in data
HipoDataBank genBank;
HipoDataBank ecBank;
HipoDataBank TOFBank;
HipoDataEvent event;

// various counters, indexes, and general information variables
int nevents = 0;
int nentries1_3, nentries1_2, nentries1_1, nentries1_0, nentries0_98, nentries0_95, nentries0_92, nentries0_90 = 0;
int nentriesTotal1_3, nentriesTotal1_2, nentriesTotal1_1, nentriesTotal1_0, nentriesTotal0_98, nentriesTotal0_95, nentriesTotal0_92, nentriesTotal0_90 = 0;
int genRecDiff = 0;
int ecRows = 0
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
float ndeAtI1_3, ndeAtI1_2, ndeAtI1_1, ndeAtI1_0, ndeAtI0_98, ndeAtI0_95, ndeAtI0_92, ndeAtI0_90 = 0;
float dndeAtI1_3, dndeAtI1_2, dndeAtI1_1, dndeAtI1_0, dndeAtI0_98, dndeAtI0_95, dndeAtI0_92, dndeAtI0_90 = 0;

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
//****************************************************************************************************************

//* Geometry set-up **********************************************************************************************
System.out.println("Loading EC Geometry");

// Get constants
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
//****************************************************************************************************************

//* Create histograms ********************************************************************************************
HistHandler handler = new HistHandler();
TDirectory histFile = handler.initializeHist();
H1F[] histograms1D = handler.get1DHist();
H2F[] histograms2D = handler.get2DHist();
GraphErrors[] graphs = handler.getGraphs();
//****************************************************************************************************************

//* Analysis loop ************************************************************************************************

// skip header event
if(reader.hasEvent()) reader.getNextEvent();

//loop over events
while(reader.hasEvent()){
    // get event
    event = reader.getNextEvent();

    // Since this should only be running with a skimmed file, we know the Time based tracking bank exist and
    // we know there should be exactly two particles in the bank.
    TOFBank = event.getBank("TimeBasedTrkg::TBTracks");

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
    scatteredNeutron.unit();

    // Get generated data bank
    genBank = event.getBank("MC::Particle");
    for(int j = 0; j < genBank.rows(); j++){ // loop over generated particles
        if(genBank.getInt("pid",j) == 2112){ // check to see if particle is a neutron, if so get momentum info
            genNeutronPx = genBank.getFloat("px",j);
            genNeutronPy = genBank.getFloat("py",j);
            genNeutronPz = genBank.getFloat("pz",j);
            genNeutron = new Vector3D(genNeutronPx,genNeutronPy,genNeutronPz);
            genMomentum = genNeutron.mag();
            genNeutron.unit();
            break;
        } // end if
    } // end loop over generated data

    // Calculate the angle betwen the generated neutron and the reconstructed neutron no subject to the missing mass cut
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

    histograms1D[Hist1D.hmissingMass.ordinal()].fill(missingMass);

    // If m^2 isn't around what it should be for a neutron then skip the event
    if( missingMass < 1.3 ) {
        histograms1D[Hist1D.hmissingMassHerm1_3.ordinal()].fill(missingMass);

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
        for(int j = 0; j < 6 && !intersectionStatus; j++){ // loop over EC sectors to see if reconstructed neutron hits
            intersectionStatus = ECfaces[j].hasIntersection(neutronPath);
            if(intersectionStatus) reconstructedSector = j+1;
        } // end loop over sectors to see if reconstructed neutrons hit

        for(int j = 0; j < 6 && !genIntersectionStatus; j++){ // loop over EC sectors to see if generated neutron hits
            genIntersectionStatus = ECfaces[j].hasIntersection(genNeutronPath);
            if(genIntersectionStatus) genSector = j+1;
        } // end loop over sectors to see if generated neutrons hit

        if(reconstructedSector != genSector){ // if the reconstructed and generated neutron didn't hit the same sector the see which situation we have
            if(intersectionStatus == false && genIntersectionStatus == true){ // reconstructed didn't hit EC but generated did
                System.out.println("Generated neutron in event " + nevents + " hit sector " + genSector + " but reconstructed neutron didn't!");
            }else if(intersectionStatus == true && genIntersectionStatus == false){ // reconstructed did hit EC but generated didn't
                System.out.println("Reconstructed neutron in event " + nevents + " hit sector " + reconstructedSector + " but generated neutron didn't!");
            }else{ // Bot hit EC but hit in different sectors
                System.out.println("Generated neutron in event " + nevents + " hit sector " + genSector + " but reconstructed neutron hit sector " + reconstructedSector);
            } // end if checking situation
            genRecDiff++;
        } // end if between reconstructed and generated neutron

        if(!intersectionStatus && !genIntersectionStatus){ // if neither reconstructed nor generated neutron wouldn't hit then skip event
            intersectionStatus = false;
            genIntersectionStatus = false;
            reconstructedSector = -1;
            genSector = -1;
            skippedEvents++;
            nevents++;
            continue;
        } // end if checking that the neutron doesn't miss

        if(intersectionStatus){ // if reconstruced neutron should hit EC then fill appropriate histograms
            histograms1D[Hist1D.hmomentumRec1_3.ordinal()].fill(momentum);

            // The following if statements check where the missing mass of the current event lies and fills 
            // histograms based on that. This is to shwon how changing the missing mass cut changes the NDE
            if(missingMass < 1.2){
                histograms1D[Hist1D.hmissingMassHerm1_2.ordinal()].fill(missingMass);
                histograms1D[Hist1D.hmomentumRec1_2.ordinal()].fill(momentum);
            }
            if(missingMass < 1.1){
                histograms1D[Hist1D.hmissingMassHerm1_1.ordinal()].fill(missingMass);
                histograms1D[Hist1D.hmomentumRec1_1.ordinal()].fill(momentum);
            }
            if(missingMass < 1.0){
                histograms1D[Hist1D.hmissingMassHerm1_0.ordinal()].fill(missingMass);
                histograms1D[Hist1D.hmomentumRec1_0.ordinal()].fill(momentum);
            }
            if(missingMass < 0.98){
                histograms1D[Hist1D.hmissingMassHerm0_98.ordinal()].fill(missingMass);
                histograms1D[Hist1D.hmomentumRec0_98.ordinal()].fill(momentum);
            }
            if(missingMass < 0.95){
                histograms1D[Hist1D.hmissingMassHerm0_95.ordinal()].fill(missingMass);
                histograms1D[Hist1D.hmomentumRec0_95.ordinal()].fill(momentum);
            }
            if(missingMass < 0.92){
                histograms1D[Hist1D.hmissingMassHerm0_92.ordinal()].fill(missingMass);
                histograms1D[Hist1D.hmomentumRec0_92.ordinal()].fill(momentum);
            }
            if(missingMass < 0.90){
                histograms1D[Hist1D.hmissingMassHerm0_90.ordinal()].fill(missingMass);
                histograms1D[Hist1D.hmomentumRec0_90.ordinal()].fill(momentum);
            }
        } // end if filling reconstructed neutron histograms

        // if the generated neutron hit the EC then fill correct histogram
        if(genIntersectionStatus) histograms1D[Hist1D.hmomentumGen.ordinal()].fill(genMomentum);

        // Angle betwen the generated neutron and the reconstructed neutron subject to the missing mass cut
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

            if(foundSector == genSector){ // if the sector of the hit matches the sector the generated neutron hit then do stuff
                genThetapq2 = Math.acos(genNeutron.dot(ECHit))*(180/Math.PI); // calculate angle between scattered neutron and neutron hit candidate 
                if(genThetapq < 1.5){ // if the hit in the EC is within 1.5 degrees of the generated neutron EC intersection then count it as found
                    histograms1D[Hist1D.hmomentumGenFound.ordinal()].fill(genMomentum);
                    genSector = -1;
                } // end if checking hit angle
            } // end if checking hit-generated sector

            if(foundSector == reconstructedSector){ // if the sector of the hit matches the sector the reconstructed neutron hit then do stuff
                thetapq = Math.acos(scatteredNeutron.dot(ECHit))*(180/Math.PI); // calculate angle between scattered neutron and neutron hit candidate 
                histograms1D[Hist1D.htheta.ordinal()].fill(thetapq);
                if(thetapq < 1.5) { // if the hit in teh EC is within 1.5 degrees of the reconstructed neutron EC intersection then count it as found
                    histograms1D[Hist1D.hthetapq.ordinal()].fill(thetapq);
                    histograms2D[Hist2D.hhits.ordinal()].fill(hitX,hitY);
                    histograms1D[Hist1D.hecSectors.ordinal()].fill(foundSector);
                    histograms1D[Hist1D.hmomentumFound1_3.ordinal()].fill(momentum);

                    // The following if statements check where the missing mass of the current event lies and fills 
                    // histograms based on that. This is to shwon how changing the missing mass cut changes the NDE
                    if(missingMass < 1.2){
                        histograms1D[Hist1D.hmomentumFound1_2.ordinal()].fill(momentum);
                    }
                    if(missingMass < 1.1){
                        histograms1D[Hist1D.hmomentumFound1_1.ordinal()].fill(momentum);
                    }
                    if(missingMass < 1.0){
                        histograms1D[Hist1D.hmomentumFound1_0.ordinal()].fill(momentum);
                    }
                    if(missingMass < 0.98){
                        histograms1D[Hist1D.hmomentumFound0_98.ordinal()].fill(momentum);
                    }
                    if(missingMass < 0.95){
                        histograms1D[Hist1D.hmomentumFound0_95.ordinal()].fill(momentum);
                    }
                    if(missingMass < 0.92){
                        histograms1D[Hist1D.hmomentumFound0_92.ordinal()].fill(momentum);
                    }
                    if(missingMass < 0.90){
                        histograms1D[Hist1D.hmomentumFound0_90.ordinal()].fill(momentum);
                    }

                    theta = Math.acos(scatteredNeutron.z()/scatteredNeutron.mag())*(180/Math.PI);
                    histograms2D[Hist2D.hacceptance.ordinal()].fill(theta,momentum);
                    reconstructedSector = -1;
                } // end if checking thetapq
            } // end if checking hit-reconstructed sector
        } // end loop of EC hits
        intersectionStatus = false;
        genIntersectionStatus = false;
        reconstructedSector = -1;
        genSector = -1;
    } else { // if missing mass is above cut then skip event
        skippedEvents++;
    } // end missing mass if
    nevents++;
} // end loop over events
//****************************************************************************************************************

//* Calculate NDE ************************************************************************************************ 
 // calculate neutron detection efficiency and print run info to screen
System.out.println("Num events: " + nevents + " Skipped events: " + skippedEvents);
System.out.println("Num differences between generated and reconstructed neutrons: " + genRecDiff);

// Get number of entries in momentum histograms
nentries1_3 = histograms1D[Hist1D.hmomentumFound1_3.ordinal()].getEntries();
nentriesTotal1_3 = histograms1D[Hist1D.hmomentumRec1_3.ordinal()].getEntries();
nentries1_2 = histograms1D[Hist1D.hmomentumFound1_2.ordinal()].getEntries();
nentriesTotal1_2 = histograms1D[Hist1D.hmomentumRec1_2.ordinal()].getEntries();
nentries1_1 = histograms1D[Hist1D.hmomentumFound1_1.ordinal()].getEntries();
nentriesTotal1_1 = histograms1D[Hist1D.hmomentumRec1_1.ordinal()].getEntries();
nentries1_0 = histograms1D[Hist1D.hmomentumFound1_0.ordinal()].getEntries();
nentriesTotal1_0 = histograms1D[Hist1D.hmomentumRec1_0.ordinal()].getEntries();
nentries0_98 = histograms1D[Hist1D.hmomentumFound0_98.ordinal()].getEntries();
nentriesTotal0_98 = histograms1D[Hist1D.hmomentumRec0_98.ordinal()].getEntries();
nentries0_95 = histograms1D[Hist1D.hmomentumFound0_95.ordinal()].getEntries();
nentriesTotal0_95 = histograms1D[Hist1D.hmomentumRec0_95.ordinal()].getEntries();
nentries0_92 = histograms1D[Hist1D.hmomentumFound0_92.ordinal()].getEntries();
nentriesTotal0_92 = histograms1D[Hist1D.hmomentumRec0_92.ordinal()].getEntries();
nentries0_90 = histograms1D[Hist1D.hmomentumFound0_90.ordinal()].getEntries();
nentriesTotal0_90 = histograms1D[Hist1D.hmomentumRec0_90.ordinal()].getEntries();

// Print number of entries for visual check that things make sense
System.out.println("Found 1_3: " + nentries1_3 + " Rec 1_3: " + nentriesTotal1_3);
System.out.println("Found 1_2: " + nentries1_2 + " Rec 1_2: " + nentriesTotal1_2);
System.out.println("Found 1_1: " + nentries1_1 + " Rec 1_1: " + nentriesTotal1_1);
System.out.println("Found 1_0: " + nentries1_0 + " Rec 1_0: " + nentriesTotal1_0);
System.out.println("Found 0_98: " + nentries0_98 + " Rec 0_98: " + nentriesTotal0_98);
System.out.println("Found 0_95: " + nentries0_95 + " Rec 0_95: " + nentriesTotal0_95);
System.out.println("Found 0_92: " + nentries0_92 + " Rec 0_92: " + nentriesTotal0_92);
System.out.println("Found 0_90: " + nentries0_90 + " Rec 0_90: " + nentriesTotal0_90);

// Over all NDE from largest missing mass cut
nde = (float) nentries1_3/nentriesTotal1_3;
dnde = nde*Math.sqrt(nde*(1-nde)/nentriesTotal1_3);
System.out.println("Reconstructed: " + nentriesTotal1_3);
System.out.println("Found: " + nentries1_3);
System.out.println("Overall nde= " + nde + " +/- " + dnde);

// Get generated momentum info and overall generated NDE 
nentriesGen = histograms1D[Hist1D.hmomentumGenFound.ordinal()].getEntries();
nentriesTotalGen = histograms1D[Hist1D.hmomentumGen.ordinal()].getEntries();
ndeGen = (float) nentriesGen/nentriesTotalGen;
dndeGen = Math.sqrt(ndeGen*(1-ndeGen)*nentriesTotalGen)/nentriesTotalGen;
System.out.println("Gen Rec: " + nentriesGen);
System.out.println("Gen Found: " + nentriesTotalGen);
System.out.println("Overall gen nde= " + ndeGen + " +/- " + dndeGen);

// get data from momentum histograms
float[] hmomentumRecData1_3 = histograms1D[Hist1D.hmomentumRec1_3.ordinal()].getData();
float[] hmomentumFoundData1_3 = histograms1D[Hist1D.hmomentumFound1_3.ordinal()].getData();
float[] hmomentumRecData1_2 = histograms1D[Hist1D.hmomentumRec1_2.ordinal()].getData();
float[] hmomentumFoundData1_2 = histograms1D[Hist1D.hmomentumFound1_2.ordinal()].getData();
float[] hmomentumRecData1_1 = histograms1D[Hist1D.hmomentumRec1_1.ordinal()].getData();
float[] hmomentumFoundData1_1 = histograms1D[Hist1D.hmomentumFound1_1.ordinal()].getData();
float[] hmomentumRecData1_0 = histograms1D[Hist1D.hmomentumRec1_0.ordinal()].getData();
float[] hmomentumFoundData1_0 = histograms1D[Hist1D.hmomentumFound1_0.ordinal()].getData();
float[] hmomentumRecData0_98 = histograms1D[Hist1D.hmomentumRec0_98.ordinal()].getData();
float[] hmomentumFoundData0_98 = histograms1D[Hist1D.hmomentumFound0_98.ordinal()].getData();
float[] hmomentumRecData0_95 = histograms1D[Hist1D.hmomentumRec0_95.ordinal()].getData();
float[] hmomentumFoundData0_95 = histograms1D[Hist1D.hmomentumFound0_95.ordinal()].getData();
float[] hmomentumRecData0_92 = histograms1D[Hist1D.hmomentumRec0_92.ordinal()].getData();
float[] hmomentumFoundData0_92 = histograms1D[Hist1D.hmomentumFound0_92.ordinal()].getData();
float[] hmomentumRecData0_90 = histograms1D[Hist1D.hmomentumRec0_90.ordinal()].getData();
float[] hmomentumFoundData0_90 = histograms1D[Hist1D.hmomentumFound0_90.ordinal()].getData();

float[] hmomentumGenData = histograms1D[Hist1D.hmomentumGen.ordinal()].getData();
float[] hmomentumGenFoundData = histograms1D[Hist1D.hmomentumGenFound.ordinal()].getData();

// calculate steps between bins
float step = BEAM_ENERGY/BIN_NUM;
float currentP = step/2;

// loop over momentum data and create NDE plots
for(int i = 0; i < BIN_NUM; i++){ 
    if(hmomentumRecData1_3[i] != 0){
        ndeAtI1_3 = hmomentumFoundData1_3[i]/hmomentumRecData1_3[i];
        dndeAtI1_3 = Math.sqrt(ndeAtI1_3*(1-ndeAtI1_3)*nentriesTotal1_3)/nentriesTotal1_3;
        graphs[Graph.hNDE1_3.ordinal()].addPoint(currentP, ndeAtI1_3, 0, dndeAtI1_3);
    }
    if(hmomentumRecData1_2[i] != 0){
        ndeAtI1_2 = hmomentumFoundData1_2[i]/hmomentumRecData1_2[i];
        dndeAtI1_2 = Math.sqrt(ndeAtI1_2*(1-ndeAtI1_2)*nentriesTotal1_2)/nentriesTotal1_2;
        graphs[Graph.hNDE1_2.ordinal()].addPoint(currentP, ndeAtI1_2, 0, dndeAtI1_2);
    }
    if(hmomentumRecData1_1[i] != 0){
        ndeAtI1_1 = hmomentumFoundData1_1[i]/hmomentumRecData1_1[i];
        dndeAtI1_1 = Math.sqrt(ndeAtI1_1*(1-ndeAtI1_1)*nentriesTotal1_1)/nentriesTotal1_1;
        graphs[Graph.hNDE1_1.ordinal()].addPoint(currentP, ndeAtI1_1, 0, dndeAtI1_1);
    }
    if(hmomentumRecData1_0[i] != 0){
        ndeAtI1_0 = hmomentumFoundData1_0[i]/hmomentumRecData1_0[i];
        dndeAtI1_0 = Math.sqrt(ndeAtI1_0*(1-ndeAtI1_0)*nentriesTotal1_0)/nentriesTotal1_0;
        graphs[Graph.hNDE1_0.ordinal()].addPoint(currentP, ndeAtI1_0, 0, dndeAtI1_0);
    }
    if(hmomentumRecData0_98[i] != 0){
        ndeAtI0_98 = hmomentumFoundData0_98[i]/hmomentumRecData0_98[i];
        dndeAtI0_98 = Math.sqrt(ndeAtI0_98*(1-ndeAtI0_98)*nentriesTotal0_98)/nentriesTotal0_98;
        graphs[Graph.hNDE0_98.ordinal()].addPoint(currentP, ndeAtI0_98, 0, dndeAtI0_98);
    }
    if(hmomentumRecData0_95[i] != 0){
        ndeAtI0_95 = hmomentumFoundData0_95[i]/hmomentumRecData0_95[i];
        dndeAtI0_95 = Math.sqrt(ndeAtI0_95*(1-ndeAtI0_95)*nentriesTotal0_95)/nentriesTotal0_95;
        graphs[Graph.hNDE0_95.ordinal()].addPoint(currentP, ndeAtI0_95, 0, dndeAtI0_95);
    }
    if(hmomentumRecData0_92[i] != 0){
        ndeAtI0_92 = hmomentumFoundData0_92[i]/hmomentumRecData0_92[i];
        dndeAtI0_92 = Math.sqrt(ndeAtI0_92*(1-ndeAtI0_92)*nentriesTotal0_92)/nentriesTotal0_92;
        graphs[Graph.hNDE0_92.ordinal()].addPoint(currentP, ndeAtI0_92, 0, dndeAtI0_92);
    }
    if(hmomentumRecData0_90[i] != 0){
        ndeAtI0_90 = hmomentumFoundData0_90[i]/hmomentumRecData0_90[i];
        dndeAtI0_90 = Math.sqrt(ndeAtI0_90*(1-ndeAtI0_90)*nentriesTotal0_90)/nentriesTotal0_90;
        graphs[Graph.hNDE0_90.ordinal()].addPoint(currentP, ndeAtI0_90, 0, dndeAtI0_90);
    }
    if(hmomentumGenData[i] != 0){
        ndeGenAtI = hmomentumGenFoundData[i]/hmomentumGenData[i];
        dndeGenAtI = Math.sqrt(ndeGenAtI*(1-ndeGenAtI)*nentriesTotalGen)/nentriesTotalGen;
        graphs[Graph.hNDEGen.ordinal()].addPoint(currentP, ndeGenAtI, 0, dndeGenAtI);
    }
    currentP += step;
} // end loop over data
//****************************************************************************************************************

// write histograms to file
histFile.writeFile("nde_histograms_skimmed.hipo");