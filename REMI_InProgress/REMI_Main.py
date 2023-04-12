import os
import re
import sys
import glob
import pickle
from collections import OrderedDict

import nltk
import spacy
import eng_spacysentiment
import numpy as np
import pandas as pd
from nltk.corpus import wordnet2021 as wn
from quantulum3 import parser

from DictBuilder import Recipe
from DictBuilder import isCookingVerb, parseTechniques, parsefromRecipes

class UserState():
    def __init__(self,
                 name = 'None_Provided',
                 ingredients = list(),
                 pmethods = list(),
                 dmethods = list(),
                 recipes = OrderedDict(),
                 nutrition = dict(),
                 rating = int,
                 time = int
                 ):
        self.userName = name #name of the current user
        self.userIngredients = ingredients #list of ingredients (potentially tuple of ingredient, quantity object)
        self.preferredMethods = pmethods #list of methods preferred
        self.dislikedMethods = dmethods #list of methods to avoid
        self.recipeCatalog = recipes #keep track of all recipes explored so far
        self.nutritionPrefs = nutrition #dict of nutritional attr : value
        self.ratingThreshold = rating #cutoff for allowable rating
        self.maxCookTime = time #maximum time desired for recipe length

##SETUP:
    # load recipe 'database'
recipeData = pickle.load(open("recipeData.dat", 'rb'))
    # load list of ingredients and methods
ingredientData = pickle.load(open("ingredientData.dat", 'rb'))
methodData = pickle.load(open("methodData.dat", 'rb'))
    # load previous user states
try:
    userList = pickle.load(open("userStates.dat", 'rb'))
except:
    userList = list()
    # Import trained models
intentRegressor = pickle.load(open("intent_regressor.model", 'rb'))
intentBayes = pickle.load(open("intent_bayes.model", 'rb'))

    # set up default nutritional cases
healthyDiet = {
    'calories' : parser.parse('400 kcal'),
    'carbohydrateContent' : parser.parse('40 grams'),
    'cholesterolContent' : parser.parse('20 mg'),
    'fiberContent' : parser.parse('2 g'),
    'proteinContent' : parser.parse('15g'),
    'saturatedFatContent' : parser.parse('0 gram'),
    'sodiumContent' : parser.parse('500mg'),
    'sugarContent' : parser.parse('10 grams'),
    'fatContent' : parser.parse('15 grams'),
    'unsaturatedFatConent' : parser.parse('15 grams')
}

    

    # Set up spaCy Model
nlp = spacy.load("en_core_web_trf")

def main():
        # initialize first user object and sample user state
    sampleUser = UserState()
    sampleUser.userName = "Demo N. Stration"
    sampleUser.userIngredients = ("potato", "rice", "beef", "cabbage")
    sampleUser.preferredMethods = ("saute", "roast", "deep fry")
    sampleUser.nutritionPrefs = healthyDiet
        # display welcome/first prompt explaining what REMI can do
            #sub-idea: give sample conversation as demo
                #sample user state for demo????

        # working db- whole db filtered by constraints set in user models
    
    print("Welcome to REMI_V1!")
    print("REMI:\tI’m REMI, the Recipe Exploration and Modification Intelligence!")
    currentUserID = 0
    #Parse name from userIn
    foundName = False
    while(not foundName):
        print("REMI:\tTo start, please tell me your name!\n")
        userIn = input("User:\t")
        nameParse = nlp(userIn)
        if(len(nameParse) == 1):
            userList.append(UserState(name= nameParse[0].text.capitalize()))
            foundName = True
        else:
            for tok in nameParse:
                if tok.pos_ == 'PROPN' and tok.text.lower().find('remi') == -1 and not tok.is_stop:
                    foundName = True
                    userList.append(UserState(name=tok.text.capitalize()))
                    break
    

    print("REMI:\tHi " + getattr(userList[currentUserID], 'userName') + "!\n")

    while(userIn != "exit"):
        #query for intent
        intent = '\0'
        userIn = input("REMI:\tWhat can I help you with today?\nUSER:\t")
        intent = getUserIntent(userIn)


        """
            Definition Intent:
                This area provides the mechanism by which REMI provides definitions
                for ingredients or cooking methods.

                The user's statement is passed in and parsed using spaCy's english language
                transformer to tag part-of-speech information. All ingredients are nouns,
                so a list of noun words found in the sentence is constructed, and from there 
                WordNet is used to check if a form of these nouns are categorized as an ingredient.


        """
        if intent == 'd':

            ingredientDefs, methodDefs = parseforcooking(userIn)

            defs = ingredientDefs + methodDefs
            try:
                outstr = "REMI:\tSure thing, here's a definition for " + defs[0][0]
                if len(defs) > 1:
                    if len(defs) > 2:
                        outstr += ','
                        for i in range(1,len(defs)-1):
                            outstr += " " + defs[i][0] + ','
                    outstr += " and " + defs[-1][0]
                print(outstr, '\n')
                for definition in defs:
                    print(definition[0].capitalize(), ":", definition[1], "\n")
            except (IndexError):
                print("REMI:\tHmm... sorry, doesn't seem like I know what that means.")

            #retrieve information about a particular ingredient or method
                #parse relevant info from string
                 #if ingredient or method definition, query wordnet
                
                 
            pass
                
        """
            Exploration Intent:

                This is REMI's core functionality: based on what it knows about the user
                and the incoming request, it will attempt to find a recipe that best matches
                the user's desire.

        """
        if intent == 'e':
            print('e')
                 #if method substitution
                    #query vector space model based on cosine similarity
                    # and pearson correlation coefficient
                    # if agree, then good match,
                    # if not agree, then use cosine but say it might not be good
                #if ingredient substitution
                    #use current known ingredient database
                    #find item with highest wu-palmer similarity
            pass
        """
            Modification Intent:
                Using the loaded ingredient or method lists, the user's request will be parsed
                for either a method or ingredient and the next closest term will be given.

                Closeness will be determined by wu-palmer similarity.
        """
        if intent == 'm': 
            print('m')
            
            pass
        if intent == 'u':
            ##PRIMARY USE: UNCERTAINTY RESOLUTION
            print('uncertainty detected')
            #new user detection
                #proper noun?

            #refer to something in current recipe
                #if there's a number
                #word: step, else ingredient

            #conversion
                #using quantulum3, if multiple units are detected in a string, 
                #figure out how to use pint for conversions
            #genuine uncertainty- reprompt
            pass
        else:
            

            #parse/display something specifically in the recipe
            #if ingredient
                #quantulum3 parser to handle quantities
            #if method/step
                #also leverage quantulum3????
                #go check recipe class for viability
            pass
