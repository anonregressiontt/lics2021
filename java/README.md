# Global Minimisation with Boehm Constructive Reals

The Java files given here yield preliminary work towards implementing a branch-and-bound style global minimisation algorithm on constructive reals as developed by Hans-J. Boehm. The constructive real data type in `CR.java`, and its two associated `...Error.java` files, were developed by Boehm in 1999; this work can also be found [on his website](https://www.hboehm.info/crcalc/CRCalc.html).

## Running the experimentation
1. Download the files.
1. Compile the code with JDK 1.8.0_281 using `javac GlobalSearchMain.java`.
1. Run the code with `java GlobaSearchMain` and follow the on-screen instructions to experiment with minimising one-dimensional polynomials.