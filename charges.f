      BLOCK DATA CHARGES
      integer I, MCHARGE(-321:2112)
      COMMON/PIDCHRG/MCHARGE

c Currently contains charges for K-(-321), pi-(-211), e-(11), photon(22),
c pi+(211), K+(321), and the neutron(2112)

      DATA (MCHARGE(I),I=-321,2112)/-1,109*999,-1,221*999,-1,10*999,0,
     +  188*999,1,109*999,1,1790*999,0/

      END