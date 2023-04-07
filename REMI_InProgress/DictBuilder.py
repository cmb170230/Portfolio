import json
import os
import glob
import random
import pickle
from quantulum3 import parser
from collections import OrderedDict

def main():
    current_directory = os.getcwd()
    dirtyFiles = os.path.join(current_directory, r'allrecipes')
    emptydict = dict()
    for filename in glob.glob(os.path.join(dirtyFiles, '*.txt')):
        print(filename)
        with open(filename, 'r',  encoding="utf8") as file:
            ugly = file.read()
            ugly = ugly[1:]
            good = '{' + ugly + '}'
            #print(get_recipe_instructions(good))
            #print(parse_recipe(good))
            recipe = parse_recipe(good)
            emptydict.update({recipe.name:recipe})
    entry_list = list(emptydict.items())
    random_entry = random.choice(entry_list)
    print(random_entry[1])
    dbfile = open('recipeDictPickle', 'ab')
    pickle.dump(emptydict, dbfile )
    
            

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
            recipe_str += f"\t{key}: {value}\n"
        recipe_str += "How-to Steps:\n"
        for key, value in self.howToSteps.items():
            recipe_str += f"\t{key}: {value}\n"
        recipe_str += f"Techniques: {self.techniques}\n"
        recipe_str += "Reviews:\n"
        for key, value in self.reviews.items():
            recipe_str += f"\t{key}: {value}\n"
        return recipe_str

def parse_recipe(inString):
    data = json.loads(inString)

    recipe = Recipe()

    try:
        recipe.name = data['name']
    except KeyError:
        recipe.name = ''

    try:
        recipe.cuisine = data['recipeCategory']
    except KeyError:
        recipe.cuisine = ''

    try:
        recipe.rating = float(data['aggregateRating']['ratingValue'])
    except KeyError:
        recipe.rating = 0.0

    try:
        recipe.ratingCount = int(data['aggregateRating']['ratingCount'])
    except KeyError:
        recipe.ratingCount = 0

    try:
        recipe.prepTime = int(data['prepTime'][2:-1])
    except (KeyError, ValueError):
        recipe.prepTime = 0
    try:
        recipe.totalTime = int(data['totalTime'][2:-1])
    except (KeyError, ValueError):
        recipe.prepTime = 0
    try:
        recipe.cookTime = int(data['cookTime'][2:-1])
    except (KeyError, ValueError):
        recipe.cookTime = 0

    recipe.nutrition = {}
    nutrition_info = data.get('nutrition', {})
    for key, value in nutrition_info.items():
        if key != '@type':
            recipe.nutrition[key] = value

    try:
        recipe.servingCount = int(data['recipeYield'][0])
    except (KeyError, ValueError):
        recipe.servingCount = 0

    recipe.ingredients = {}
    for ingredient in data.get('recipeIngredient', []):
        try:
            quant = parser.parse(ingredient)
            name, amount = ingredient.rsplit(' ', 1)
            recipe.ingredients[name] = amount
        except (ValueError, TypeError):
            continue

    recipe.howToSteps = {}
    for i, step in enumerate(data.get('recipeInstructions', [])):
        try:
            recipe.howToSteps[f"Step {i+1}"] = step['text']
        except (KeyError, TypeError):
            continue

    recipe.techniques = []
    for step in recipe.howToSteps.values():
        words = step.split()
        for word in words:
            if word.endswith('ing'):
                recipe.techniques.append(word.capitalize())

    recipe.reviews = {}
    for review in data.get('review', []):
        try:
            author = review['author']['name']
            rating = int(review['reviewRating']['ratingValue'])
            body = review['reviewBody']
            recipe.reviews[author] = (rating, body)
        except (KeyError, TypeError, ValueError):
            continue

    return recipe
        
def get_recipe_instructions(recipe_string):
    import json
    recipe_dict = json.loads(recipe_string)
    recipe_instructions = [step['text'] for step in recipe_dict['recipeInstructions']]
    return recipe_instructions
    
#def ingredient_key_parse(tempDict):
#    #parser = spacy.load("en_core_web_sm")
#    finaldict = tempDict
#    measurementsList = ("teaspoon, teaspoons, tablespoon, tablespoons, tsp, tbsp, cup, cups, ounces, oz, ounce, ounces")
#    return finaldict

if __name__ == "__main__":
    main()
