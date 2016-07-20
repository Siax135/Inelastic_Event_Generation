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

// open file
EvioSource reader = new EvioSource();
reader.open("/home/Siax/Linux_Shared/Pythia/e_pi_n3_Rec.0.evio");

// create fitter to get data
GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);

//* Create variables for later use **************************************************
// variables for reading in data
EvioDataBank genBank;
EvioDataBank ecBank;
EvioDataEvent event;
PhysicsEvent genEvent;
PhysicsEvent recEvent;
Particle genElectron;
Particle recNeutron;

// various counters, indexes, and general information variables
int nevents, nentries, nrec, nless = 0;
int genRows, ecRows, electronIndex, piPlusIndex = 0;
int genNeutronCount, recNeutronCount, genElectronCount, recElectronCount, genPiPlusCount, recPiPlusCount, neutronCount, electronCount, piPlusCount = 0;
double electronPx, electronPy, electronPz, piPlusPx, piPlusPy, piPlusPz = 0;
double electronE, piPlusE = 0;
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
final double BEAM_ENERGY = 11.0;
final double ELECTRON_INITIAL_ENERGY = 11.0051;
final double PROTON_INITIAL_ENERGY = 0.93827;
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

histFile.getDirectory("neutrons").add(new H1D("hmissingMass", 300, 0, 1.5));
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

    // make sure we have electrons and neutrons to analyze
    if(electronCount > 0 && neutronCount > 0 && piPlusCount > 0){
        // get information about the reconstruction in lund format and pass it to a scanner
        electronFound = false;
        piPlusFound = false;
        recLund = recEvent.toLundString();
        electronSearch = new Scanner( recLund );
        piPlusSearch = new Scanner( recLund );
        numRecParticles = Integer.parseInt( electronSearch.next() );
        electronSearch.nextLine();

        // loop over each reconstructed particle looking for an electron
        for( int i = 0; i < numRecParticles; i++ ) {
            electronIndex = Integer.parseInt( electronSearch.next() );
            for( int j = 0; j < 3; j++ ) { // jump to pid in lund string
                electronInfo = electronSearch.next();
                //System.out.println(j + ": " + electronInfo);
            } // end loop jumping to pid
            recPID = Integer.parseInt(electronInfo);
            if( recPID == 11 ) { // check if current pid means we have an electron


                //System.out.println("Event: " + nevents);
                //System.out.println(recLund);

                for( int k = 0; k < 3; k++ ) { // jump to px for electron
                    electronInfo = electronSearch.next();
                } // end loop to jump to px
                // get px, py, and pz from lund string
                electronPx = Double.parseDouble(electronInfo);
                electronInfo = electronSearch.next();
                electronPy = Double.parseDouble(electronInfo);
                electronInfo = electronSearch.next();
                electronPz = Double.parseDouble(electronInfo);
                electronInfo = electronSearch.next();
                electronE = Double.parseDouble(electronInfo);
                electronFound = true; // we found an electron

                piPlusSearch.nextLine();

                for( int k = 0; k < numRecParticles; k++ ) {
                piPlusIndex = Integer.parseInt( piPlusSearch.next() );
                    for( int j = 0; j < 3; j++ ) { // jump to pid in lund string
                		piPlusInfo = piPlusSearch.next();
                    } // end loop jumping to pid
                    recPID = Integer.parseInt(piPlusInfo);
                    if( recPID == 211 ) { // check if current pid means we have a pi+
                        for( int m = 0; m < 3; m++ ) { // jump to px for pi+
                            piPlusInfo = piPlusSearch.next();
                        } // end loop to jump to px
                        // get px, py, and pz from lund string
                        piPlusPx = Double.parseDouble(piPlusInfo);
                        piPlusInfo = piPlusSearch.next();
                        piPlusPy = Double.parseDouble(piPlusInfo);
                        piPlusInfo = piPlusSearch.next();
                        piPlusPz = Double.parseDouble(piPlusInfo);
                        piPlusInfo = piPlusSearch.next();
                        piPlusE = Double.parseDouble(piPlusInfo);





                        // Create momentum vectors from electron and pi+ momentum components
                        scatteredElectron = new Vector3D(electronPx,electronPy,electronPz);
                        scatteredPiPlus = new Vector3D(piPlusPx,piPlusPy,piPlusPz);

                        energyTerm = Math.pow(ELECTRON_INITIAL_ENERGY + PROTON_INITIAL_ENERGY - electronE - piPlusE, 2);

                        momentumTermVector = new Vector3D(0,0,11).sub(scatteredElectron).sub(scatteredPiPlus);
                        momentumTerm = momentumTermVector.dot(momentumTermVector);

                        // calculate missing mass
                        missingMass = energyTerm - momentumTerm;

                        if(missingMass < 0.88){
                            nless++;
                            System.out.println("Event: " + nevents);
                            System.out.println(recLund);
                            //System.out.println("Electron: " + scatteredElectron.toString());
                            //System.out.println("Pi+: " + scatteredPiPlus.toString());
                            System.out.println("Electron Index: " + electronIndex + " Pi+ Index: " + piPlusIndex + " Missing Mass: " + missingMass);
                        }

                        hmissingMass.fill(missingMass);
                        if(numRecParticles != 2 ) break;

                        hmissingMassHerm.fill(missingMass);


                        if( missingMass > 0.87 && missingMass < 0.89 ){
                            System.out.println("In missing mass if, missing mass: " + missingMass + " event: "+ nevents);

                            if(event.hasBank("ECRec::clusters")){ // make sure we have the ECRec::clusters bank
                                System.out.println("has EC bank");
                                ecBank = event.getBank("ECRec::clusters");
                                ecRows = ecBank.rows();
                            }else{ // if we don't have the bank then move on to the next event
                                skippedEvents++;
                                break;
                            } // end if for the ECRec::clusters bank

                            // calculate scattered neutron momentum
                            scatteredNeutron = new Vector3D(-1*(electronPx+piPlusPx), -1*(electronPy+piPlusPy), (BEAM_ENERGY-electronPz-piPlusPz));
                            momentum = scatteredNeutron.mag(); // store magnitude in GeV
                            scatteredNeutron.scale(1000); // scale scatter neutron vector to MeV
                            scatteredNeutron.unit(); // make scattered neutron momentum vector a unit vector

                            genBank = event.getBank("GenPart::true");
                            generatedNeutron = new Vector3D(genBank.getDouble("px",1), genBank.getDouble("py",1), genBank.getDouble("pz",1))
                            generatedNeutron.unit();


                            // create a line representing the neutron's path and loop over EC sectors to see if the neutron would hit the sectors
                            neutronPath = new Line3D( new Point3D(0,0,0), scatteredNeutron );
                            for(int j = 0; j < 6 && !intersectionStatus; j++){
                                intersectionStatus = ECfaces[i].hasIntersection(neutronPath);
                            } // end loop over sectors to see if neutrons hit

                            if(!intersectionStatus){ // if neutron wouldn't hit then skip event
                                System.out.println("Missed");
                                skippedEvents++;
                                break;
                            } // end if checking that the neutron doesn't miss

                            hmomentumTotal.fill(momentum);

                            // loop over neutron candidates
                            for(int j = 0; j < ecRows; j++){
                                // get hit coordinates and make a vector
                                hitX = ecBank.getDouble("X",i);
                                hitY = ecBank.getDouble("Y",i);
                                hitZ = ecBank.getDouble("Z",i);
                                ECHit = new Vector3D(hitX, hitY, hitZ);

                                ECHit.unit(); // make hit vector a unit vector

                                thetapq = Math.acos(scatteredNeutron.dot(ECHit))*(180/Math.PI); // calculate angle between scattered neutron and neutron hit candidate
                                htheta.fill(thetapq);
                                if(thetapq < 1.5) { // if angle is less than 1.5 degrees then call it a hit and move on to the next event
                                    hthetapq.fill(thetapq);
                                    hmomentumRec.fill(momentum);
                                    System.out.println("Found Neutron")

                                    theta = Math.acos(scatteredNeutron.z()/momentum)*(180/Math.PI);
                                    hacceptance.fill(theta,momentum);

                                    break;
                                } // end if checking thetapq
                            } // end loop over neutron candidates */

                            intersectionStatus = false;
                        }


//                    piPlusFound = true; // we found an pi+

                   // break;
                    } // end check of the pid
                    piPlusSearch.nextLine(); // jump to next line if we didn't find an pi+
                }
                piPlusSearch = new Scanner(recLund);
            } // end loop through reconstructed particles
            electronSearch.nextLine();
        }

       /* if(!electronFound){ // make sure we actually found a reconstructed electron
            skippedEvents++;
            //nevents++;
            continue;
        } // end if for checking that we have a reconstructed electron

               nevents++;
//        intersectionStatus = false; */
    } // end if checking electron and neutron count
    nevents++;
} // end loop over events
System.out.println("Events less than 0.88:" + nless);

TCanvas c1 = new TCanvas("c1","PhysicsAnalysis",1200,640,1,3);
c1.cd(0);
c1.draw(hmissingMass);
c1.cd(1);
c1.draw(hmissingMassHerm);
c1.cd(2);
c1.draw(hacceptance);

 
 
 
 
 
 
 
 
 

 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 /*
 
 // calculate neutron detection efficiency and print run info to screen
System.out.println("Num events: " + nevents + " Skipped events: " + skippedEvents);
nentries = hmomentumRec.getEntries();
nde = (double) nentries/nevents;
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
histFile.write("neutrons_12-15_Hist_NOPCAL.evio");  */