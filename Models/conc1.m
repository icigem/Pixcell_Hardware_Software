%Returns 3 variables: Matrix of concentration values C, array of lateral
%distances L, and array of depths Z
function [C, L, Z] = conc1()

warning('off','all');




%Parameter definitions
%Faraday's constant
F = 9.65e4;
%Arbitrarily defined 0.1mA current
I = 1e-4;
%Electrode radius defined as 2mm
r = 2e-3;
%Calculation of flux from above parameters
Q = I/(F*pi*r^2)
%Diffusion coefficient to test
D = 7.26e-10;

%Lateral distances and depths at which to calculate concentrations
L = 0:0.01:5;
Z = 0:0.1:5;

%Iterate over test points
for i = 1:length(L)
    for j = 1:length(Z)
        %Evaluate numerical integral to calculate concentration from its
        %Hankel transform
        C(i,j) = integral(@(a) besselj(0,L(i)*a).*besselj(1,a).*exp(-a*Z(j))./a,0,inf);
    end
end

%Rescaling, length scale = 2mm

L = r*L;  

C = r.*Q./D.*C;


subplot(1,2,1)
plot(L,C);
title('Lateral concentration distribution at differing depth');
xlabel('Distance from electrode / mm');
ylabel('Concentration / M');
subplot(1,2,2)
plot(Z,C);
title('Vertical concentration distribution at differing lateral distance');
xlabel('Depth / mm');
ylabel('Concentration / M');
% 
% figure
surf(Z,L,C,'Edgecolor','none');
title('Concentration distribution as a function of lateral distance and depth');
xlabel('Depth / mm');
ylabel('Lateral distance from electrode / mm');
zlabel('Concentration / M');
shading interp;

theta = 1:360;
r = L;
Crad = permute(C,[1 3 2]);
%Convert lateral data into volume of revolution
x = r'*cos(theta);
y = r'*sin(theta);
for i = 1:length(theta)-1
    Crad = [Crad,permute(C,[1 3 2])];
end
figure('KeyPressFcn',@keyevent);
d = 1;
int = 0;
draw();
warning('on','all');
toc
end
%Define function to replot data upon user input
function draw()
    uicontrol('Style','text',...
            'String',['Depth: ', num2str(Z(d)),'mm'],...
            'FontSize',10,...
            'FontWeight','bold',...
            'Units','normalized','Position',[.25 .95 .5 .05]);
    subplot(1,2,1)
    surf(x,y,Crad(:,:,d),'Edgecolor','none');
    zlim([0, max(max(max(Crad)))]);
    caxis([0, max(max(max(Crad)))]);
    title('Radial Concentration');
    xlabel('Distance from electrode / mm');
    ylabel('Distance from electrode / mm');
    zlabel('Concentration of species / M');
    shading interp;
    subplot(1,2,2)    
    plot(L,C(:,d))
    title('Distribution Profile');
    xlabel('Distance from electrode in mm');
    ylabel('Concentration of species / M');    
end
%Function to loop downward through depth
function drawloopf()
    if int == 1
        int = 0;
    else
        int = 1;
    end
    while d < length(Z) && int == 1
        d = d+1;
        draw();
        drawnow;
        java.lang.Thread.sleep(30);
    end
    int = 0;
end
%Function to loop upward through depth
function drawloopb()
    if int == 2
        int = 0;
    else
        int = 2;
    end
    while d > 1 && int == 2
        d = d-1;
        draw();
        drawnow;
        java.lang.Thread.sleep(30);
    end
    int = 0;
end
    
    
%Function to register key press with case:
%Down arrow: move 1 slice deeper in z
%Up arrow: move 1 slice shallower in z
%Right arrow: animate through slices until maximum depth reached
%Left arrow: animate through slices until minimum depth reached
function keyevent(~, event)
    c = event.Key;
    if strcmp(c,'downarrow')
        if d < length(Z)
            d = d+1;
            int = 0;
            draw();
        end
    elseif strcmp(c,'uparrow')
        if d > 1
            d = d - 1;
            int = 0;
            draw();
        end
    elseif strcmp(c, 'rightarrow')
        if d < length(Z)
            drawloopf();
        end
    elseif strcmp(c, 'leftarrow')
        if d > 1
            drawloopb();
        end
    elseif strcmp(c, 'r')
        if d > 1
            d = 1;
            int = 0;
            draw();
        end   
    end
end