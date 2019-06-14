# SPLAT4Java
This is an attempt at a Java Rendition of open source RF mapping tool SPLAT

The goal is to have a (easier to build) cross-platform version of SPLAT. 

This version differs from the C++ version in the following ways:
 - Does not yet have support for loading SDF files from .bz compressed format
 - SDF file names use underscores e.g 1_2_325_326.sdf rather than full colons e.g 1:2:325:326.sdf - Windows does not allow the latter. 
 
 Different use scenarios, are still being tested for identical results as SPLAT and are noted in the commits. 
 
 In early commits, The Code is a 1:1 equivalent of the SPLAT code ( in most cases) before optimizations and java standards compliance.
 The goal is to optimize without affecting the results of computations
