import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import tensorflow as tf
from tensorflow import keras

updf = pd.read_csv(os.getcwd() + "\intenttrain.csv", header = "infer", index_col = False, encoding= 'cp1252')

###
#    THIS IS A LOCAL COPY OF THE COLAB NOTEBOOK FOR SKLEARN COMPATABILITY REASONS
###

#format for tensorflow text dataset

alldata = os.getcwd() + "/data"
ddir = os.getcwd() + "/data/define"
edir = os.getcwd() + "/data/explore"
mdir = os.getcwd() + "/data/modify"
if not os.path.exists(os.getcwd() + "/data"):
  os.mkdir(alldata)
if not os.path.exists(os.getcwd() + "/data/define"):
  os.mkdir(ddir)
if not os.path.exists(os.getcwd() + "/data/explore"):
  os.mkdir(edir)
if not os.path.exists(os.getcwd() + "/data/modify"):
  os.mkdir(mdir)

for incr, row in updf.iterrows():
  if row[1] == 'd':
    with open(ddir + '/d' + str(incr) + '.txt', "w", encoding = 'utf8') as out:
      out.write(str(row['questions']))
      out.close()
  elif row[1] == 'e':
    with open(edir + '/e' + str(incr) + '.txt', "w", encoding = 'utf8') as out:
      out.write(str(row['questions']))
      out.close()
  elif row[1] == 'm':
    with open(mdir + '/m' + str(incr) + '.txt', "w", encoding = 'utf8') as out:
      out.write(str(row['questions']))
      out.close()

batch_sz = 10

trainds = keras.utils.text_dataset_from_directory(alldata, labels='inferred', 
                                                  label_mode='categorical', batch_size = batch_sz,
                                                  validation_split = 0.2, subset = 'training',
                                                  seed = 170230
                                                  )
valds = keras.utils.text_dataset_from_directory(alldata, labels='inferred', 
                                                  label_mode='categorical', batch_size = batch_sz,
                                                  validation_split = 0.2, subset = 'validation',
                                                  seed = 170230
                                                  )

VOCAB_SIZE = 576
encoder = tf.keras.layers.TextVectorization(max_tokens = VOCAB_SIZE)
encoder.adapt(trainds.map(lambda text, label: text))
vocab = np.array(encoder.get_vocabulary())

# Define a function to extract the data from a tensorflow dataset so that we can work with Pandas Dataframes/Series objects
# when using Sklearn Naive Bayes

def get_tf_data(data):
  dat = []

  for feature_batch, label_batch in data.take(-1):
    print(feature_batch, label_batch)
    for features, label in zip(feature_batch.numpy(), label_batch.numpy()):
      dat.append((features, pd.Series(label, index = ['d', 'e', 'm']).idxmax()))

  return dat

train_df = pd.DataFrame(get_tf_data(trainds), columns = ['questions', 'label'])
test_df = pd.DataFrame(get_tf_data(valds), columns = ['questions', 'label'])

# output class distribution chart
catplotdata = pd.concat([train_df, test_df])
catplotdata.head(20)

sns.catplot(data = catplotdata, x=None, y="label", kind="count", palette="pastel")

## BEGIN MODEL TRAINING

from sklearn.feature_extraction.text import TfidfTransformer, CountVectorizer
from sklearn.naive_bayes import MultinomialNB, BernoulliNB, ComplementNB

X_train = train_df['questions']
y_train = train_df['label']

X_test = test_df['questions']
y_test = test_df['label']

from sklearn.utils.metaestimators import BaseEstimator
from sklearn.pipeline import Pipeline
from sklearn.model_selection import GridSearchCV

# Define a generic class for usage with Sklearn estimators so that we can 
# test multiple models (MultinomialNB and BernoulliNB) using the same pipeline
class naive_bayes_mod(BaseEstimator):
  def __init__(self, estimator = MultinomialNB()):
    self.estimator = estimator

  def fit(self, X, y = None, **kwargs):
    self.estimator.fit(X, y)
    return self

  def predict(self, X, y = None):
    return self.estimator.predict(X)

  def score(self, X, y):
    return self.estimator.score(X, y)

pipe = Pipeline(steps = [
    ('count_vectorizer', CountVectorizer()),
    ('tfidf_transformer', TfidfTransformer()),
    ('nb', BernoulliNB())#naive_bayes_mod())
])

params = [{
    # Second dictionary is for bernoulli naive bayes
    'count_vectorizer__stop_words': ['english', None],   
    'count_vectorizer__binary' : [True],                         # Only use binary word occurrence for bernoulli
    'tfidf_transformer': ['passthrough'],                        # Skip the TfidfTransformer step
    'nb__alpha': [0.001, 0.25, 0.33, 0.5, 0.66, 0.75, 0.9, 1],
    'nb__fit_prior' : [True, False]
    #'nb__estimator__class_prior': [(0.25, 0.75), (0.5, 0.5), (0.75, 0.25)]
}]


