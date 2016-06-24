      BLOCK DATA CHARGES
      integer I, MCHARGE(-2212:2212)
      COMMON/PIDCHRG/MCHARGE

c Currently contains charges for the antiproton (-2212), K-(-321),
c pi-(-211), positron(-11), e-(11), photon(22),K0(130), pi+(211),
c K+(321), neutron(2112) and the proton(2212)

      DATA (MCHARGE(I),I=-2212,2212)/
     +  -1,99*999,    ! antiproton (-2212)
     +  0,1790*999,   ! antineutron (-2112)
     +  -1,109*999,   ! K- (-321)
     +  -1,199*999,   ! pi- (-211)
     +  1,21*999,     ! positron (-11)
     +  -1,10*999,    ! e- (11)
     +  0,107*999,    ! photon (22)
     +  0,80*99,      ! K0 (130)
     +  1,109*999,    ! pi+ (211)
     +  1,1790*999,   ! K+ (321)
     +  0,99*999,     ! neutron (2112)
     +  1/            ! proton (2212)

      END