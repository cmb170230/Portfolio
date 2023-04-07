import os
import re
import sys
import glob
import pickle
from collections import OrderedDict

import nltk
import sklearn
import numpy as np
import pandas as pd

def main():
    print("Welcome to REMI_V1! To start, tell me your name!\n")
    userIn = input()

    ##SETUP:
        # load list of ingredients (ordered dict with key [firstletter][number])
        # load list of methods (ordered dict with key [firstletter][number])
        # load recipe 'database'

        # one of: [DialogFlow Setup] or [Import trained model] or [Wordnet Traversal Categorization]
        # initialize first user object
        # display welcome/first prompt explaining what REMI can do
            #sub-idea: give sample conversation as demo
                #sample user state for demo????

        # working db- whole db filtered by constraints set in user model

    while(userIn != "exit"):
        #parse userin for desired intent (dialogflow???)

        intent = 1
        if intent == 0:
            #retrieve information about a particular ingredient or method
                #parse relevant info from string
                 #if ingredient or method definition, query wordnet
                #elif substitution
                 #determine if ingredient or method
                 #if ingredient substitution
                    #query wordnet for lemmas
                    #if no lemmas
                        #query hyponyms of hypernym
                
               
                 #if method substitution
                    #query vector space model based on cosine similarity
                    # and pearson correlation coefficient
                    # if agree, then good match,
                    # if not agree, then use cosine but say it might not be good
                #  
            pass
        elif intent == 1: 
            #search database for recipe based on attribute
            #if nutrition related
                #if specifics given
                 #parse/constrain based on given data

                #if more general given
                 #filter by preset 'healthy' nutritional standards

                 #dietary restrictions??? if time
            pass
        elif intent == 2:
            #parse/display something specifically in the recipe
            #if ingredient
                #quantulum3 parser to handle quantities
            #if method/step
                #also leverage quantulum3????
                #go check recipe class for viability
            pass
        

class UserState():
    def __init__(self) -> None:
        userName = "" #name of the current user
        userIngredients = list() #list of ingredients (potentially tuple of ingredient, quantity object)
        preferredMethods = list() #list of methods preferred, starts with all and is pared down when dislike indicated
        recipeCatalog = OrderedDict() #keep track of all recipes explored so far
        nutritionPrefs = list() #list of user's nutritional preferences, default cases for 'healthy' / 'don't care' or precise def
