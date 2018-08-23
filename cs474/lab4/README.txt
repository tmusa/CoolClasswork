README

by Tumsa Musa

This project was written in python 3.6.2 for 64 bit windows 
see here: https://www.python.org/downloads/release/python-362/

you don't have to install the windows version ofcourse. Just python 3 should be fine. 
also install pip and add both to your PATH variable.

Then run the following command:

########################################################################################
pip install -U scikit-learn
########################################################################################

this will install the required packages
see here for details: http://scikit-learn.org/stable/install.html 

I am assuming that you already have SciPy and numpy installed but if you dont: 
see here for details: https://www.scipy.org/install.html

########################################################################################
pip install numpy scipy matplotlib ipython jupyter pandas sympy nose
########################################################################################

run the previous command to instal numpy scipy and some other cool packages. 
You'll need the first two for for sklearn.





Anyway after you've run those comands all you have to do is run 

########################################################################################
python Ensemble.py
########################################################################################

it will print all the accuracies and confusion matrices. 
There's an huge amount of data printed for the tuning process
so I print that first then several blank lines. 

I place the training and testing files in the folder. 
If you want to change them please name them the same thing and put them in the same place. 
Otherwise change lines 13 and 14 directly in the file. 

Thank you for grading!