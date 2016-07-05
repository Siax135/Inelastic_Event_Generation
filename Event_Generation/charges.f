      BLOCK DATA CHARGES
      integer I, MCHARGE(-2212:2212)
      COMMON/PIDCHRG/MCHARGE

c...Contains electric charges for the listed particles. Their position
c...in the array is the same as their pid

      DATA (MCHARGE(I),I=-2212,2212)/
     +  -1,99*999,    ! antiproton (-2212)
     +  0,1790*999,   ! antineutron (-2112)
     +  -1,109*999,   ! K- (-321)
     +  -1,197*999,   ! pi- (-211)
     +  1,999,        ! antimuon (-13)
     +  1,21*999,     ! positron (-11)
     +  -1,999,       ! e- (11)
     +  -1,8*999,     ! muon (13)
     +  0,107*999,    ! photon (22)
     +  0,80*999,     ! K0 (130)
     +  1,109*999,    ! pi+ (211)
     +  1,1790*999,   ! K+ (321)
     +  0,99*999,     ! neutron (2112)
     +  1/            ! proton (2212)

      END

c...Test routine that shows that pid's are in the array and what
c...charge is associated with them
	SUBROUTINE test_charge_array
	integer MCHARGE(-2212:2212)
	COMMON/PIDCHRG/MCHARGE

	write(*,*) 'Test MCHARGE(-2212):',MCHARGE(-2212)
      write(*,*) 'Test MCHARGE(-2112):',MCHARGE(-2112)
      write(*,*) 'Test MCHARGE(-321):',MCHARGE(-321)
      write(*,*) 'Test MCHARGE(-211):',MCHARGE(-211)
      write(*,*) 'Test MCHARGE(-13):',MCHARGE(-13)
      write(*,*) 'Test MCHARGE(-11):',MCHARGE(-11)
      write(*,*) 'Test MCHARGE(11):',MCHARGE(11)
      write(*,*) 'Test MCHARGE(13):',MCHARGE(13)
      write(*,*) 'Test MCHARGE(22):',MCHARGE(22)
      write(*,*) 'Test MCHARGE(130):',MCHARGE(130)
      write(*,*) 'Test MCHARGE(211):',MCHARGE(211)
      write(*,*) 'Test MCHARGE(321):',MCHARGE(321)
      write(*,*) 'Test MCHARGE(2112):',MCHARGE(2112)
      write(*,*) 'Test MCHARGE(2212):',MCHARGE(2212)
      write(*,*) 'Test MCHARGE(45):',MCHARGE(45)

	END
