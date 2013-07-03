LD_LIBRARY_PATH="/usr/local/lib64"
printf ("Loading TxD Matrix\n")
x = csvread('/bolt/ir2/columbia/wojo/trunk/EventQA/data/TxD_sparse.txt');
x(:,1) = x(:,1)+1;
x(:,2) = x(:,2)+1;
m = sparse(x(:,1), x(:,2), x(:,3), 36577, 107274);
#m = csvread('/bolt/ir2/columbia/wojo/trunk/EventQA/data/TxD.txt');
printf ("Performing SVD with 100 singular values\n")
[u, s, v] = svds (m, 100);
printf ("Computing Semantic Space Matrix\n")
space = pinv(s) * u';
printf ("Writing to file\n")
csvwrite('/bolt/ir2/columbia/wojo/trunk/EventQA/data/space.txt', space);