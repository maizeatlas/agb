Accrete Genetics and Breeding: Open Source Software for Managing the Workflow and Resources in Plant Genetics and Breeding Labs

# Overview
Accrete Genetics & Breeding (AGB) is an opens-source laboratory information management system with an accompanying multi-user interface for plant genetics & breeding programs. AGB supports the complete workflow for managing genetics and breeding projects (from selecting stocks for a given planting to inventorying seed stocks from a harvest). AGB uses a modular design. A project manager module provides an active memory of the workflow for different projects and allows collaborators to share/transfer responsibility in executing breeding and trialing activities across seasons and locations. Projects and data are preserved as users advance through modules to choose germplasm, create experimental designs, organize and plant genetic/breeding stocks and produce row or plant tags, create data collection files and upload phenotype or weather data, track tissue samples, record mating types and pedigrees when harvesting, inventory seed stocks created, and more. AGB does not handle sequence or genotype data or perform data analysis. There are plans to add a module for users to export datasets in formats for downstream analysis with other tools.

AGB was designed based on a maize genetics and breeding program but should be compatible with other programs.

# Programing design
This system is fully integrated in the Eclipse programming environment and is platform-independent. AGB is programmed in Java Swing using the Spring framework for control of software interface components and the Hibernate framework for mapping data from two relational databases, AGB-Manager and AGB-Data. AGB also connects to R providing extensibility for AGB through the he vast collection of packages available via R. Currently, the R API is only used for the experimental design module, calling functions from the agricolae package for randomizing trials.

# Developers
Ningjing Tian, Naveen Kumar, Matthew Saponaro, Chinmay Pednekar, Teclemariam Weldekidan, Randy Wisser

# Funding
This material is based upon work supported by the National Institute of Food and Agriculture, U.S. Department of Agriculture, including (i) Conservation, Management, Enhancement and Utilization of Plant Genetic Resources, Hatch 0227433 and (ii) Climate Variability and Change Challenge Area, AFRI 2011-67003-30342.
