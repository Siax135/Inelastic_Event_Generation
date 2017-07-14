import org.jlab.io.hipo.*;
import java.lang.Integer;

String inputFile;
String outputFile;
boolean analyze = false;

//* Input Argument Stuff *****************************************************************************************
// if to check the number of arguments
if(args.length < 4){
    System.out.println("Usage: >run-groovy epiplusSkimmer.groovy -i <input_file.hipo> -o <output_file.hipo> -a");
    System.out.println();
    System.out.println("The -a flag is optional but if it is added then the skimmer will produce the skimmed file");
    System.out.println("and then run it through the analysis script.")
    System.out.println();
    System.exit(1);
} // end if to check number of arguments

// loop over arguments to get input and output files and see if analysis should be run
for(int i = 0; i < args.length; i++){
    switch(args[i]) { // switch to check which argument to parse
        case "-i":
            if(i != args.length-1) inputFile = args[i+1];
            break;
        case "-o":
            if(i != args.length-1) outputFile = args[i+1];
            break;
        case "-a":
            analyze = true;
            break;
    }
} // end loop to get files and check analysis

// Check to make sure we have an input file and an output file
if(inputFile == null || outputFile == null){
    System.out.println("Missing input file or output file!");
    System.out.println("Exiting!");
    System.out.println();
    System.exit(1);
} // end if checking for input and output files

//****************************************************************************************************************

//* Open file and set up variables *******************************************************************************
// Open file
HipoDataSource reader = new HipoDataSource();
reader.open(inputFile);

// Create variables for later use 
HipoDataBank TOFBank;
HipoDataEvent event;

HipoDataSync fileWriter = new HipoDataSync();

// various counters, indexes, and general information variables
int nevents, nentries = 0;
int TOFrows = 0
int chargeOne, chargeTwo = 0;

// keep track of skipped events
int skippedEvents = 0;
//****************************************************************************************************************

//* Write skimmed events to file *********************************************************************************
// Open/create output file and write header event to file
fileWriter.open(outputFile);
if(reader.hasEvent()){ // make sure input file has events
    fileWriter.writeEvent(reader.getNextEvent());
    nevents++;
} // end 

//loop over events
while(reader.hasEvent()){
    // get reconstructed events
    event = reader.getNextEvent();

    TOFrows = 0;
    if(event.hasBank("TimeBasedTrkg::TBTracks")){ // check to see if the event has the time based tracking bank I need
        TOFBank = event.getBank("TimeBasedTrkg::TBTracks");
        TOFrows = TOFBank.rows();
        // Since I am only interested in the 3 particle event e pi+ n and n won't be reconstructed 
        // I only want to look at events that have exactly 2 reconstructed particles ( e and pi+ )
        if(TOFrows != 2){ 
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
        nentries++;
    } else if(chargeOne == -1 && chargeTwo == 1){ // chargeOne is the electron and chargeTwo is the pi+ 
        nentries++;
    } else{ // either both charges are the same or one (or both) are 0
        skippedEvents++;
        nevents++;
        continue;
    } // end if to check which charge is which

    nevents++;

    fileWriter.writeEvent(event);
} // end loop over events

fileWriter.close();

System.out.println("Number of events in " + inputFile + ": " + nevents);
System.out.println("Number of events in " + outputFile + ": " + nentries);
//****************************************************************************************************************

//* Analysis *****************************************************************************************************
if(analyze){ // if the analyze flag was given then run analysis
    System.out.println("Analyzing...");
    String command = "run-groovy neutronEfficiencySkimmed.groovy " + outputFile;
    command.execute(); 
} // end if checking for analysis
//****************************************************************************************************************