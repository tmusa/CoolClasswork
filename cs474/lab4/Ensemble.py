import sys
import numpy as np

from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import AdaBoostClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.naive_bayes import GaussianNB
from sklearn.metrics import confusion_matrix

# filepaths
trainpath = "lab4-train.csv"
testpath = "lab4-test.csv"


def tune():
    # load files
    traindata_file_path = trainpath
    testdata_file_path = testpath

    printSpacing = len("ensemble_5 unweighted accuracy")

    if len(sys.argv) == 3:
        traindata_file_path = sys.argv[1]
        testdata_file_path = sys.argv[2]

    traindata = np.loadtxt(traindata_file_path, delimiter=',', dtype=int, skiprows=1)
    testdata = np.loadtxt(testdata_file_path, delimiter=',', dtype=int, skiprows=1)

    # splitting into x,y sets
    x = traindata[:, :4]
    y = traindata[:, 4]

    x_test = testdata[:, :4]
    y_test = testdata[:, 4]

    np.random.seed(5)

    # models --

    print("Tuning Models\n")

    # AdaBoost.M1

    np.random.seed(5)

    def tune_ada():

        max_depth = range(1, 10)
        algos = ["SAMME", "SAMME.R"]
        learning_rates = [0.001, 0.002, 0.005, 0.10, 0.25, 0.5, 1, 1.25, 1.5, 2, 5]
        num_estimators = range(1, 25)
        ada_dict = {'algorithm': algos, 'learning_rate': learning_rates, 'n_estimators': num_estimators}

        hyperparams = {
            'algo': "SAMME",
            'max_depth': 1,
            'n_estimators': 50,
            'learning_rate': 1,
            'score': 0
        }

        best_score = 0

        for depth in max_depth:
            for algorithm in algos:
                for rate in learning_rates:
                    for num in num_estimators:
                        np.random.seed(5)
                        ada = AdaBoostClassifier(DecisionTreeClassifier(max_depth=depth, random_state=1)
                                                 , algorithm=algorithm
                                                 , n_estimators=num, learning_rate=rate, random_state=1)
                        ada.fit(x, y)
                        score = ada.score(x_test, y_test)
                        print(algorithm, ',', depth, ',', num, ',', rate, ',', score)
                        abs_score = abs(score - .50)

                        if abs_score > best_score:
                            best_score = abs_score
                            hyperparams['algo'] = algorithm
                            hyperparams['max_depth'] = depth
                            hyperparams['n_estimators'] = num
                            hyperparams['learning_rate'] = rate
                            hyperparams['score'] = score
                            print("*")

        print(hyperparams, '\n', best_score)

    print('AdaBoost tunning \n')

    # tune_ada()

    print('\n\n')

    def tune_rf():

        num_estimators = range(1, 20)
        max_features = range(2, 4)
        min_sample_splits = range(2, 10)
        min_samples_leaf = range(2, 10)
        max_depth = range(3, 20)

        hyperparams = {
            'num_estimators': 0,
            'max_features': 0,
            'min_sample_splits': 0,
            'min_samples_leaf': 0,
            'max_depth': 0,
            'score': 0
        }

        best_score = -1

        for num in num_estimators:
            for features in max_features:
                for min in min_sample_splits:
                    for cit in min_samples_leaf:
                        for depth in max_depth:
                            np.random.seed(5)
                            rf = RandomForestClassifier(
                                n_estimators=num, min_samples_leaf=cit, max_features=features,
                                max_depth=depth, min_samples_split=min, random_state=1
                            )
                            rf.fit(x, y)
                            score = rf.score(x_test, y_test)

                            print(num, ',', features, ',', min, ',', cit, ',', depth, ',', score)
                            abs_score = abs(score - .50)

                            if abs_score > best_score:
                                best_score = abs_score
                                hyperparams['num_estimators'] = num
                                hyperparams['max_features'] = features
                                hyperparams['min_sample_splits'] = min
                                hyperparams['min_samples_leaf'] = cit
                                hyperparams['max_depth'] = depth
                                hyperparams['score'] = score
                                print('*')

        print(hyperparams, best_score)

    print('Random Forest tunning \n')

    tune_rf()

    print('\n\n')

    def tune_nn():
        activation = ['identity', 'logistic', 'tanh', 'relu']
        solver = ['lbfgs', 'sgd', 'adam']
        alpha = [0.0001, 0.001, 0.01, 0.1, 1, 50]
        learning_rate = ['constant', 'invscaling', 'adaptive']

        hyperparams = {
            'activation': 0,
            'solver': 0,
            'alpha': 'gini',
            'learning_rate': 0,
            'score': 0
        }

        best_score = -1

        for act in activation:
            for s in solver:
                for a in alpha:
                    for l in learning_rate:
                        np.random.seed(5)
                        rf = MLPClassifier(activation=act, solver=s, alpha=a, learning_rate=l
                        )
                        rf.fit(x, y)
                        score = rf.score(x_test, y_test)

                        print(act, ',', s, ',', a, ',', l, ',', score)
                        ab = abs(score - .5)
                        if ab > best_score:
                            best_score = ab
                            hyperparams['activation'] = act
                            hyperparams['solver'] = s
                            hyperparams['alpha'] = a
                            hyperparams['learning_rate'] = l
                            hyperparams['score'] = score
                            print('*')

        print(hyperparams, best_score)

    print('Neural Network tunning \n')

    # tune_nn()

    def tune_dt():

        max_features = range(1, 4)
        min_sample_splits = range(2, 10)
        min_samples_leaf = range(1, 10)
        max_depth = range(1, 20)

        hyperparams = {
            'max_features': 0,
            'min_sample_splits': 0,
            'min_samples_leaf': 0,
            'max_depth': 0,
            'score': 0,
        }

        best_score = -1

        for features in max_features:
            for min in min_sample_splits:
                for cit in min_samples_leaf:
                    for depth in max_depth:
                        np.random.seed(5)
                        rf = DecisionTreeClassifier(
                            min_samples_leaf=cit, max_features=features,
                            max_depth=depth, min_samples_split=min, random_state=1
                        )
                        rf.fit(x, y)
                        score = rf.score(x_test, y_test)

                        print(features, ',', min, ',', cit, ',', depth, ',', score)
                        ab = abs(score - .5)
                        if ab > best_score:
                            best_score = ab
                            hyperparams['max_features'] = features
                            hyperparams['min_sample_splits'] = min
                            hyperparams['min_samples_leaf'] = cit
                            hyperparams['max_depth'] = depth
                            hyperparams['score'] = score
                            print('*')

        print(hyperparams, best_score)

    print('Decision Tree tunning \n')

    # tune_dt()

    def tune_knn():

        neigh = range(1, 10)
        weigh = ['uniform', 'distance']
        algo = ['auto', 'ball_tree', 'kd_tree', 'brute']
        leaf_s = range(1, 35)

        hyperparams = {
            'neigh': 0,
            'weigh': 0,
            'algo': 0,
            'leaf_s': 0,
            'score': 0
        }

        best_score = -1

        for n in neigh:
            for w in weigh:
                for a in algo:
                    for l in leaf_s:
                        np.random.seed(5)
                        knn = KNeighborsClassifier(n_neighbors=n, weights=w, algorithm=a, leaf_size=l)
                        knn.fit(x, y)
                        score = knn.score(x_test, y_test)

                        print(n, ',', w, ',', a, ',', l, ',', score)

                        ab = abs(score - .50)

                        if ab > best_score:
                            best_score = ab
                            hyperparams['neigh'] = n
                            hyperparams['weigh'] = w
                            hyperparams['algo'] = a
                            hyperparams['leaf_s'] = l
                            hyperparams['score'] = score
                            print('*')

        print(hyperparams, best_score)

    print('K-nearest neighbors tunning \n')

    # tune_knn()

    print('\n\n')

    def tune_lr():

        penalty = ['l1', 'l2']
        tol = [1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 1e-1, 1, 10]
        c = [1, .1, .01, 1.5, 2, 3, 4, .001, .005]
        weights = [None, 'balanced']
        solver = ['newton-cg', 'lbfgs', 'sag']
        solverl1 = ['liblinear', 'saga']
        fit = [True, False]

        hyperparams = {
            'penalty': 0,
            'tol': 0,
            'c': 0,
            'weights': 0,
            'solver': 0,
            'fit': 0,
            'score': 0
        }

        best_score = -1
        for f in fit:
            for i in penalty:
                for j in tol:
                    for k in c:
                        for l in weights:
                            if i == 'l1':
                                for h in solverl1:
                                    np.random.seed(5)
                                    lr = LogisticRegression(random_state=1, penalty=i,
                                                            tol=j, C=k, class_weight=l, solver=h, fit_intercept=f)
                                    lr.fit(x, y)
                                    score = lr.score(x_test, y_test)

                                    print(f, ',', i, ',', j, ',', k, ',', l, ',', h, ',', score)

                                    ab = abs(.5 - score)

                                    if ab > best_score:
                                        best_score = ab
                                        hyperparams['penalty'] = i
                                        hyperparams['tol'] = j
                                        hyperparams['c'] = k
                                        hyperparams['weights'] = l
                                        hyperparams['solver'] = h
                                        hyperparams['fit'] = f
                                        hyperparams['score'] = score
                                        print('*')

                            else:
                                for h in solver:
                                    np.random.seed(5)
                                    lr = LogisticRegression(random_state=1, penalty=i,
                                                            tol=j, C=k, class_weight=l, solver=h, fit_intercept=f)

                                    lr.fit(x, y)
                                    score = lr.score(x_test, y_test)

                                    print(f, ',', i, ',', j, ',', k, ',', l, ',', h, ',', score)

                                    ab = abs(.5 - score)
                                    if ab > best_score:
                                        best_score = ab
                                        hyperparams['penalty'] = i
                                        hyperparams['tol'] = j
                                        hyperparams['c'] = k
                                        hyperparams['weights'] = l
                                        hyperparams['solver'] = h
                                        hyperparams['fit'] = f
                                        hyperparams['score'] = score
                                        print('*')

        print(hyperparams, best_score)

    print('Linear Regression tunning \n')

    # tune_lr()

    print('\n\n')


