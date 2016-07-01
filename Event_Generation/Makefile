OBJ= pythia-6.4.28.o charges.o inelastic1.o

all : $(OBJ)
	gfortran -o inelastic1 $(OBJ)

$(OBJ) : %.o : %.f
	gfortran -c $< -o $@

clean:
	rm -f inelastic1 $(OBJ)
