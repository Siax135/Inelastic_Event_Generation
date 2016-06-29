# Inelastic_Event_Generation

 To build this program simply run make which will produce the executable inelastic1.
 Running `./inelastic1 -h` will print a help meassage which shows the allowed options
 and what the default setting for each are. 

 It should be noted, the main loop in this code tells pythia to generate a single event 
 per loop and then checks to see if the event produced is a ep -> e'pi<sup>+</sup>n event. If it is
 then it will store the event in the LUND format in the given output file. If it isn't then
 it just skips along to the next event and doesn't put anything in the output file. What
 this means is that even though the program may be told to generate 50,000 events as an argument
 it will put signifcantly less than 50,000 event in the output file because they aren't all
 ep -> e'pi<sup>+</sup>n events. The total number of events put in the output file will be printed at
 end of execution.
