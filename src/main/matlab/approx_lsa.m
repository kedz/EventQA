printf ("Loading TxD Matrix: TxD_sparse.txt\n")
x = csvread('/bolt/ir2/columbia/wojo/trunk/EventQA/data/TxD_sparse.txt');
x(:,1) = x(:,1)+1;
x(:,2) = x(:,2)+1;
m = sparse(x(:,1), x(:,2), x(:,3), 36577, 107274);
k = 300;
printf ("Performing SVD with %d singular values\n", k)
[u, s, v] = approx_svd(m, k, 1);
printf ("Computing Semantic Space Matrix\n")
space = pinv(s) * u';
printf ("Writing to file: space.txt\n")
sfile = sprintf('/bolt/ir2/columbia/wojo/trunk/EventQA/data/space_%d.txt', k);
csvwrite(sfile, space);