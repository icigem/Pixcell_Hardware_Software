%Function to return the radial distance (at z=0) from the centre of an electrode
%(radius 1) at which the concentration has decreased by the factor in the
%argument
function rad = inspectConc(decrease)

concFun = @(L)integral(@(a) besselj(0,L*a).*besselj(1,a)./a,0,inf) - integral(@(a) besselj(0,0*a).*besselj(1,a)./a,0,inf)/decrease;

rad = fzero(concFun,1);