grid_nb = GridSearchCV(pipe, params, cv = 10, scoring = 'accuracy').fit(X_train, y_train)

# best_estimator_ returns the entire pipeline, so we can use the predict method directly on un-vectorized test data
best_nb = grid_nb.best_estimator_

#recover optimal parameters from the model
best_nb.get_params

from sklearn.metrics import classification_report, ConfusionMatrixDisplay

mappings = {'d' : 'define', 'e' : 'explore', 'm' : 'modify'}

# Predict on the test data and print evaluation metrics
preds = best_nb.predict(X_test)
print(classification_report(y_test, preds, target_names = mappings.values()))
ConfusionMatrixDisplay.from_predictions(y_test, preds, display_labels = mappings.values())

misclassification_count = 0
correct_disp = 0
i = 0
print("INCORRECT PREDICTIONS:")
while misclassification_count < 10:
  try:
    if (preds[i] != y_test[i]):
      print('\t"' + str(X_test[i])[2:] + '"')     # [2:-3] to cut off unwanted starting and ending characters
      print('\tActual Sentiment: {}\t\tPredicted Sentiment: {}\n'.format(mappings.get(y_test[i]), mappings.get(preds[i])))
      misclassification_count += 1
  except:
    print("less than 10")
    break
  i += 1

print("CORRECT PREDICTIONS:")
i = 0
while correct_disp < 10:
  if (preds[i] == y_test[i]):
    print('\t"' + str(X_test[i])[2:] + '"')     # [2:-3] to cut off unwanted starting and ending characters
    print('\tActual Sentiment: {}\t\tPredicted Sentiment: {}\n'.format(mappings.get(y_test[i]), mappings.get(preds[i])))
    correct_disp += 1

  i += 1

from sklearn.linear_model import LogisticRegression

pipe = Pipeline(steps = [
    ('count_vectorizer', CountVectorizer()),
    ('tfidf_transformer', TfidfTransformer()),
    ('linreg', LogisticRegression())
])
### USES BEST PARAMS FROM NOTEBOOK
params = [{ 
    'count_vectorizer__stop_words': ['english', None],           # English stop words set has many known problems, so try the vectorization 
                                                                 # without using stop words at all
           
    'tfidf_transformer': [TfidfTransformer(), 'passthrough'],    # Whether to use count vectorized (skipping the TfidfTransformer step) 
                                                                 # or tfidf vectorized
    'linreg__penalty': ['l1', 'l2', 'elasticnet', None],                 
    'linreg__tol' : [1e-3, 1e-4, 1e-5, 1e-6],
    'linreg__C' : [10],
    'linreg__class_weight' : ['balanced'],
    'linreg__random_state' : [12],
    'linreg__solver' : ['sag'],
    'linreg__max_iter' : [500]
}]

grid_logr = GridSearchCV(pipe, params, cv = 10, scoring = 'accuracy').fit(X_train, y_train)

# best_estimator_ returns the entire pipeline, so we can use the predict method directly on un-vectorized test data
best_linreg = grid_logr.best_estimator_

#recover optimal parameters from the model
best_linreg.get_params

# Predict on the test data and print evaluation metrics
preds = best_linreg.predict(X_test)
print(classification_report(y_test, preds, target_names = mappings.values()))
ConfusionMatrixDisplay.from_predictions(y_test, preds, display_labels = mappings.values())

misclassification_count = 0
correct_disp = 0
i = 0
print("INCORRECT PREDICTIONS:")
while misclassification_count < 10:
  try:
    if (preds[i] != y_test[i]):
      print('\t"' + str(X_test[i])[2:] + '"')     # [2:-3] to cut off unwanted starting and ending characters
      print('\tActual Sentiment: {}\t\tPredicted Sentiment: {}\n'.format(mappings.get(y_test[i]), mappings.get(preds[i])))
      misclassification_count += 1
  except:
    print("less than 10")
    break
  i += 1

print("CORRECT PREDICTIONS:")
i = 0
while correct_disp < 10:
  if (preds[i] == y_test[i]):
    print('\t"' + str(X_test[i])[2:] + '"')     # [2:-3] to cut off unwanted starting and ending characters
    print('\tActual Sentiment: {}\t\tPredicted Sentiment: {}\n'.format(mappings.get(y_test[i]), mappings.get(preds[i])))
    correct_disp += 1

  i += 1

import pickle

rmodelout = "intent_regressor.model"
nbmodelout = "intent_bayes.model"
pickle.dump(best_linreg, open(rmodelout, 'wb'))
pickle.dump(best_nb, open(nbmodelout, 'wb'))