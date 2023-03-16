%% Read in and Preprocess Data
wholedata_train = importdata("C:\Users\benne\Google Drive\Laptop Sync\UTD\Old\Fall 2022\CS 4375\Homeworks\Problem Set 4\sonar_train.data");
wholedata_validate = importdata("C:\Users\benne\Google Drive\Laptop Sync\UTD\Old\Fall 2022\CS 4375\Homeworks\Problem Set 4\sonar_valid.data"); 
wholedata_test = importdata("C:\Users\benne\Google Drive\Laptop Sync\UTD\Old\Fall 2022\CS 4375\Homeworks\Problem Set 4\sonar_test.data");

x_train = wholedata_train(:, 1:60);
y_train = wholedata_train(:, 61);

x_valid = wholedata_validate(:, 1:60);
y_valid = wholedata_validate(:, 61);

x_test = wholedata_test(:, 1:60);
y_test = wholedata_test(:, 61);

for i = 1:size(wholedata_train,1)
    if(y_train(i) == 2)
        y_train(i) = -1;
    end
end

for i = 1:size(wholedata_validate,1)
    if(y_valid(i) == 2)
        y_valid(i) = -1;
    end
end

for i = 1:size(wholedata_test,1)
    if(y_test(i) == 2)
        y_test(i) = -1;
    end
end


%% construct W matrix for covariance matrix calculation
W = ones(size(x_train,2),size(x_train,1));
datamean = 0;
for j = 1:size(W,2)
        datamean = datamean + x_train(j,:);
end
datamean = datamean/size(W,2);
for i = 1:size(W,2)
    W(:,i) = x_train(i,:)-datamean;
end

cov = W*W';

%compute eigenvectors and eigenvalues
[evect, evals] = eig(cov);

%for curiosity/experimentation, find total eigenvalue mass to be able to
%calculate how much of the variance can be explained by a direction given 
%by an eigenvector, not used/relevant to script functioning
esum = 0;
for i = 1:size(evals,1)
    esum = esum + evals(i,i);
end

%% Main Execution Body

%initialize range of values for slack 
C = [1, 10, 10^2, 10^3];
rankArr = [1, 2, 3, 4, 5, 6];
accuracyArr = zeros(24,4);

cSlack = 1;
rank = 1;
for i = 1:24
    %controls combination of values
    if(mod(i-1,6) == 0 && i ~= 1)
        cSlack = cSlack + 1;
        rank = rank - 6;
    end

    %reduce the dimensionality of the data according to the rank argument
    reducedDim = projectData(rank, evect, evals);
    projectedData_train = x_train * reducedDim;
    projectedData_valid = x_valid * reducedDim;

    %learn a classifier for the projected data
    [itrain, itest, w, b] = descendGradient(C(cSlack), projectedData_train, projectedData_valid, y_train, y_valid, 30000);

    %store the results on the training and test data along with input
    %parameters
    accuracyArr(i,1) = 1 - itrain / size(x_train, 1); 
    accuracyArr(i,2) = 1 - itest / size(x_valid, 1); 
    accuracyArr(i,3) = C(cSlack);
    accuracyArr(i,4) = rank;

    %increment the rank
    rank = rank + 1;
end

%using the results from the above trials, run again using the optimal
%hyperparameters
testdim = projectData(6, evect, evals);
projectedData_train = x_train * testdim;
projectedData_test = x_test * testdim;
[inctr, inctst, testw, testb] = descendGradient(C(3), projectedData_train, projectedData_test, y_train, y_test, 30000);

%% Function Definitions

%projects data down to the specified dimension
function projected = projectData(rankproj, evect, ~)
    projected = evect(:,size(evect,1)-rankproj:size(evect,1));
end

% Support Vector Machine allowing for Slack calculated via Gradient Descent
function [incorrect_train, incorrect_test, w, b] = descendGradient(c, x_train, x_test, y_train, y_test, max_iter)
    %set initial conditions for gradient descent
    w = zeros(1,size(x_train,2));
    b = 0;
    gamma = 0.1;
    
    %initialize relevant variables
    epoch = 1;
    numData = size(x_train,1);
    tol = 0.1;
    dfdw = inf;
    dfdb = inf;
    
    %begin descending the gradient
    while((abs(norm(dfdw)) > tol || abs(dfdb) > tol) && epoch <= max_iter)
        wSum = 0;
        bSum = 0;
        for i = 1:numData
            hyperPlane = dot(w,x_train(i,:)) + b;
            indicator = (0.5* dot(w,w)) + c*(1-y_train(i)*(hyperPlane)); 
            if (indicator >= 0)
                indicator = 1;
            else
                indicator = 0;
            end
            if(indicator > 0)
                wSum = wSum + y_train(i)*x_train(i,:)*indicator;
                bSum = bSum + y_train(i)*indicator;
            end
        end
        
        dfdw = (1/numData)*wSum;
        dfdb = (1/numData)*bSum;
        
        w = w + dfdw*gamma;
        b = b + dfdb*gamma;
        
        epoch = epoch + 1;
    end
    
    fprintf("C = %d\nIterations = %i\n", c, epoch-1)

    %check classifier performance on the training data
    incorrect_train = 0;
        for i = 1:numData
        classify = sign(dot(w,x_train(i,:))+b);
            if (y_train(i) ~= classify || classify == 0)
                incorrect_train = incorrect_train + 1;
            end
        end
        fprintf("# of misclassified train points: ")
        disp(incorrect_train)
        
    %check classifier performance on the test data
    incorrect_test = 0;
        for i = 1:size(x_test,1)
        classify = sign(dot(w,x_test(i,:))+b);
            if (y_test(i) ~= classify || classify == 0)
                incorrect_test = incorrect_test + 1;
            end
        end
        fprintf("# of misclassified test points: ")
        disp(incorrect_test)
end