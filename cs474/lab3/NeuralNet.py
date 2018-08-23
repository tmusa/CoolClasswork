from numpy import loadtxt
from numpy import delete
from numpy import asarray
from numpy import random as rand
from numpy import expand_dims
from keras.models import Sequential
from keras.layers import Dense,Conv1D, MaxPooling1D, Dropout, Flatten
from keras.utils import np_utils
from keras import backend as K
from keras import losses as L
from keras import optimizers as Opt
from keras.callbacks import EarlyStopping
from sklearn.metrics import confusion_matrix
# from evolutionary_search import maximize

rand.seed(7)



relu_o_tanh = 'relu'


# parses the dataset into numpy arrays
train_data = loadtxt('optdigits_tra.csv', delimiter=',', dtype=int) #(1797, 65)
test_data = loadtxt('optdigits_tes.csv', delimiter=',', dtype=int) #(3823, 65)


def class_split(xarr, yarr, data):
    y = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    for i in range(0, 10):
        Xres = []
        Yres = []
        for line in data:
            if line[len(line) - 1] == i:
                Xres.append(line[:-1])
                b = list(y)
                b[i] = 1
                Yres.append(b)
        xarr.append(asarray(Xres))
        temp = asarray(Yres)
        yarr.append(temp)


# Create class splits
a = test_data.tolist()
b = train_data.tolist()
x_test_classes = []
y_test_classes = []
x_train_classes = []
y_train_clasees = []

class_split(x_test_classes, y_test_classes, a)
class_split(x_train_classes, y_train_clasees, b)

# vector of class column from raw train data
y_train = train_data[:, 64]
y_train_true = y_train

# numpy array 1-c encoding for train class column
y_train = np_utils.to_categorical(y_train, 10)

# numpy array for train data minus class column
x_train = delete(train_data, 64, axis=1)

# vector of class column from raw test data
y_test = test_data[:, 64]
y_test_true = y_test

# numpy array 1-c encoding for test class column
y_test = np_utils.to_categorical(y_test, 10)

# numpy array for test data minus class column
x_test = delete(test_data, 64, axis=1)


# Early stopping configured: stop training when there is no improvement
stop = EarlyStopping(monitor='val_loss',
                              min_delta=0,
                              patience=0,
                              verbose=0, mode='auto')


def dense_input_layer(nmodel, nodes):
    nmodel.add(Dense(nodes, activation=relu_o_tanh, input_shape=(64,)))


def dense_output_layer(nmodel):
    nmodel.add(Dense(10, activation='softmax'))


def dense_layer(nmodel, nodes):
    nmodel.add(Dense(nodes, activation=relu_o_tanh))


def create_hidden_layers(nmodel, num_layers, layerfunc, first_size, decay):
    for i in range(0, num_layers):
        if first_size*decay > 10:
            first_size = first_size*decay
        else:
            first_size = 10
        layerfunc(nmodel, int(first_size))


def create_ff_model(nlayers, first, dec):
    md = Sequential()
    dense_input_layer(md, first)
    create_hidden_layers(nmodel=md, num_layers=nlayers-1, layerfunc=dense_layer, first_size=first, decay=dec)
    dense_output_layer(md)
    return md


def sum_squared_error(y_true, y_pred):
    return K.sum(K.square(y_pred - y_true), axis=-1)


# mean squared error = 'mean_squared_error' cross entropy error = 'categorical_crossentropy'
# configures training of the model
def configure_training(nmodel, loss_index, learn):
    losses = [L.categorical_crossentropy, sum_squared_error, L.mean_squared_error]
    nmodel.compile(optimizer=Opt.rmsprop(lr=learn),
                   loss=losses[loss_index],
                   metrics=['accuracy'])


# print class accuracy for  test
def print_class_accuracy(x, y, statement, mod, cnn=False):
    rand.seed(7)
    print(statement)
    for j in range(0, 10):
        if cnn:
            s = mod.evaluate(expand_dims(x[j], axis=2), y[j], verbose=0)
            print(str(j).ljust(5), s[1])
        else:
            s = mod.evaluate(x[j], y[j], verbose=0)
            print(str(j).ljust(5), s[1])



