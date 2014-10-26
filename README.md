The problem of planar laying.
Problem Statement: Determine whether the graph is planar, and, if so, to make it planar-laying.
For planar laying graph and concurrent validation, whether it is planar, it is convenient to use the gamma algorithm.
For correct operation of the algorithm is necessary to limit the properties of graphs is input.
Are input columns with the following properties:
1. The graph is connected;
2. The graph has at least one cycle;
3. The graph has no bridges, t. E. The edges after removing them, the graph splits into two connected components.
If the property is violated (1), then the graph is necessary to lay separately on the connected components.
If the property is violated (2), then the graph - wood and paint it flat stacking trivial.
Case of violation of property (3) we consider in more detail. If the graph has bridges, their
you have to cut, hold separate flat stacking of each connected component, and then
bridges to connect them. There may be difficulties: in the process of laying the end vertices
bridge may be inside of a planar graph. Draw one connected component, and we
accedes to the other sequences. Each new connected component will draw
to the face, which is the terminal vertex of the corresponding bridge. Since the graph connectivity
bridges connected components is a tree, we are able to get a flat packing.