def main():

    # load files
    traindata_file_path = trainpath
    testdata_file_path = testpath

    printSpacing = len("ensemble_5 unweighted accuracy")

    if len(sys.argv) == 3:
        traindata_file_path = sys.argv[1]
        testdata_file_path = sys.argv[2]

    traindata = np.loadtxt(traindata_file_path, delimiter=',', dtype=int, skiprows=1)
    testdata = np.loadtxt(testdata_file_path, delimiter=',', dtype=int, skiprows=1)

    # splitting into x,y sets
    x = traindata[:, :4]
    y = traindata[:, 4]

    x_test = testdata[:, :4]
    y_test = testdata[:, 4]

    np.random.seed(5)

    # models --

    print("Model Accuracies::\n")

    # AdaBoost.M1
    algos = ["SAMME", "SAMME.R"]
    learning_rates = [0.5, 1, 0.005, 0.001, 0.1]
    num_estimators = [10, 50, 100, 200, 1000]
    ada_dict = {'algorithm': algos, 'learning_rate': learning_rates, 'n_estimators': num_estimators}

    np.random.seed(5)
    ada = AdaBoostClassifier(DecisionTreeClassifier(max_depth=1, random_state=1), algorithm="SAMME.R"
                             , n_estimators=9, learning_rate=1, random_state=1)

    ada.fit(x, y)
    ada_score = ada.score(x_test, y_test)
    print("adaboost score".ljust(printSpacing), ": ", "{:>9}".format(ada_score))

    # random forest
    np.random.seed(5)
    rf = RandomForestClassifier(random_state=1, n_estimators=5, max_features=2, min_samples_split=3, criterion="gini"
                                , max_depth=5)
    rf.fit(x, y)
    rf_score = rf.score(x_test, y_test)
    print("random forest accuracy".ljust(printSpacing), ": ", "{:>9}".format(rf_score))

    # Neural Network
    np.random.seed(5)
    # nn = MLPClassifier(random_state=1, verbose=True, early_stopping=True, validation_fraction=0.1)
    nn = MLPClassifier(activation='tanh', solver='lbfgs', alpha=1, learning_rate='constant')
    nn.fit(x, y)
    nn_score = nn.score(x_test, y_test)
    print("neural network score".ljust(printSpacing), ": ", "{:>9}".format(nn_score))

    # K nearest neighbors
    np.random.seed(5)
    knn = KNeighborsClassifier(n_neighbors=5, weights='uniform', algorithm='auto', leaf_size=4)
    knn.fit(x, y)
    knn_score = knn.score(x_test, y_test)
    print("knn score".ljust(printSpacing), ": ", "{:>9}".format(knn_score))

    # Logistic Regression
    np.random.seed(5)
    lr = LogisticRegression(random_state=1, penalty='l1', tol=1e-06, C=1, class_weight=None
                            , solver='liblinear', fit_intercept=False)
    lr.fit(x, y)
    lr_score = lr.score(x_test, y_test)
    print("logistic regression score".ljust(printSpacing), ": ", "{:>9}".format(lr_score))

    # naive bayes
    np.random.seed(5)
    nb = GaussianNB()
    nb.fit(x, y)
    nb_score = nb.score(x_test, y_test)
    print("naive bayes score".ljust(printSpacing), ": ", "{:>9}".format(nb_score))

    # decision trees
    np.random.seed(5)
    dt = DecisionTreeClassifier(presort=True, random_state=1,
                                max_features=1,
                                min_samples_split=2, min_samples_leaf=7, max_depth=7)
    dt.fit(x, y)
    dt_score = dt.score(x_test, y_test)
    print("decision trees".ljust(printSpacing), ": ", "{:>9}".format(dt_score))

    # establish weights
    ensemble_weights = [ada_score, rf_score, nn_score, knn_score, lr_score, nb_score, dt_score]
    simple_weights = [1, 1, 1, 1, 1, 1, 1]

    def ensemble(nnp, knnp, lrp, nbp, dtp, adap=None, rfp=None, weights=ensemble_weights):

        w0 = weights[0]
        w1 = weights[1]
        w2 = weights[2]
        w3 = weights[3]
        w4 = weights[4]
        w5 = weights[5]
        w6 = weights[6]

        prediction = []
        for index in range(len(nnp)):
            count = 0
            if adap is not None:
                if adap[index] == 0:
                    count += w0
                else:
                    count -= w0
            if rfp is not None:
                if rfp[index] == 0:
                    count += w1
                else:
                    count -= w1
            if nnp[index] == 0:
                count += w2
            else:
                count -= w2
            if knnp[index] == 0:
                count += w3
            else:
                count -= w3
            if lrp[index] == 0:
                count += w4
            else:
                count -= w4
            if nbp[index] == 0:
                count += w5
            else:
                count -= w5
            if dtp[index] == 0:
                count += w6
            else:
                count -= w6
            if count > 0:
                prediction.append(0)
            else:
                prediction.append(1)
        return prediction

    def accuracy(guess, truth):
        correct = 0
        for index in range(len(guess)):
            if guess[index] == truth[index]:
                correct += 1
        return correct/len(guess)


    # predictions
    adap = ada.predict(x_test)
    rfp = rf.predict(x_test)
    nnp = nn.predict(x_test)
    knnp = knn.predict(x_test)
    lrp = lr.predict(x_test)
    nbp = nb.predict(x_test)
    dtp = dt.predict(x_test)

    # base model matrices
    ada_confusion = confusion_matrix(y_test, adap)
    rf_confusion = confusion_matrix(y_test, rfp)
    nn_confusion = confusion_matrix(y_test, nnp)
    knn_confusion = confusion_matrix(y_test, knnp)
    lr_confusion = confusion_matrix(y_test, lrp)
    nb_confusion = confusion_matrix(y_test, nbp)
    dt_confusion = confusion_matrix(y_test, dtp)


    # Ensemble

    # unweighted marked with 'u', 5 model ensemble is marked with '5', 7 model unmarked

    eu_guess = ensemble(nnp, knnp, lrp, nbp, dtp, adap, rfp, weights=simple_weights)
    eu_score = accuracy(eu_guess, y_test)

    eu5_guess = ensemble(nnp, knnp, lrp, nbp, dtp, weights=simple_weights)
    eu5_score = accuracy(eu5_guess, y_test)

    e_guess = ensemble(nnp, knnp, lrp, nbp, dtp, adap, rfp)
    e_score = accuracy(e_guess, y_test)

    e5_guess = ensemble(nnp, knnp, lrp, nbp, dtp)
    e5_score = accuracy(e5_guess, y_test)

    print("ensemble_5 unweighted accuracy".ljust(printSpacing), ": ", str(eu5_score).rjust(9))
    print("ensemble_5 accuracy".ljust(printSpacing), ": ", str(e5_score).rjust(9))

    print("ensemble unweighted accuracy".ljust(printSpacing), ": ", str(eu_score).rjust(9))
    print("ensemble accuracy".ljust(printSpacing), ": ", str(e_score).rjust(9))

    print("\nConfusion Matrices::\n")

    # confusion matricies
    e_confusion = confusion_matrix(y_test, e_guess)
    e5_confusion = confusion_matrix(y_test, e5_guess)
    eu_confusion = confusion_matrix(y_test, eu_guess)
    eu5_confusion = confusion_matrix(y_test, eu5_guess)


    print("adaboost.m1 matrix:\n", ada_confusion)
    print("random forrest matrix:\n", rf_confusion)
    print("neural network matrix:\n", nn_confusion)
    print("k nearest neighbor matrix:\n", knn_confusion)
    print("logistic regression matrix:\n", lr_confusion)
    print("naive bayes matrix:\n", nb_confusion)
    print("decision tree matrix:\n", dt_confusion)
    print("Ensemble_u5 matrix:\n", eu5_confusion)
    print("Ensemble_5 matrix:\n", e5_confusion)
    print("Ensemble_u matrix:\n", eu_confusion)
    print("Ensemble matrix:\n", e_confusion)


if __name__ == "__main__":
    #tune()
    # print('\n\n\n')
     main()