def print_train_report(ts, cmt, mod, cnn=False):
    print(line)
    print('Train Accuracy: %.2f%% \n' % (ts[1] * 100))
    print_class_accuracy(x_test_classes, y_test_classes, 'Train Class Accuracy: \n\nClass| Accuracy', mod, cnn=cnn)
    print('\nTrain Confusion Matrix', '\n', cmt)


def print_test_report(ts, cmt, mod, cnn=False):
    print(line)
    print('Test Accuracy: %.2f%% \n' % (ts[1] * 100))
    print_class_accuracy(x_test_classes, y_test_classes, 'Test Class Accuracy: \n\nClass | Accuracy', mod, cnn=cnn)
    print('\nTest Confusion Matrix', '\n', cmt)


def print_report(test_s, train_s, cm_test, cm_train, mod, cnn=False):
    print_train_report(train_s, cm_train, mod, cnn)
    print_test_report(test_s, cm_test, mod, cnn)


def run_model(model):
    # model = create_ff_model(nlayers=_nl, first=_fir, dec=0.5)
    # # configures training model for crossentropy and sgd
    # configure_training(model, loss_index=_loss_index, learn=l)
    # # train model
    # model.fit(x_train, y_train, epochs=10, batch_size=32, callbacks=[stop], verbose=0, validation_split=0.2)
    # evaluate model
    test_scores = model.evaluate(x_test, y_test, verbose=0)
    train_scores = model.evaluate(x_train, y_train, verbose=0)

    # retrieve the test predictions
    testpred = asarray(model.predict(x_test)).argmax(axis=1)
    trainpred = asarray(model.predict(x_train)).argmax(axis=1)

    # create confusion matrix
    cmtest = confusion_matrix(y_test_true, testpred)
    cmtrain = confusion_matrix(y_train_true, trainpred)

    print_report(test_scores, train_scores, cmtest, cmtrain, model)


def tbd_max_func(_nlayers, _first, l, _dec=0.5, _loss_index=0):
    rand.seed(7)
    m = create_ff_model(nlayers=_nlayers, first=_first, dec=_dec)
    configure_training(m, loss_index=_loss_index, learn=l)
    m.fit(x_train, y_train, epochs=10, batch_size=32, callbacks=[stop], verbose=0, validation_split=0.2)
    score = m.evaluate(x_test, y_test, verbose=0)
    # print(score)
    h = [score[1], m]
    return h


# change lr to modify the learning rate
# change the f parameter to determine the number of nodes in the first hidden layer
# (decreases by half each consecutive layer to a minimum of 10)
# change nl to play with the number of hidden layer (nl+1 is the number of hidden layers)
def best_hparams(li):
    nl = [1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3]
    f = [34, 64, 265, 512, 34, 64, 265, 512, 34, 64, 265, 512]
    lr = [0.001, 0.0025, 0.005, 0.01]
    best_score= -1;
    best_modle = [];
    for i in range(0, 12):
        v1 = tbd_max_func(_nlayers=nl[i], _first=f[i], _loss_index=li, l=lr[0])
        v2 = tbd_max_func(_nlayers=nl[i], _first=f[i], _loss_index=li, l=lr[1])
        v3 = tbd_max_func(_nlayers=nl[i], _first=f[i], _loss_index=li, l=lr[2])
        v4 = tbd_max_func(_nlayers=nl[i], _first=f[i], _loss_index=li, l=lr[3])
        if v1[0] > best_score:
            best_score = v1[0]
            best_modle = [v1[1], i, lr[0]]
        if v2[0] > best_score:
            best_score = v2[0]
            best_modle = [v2[1], i, lr[1]]
        if v3[0] > best_score:
            best_score = v3[0]
            best_modle = [v3[1], i, lr[2]]
        if v4[0] > best_score:
            best_score = v4[0]
            best_modle = [v4[1], i, lr[3]]
        print(nl[i], ',', f[i], ',', lr[0], ',', v1[0])
        print(nl[i], ',', f[i], ',', lr[1], ',', v2[0])
        print(nl[i], ',', f[i], ',', lr[2], ',', v3[0])
        print(nl[i], ',', f[i], ',', lr[3], ',', v4[0])

    print('Printing Best Model data for section.....')
    run_model(best_modle[0])
    print(best_modle, " \n")


line = '------------------------------------------'

FIND_PARAMS = True
if FIND_PARAMS:
    # 1 A
    print('Cross entropy for RELU')
    best_hparams(0)
    print('Sum of Squares for RELU')
    best_hparams(1)
    # 1 B
    relu_o_tanh = 'tanh'
    print('Cross entropy for TANH')
    best_hparams(0)


