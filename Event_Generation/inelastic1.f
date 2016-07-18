      program inelastic1


C...Double precision and integer declarations.
      IMPLICIT DOUBLE PRECISION(A-H, O-Z)
      IMPLICIT INTEGER(I-N)
      INTEGER PYK,PYCHGE,PYCOMP
C...PYTHIA Commonblocks.
      COMMON/PYDAT1/MSTU(200),PARU(200),MSTJ(200),PARJ(200)
      COMMON/PYDAT2/KCHG(500,4),PMAS(500,4),PARF(2000),VCKM(4,4)
      COMMON/PYDAT3/MDCY(500,3),MDME(8000,2),BRAT(8000),KFDP(8000,5)
      COMMON/PYDAT4/CHAF(500,2)
      CHARACTER CHAF*16
      COMMON/PYDATR/MRPY(6),RRPY(100)
      COMMON/PYSUBS/MSEL,MSELPD,MSUB(500),KFIN(2,-40:40),CKIN(200)
      COMMON/PYPARS/MSTP(200),PARP(200),MSTI(200),PARI(200)
      COMMON/PYINT1/MINT(400),VINT(400)
      COMMON/PYINT2/ISET(500),KFPR(500,2),COEF(500,20),ICOL(40,4,2)
      COMMON/PYINT3/XSFX(2,-40:40),ISIG(1000,3),SIGH(1000)
      COMMON/PYINT4/MWID(500),WIDS(500,5)
      COMMON/PYINT5/NGENPD,NGEN(0:500,3),XSEC(0:500,3)
      COMMON/PYINT6/PROC(0:500)
      CHARACTER PROC*28
      COMMON/PYINT7/SIGT(0:6,0:6,0:5)
      COMMON/PYMSSM/IMSS(0:99),RMSS(0:99)
      COMMON/PYSSMT/ZMIX(4,4),UMIX(2,2),VMIX(2,2),SMZ(4),SMW(2),
     &SFMIX(16,4),ZMIXI(4,4),UMIXI(2,2),VMIXI(2,2)
      COMMON/PYMSRV/RVLAM(3,3,3), RVLAMP(3,3,3), RVLAMB(3,3,3)
      COMMON/PYTCSM/ITCM(0:99),RTCM(0:99)
      COMMON/PYBINS/IHIST(4),INDX(1000),BIN(20000)
      COMMON/PYLH3P/MODSEL(200),PARMIN(100),PAREXT(200),RMSOFT(0:100),
     &     AU(3,3),AD(3,3),AE(3,3)
      COMMON/PYLH3C/CPRO(2),CVER(2)
      CHARACTER CPRO*12,CVER*12

c...Required variables
      integer I,MJ,MPARN,NUMEV,NEULOC,MPILOC,MELECLOC,NGENEV
      integer NPRINT,NSEED,NCOUNT
      integer MCHARGE(-2212:2212)
      double precision Q2,W,Q2MAX,Q2MIN,WMAX,WMIN
      logical NEU,PION,TEST, VERBOSE
      character ARG*32,OUTPUT*32
      real ELECANG, THETAMIN, THETAMAX
      double precision P(4000,5),V(4000,5)
      integer N,NPAD,K(4000,5)
      COMMON/PYJETS/N,NPAD,K,P,V
      COMMON/PIDCHRG/MCHARGE


c...For future reference, These are the relevant PYTHIA variables and their meaing
c...N -> number of particle in the event record table for the most recently generated event
c...P(I,1) -> momentum in the x-direction in GeV/c
c...P(I,2) -> momentum in the y-direction in GeV/c
c...P(I,3) -> momentum in the z-direction in GeV/c
c...P(I,4) -> Energy in GeV
c...P(I,5) -> mass in GeV/c^2
c...K(I,2) -> PID
c...K(I,3) -> line of parent particle


      SAVE /PYDAT1/,/PYDAT2/,/PYDAT3/,/PYDAT4/,/PYDATR/,/PYSUBS/,
     &/PYPARS/,/PYINT1/,/PYINT2/,/PYINT3/,/PYINT4/,/PYINT5/,
     &/PYINT6/,/PYINT7/,/PYMSSM/,/PYSSMT/,/PYMSRV/,/PYTCSM/,
     &/PYBINS/,/PYLH3P/,/PYLH3C/,/PIDCHRG/

c...Set base value for various variables
      NEU = .FALSE.
      PION = .FALSE.
      MPARN = 0
      NUMEV = 0
      NCOUNT = 0

      Q2MAX = 0
      Q2MIN = 50  ! just need this to be semi-large
      WMAX = 0
      WMIN = 50   ! also needed to be semi-large


