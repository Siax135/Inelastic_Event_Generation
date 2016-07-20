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
EvioDataEvent event;
PhysicsEvent genEvent;
PhysicsEvent recEvent;

// various counters, indexes, and general information variables
int nevents = 1;
int electronIndex, piPlusIndex, otherParticlesIndex = 0;
int genNeutronCount, recNeutronCount, genElectronCount, recElectronCount, genPiPlusCount, recPiPlusCount, neutronCount, electronCount, piPlusCount = 0;
double electronPx, electronPy, electronPz, piPlusPx, piPlusPy, piPlusPz, otherParticlesPx, otherParticlesPy, otherParticlesPz = 0;
double electronE, piPlusE = 0;
double energyTerm, momentumTerm, missingMass = 0;

// variables for keeping track of relevant particles paths and hits
Vector3D scatteredElectron;
Vector3D scatteredPiPlus;
Vector3D momentumTermVector;

Vector3D otherParticlesP = new Vector3D(0,0,0);
double otherParticlesE = 0;

// used to look for reconstructed electron and pi+ info
Scanner electronSearch, piPlusSearch, otherParticlesSearch;
String genLund, electronInfo, piPlusInfo, otherParticlesInfo;

// constants
Vector3D ELECTRON_INITIAL_P; // = new Vector3D(0.0,0.0,11.0);
final double ELECTRON_INITIAL_ENERGY = 11.00051;
final double PROTON_INITIAL_ENERGY = 0.93827;
//**********************************************************************************

//* Create histograms **************************************************************
TDirectory histFile = new TDirectory();
histFile.mkdir("neutrons");

histFile.getDirectory("neutrons").add(new H1D("hmissingMass", 300, 0, 6));
H1D hmissingMass = (H1D)histFile.getDirectory("neutrons").getObject("hmissingMass");
hmissingMass.setXTitle("mass (GeV/c^2)");

