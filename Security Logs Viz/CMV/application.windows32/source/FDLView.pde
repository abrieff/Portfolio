class FDLView extends AbstractView {
  ArrayList<Node> nodes = null;
  ArrayList<Edge> edges = null;
  String[] nodesFrom = null, nodesTo = null;
  final float massDefault = 1.0, radiusDefault = 20.0, borderDefault = radiusDefault, lengthDefault = 200;
  FDL fdl = null;
  String[] namesSources = new String[2];
  ArrayList<Integer> idsMarkedNodes = new ArrayList<Integer>(), idsMarkedEdges = new ArrayList<Integer>();
  boolean selfHighlightHover = false, selfHighlightRect = false;

  // this deals with selection when items are under the mouse cursor
  public void hover() {
    // create the highlight Conditions to send as a message to all other plots
    // through the Controller using the messages architecture

    boolean nodeIntersected = false;
    String nodeSentinel = "boo123@";

    // find the node the mouse is over, if any
    for ( int id=0; id<nodes.size (); id++ ) {
      if ( fdl.isect(id) ) {
        nodeIntersected = true;
        selfHighlightHover = true;
        if(!selfHighlightRect) {
          idsMarkedNodes = new ArrayList<Integer>();
        }
        idsMarkedNodes.add(id);

        // first send condition for "from" node
        Condition[] conds1 = new Condition[1];
        conds1[0] = new Condition(namesSources[0], "=", nodes.get(id).get_label());
        Message msg1 = new Message();
        msg1.setSource(name)
          .setAction("normal")
            .setConditions(conds1);
        sendMsg(msg1);

        // then send condition from "to" node
        Condition[] conds2 = new Condition[1];
        conds2[0] = new Condition(namesSources[1], "=", nodes.get(id).get_label());
        Message msg2 = new Message();
        msg2.setSource(name)
          .setAction("normal")
            .setConditions(conds2);
        sendMsg(msg2);

        break; // only one node can be hovered over at a time
      }
    }

    if (!nodeIntersected) {
      selfHighlightHover = false;
      if(!selfHighlightRect) {
        idsMarkedNodes = new ArrayList<Integer>();
        Condition[] conds1 = new Condition[1];
        conds1[0] = new Condition(namesSources[0], "=", nodeSentinel);
        Message msg1 = new Message();
        msg1.setSource(name)
          .setAction("normal")
            .setConditions(conds1);
        sendMsg(msg1);
      }
    }
  }

  // handle sending messages to the Controller when a rectangle is selected
  public void handleThisArea(Rectangle rect) {
    // this rectangle holds the _pixel_ coordinates of the selection rectangle 
    Rectangle rectSub = getIntersectRegion(rect);
    String nodeSentinel = "boo123@";

    if (rectSub != null) {
      idsMarkedNodes = fdl.getNodeIdsWithinRect(rectSub);       
      
      if (idsMarkedNodes.size()>0) { // node inside rect
        selfHighlightRect = true;
        for ( int i=0; i<idsMarkedNodes.size (); i++ ) {
          int id = idsMarkedNodes.get(i);

          // first send condition for "from" node
          Condition[] conds1 = new Condition[1];
          conds1[0] = new Condition(namesSources[0], "=", nodes.get(id).get_label());
          Message msg1 = new Message();
          msg1.setSource(name)
            .setAction("normal")
              .setConditions(conds1);
          sendMsg(msg1);

          // then send condition from "to" node
          Condition[] conds2 = new Condition[1];
          conds2[0] = new Condition(namesSources[1], "=", nodes.get(id).get_label());
          Message msg2 = new Message();
          msg2.setSource(name)
            .setAction("normal")
              .setConditions(conds2);
          sendMsg(msg2);
        }
      } else { // no nodes inside rect
        selfHighlightRect = false;
        if(!selfHighlightHover) {
          idsMarkedNodes = new ArrayList<Integer>();
          Condition[] conds1 = new Condition[1];
          conds1[0] = new Condition(namesSources[0], "=", nodeSentinel);
          Message msg1 = new Message();
          msg1.setSource(name)
            .setAction("normal")
              .setConditions(conds1);
          sendMsg(msg1);
        }
      }
    }
  }

  public void display() {

    fdl.setSize(w, h);

    pushStyle();

    if (!(selfHighlightHover|selfHighlightRect)) { // if highlighting by other vizes, convert marks to marked nodes and edges
      idsMarkedNodes = new ArrayList<Integer>();
      idsMarkedEdges = new ArrayList<Integer>();
      for ( int i=0; i<marks.length; i++ ) {
        if (marks[i]) {
          int id1 = indexofNodeLabel(nodes, nodesFrom[i]);
          if ( !idsMarkedNodes.contains(id1) ) {
            idsMarkedNodes.add(id1);
          }
          int id2 = indexofNodeLabel(nodes, nodesTo[i]);
          if ( !idsMarkedNodes.contains(id2) ) {
            idsMarkedNodes.add(id2);
          }
          int ide = indexofEdge(edges, new Edge(id1, id2, 0, 0));
          if ( !idsMarkedEdges.contains(ide) ) {
            idsMarkedEdges.add(ide);
          }
        }
      }
    } else { // self-highlighting
      // highlight edges emanating from (self-)marked nodes
      idsMarkedEdges = new ArrayList<Integer>();
      for ( int i=0; i<idsMarkedNodes.size (); i++ ) {
        ArrayList<Integer> l = indicesofEdgesWithNodeId(edges, idsMarkedNodes.get(i));
        for ( int j=0; j<l.size (); j++ ) {
          if ( !idsMarkedEdges.contains(l.get(j)) ) {
            idsMarkedEdges.add(l.get(j));
          }
        }
      }
    }
    
//          for( int i=0;i<idsMarkedNodes.size(); i++ ) {
//        System.out.printf("%d ",idsMarkedNodes.get(i));
//      }
//      println("");

    // draw
    fdl.draw(idsMarkedNodes, idsMarkedEdges);

    popStyle();
  }

  public FDLView setSources(String source1, String source2) {

    namesSources[0] = source1; 
    namesSources[1] = source2;

    return this;
  }

  public FDLView setData(String[] nodesFrom, String[] nodesTo) {
    this.nodesFrom = nodesFrom;
    this.nodesTo = nodesTo;

    nodes = new ArrayList<Node>();
    ArrayList<String> nodesAll = new ArrayList<String>();
    Collections.addAll(nodesAll, unique(concatStringArrays(nodesFrom, nodesTo)));
    for ( int i=0; i<nodesAll.size (); i++ ) {
      float px = random(leftX+w/4, leftX+w-borderDefault-w/4);
      float py = random(leftY+h/4, leftY+h-borderDefault-h/4);
      nodes.add(new Node(px, py, massDefault, radiusDefault, i, nodesAll.get(i)));
    }

    edges = new ArrayList<Edge>();
    String[] sa = new String[nodesFrom.length];
    for ( int i=0; i<nodesFrom.length; i++ ) {
      sa[i] = nodesFrom[i] + "," + nodesTo[i];
    }
    ArrayList<String> sal = new ArrayList<String>();
    Collections.addAll(sal, sa);
    for ( int i=0; i<sal.size (); i++ ) {
      if ( sal.get(i) != null ) {
        int weight = Collections.frequency(sal, sal.get(i));
        String[] thesenodes = sal.get(i).split(",");
        int idNode1 = indexofNodeLabel(nodes, thesenodes[0]);
        int idNode2 = indexofNodeLabel(nodes, thesenodes[1]);
        int indexEdge = indexofEdge(edges, new Edge(idNode1, idNode2, 0, 0));
        if ( indexEdge == -1 ) { // new edge
          edges.add(new Edge(idNode1, idNode2, lengthDefault, weight));
        } else {
          edges.get(indexEdge).addWeight(weight);
        }
        Collections.replaceAll(sal, sal.get(i), null);
      }
    }

    fdl = new FDL(nodes, edges, leftX, leftY, w, h, borderDefault);

    return this;
  }
}