c...Set PYTHIA generation parameters
      call PYGIVE('MSTJ(12)=0')   ! Don't allow for the production diquark-antidiquark pairs
      call PYGIVE('PARP(2)=2D0')  ! minimum allowed CM energy in GeV

c...Set PYTHIA kinematic cuts
      call PYGIVE('CKIN(65)=3.1')   ! Q2 min incoming channel
      call PYGIVE('CKIN(66)=18.0')  ! Q2 max incoming channel
      call PYGIVE('CKIN(67)=3.1')   ! Q2 min outgoing channel
      call PYGIVE('CKIN(68)=18.0')  ! Q2 max outgoing channel
      call PYGIVE('CKIN(77)=0.9')    ! W min
      call PYGIVE('CKIN(78)=2.0005') ! W max


c...Set argument defaults
      OUTPUT = 'out.dat'
      NGENEV = 20
      NPRINT = 5
      THETAMIN = 0
      THETAMAX = 90
	NSEED = 19780503  ! default set by PYTHIA
      VERBOSE = .FALSE.
	TEST = .FALSE.

c...Set up format for the LUND format
100   format(11X,I2,     ! Number of particles
     1 2X,I1,            ! Num of target nucleons
     2 2X,I1,            ! Num of target protons
     3 2X,F5.3,          ! Target polarization
     4 2X,F5.3,          ! Beam polarization
     5 2X,F5.3,          ! x
     6 1X,F5.3,          ! y
     7 2X,F8.3,          ! W
     8 1X,F8.3,          ! Q^2
     9 2X,F5.3)          ! nu

200   format(4X,I2,      ! Index
     1 3X,I2,            ! Charge
     2 8X,I1,            ! Type
     3 8X,I7,            ! PID
     4 7X,I2,            ! Parent Index
     5 7X,I2,            ! Daughter Index
     6 8X,F7.4,          ! Px
     7 3X,F7.4,          ! Py
     8 3X,F7.4,          ! Pz
     9 3X,F7.4,          ! E
     1 3X,F7.4,          ! mass
     2 3X,F8.4,          ! Vertex x
     3 3X,F8.4,          ! Vertex y
     4 3X,F8.4)          ! Vertex z

c...Set format to show given run parameters
300   format('Run Parameters',/,
     1 'Output file:',13X,A32,/,
     2 'Num events:',6X,I10,/,
     3 'Num events/print:',I10,/,
     4 'Theta min:',14X,F7.4,/,
     5 'Theta max:',15X,F7.4,/,
     6 'RNG seed:',15X,I9,/)

c...Parse given arguments
      J = 1
      do
        call get_command_argument(J,ARG)
        if(LEN_TRIM(ARG) .EQ. 0) exit

        if(TRIM(ARG) .EQ. '-o') then  ! parse output file
          J = J+1
          call get_command_argument(J,OUTPUT)
        else if(TRIM(ARG) .EQ. '-n') then  ! parse num of events to generate
          J = J+1
          call get_command_argument(J,ARG)
          read(ARG,'(I10)') NGENEV
        else if(TRIM(ARG) .EQ. '-n_print') then  ! parse num of events between prints
          J = J+1
          call get_command_argument(J,ARG)
          read(ARG,'(I10)') NPRINT
        else if(TRIM(ARG) .EQ. '-theta_min') then  ! parse electron theta min
          J = J+1
          call get_command_argument(J,ARG)
          read(ARG,*) THETAMIN
        else if(TRIM(ARG) .EQ. '-theta_max') then  ! parse electron theta maax
          J = J+1
          call get_command_argument(J,ARG)
          read(ARG,*) THETAMAX
	  else if(TRIM(ARG) .EQ. '-seed') then  ! parse RNG seed
	    J = J+1
	    call get_command_argument(J,ARG)
	    read(ARG,'(I9)') NSEED
	  else if(TRIM(ARG) .EQ. '-test') then  ! parse debug option
	    J = J+1
	    call get_command_argument(J,ARG)
	    read(ARG,'(L3)') TEST
        else if(TRIM(ARG) .EQ. '-v') then  ! parse debug option
          VERBOSE = .TRUE.
        else if(TRIM(ARG) .EQ. '-h') then  ! parse help message
          write(*,*) 'Options:'
          write(*,*) '-o           Output file name (default: out.dat)'
          write(*,*) '-n           Number of events to generate (default
     +: 20)'
          write(*,*) '-n_print     Number of events between print statem
     +ents (default: 5)'
          write(*,*) '-theta_min   Minimum electron angle (default: 0)'
          write(*,*) '-theta_max   Maximum electron angle (default: 90)'
	    write(*,*) '-seed        Seed for RNG, allowed values 0 <= see
     +d <= 900000000 (default: 19780503)'
          write(*,*) '-v           Add this flag to set the output to be
     + more verbose, this flag doesn''t take a following argument'
          goto 40
        endif
        J = J+1
      enddo