histFile.getDirectory("neutrons").add(new H1D("hmissingMassHerm", 300, 0, 1.5));
H1D hmissingMassHerm = (H1D)histFile.getDirectory("neutrons").getObject("hmissingMassHerm");
hmissingMassHerm.setXTitle("Missing Mass ((GeV/c^2)^2)");
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

    // get generated and reconstructed pi+ counts
    genPiPlusCount = genEvent.countByPid(211);
    recPiPlusCount = recEvent.countByPid(211);
    piPlusCount = Math.max(genPiPlusCount, recPiPlusCount);

    // get generated and reconstructed electron counts
    genNeutronCount = genEvent.countByPid(2112);
    recNeutronCount = recEvent.countByPid(2112);
    neutronCount = Math.max(genNeutronCount, recNeutronCount);

    ELECTRON_INITIAL_P = new Vector3D(0,0,11);


    // make sure we have electrons, pi+'s, and neutrons to analyze
    if(electronCount > 0 && neutronCount > 0 && piPlusCount > 0){
        // get information about the reconstruction in lund format and pass it to a scanner
        genLund = genEvent.toLundString();
        //System.out.println(genLund);
        electronSearch = new Scanner( genLund ); // scanner to look for electrons
        piPlusSearch = new Scanner( genLund );   // scanner to look for pi+
        numRecParticles = Integer.parseInt( electronSearch.next() );  // determine how many particles are in the LUND string
        electronSearch.nextLine();

        // loop over each reconstructed particle looking for an electron
        for( int i = 0; i < numRecParticles; i++ ) {
            electronIndex = Integer.parseInt( electronSearch.next() ); // grab index of the current particle in the LUND string
            for( int j = 0; j < 3; j++ ) { // jump to pid in lund string
                electronInfo = electronSearch.next();
            } // end loop jumping to pid
            recPID = Integer.parseInt(electronInfo);
            if( recPID == 11 ) { // check if current pid means we have an electron

                // Uncomment line below to see the LUND string*******************************************************
                //if(nevents == 2383){
                   // System.out.println("Event: " + nevents);
                    //System.out.println(genLund);
                    //System.out.println();
                    //System.out.println(recEvent.toLundString());
                //}

                for( int k = 0; k < 3; k++ ) { // jump to px for electron
                    electronInfo = electronSearch.next();
                } // end loop to jump to px
                // get px, py, and pz from lund string
                electronPx = Double.parseDouble(electronInfo);  // px for electron
                electronInfo = electronSearch.next();
                electronPy = Double.parseDouble(electronInfo);  // py for electron
                electronInfo = electronSearch.next();
                electronPz = Double.parseDouble(electronInfo);  // pz for electron
                electronInfo = electronSearch.next();
                electronE = Double.parseDouble(electronInfo);   // E for electron

                piPlusSearch.nextLine();  // mak sure pi+ scanner starts at the right place for parsing the LUND string

                for( int k = 0; k < numRecParticles; k++ ) {
                    piPlusIndex = Integer.parseInt( piPlusSearch.next() );  // grab index of the current particle in the LUND string
                    for( int j = 0; j < 3; j++ ) { // jump to pid in lund string
                		piPlusInfo = piPlusSearch.next();
                    } // end loop jumping to pid
                    recPID = Integer.parseInt(piPlusInfo);
                    //if(nevents == 2383) System.out.println("Pre-if pid: " + recPID + " Index: " + piPlusIndex);
                    if( recPID == 211 ) { // check if current pid means we have a pi+
                    //if(nevents == 2383) System.out.println("Post-if pid: " + recPID + " Index: " + piPlusIndex);
                        for( int m = 0; m < 3; m++ ) { // jump to px for pi+
                            piPlusInfo = piPlusSearch.next();
                    } // end loop to jump to px
                    // get px, py, and pz from lund string
                    piPlusPx = Double.parseDouble(piPlusInfo);  // px for pi+
                    piPlusInfo = piPlusSearch.next();
                    piPlusPy = Double.parseDouble(piPlusInfo);  // py for pi+
                    piPlusInfo = piPlusSearch.next();
                    piPlusPz = Double.parseDouble(piPlusInfo);  // pz for pi+
                    piPlusInfo = piPlusSearch.next();
                    piPlusE = Double.parseDouble(piPlusInfo);   // E for pi+

                    // Create momentum vectors from electron and pi+ momentum components
                    scatteredElectron = new Vector3D(electronPx,electronPy,electronPz);
                    scatteredPiPlus = new Vector3D(piPlusPx,piPlusPy,piPlusPz);


                    energyTerm = Math.pow(ELECTRON_INITIAL_ENERGY + PROTON_INITIAL_ENERGY - electronE - piPlusE, 2);


                    momentumTermVector = new Vector3D(0,0,11).sub(scatteredElectron).sub(scatteredPiPlus);
                    momentumTerm = momentumTermVector.dot(momentumTermVector);

                    missingMass = energyTerm - momentumTerm;

                    // Uncomment line below to see which electron is being paired with which pi+ and what the missing mass with that pairing is.
                    // If the below line is uncommented you should also uncomment line 109
//                    if( nevents == 2383 ) {
//                        System.out.println("Electron in E: " + ELECTRON_INITIAL_ENERGY);
//                        System.out.println("Proton in E: " + PROTON_INITIAL_ENERGY);
//                        System.out.println("Electron In: " + ELECTRON_INITIAL_P.toString());
//                        System.out.println("Electron Out: " + scatteredElectron.toString());
//                        System.out.println("Final Vector: " + momentumTermVector.toString());
//                        System.out.println("Energy Term: " + energyTerm + " Momentum Term: " + momentumTerm);
//                        System.out.println("Electron Index: " + electronIndex + " Pi+ Index: " + piPlusIndex + " Missing Mass: " + missingMass);
//                        System.out.println();
//                    }

                        if(missingMass < 0.88){
                            System.out.println("Event: " + nevents);
                            System.out.println(genLund);
                            //System.out.println("Electron: " + scatteredElectron.toString());
                            //System.out.println("Pi+: " + scatteredPiPlus.toString());
                            System.out.println("Electron Index: " + electronIndex + " Pi+ Index: " + piPlusIndex + " Missing Mass: " + missingMass);
                        }

                    hmissingMass.fill(missingMass);
                    if(numRecParticles == 3 ) hmissingMassHerm.fill(missingMass);
                    } // end check of the pi+ pid
                    piPlusSearch.nextLine(); // jump to next line if we didn't find an pi+
                } // end loop looking for pi+ particles
                piPlusSearch = new Scanner(genLund);  // reset pi+ scanner so that if there are multiple electrons we can pair each of the pi+'s with the next electron as well
            } // end if checking the electron pid
            electronSearch.nextLine();
        } // end loop looking for electrons
    } // end if checking electron and neutron count */
    nevents++;
} // end loop over events

// Show Histogram
TCanvas c1 = new TCanvas("c1","PhysicsAnalysis",1200,640,1,2);
c1.cd(0);
c1.draw(hmissingMass);
c1.cd(1);
c1.draw(hmissingMassHerm);