#    pickle.dump(userList, open("userStates.dat", 'wb'))


def getUserIntent(userstring):
    intent = ''
    reg = intentRegressor.predict([userstring]), intentRegressor.predict_proba([userstring])
    bayes = intentBayes.predict([userstring]), intentBayes.predict_proba([userstring])

    if bayes[0][0] == reg[0][0]:
        intent = bayes[0][0]
    else:
        if(reg[1].max() > 0.8):
            intent = reg[0][0]
        elif(bayes[1].max() > 0.8):
            intent = bayes[0][0]
        else:
            intent = 'u'

    return intent    


"""
    Parse For Cooking:
        This function takes in a sentence parsed by spaCy and outputs a list
        of ingredients (i.e. food nouns) and methods (i.e. cooking related verbs)

"""
def parseforcooking(inputFromUser):
    definitionParse = nlp(inputFromUser)
    ingredientList = list()
    methodList = parseTechniques(inputFromUser, nlp=nlp)

    defineIngList = list()
    defineMethodList = list()
    
    if len(definitionParse) < 4:
        for tok in definitionParse:
            ingredientList.append(tok.text)
    else:
        for tok in definitionParse:
            if tok.pos_ == 'NOUN':
                ingredientList.append(tok.text)


    #if ingredients found,  
    listind = 0
    for ingr in ingredientList:
        #check to see if this is actually an ingredient
        try:
            syn = wn.synsets(ingr, pos=wn.NOUN)
            ingrIndex = -1
            for s in syn:
                isIngredientHyponym = False
                ingrIndex +=1
                try:
                    hyp = s.hypernyms()[0]
                    entity = wn.synset('entity.n.01')
                    ingredient = wn.synset('ingredient.n.03')
                    herb = wn.synset('herb.n.01')
                    food = wn.synset('food.n.02')
                    while hyp:
                        #print(bechHyp)
                        if hyp == ingredient or hyp == herb or hyp == food:
                            isIngredientHyponym = True
                            break
                        if hyp == entity:
                            break
                        if hyp.hypernyms():
                            hyp = hyp.hypernyms()[0]
                    if isIngredientHyponym:
                        break
                except (IndexError):
                    #print(s, s.definition())
                    continue
            if ingrIndex != -1 and isIngredientHyponym:
                #append word and definition to list
                defineIngList.append((ingredientList[listind], syn[ingrIndex].definition()))
        except:
            continue
        listind +=1

    listind = 0
    for method in methodList:
        try:
            syn = wn.synsets(method, pos=wn.VERB)
            mIndex = -1
            for s in syn:
                isMethodHyponym = False
                mIndex +=1
                try:
                    hyp = s.hypernyms()[0]
                    cap = 0
                    while hyp:
                        cap +=1
                        raw = wn.synset('create_from_raw_material.v.01')
                        cut = wn.synset('cut.v.01')
                        cook = wn.synset('cook.v.03')
                        heat = wn.synset('heat.v.01')
                        if hyp == cook or hyp == raw or hyp == cut or hyp == heat:
                            isMethodHyponym = True
                            break
                        if cap > 10:
                            break
                        if hyp.hypernyms():
                            hyp = hyp.hypernyms()[0]
                    if isMethodHyponym:
                        break
                except (IndexError):
                    print(s, s.definition())
                    continue
            if mIndex != -1 and isMethodHyponym:
                #append word and definition to list
                defineIngList.append((methodList[listind], syn[mIndex].definition()))
        except:
            continue
        listind += 1

    return defineIngList, defineMethodList

main()