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

%% construct W matrix, covariance matrix, and find eigenvals/vects
w = ones(size(x_train,2),size(x_train,1));
datamean = 0;
for j = 1:size(w,2)
        datamean = datamean + x_train(j,:);
end
datamean = datamean/size(w,2);
for i = 1:size(w,2)
    w(:,i) = x_train(i,:)-datamean;
end

cov = w*w';

[evect, evals] = eig(cov);

%diagnostic code used in writing of this script
%testpi = createpidist(10, evect);

% tempsum = 0;
% for i = 1:size(testpi)
%     tempsum = tempsum + testpi(i);
% end
%testsamp = samplepi(20, testpi);

%testsampledata = datagen(testsamp, x_train);

%% main execution section- for the range of top k eigenvectors to try, try
%  sampling 1-20 features from the corresponding pi distribution- for each
%  eigenvector/sampling pair, take the average of 100 trials to better 
%  capture accuracy of this technique

accuracyArr = zeros(10,20,2);
for k = 1:10
    pidist = createpidist(k, evect);
    for s = 1:20
        trialavg_tr = 0;
        trialavg_va = 0;
        for i = 1:100
            samplecols = samplepi(s, pidist);
            sampletrain = datagen(samplecols, x_train);
            sampletest = datagen(samplecols, x_test);
            [tr_inc, va_inc, w_arr, b_offset] = descendGradient(1000, sampletrain, sampletest, y_train, y_test, 1500);
            trialavg_tr = trialavg_tr + tr_inc;
            trialavg_va = trialavg_va + va_inc;
        end
        accuracyArr(k,s,1) = 1- trialavg_tr/(104*100);
        accuracyArr(k,s,2) = 1- trialavg_va/(52*100);
    end
end


%% grab the data corresponding to the selected features
function sampledata = datagen(samplecols, x_data)
    sampledata = zeros(size(x_data,1), size(samplecols,1));
    for i = 1:size(samplecols,1)
        sampledata(:,i) = x_data(:,samplecols(i));
    end
end

%% sample features based on generated pi distribution
function samplecols = samplepi(s, pidist)
    %generate cumulative distribution based on pi probability distribution
    picdf = zeros(size(pidist,1),1);
    runningsum = 0;
    for i = 1:size(pidist)
        runningsum = runningsum + pidist(i);
        picdf(i) = runningsum;
    end

    %create list of features to be sampled- generate a random value and add
    %to list if value falls in corresponding range
    samplefeat = zeros(s,1);
    for i = 1:s
        randval = rand;
        %disp(randval)
        for j = 1:size(picdf)
            if(randval >= picdf(j))
                if(randval < picdf(j+1))
                    samplefeat(i) = j;
                end
            end
        end
    end
    %if a feature is selected multiple times, include it only once
    samplecols = unique(samplefeat); 
    %correct any indexing errors
    for q = 1:size(samplecols,1)
        if(samplecols(q) == 0)
            samplecols(q) = 1;
        end
    end
end

%% create a probability distribution that maps a given feature's importance
%  based off the magnitude of the corresponding element across k eigenvectors
%  to a probability, allowing features with more magnitude across all
%  eigenvectors to be more likely to be sampled
function pidist = createpidist(k, evect)
    pidist = zeros(size(evect,2),1);
    evect_k = evect(:, size(evect,1)-k+1:size(evect,1));
    for j = 1:size(evect_k,1)
        pij = 0;
        for i = 1:k
            pij = pij + evect_k(j,i)^2;
        end
        pidist(j) = pij/k;
    end
end

%% SVM
function [incorrect_train, incorrect_test, w, b] = descendGradient(c, x_train, x_test, y_train, y_test, max_iter)
    %set initial conditions for gradient descent
    w = zeros(1,size(x_train,2));
    b = 0;
    gamma = 0.1;
    
    %initialize relevant variables
    epoch = 1;
    numData = size(x_train,1);
    tol = 0.01;
    dfdw = inf;
    dfdb = inf;

    %while gradient descent not close enough to minima and under iteration
    %cap, keep descending the gradient
    while((abs(norm(dfdw)) > tol && abs(dfdb) > tol) && epoch <= max_iter)
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
                %disp(i)
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
                %disp(i)
            end
        end
        fprintf("# of misclassified test points: ")
        disp(incorrect_test)
end