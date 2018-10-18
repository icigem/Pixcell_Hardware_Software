% CALL: [max rate of GFP production, time of max rate of GFP prod, maximum GFP, time of maximum GFP, output time vector,output data vector] = xl0123d([0.005, 1, 4, 2, 0.005, 6.8765e-05, 0.183], [0 1000;1 0;-1 -1],[0 2000],1);
%                                               xl0123d(params, supply,T,plots);
%Params: [K,m,ratio, n, conc, deggfp,rbsgfp]
%Supply is a matrix describing the time evolution of Pyocyanin supply:
%supply = [times of changes in Pyocyanin state;
%           proportion of pyocyanin oxidised at each time point;
%           Steady state: always -1];
%T: tspan of the simulation in seconds
%Plots: 1 for autoplot, 0 for no plot (used in parameter sweeps)
function [vmax, tvmax, xmax, txmax, tout,dout] = xl0123d(params, supply, T, plots)

names = ["SoxR mRNA", "Total SoxR", "GFP mRNA", "GFP", "Reduced SoxR", "Oxidised SoxR"];
%Global variables
Avo = 6.02214e23;
Vcell = 2e-6*pi*(0.5e-6)^2;
%Convert concentration to number of molecules
    function num = Ccon(conc)
        num = conc*Avo*Vcell;
    end

nrep = 15;
dmrna = 0.139/60; %ROD of mrna
dprot = 0.0198/60; %1/1200; %ROD of prot
dgfp = params(6);
%Product of transcription and translation rates of soxR, as their specific
%dynamics are unimportant (they are always at steady state for this model)
product = 75*dmrna*dprot;
kx1 = sqrt(product/params(3)); %ROX of soxr
kx2 = 5.8e-3; %ROX of GFP (pSox (sRight) relative promoter strength
kl1 = params(3)*kx1; %ROL of soxr (RBS) 
kl2 = params(7); %ROL of GFP (RBS)
Kox = Ccon(0.045e-6); %Activation hill coeff
Kred = Ccon(0.011e-6); %Repression hill coeff
%Governing ODEs
function [dvdt] = odered(dat, params, pyo)

    soxrr = dat(2)./(1 + params(1)*pyo(1)^params(2));
    soxro = dat(2) - soxrr; %soxro
    
    dvdt = [nrep*kx1 - dat(1)*dmrna;...                   %soxR mRNA
        kl1*dat(1)  - dat(2)*dprot;...                    %Total soxR
        nrep*kx2./(1 + (soxro./Kox).^-params(4) + (soxrr./Kred)^params(4)) - dat(3)*dmrna;... %GFP mRNA
        kl2*dat(3) - dat(4)*dgfp;...        %GFP
        kl2*(nrep*kx2./(1 + (soxro./Kox).^-params(4) + (soxrr./Kred)^params(4)) - dat(3)*dmrna) - dgfp*(kl2*dat(3) - dat(4)*dgfp)]; %Analytical derivative of GFP for feature extraction
end
%Events function, which identifies maxima or points of inflection in the
%ODE solution, terminates the solver, and logs the event
    function [value, isterminal, direction] = events(t,dat)
        %Calculate oxidised and reduced soxR
        soxrr = dat(2)./(1 + params(1)*(pyo(1))^params(2));
        soxro = dat(2) - soxrr; %soxro
        %Values the function searches for
        value = [kl2*dat(3) - dat(4)*dgfp;...          %GFP maximum
                 kl2*(nrep*kx2./(1 + (soxro./Kox).^-params(4) + (soxrr./Kred)^params(4)) - dat(3)*dmrna) - dgfp*(kl2*dat(3) - dat(4)*dgfp)];    %GFP P.O.I   %Rate of GFP maximal
        isterminal = [1; 1];    %Terminate on either event
        % Direction = -1 identifies zeros where event fn is decreasing, +1 identifies zeros where event fn is increasing
        direction = [-1; -1];
    end  
%Calculate pyocyanin vector from concentration and supply matrix
pyo = Ccon([supply(2,1)*Avo*Vcell*params(5), (1-supply(2,1))*Avo*Vcell*params(5)]);
%Total soxR
soxrt = 75*nrep;
%Initial values of the ODE solutions
init = [kx1*nrep/dmrna,...                      %msox
        soxrt,...                             %soxrt
        0,...       %mgfp
        0,...       %GFP
        0];         %dgfp/dt
%Initialise to start of T            
tout = T(1);
%Initialise data output matrix with ODE initialisation and oxidised/reduced
%soxR: dout = [msoxR, soxRtotal, mGFP, GFP, reduced soxR, oxidised soxR]
dout = [init, init(2)./(1 + params(1)*(pyo(1)/pyo(2))^params(2)), init(2)*(1-1./(1 + params(1)*(pyo(1)/pyo(2))^params(2)))];
%Designate Events function for ODE solver
options = odeset('Events', @events);
%Iterate through input concentration changes
for i = 1:size(supply,2)
    if i < size(supply,2)
        tspan = [supply(1,i), supply(1,i+1)];
    else
        tspan = [supply(1,i), T(end)];
    end
    if i > 1
        pyo = [supply(2,i)*Avo*Vcell*params(5), (1-supply(2,i))*Avo*Vcell*params(5)];
        init = dat(end,:);
    end

fin = 0;
%Until fin flag:
while fin == 0
    %Run ODE solver
    [t, dat, te, ye, ie] = ode23(@(t,dat) odered(dat, params, pyo), tspan, init, options);
    %Upon event function callback, identify event
    if ie == 1 
        %log time and value of maximum GFP
        xmax = ye(4);
        txmax = te;
    elseif ie == 2 
        %log time and value of POI
        vmax = ye(5);
        tvmax = te;
    end
    %Append time vector
    tout = [tout;t(2:end)];
    %Append data vector
    dout = [dout;dat(2:end,:),dat(2:end,2)./(1 + params(1)*(pyo(1)/pyo(2))^params(2)), dat(2:end,2)*(1-1./(1 + params(1)*(pyo(1)/pyo(2))^params(2)))];
    if tout(end) == tspan(2)
        %Flag end
        fin = 1;
    else
        %Set initial timepoint to current timepoint
        tspan(1) = t(end);
        init = dat(end,:);
    end
end
end
%Double check output variables are properly assigned
if ~exist('vmax', 'var')
    vmax = 0;
    tvmax = 0;
    end
if ~exist('xmax','var')
    xmax = dout(end,4);
    txmax = tout(end);
end
        
    
if plots == 1
figure
for j = 1:6
    subplot(2,3,j)
    plot(tout,dout(:,j));
    title(names(j));
end
end

end