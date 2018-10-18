function plotintegrand()
%Define variables to iterate across
%Hankel variable
a = 0:0.01:20;
%Radius
r = 0:0.01:10;

for j = 1:length(r)
    for i = 1:length(a)
        integrand(i,j) = besselj(0,r(j)*a(i)).*besselj(1,a(i))./a(i);
        
    end
    
    
end
h = surf(a,r,integrand','Edgecolor','none')
view(40,30)
shading interp
lightangle(100,25)
h.FaceLighting = 'gouraud';
h.AmbientStrength = 0.5;
h.DiffuseStrength = 0.8;
h.SpecularStrength = 0.9;
h.SpecularExponent = 25;
h.BackFaceLighting = 'unlit';

xlabel("Variable of integration");
ylabel("Distance from Electrode / electrode radius (unitless)");
zlabel("Value of integrand");
title("Plot of the integrand for the final step of the diffusion profile, at z=0");

