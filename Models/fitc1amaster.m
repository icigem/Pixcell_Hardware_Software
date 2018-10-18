%Fit the time series data from the master datasheet on Construct 1
%Returns fitted parameters, and the residual matrix
function [params,Rsd] = fitc1amaster()
    close all
    %Need to load the required data
    c1aMasDat = load('c1aMasDat.mat');
    dat = c1aMasDat.c1amaster;
    %Extract the desired data from the master spreadsheet
    datastruct = unpackMaster(dat);
    %Convert time data into seconds
    t = datastruct.t*60;
    
    data = datastruct.results;
    %Fit only Pyocyanin concentrations up to 5 microMolar (beyond this
    %point, cells become unhealthy and unpredictable by this model)
    data = data(:,1:end-4,:,:);
   
    
    %Expected orders of magnitude for fitted parameters. Taking this order
    %outside the fitting function normalises the function gradient across the parameters, speeding up
    %descent
    order = [0, 5, 0,1, -2,-2,3];
    
    
    %Global constants
    Avo = 6.02214e23;
    Vcell = 2e-6*pi*(0.5e-6)^2;
    %Convert concentration to number of molecules
    function num = Ccon(conc)
        num = conc*Avo*Vcell;
    end
    %Convert dissociation coefficients into molecule numbers
    Kox = Ccon(0.045e-6);
    Kred = Ccon(0.011e-6);
    %Define soxR cooperativity as 1.6 (from tetR)
    n = 1.6;
    psoxr = 5.8e-3;
    rbsGFP = 0.11;
    dmGFP = 0.139/60;
    nrep = 15;
    %Define bounds for parameter fitting
    lb = [0,0,1,0,0,0,0];
    ub = [3,inf,inf,inf,10,10,inf];
    %Plot norm of residuals at each iteration, decrease the function
    %tolerance by 10^2
    options = optimset('PlotFcns', @optimplotresnorm,'TolFun',1e-8);
    %Call curve fitter
    [params,Rsdnrm,Rsd,ExFlg,OptmInfo,Lmda,Jmat] = lsqcurvefit(@modelFn,rand(7,1),t,data,lb,ub,options);
    
    %Denormalise parameters to restore their orders of magnitude
    params = params.*(10.^order');
    Scale = params(1)
    K = params(2)
    m = params(3)
    Basal = params(4)
    dGFP = params(5:6)'
    delay = params(7)
    
    

    
    %Model function to be fitted
    function mGFP = modelFn(params,t)   %NOTE mGFP here is model GFP,not GPF mRNA!
        %Vector of analysed pyocyanin concentrations
        pyo = [0.0000 ,0.0100 ,0.0250, 0.0500, 0.1000, 0.2500, 0.5000, 1.0000, 2.5000, 5.0000];
        %ID labels for tagged and untagged constructs
        degid = [1,2];
        Scale = params(1);
        ScaleO = order(1);
        K = params(2);
        KO = order(2);
        m = params(3);
        mO = order(3);
        Basal = params(4);
        BasalO = order(4);
        dGFP = params(5:6)';
        dGFPO = order(5:6)';
        delay = params(7);
        delayO = order(7);
        
        
    %Define 0 initial GFP and GFP mRNA
    init = [0,0];
    %Governing ODEs
    function [dvdt] = odes(dat, params, pyo,degid)
                %Calculate oxidised and reduced soxR
                soxrr = 75*nrep./(1 + (K*10^KO)*pyo^(m*10^mO));
                soxro = 75*nrep - soxrr; %soxro
                
                dvdt = [nrep*psoxr./(1 + (soxro./Kox).^-n + (soxrr./Kred)^n) - dat(1)*dmGFP;... %mgfp
                    rbsGFP*dat(1) - dat(2)*(dGFP(degid).*10.^(dGFPO(degid)))];                           %GFP
    end
    %Iterate over pyocyanin concentrations and constructs
    for i = 1:length(pyo)
        for j = 1:length(degid)
            %Delay the start of ODE solution to account for lag in cellular
            %response
            lagIters = sum(t<delay);
            tnew = t(lagIters:end);
            %Compute model time series 
            [~, mDat] = ode15s(@(tnew,mDat) odes(mDat,params,pyo(i),degid(j)), tnew, init);
            
            mDat = [zeros(lagIters-1,1); mDat(:,2)];    
            
            %Add basal expression component and scaling factor to ODE
            %solution
             mGFP(:,i,j) = (Scale*10^ScaleO)*(mDat + (Basal*10^BasalO));
        end
    end
    %Concatenate 3* (one for each biological replicate)
    mGFP = cat(4, mGFP,mGFP,mGFP);

    end

%Draw model plot with fitted parameters, at 2.5microMolar
[~,~,~,~,ttest,test] = xl0123d([K, m, 4, 1.6, 2.5, dGFP(1), rbsGFP], [delay;1;-1],[delay,t(end)],0);
ttest = ttest+delay;
test = Scale.*(test(:,4)+Basal);

%Plot fitted model alongside scattered experimental data
figure
plot(t,data(:,9,2,1),'x',t,data(:,9,1,2),'x',t,data(:,9,1,3),'x',ttest,test);

%Post-processing
%Calculate the standard deviation (across biological replicates) for each
%point in the source data
sdev = std(data,0,4);
%Reshape standard deviation to allow plotting on a 2D surface
sdev = reshape(sdev,288,[]);
%Concatenate 3* for each replicate
sdev = cat(2,sdev,sdev,sdev);


%Variable for statistical analysis
testvar = (Rsd./abs(Rsd)).*(abs(Rsd)-sdev);


figure
ax1 = subplot(1,2,1);
testplot = surf(1:size(testvar,2),1:size(testvar,1),testvar,'Edgecolor','none');
xlabel("Sample number")
ylabel("t/min")
title("Test variable, up to 5\muM Pyocyanin");
shading interp
colormap(ax1,parula);
colorbar
ax2 = subplot(1,2,2);
datplot = surf([1:size(Rsd,2)],[1:size(Rsd,1)],reshape(data,288,[]),'Edgecolor','none');
xlabel("Sample number")
ylabel("t/min")
title("Dataset values up to 5\muM Pyocyanin")
colormap(ax2,parula)
colorbar
end