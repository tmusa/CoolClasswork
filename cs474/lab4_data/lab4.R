# install statements


# install.packages('caret', dependencies = TRUE)
# install.packages('dplyr', dependencies = TRUE)
# install.packages('plyr', dependencies = TRUE)
# install.packages('recipes', dependencies = TRUE)
# install.packages('e1071', dependencies = TRUE)
# install.packages('adabag', dependencies = TRUE)


# load in packages
library(caret)
library(plyr)
library(dplyr)
library(recipes)
library(e1071)
library(adabag)	

# load in Blood Transfusion Service Center dataset

print("Choose the training file: ")
f_train <- file.choose()
sprintf("You Choose: %s", f_train)

print("Choose the testing file: ")
f_test <- file.choose()
sprintf("You Choose: %s", f_test)

btsc_train <- read.csv(f_train, header = TRUE, sep = ",")
btsc_test <- read.csv(f_test, header= TRUE, sep = ",")


# training constants    ###########################[params]
# Zhu         1        72      0.7607839  0.1812374
# mtry = 4, splitrule = extratrees, and min.node.size = 50.

m_ada <- 'AdaBoost.M1'
m_forrest <- 'ranger'

fitControl <- trainControl(method = "cv",
                           number = 10, search = "random")

fitControl2 <- trainControl(method = "cv",
                           number = 10)

# seed
set.seed(825)

# accuracy metric 
metric <- "Accuracy"

# Split data into x-attributes & class    [training]
y = btsc_train[,5]
x = btsc_train[, 1:4]

# Split data into x-attributes & class    [testing]
x_test = btsc_test[,1:4]
y_test = btsc_test[,5]

# define the grid
rf_grid <- expand.grid(mtry = c(3, 4),
                      splitrule = c("gini", "extratrees"),
                      min.node.size = c(25, 35, 40, 50, 60, 70, 80))

rf_best <- expand.grid(mtry = c(4), splitrule = c("extratrees"), 
                       min.node.size= c(50) )

# Preprocess the data to center and scale the values 
x_tr_p <- preProcess(x, method=c( "center", "scale"))
trans <- predict(x_tr_p, x)

# Train my models 
set.seed(825)
rf_fit <- train(trans, as.factor(y), method = m_forrest, 
		trControl = fitControl2, metric=metric, tuneGrid = rf_best)

print("Finished training the random forest")

set.seed(825)
ada_fit <- train(x, as.factor(y), method = m_ada, metric=metric, 
				trControl = fitControl, tuneLength = 3 )

print("Finished training the AdaBoost.M1")

rf_fit
ada_fit





# #################################################

rec_cls <- recipe(Class ~ ., data = btsc_train) %>%
  step_center(all_predictors()) %>%
  step_scale(all_predictors())

grid <- expand.grid(mfinal = (1:3)*3, maxdepth = c(1, 3),
                    coeflearn = c("Breiman", "Freund", "Zhu"))

seeds <- vector(mode = "list", length = nrow(btsc_train) + 1)
seeds <- lapply(seeds, function(x) 1:20)

cctrl1 <- trainControl(method = "cv", number = 3, returnResamp = "all",
                       classProbs = TRUE, 
                       summaryFunction = twoClassSummary, 
                       seeds = seeds)
cctrl2 <- trainControl(method = "LOOCV",
                       classProbs = TRUE, 
                       summaryFunction = twoClassSummary, 
                       seeds = seeds)
cctrl3 <- trainControl(method = "none",
                       classProbs = TRUE, 
                       summaryFunction = twoClassSummary,
                       seeds = seeds)
cctrlR <- trainControl(method = "cv", number = 3, returnResamp = "all", search = "random")

set.seed(849)
test_class_cv_model <- train(x, as.factor(y), 
                             method = "AdaBoost.M1", 
                             trControl = cctrl1,
                             tuneGrid = grid,
                             metric = "ROC", 
                             preProc = c("center", "scale"))


####################################################

model <- "AdaBoost.M1"



#########################################################################

set.seed(2)
training <- twoClassSim(50, linearVars = 2)
testing <- twoClassSim(500, linearVars = 2)
trainX <- training[, -ncol(training)]
trainY <- training$Class

rec_cls <- recipe(Class ~ ., data = btsc_train) %>%
  step_center(all_predictors()) %>%
  step_scale(all_predictors())

grid <- expand.grid(mfinal = (1:3)*3, maxdepth = c(1, 3),
                    coeflearn = c("Breiman", "Freund", "Zhu"))

seeds <- vector(mode = "list", length = nrow(training) + 1)
seeds <- lapply(seeds, function(x) 1:20)

cctrl1 <- trainControl(method = "cv", number = 3, returnResamp = "all",
                       classProbs = TRUE, 
                       summaryFunction = twoClassSummary, 
                       seeds = seeds)
cctrl2 <- trainControl(method = "LOOCV",
                       classProbs = TRUE, 
                       summaryFunction = twoClassSummary, 
                       seeds = seeds)
cctrl3 <- trainControl(method = "none",
                       classProbs = TRUE, 
                       summaryFunction = twoClassSummary,
                       seeds = seeds)
cctrlR <- trainControl(method = "cv", number = 3, returnResamp = "all", search = "random")

set.seed(849)
test_class_cv_model <- train(trainX, trainY, 
                             method = "AdaBoost.M1", 
                             trControl = cctrl1,
                             tuneGrid = grid,
                             metric = "ROC", 
                             preProc = c("center", "scale"))

set.seed(849)
test_class_cv_form <- train(Class ~ ., data = training, 
                            method = "AdaBoost.M1",
                            tuneGrid = grid, 
                            trControl = cctrl1,
                            metric = "ROC", 
                            preProc = c("center", "scale"))

test_class_pred <- predict(test_class_cv_model, testing[, -ncol(testing)])
test_class_prob <- predict(test_class_cv_model, testing[, -ncol(testing)], type = "prob")
test_class_pred_form <- predict(test_class_cv_form, testing[, -ncol(testing)])
test_class_prob_form <- predict(test_class_cv_form, testing[, -ncol(testing)], type = "prob")

set.seed(849)
test_class_rand <- train(trainX, trainY, 
                         method = "AdaBoost.M1", 
                         trControl = cctrlR,
                         tuneLength = 4,
                         preProc = c("center", "scale"))

set.seed(849)
test_class_loo_model <- train(trainX, trainY, 
                              method = "AdaBoost.M1", 
                              trControl = cctrl2,
                              tuneGrid = grid,
                              metric = "ROC", 
                              preProc = c("center", "scale"))

set.seed(849)
test_class_none_model <- train(trainX, trainY, 
                               method = "AdaBoost.M1", 
                               trControl = cctrl3,
                               tuneGrid = data.frame(mfinal = 10, 
                                                     maxdepth = 1,
                                                     coeflearn = "Zhu"),
                               metric = "ROC", 
                               preProc = c("center", "scale"))

test_class_none_pred <- predict(test_class_none_model, testing[, -ncol(testing)])
test_class_none_prob <- predict(test_class_none_model, testing[, -ncol(testing)], type = "prob")

set.seed(849)
test_class_rec <- train(x = rec_cls,
                        data = btsc_train,
                        method = "AdaBoost.M1", 
                        trControl = cctrl1,
                        tuneGrid = grid,
metric = "ROC")

