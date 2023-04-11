import os
import re
import sys
import glob
import pickle
from collections import OrderedDict

import nltk
import sklearn
import spacy
import eng_spacysentiment
import numpy as np
import pandas as pd
from nltk.corpus import wordnet2021 as wn
from quantulum3 import parser

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
        userName = name #name of the current user
        userIngredients = ingredients #list of ingredients (potentially tuple of ingredient, quantity object)
        preferredMethods = pmethods #list of methods preferred
        dislikedMethods = dmethods #list of methods to avoid
        recipeCatalog = recipes #keep track of all recipes explored so far
        nutritionPrefs = nutrition #dict of nutritional attr : value
        ratingThreshold = rating #cutoff for allowable rating
        maxCookTime = time #maximum time desired for recipe length

class Recipe:
    def __init__(self):
        self.name = '' #Contains the recipe name
        self.cuisine = '' #Contains what type of food the recipe is
        self.rating = 0 #Contains the average rating
        self.ratingCount = 0 #Contains the number of ratings
        self.prepTime = 0 #The ammount of preptime
        self.cookTime = 0 #the ammount of time needed to cook
        self.totalTime = 0
        self.nutrition = {} #Nutritional values, as a list of tuples.  Eg. ("calories", # of calories)
        self.servingCount = 0 #Number of servings made
        self.ingredients = {}# Contains a list of tuples, in the format (ingredient, ammount)
        self.howToSteps = {} #Contains a list of the steps in the recipe text
        self.techniques = [] #Contains a list of techniques used, like frying, baking, grilling, etc.
        self.reviews = {} #Contains a list of reviews people have written for the recipe

    def __str__(self):
        recipe_str = f"Name: {self.name}\n"
        recipe_str += f"Cuisine: {self.cuisine}\n"
        recipe_str += f"Rating: {self.rating}\n"
        recipe_str += f"Rating Count: {self.ratingCount}\n"
        recipe_str += f"Time: {self.totalTime}\n"
        recipe_str += f"prep Time: {self.prepTime}\n"
        recipe_str += f"cook Time: {self.cookTime}\n"
        recipe_str += f"Nutrition: {self.nutrition}\n"
        recipe_str += f"Serving Count: {self.servingCount}\n"
        recipe_str += "Ingredients:\n"
        for key, value in self.ingredients.items():
            vals = ''
            try:
                for v in value:
                    vals += v.surface
            except:
                print(self.name)
                vals = value
            recipe_str += f"\t{key}: {vals}\n"
        recipe_str += "How-to Steps:\n"
        for key, value in self.howToSteps.items():
            recipe_str += f"\t{key}: {value}\n"
        recipe_str += f"Techniques: {self.techniques}\n"
        recipe_str += "Reviews:\n"
        for key, value in self.reviews.items():
            recipe_str += f"\t{key}: {value}\n"
        return recipe_str

##SETUP:
    # load recipe 'database'
recipeData = pickle.load(open("recipeData.dat", 'rb'))
    # load list of ingredients and methods
ingredientList = list()
methodList = list()
for recipekey, recipevalue in recipeData.items():
    for ingredientkey, _ in recipevalue.ingredients.items():
        ingredientList.append(str(ingredientkey))
    for tech in recipevalue.techniques:
        methodList.append(tech)
ingredientSet = set(ingredientList)
ingredientList = list(ingredientSet)
methodSet = set(methodList)
methodList = list(methodSet)

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

    # Import trained models
intentRegressor = pickle.load(open("intent_regressor.model", 'rb'))
intentBayes = pickle.load(open("intent_bayes.model", 'rb'))

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
    userList = list()
    #Parse name from userIn
    foundName = False
    while(not foundName):
        print("REMI:\tTo start, please tell me your name!\n")
        userIn = input()
        nameParse = nlp(userIn)
        for tok in nameParse:
            if tok.pos_ == 'PROPN':
                foundName = True
                userList.append(UserState(name=tok.text))
                break



    while(userIn != "exit"):
        print("REMI:\tHi " + userList[currentUserID].userName + "!\n")
        intent = '\0'

        userIn = input("What can I help you with today?")
        #query for intent

        intent = getUserIntent(userIn)

        if intent == 'd':
            definitionParse = nlp(userIn)

            defineListIngr = list()
            defineListMethod = list()

            for tok in definitionParse:
                if tok.pos_ == 'NOUN':
                    defineListIngr.append(tok.text)
                elif tok.pos_ == 'VERB':
                    defineListMethod.append(tok.text)
            #retrieve information about a particular ingredient or method
                #parse relevant info from string
                 #if ingredient or method definition, query wordnet
                #elif substitution
                 #determine if ingredient or method
                 #if ingredient substitution
                    #query wordnet for lemmas
                    #if no lemmas
                        #query hyponyms of hypernym
            pass
                
        elif intent == 'e':
                 #if method substitution
                    #query vector space model based on cosine similarity
                    # and pearson correlation coefficient
                    # if agree, then good match,
                    # if not agree, then use cosine but say it might not be good
                #  
            pass
        elif intent == 'm': 
            #search database for recipe based on attribute
            #if nutrition related
                #if specifics given
                 #parse/constrain based on given data

                #if more general given
                 #filter by preset 'healthy' nutritional standards

                 #dietary restrictions??? if time
            pass
        elif intent == 'u':
            ##PRIMARY USE: UNCERTAINTY RESOLUTION

            #REPROMPT USER // PROBABILITY THRESHOLD CUTOFF
            pass
        else:
            

            #parse/display something specifically in the recipe
            #if ingredient
                #quantulum3 parser to handle quantities
            #if method/step
                #also leverage quantulum3????
                #go check recipe class for viability
            pass

def getUserIntent(userstring):
    intent = ''
    reg = intentRegressor.predict([userstring]), intentRegressor.predict_proba([userstring])
    bayes = intentBayes.predict([userstring]), intentBayes.predict_proba([userstring])

    if bayes == reg:
        intent = bayes
    else:
        intent = 'u'

    return intent    



main()