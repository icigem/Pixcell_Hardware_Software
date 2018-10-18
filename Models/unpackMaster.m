%Returns data structure with fields:
%t: time points for the data
%results: [t*pyocyanin*constructs*3 biological replicates] construct
function data = unpackMaster(c1amaster)

c1a=[];
c1atag=[];
t = c1amaster(:,1,1);
%Extract data: columns 12:14 are untagged, columns 27:29 are tagged
%Iterate through 14 pyocyanin concentrations
for i = 1:14
    idx = 1;
    for j = 12:14
        c1a(:,i,1,idx) = c1amaster(:,j,i);
        idx = idx+1;
    end
    idx = 1;
    for j = 27:29
        c1atag(:,i,1,idx) = c1amaster(:,j,i);
        idx=idx+1;
    end
    
end
data.results = cat(3,c1a,c1atag);
data.t = t;
end
        
    