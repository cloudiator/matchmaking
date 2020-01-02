package org.cloudiator.matchmaking.cmpl;

public class CMPLTemplate {

  public static final String BOTH =
      "%data : NODES set, nodeSize, privateSize, publicSize, private[NODES], costs[NODES], public[NODES]\n"
          + "\n"
          + "variables:\n"
          + " x[NODES]: integer[0..];\n"
          + "\n"
          + "objectives:\n"
          + " cost: costs[]T * x[]->min;\n"
          + "\n"
          + "constraints:\n"
          + "  $1$: public[]T * x[] = publicSize;\n"
          + "  $2$: private[]T * x[] = privateSize;\n"
          + "  $3$: sum{n in NODES : x[n]} = nodeSize;";

  public static final String SINGLE =
      "%data : NODES set, nodeSize, costs[NODES]\n"
          + "\n"
          + "variables:\n"
          + " x[NODES]: integer[0..];\n"
          + "\n"
          + "objectives:\n"
          + " cost: costs[]T * x[]->min;\n"
          + "\n"
          + "constraints:\n"
          + "  $1$: sum{n in NODES : x[n]} = nodeSize;\n";

  public static final String NODES =
      "%data : NODES set, LOCATIONS set, nodeSize, largerOrEqual4Cores[NODES], costs[NODES], cores[NODES], inLocation[LOCATIONS, NODES]\n"
          + "\n"
          + "variables:\n"
          + " x[NODES]: integer[0..];\n"
          + "\n"
          + "objectives:\n"
          + " cost: costs[]T * x[]->min;\n"
          + "\n"
          + "constraints:\n"
          + "  $1$: cores[]T * x[] >= 15;\n"
          + "  $2$: largerOrEqual4Cores[]T * x[] = 2;\n"
          + "  $3$: sum{n in NODES : x[n]} = nodeSize;\n"
          + "  $4$: inLocation[,] * x[] <= 1;";


}