relu_o_tanh = 'relu'

print(x_test.shape)
x_test = expand_dims(x_test, axis=2)
x_train = expand_dims(x_train, axis=2)
print(x_test.shape)


def cnn(_numlayers_sets=1, _filter_size=32, _kernel_size=3, _dropout_rate=0.25, _pool_size=2,
        _batch_size=32, _epoch=10, print=False):
    rand.seed(7)
    model = Sequential()

    model.add(Conv1D(_filter_size, _kernel_size, activation='relu', input_shape=(64, 1)))

    while _numlayers_sets > 0:
        model.add(Conv1D(_filter_size, _kernel_size, activation='relu'))
        model.add(MaxPooling1D(pool_size=_pool_size))
        model.add(Dropout(_dropout_rate))
        _numlayers_sets = _numlayers_sets - 1

    model.add(Flatten())
    model.add(Dense(10, activation='softmax'))

    model.compile(optimizer='adam', loss=L.categorical_crossentropy, metrics=['accuracy'])
    model.fit(x_train, y_train, epochs=_epoch, batch_size=_batch_size, callbacks=[stop], verbose=0, validation_split=0.2)
    scores = model.evaluate(x_test, y_test, verbose=0)
    if print:
        test_scores = model.evaluate(x_test, y_test, verbose=0)
        train_scores = model.evaluate(x_train, y_train, verbose=0)

        # retrieve the test predictions
        test_pred = asarray(model.predict(x_test)).argmax(axis=1)
        train_pred = asarray(model.predict(x_train)).argmax(axis=1)

        # create confusion matrix
        cm_test = confusion_matrix(y_test_true, test_pred)
        cm_train = confusion_matrix(y_train_true, train_pred)

        print_report(test_scores, train_scores, cm_test, cm_train, model, cnn=True)
    return [scores[1], model]


def print_cnn(nmodel):
    test_scores = nmodel.evaluate(x_test, y_test, verbose=0)
    train_scores = nmodel.evaluate(x_train, y_train, verbose=0)

    # retrieve the test predictions
    test_pred = asarray(nmodel.predict(x_test)).argmax(axis=1)
    train_pred = asarray(nmodel.predict(x_train)).argmax(axis=1)

    # create confusion matrix
    cm_test = confusion_matrix(y_test_true, test_pred)
    cm_train = confusion_matrix(y_train_true, train_pred)

    print_report(test_scores, train_scores, cm_test, cm_train, nmodel, cnn=True)


def do_cnn():
    layer_sets = [0, 0, 0, 1, 1, 1,  2, 2, 2]
    filter_sizes = [16, 32, 64, 16, 32, 64,  16, 32, 64]
    batch_sizes = [8, 64, 128, 8, 64, 128, 8, 64, 128]
    epochs = [5, 20, 30, 5, 20, 30, 5, 20, 30]
    best_score = -1;
    best_modle = [];
    for i in range(0, 9):

        v1 = cnn(_numlayers_sets=layer_sets[i], _filter_size=filter_sizes[i])
        v2 = cnn(_numlayers_sets=layer_sets[i], _filter_size=filter_sizes[i], _batch_size=batch_sizes[i])
        v3 = cnn(_numlayers_sets=layer_sets[i], _filter_size=filter_sizes[i],
                 _batch_size=batch_sizes[i], _epoch=epochs[i])
        if v1[0] > best_score:
            best_score = v1[0]
            best_modle = [v1[1], i, "v1"]
        if v2[0] > best_score:
            best_score = v2[0]
            best_modle = [v2[1], i, "v2"]
        if v2[0] > best_score:
            best_score = v2[0]
            best_modle = [v2[1], i, "v3"]

        print(layer_sets[i], ',', filter_sizes[i], ',', 32, ',', 10, ',', v1[0])
        print(layer_sets[i], ',', filter_sizes[i], ',', batch_sizes[i], ',', 10, ',', v2[0])
        print(layer_sets[i], ',', filter_sizes[i], ',', batch_sizes[i], ',', epochs[i], ',', v3[0])

    print('Best model for CNN')
    print_cnn(best_modle[0])
    print(best_modle)


if FIND_PARAMS:
    print('Printing CNN model')
    do_cnn()
