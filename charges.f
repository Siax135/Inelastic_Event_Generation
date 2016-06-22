      BLOCK DATA CHARGES
      integer I, MCHARGE(-2212:2212)
      COMMON/PIDCHRG/MCHARGE

c Currently contains charges for the antiproton (-2212), K-(-321),
c pi-(-211), e-(11), photon(22), pi+(211), K+(321), neutron(2112) and
c the proton(2212)

      DATA (MCHARGE(I),I=-2212,2212)/-1,1890*999,-1,109*999,-1,221*999,
     +  -1,10*999,0,188*999,1,109*999,1,1790*999,0,99*999,1/

      END