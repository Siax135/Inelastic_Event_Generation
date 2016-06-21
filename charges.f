      COMMON/PIDCHRG/CHARGE(-321:2112)
      INTEGER I

      do I=-321,2112
        CHARGE(I) = 999
      enddo

      CHARGE(-321) = -1 ! K-
      CHARGE(-211) = -1 ! pi-
      CHARGE(11) = -1   ! e-
      CHARGE(22) = 0    ! photon
      CHARGE(211) = 1   ! pi+
      CHARGE(321) = 1   ! K+
      CHARGE(2112) = 0  ! neutron