c...Check that given theta values aren't backwards
      if( THETAMAX < THETAMIN ) then
        write(*,*) 'Theta max is less than theta min!'
        write(*,*) 'Stopping execution!'
        goto 40
      endif

c...Print run parameters
      write(*,300) OUTPUT,NGENEV,NPRINT,THETAMIN,THETAMAX,NSEED

      open(2,file=OUTPUT)

c...Set RNG seed
	MRPY(1) = NSEED

c...Start PYTHIA stuff
      write(*,*) 'Starting Initialization'

c...Initialize everything, FIXT tells pythia that I have a beam hitting a fixed target
      call pyinit('FIXT','gamma/e-','p+',11D0)

      write(*,*) 'Initialized'

c...If the test option is set to be true then the values in the MCHARGES array
c...will be printed to screen for test purposes. This option is not shown
c...in the documention or in the help message as it is a debug option
	if(TEST) then
	  call test_charge_array
	endif

	write(*,*) 'Starting Event Generation'

c...Generate events, start of main loop
      do I=0,NGENEV

      if( MOD(I,NPRINT) .EQ. 0) then
        write(*,*) 'Event: ',I
      endif

      call pyevnt

c...Check to see if we have a neutron, if so then grab its parent
        do MJ=1,N
          if (K(MJ,2) .EQ. 2112) then
            NEULOC = MJ
            MPARN = K(MJ,3)
            NEU = .TRUE.
            goto 20
          endif
        enddo

c...If the code gets to this line it means there wasn't a neutron so go to
c...the end of the loop and generate another event.
        goto 10

c...Check to see if neutron parent is a delta+ or PYTHIA string, if not skip to next event
20      if (.NOT.(K(MPARN,2) .NE. 2214 .OR. K(MPARN,2) .NE. 92)) then
          NEU = .FALSE.
          NUELOC = -1
          goto 10
        endif

c...Look through generated particles to find pi+ that has the the delta+
c...parent as the neutron
        do MJ=1,N
          if (K(MJ,2) .EQ. 211 .AND. K(MJ,3) .EQ. MPARN) then
            MPILOC = MJ
            PION = .TRUE.
          endif
        enddo

c...Find scattered electron and make sure its scattering angle is in
c...an acceptable range
        do MJ=3,N
          if (K(MJ,2) .EQ. 11) then
            MELECLOC = MJ
            ELECANG = ACOS((P(MELECLOC,3)/P(MELECLOC,4)))*(180/3.14159)
            goto 30
          endif
        enddo

c...If a neutron and a pi+ have been found with the the same delta+ parent
c...and the electron scattered at an acceptable angle then output the event
c...in the LUND format to the output file
30      if (NEU .AND. PION .AND. (ELECANG < THETAMAX .AND.
     +    ELECANG > THETAMIN)) then
          if(VERBOSE) then
            call pylist(2)
          endif

c...Calculate Q2 and W
          Q2 = 4*11.00051*P(MELECLOC,4)*(SIN((ELECANG*(3.14159/180))/2)
     + **2)
          W = SQRT((P(2,5)**2)+(2*(11.00051-P(MELECLOC,4))*P(2,5))-Q2)

          call pyedit(1)

c...Q2 and W range information for 3-particle events
          if( N .LE. 3 ) then
          NCOUNT = NCOUNT+1
          Q2MAX = MAX(Q2MAX,Q2)
          Q2MIN = MIN(Q2MIN,Q2)
          WMAX = MAX(WMAX,W)
          WMIN = MIN(WMIN,W)
          endif

          write(2,100) N,1,1,0.000,0.000,0.000,0.000,W,Q2,0.000
          do MJ=1,N
            write(2,200) MJ,MCHARGE(K(MJ,2)),1,K(MJ,2),K(MJ,3),0,
     +          P(MJ,1),P(MJ,2),P(MJ,3),P(MJ,4),
     +          P(MJ,5),V(MJ,1)/10,V(MJ,2)/10,
     +          V(MJ,3)/10
          enddo

          NUMEV = NUMEV+1
        endif

10    enddo
c...End of main loop
      if(VERBOSE) then
        call pystat(1)
      endif

      print*,"Total events in ",TRIM(OUTPUT),": ",NUMEV
      print*,"Number of 3-particle events: ",NCOUNT
      print*,"3-particle event ranges:"
      print*,"Q2 max: ",Q2MAX," Q2 min: ",Q2MIN," W max: ",WMAX," W min:
     + ",WMIN

40    stop
      end