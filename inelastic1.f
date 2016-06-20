      program inelastic1


C...Double precision and integer declarations.
      IMPLICIT DOUBLE PRECISION(A-H, O-Z)
      IMPLICIT INTEGER(I-N)
      INTEGER PYK,PYCHGE,PYCOMP
C...Commonblocks.
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


      integer I,J,PARN,NUMEV,NEULOC,PILOC,ELECLOC,EVENT,FINAL
      logical NEU,PION
      character OUTPUT*15
      real ELECP, ELECANG
      double precision P(4000,5),V(4000,5)
      integer N,NPAD,K(4000,5)
      COMMON/PYJETS/N,NPAD,K,P,V

      SAVE /PYDAT1/,/PYDAT2/,/PYDAT3/,/PYDAT4/,/PYDATR/,/PYSUBS/,
     &/PYPARS/,/PYINT1/,/PYINT2/,/PYINT3/,/PYINT4/,/PYINT5/,
     &/PYINT6/,/PYINT7/,/PYMSSM/,/PYSSMT/,/PYMSRV/,/PYTCSM/,
     &/PYBINS/,/PYLH3P/,/PYLH3C/

      NEU = .FALSE.
      PION = .FALSE.
      PARN = 0
      NUMEV = 0
      PARP(2) = 2D0

100   format(11X,I1,     ! Number of particles
     1 2X,I1,            ! Num of target nucleons
     2 2X,I1,            ! Num of target protons
     3 2X,F5.3,          ! Target polarization
     4 2X,F5.3,          ! Beam polarization
     5 2X,F5.3,          ! x
     6 1X,F5.3,          ! y
     7 2X,F5.3,          ! W
     8 1X,F5.3,          ! Q^2
     9 2X,F5.3).         ! nu

200   format(4X,I2,      ! Index
     1 3X,I2,            ! Charge
     2 8X,I1,            ! Type
     3 8X,I7,            ! PID
     4 7X,I2,            ! Parent Index
     5 7X,I2,            ! Daughter Index
     6 8X,F7.4,          ! Px
     7 3X,F7.4,          ! Py
     8 3X,F7.4,          ! Pz
     9 3X,F6.4,          ! E
     1 3X,F6.4,          ! mass
     2 3X,F6.4,          ! Vertex x
     3 3X,F6.4,          ! Vertex y
     4 3X,F6.4)          ! Vertex z

      call getarg(1,OUTPUT)
      open(2,file=OUTPUT)

      write(*,*) 'Starting Initialization'

c     Initialize everything, FIXT tells pythia that I have a beam hitting a fixed target
      call pyinit('FIXT','gamma/e-','p+',11D0)

      write(*,*) 'Initialized'

c     Generate a single event and print it out
      do I=1,10000
      call pyevnt
c        call pylist(2)
c        write(*,*) 'e-', P(1,1),P(1,2),P(1,3)
c        write(*,*) 'p', P(2,1),P(2,2),P(2,3)
c        write(*,*) 'In Loop'

c        write(*,*) 'Got Length'

c       Check to see if we have a neutron, if so then grab its parent
        do J=1,N
          if (K(J,2) .EQ. 2112) then
            NEULOC = J
            PARN = K(J,3)
            NEU = .TRUE.
            goto 20
          endif
        enddo

c        write(*,*) 'Looked for neutron'

20      if (K(PARN,2) .NE. 2214) then
          NEU = .FALSE.
          NUELOC = -1
          goto 10
        endif



c        write(*,*) 'Found a neutron'

        do J=1,N
          if (K(J,2) .EQ. 211 .AND. K(J,3) .EQ. PARN) then
            PILOC = J
            PION = .TRUE.
c            NUMEV = NUMEV+1
c            call pylist(2)
          endif
        enddo

        do J=3,N
          if (K(J,2) .EQ. 11) then
            ELECLOC = J
c            ELECP = SQRT((P(ELECLOC,1)**2)+(P(ELECLOC,2)**2)+
c     +       (P(ELECLOC,3)**2))
            ELECANG = ACOS((P(ELECLOC,3)/P(ELECLOC,4)))*(180/3.14159)
c            write(*,*) 'e anlge:',ELECANG,'Pe:',ELECP
            goto 30
          endif
        enddo

30      if (NEU .AND. PION .AND. (ELECANG < 18 .AND. ELECANG > 12)) then
          call pyedit(1)



          call pylist(2)


          write(2,100) N
          do J=1,N
            write(2,200) J,0,1,K(J,2),K(J,3),0,
     +          P(J,1),P(J,2),P(J,3),P(J,4),
     +          P(J,5),V(J,1)/10,V(J,2)/10,
     +          V(J,3)/10
          enddo

c            do J=3,LENGTH
c              if (K(J,4) .EQ. 0) then
c                  FINAL = FINAL+1
c              endif
c            enddo
c           write(*,*) 'Undecayed Particles:',FINAL
c          write(*,*) P(ELECLOC,1),P(ELECLOC,2),P(ELECLOC,3)
c          write(2,100) 3

c          write(2,200) 1,-1,1,K(ELECLOC,2),K(ELECLOC,3),0,
c     +          P(ELECLOC,1),P(ELECLOC,2),P(ELECLOC,3),P(ELECLOC,4),
c     +          P(ELECLOC,5),V(ELECLOC,1)/10,V(ELECLOC,2)/10,
c     +          V(ELECLOC,3)/10

c          write(2,200) 2,1,1,K(PILOC,2),K(PILOC,3),0,
c     +          P(PILOC,1),P(PILOC,2),P(PILOC,3),P(PILOC,4),
c     +          P(PILOC,5),V(PILOC,1)/10,V(PILOC,2)/10,
c     +          V(PILOC,3)/10

c          write(2,200) 3,0,1,K(NEULOC,2),K(NEULOC,3),0,
c     +          P(NEULOC,1),P(NEULOC,2),P(NEULOC,3),P(NEULOC,4),
c     +          P(NEULOC,5),V(NEULOC,1)/10,V(NEULOC,2)/10,
c     +          V(NEULOC,3)/10

          NUMEV = NUMEV+1
        endif

10    EVENT = EVENT+1
      enddo

      write(*,*) NUMEV, EVENT

      stop
      end