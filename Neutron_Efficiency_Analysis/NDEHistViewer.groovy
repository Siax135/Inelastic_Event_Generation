import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;

String inputFile = args[0];

TDirectory histFile = new TDirectory();
histFile.readFile(inputFile);

H1F hthetapq = (H1F)histFile.getObject("neutrons","hthetapq");
H1F htheta = (H1F)histFile.getObject("neutrons","htheta");
H1F hmomentumTotal = (H1F)histFile.getObject("neutrons","hmomentumTotal");
H1F hmomentumRec = (H1F)histFile.getObject("neutrons","hmomentumRec");
H1F hmissingMass = (H1F)histFile.getObject("neutrons","hmissingMass");
H1F hmissingMassHerm = (H1F)histFile.getObject("neutrons","hmissingMassHerm");
H2F hacceptance = (H2F)histFile.getObject("neutrons","hacceptance");
H2F hNDE = (H2F)histFile.getObject("neutrons","hNDE");

TCanvas c1 = new TCanvas("NDE Analysis",1800,1000);
c1.divide(4,2);
c1.cd(0);
c1.draw(hthetapq);
c1.cd(1);
c1.draw(htheta);
c1.cd(2);
c1.draw(hmomentumTotal);
c1.cd(3);
c1.draw(hmomentumRec);
c1.cd(4);
c1.draw(hmissingMass);
c1.cd(5);
c1.draw(hmissingMassHerm);
c1.cd(6);
c1.draw(hacceptance);
c1.cd(7);
c1.draw(hNDE);