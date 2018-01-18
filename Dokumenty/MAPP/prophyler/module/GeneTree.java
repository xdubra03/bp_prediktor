package prophyler.module;

import java.util.*;
import java.lang.Math;
import java.math.*;
import java.text.*;

/*
* <p>Copyright: Copyright (c) 2005  Eric Stone and Arend Sidow, Stanford University </p>
*
* <p>University: Stanford University</p>
*/


class GTNode {

    static final double NO_LENGTH = -1.0;

    int id; // 1-based position in the node's parent vector
    boolean isLeaf;
    String name ="";

    /*
      The name is extracted from the parenthesis tree (characters beyond 29 get trimmed)

      eg. (>>danio_rerio_10000063<<:0.081915,takifugu_rubripes_10000064:0.101432):0.195985);";
      Name is taken as text between '>>' and '<<'.
    */

    GTNode parent;
    GTNode leftchild;
    GTNode rightchild;
    double length_to_parent;
    int PrettyTreeRow = -1; // 1-based
    int PrettyTreeColumn = -1; // 1-based
    String HNRId = "";
    String user_gene_name ="";
    String NodeNameForDisplay = "";
    STNode species;
    boolean IsAGeneDuplication;
    boolean IsAStrongGeneDuplication;

    GTNode() {
	isLeaf=false;
	name="";
	parent=null;
	leftchild=null;
	rightchild=null;
	length_to_parent = GTNode.NO_LENGTH;
	species = null;
	IsAGeneDuplication = false;
	IsAStrongGeneDuplication = false;
    }
}

class STNode {

    String id = ""; // db - species.id_number (nn)
    boolean isLeaf; // db - species.is_leaf Y/N (nn)
    String name =""; // db - species.node_name
    String genus = ""; // db - species.genus_name
    String species = ""; // db - species.species_name
    String mostCommonName = ""; // db - species.most_common_name
    String rank = ""; // db
    int level = -1; // level in tree .. root = 1, child of root = 2, etc.
    STNode parent;
    Vector children = new Vector();
    int PrettyTreeRow = -1; // 1-based
    int PrettyTreeColumn = -1; // 1-based

    STNode() {}

    public boolean equals(STNode s) {
	if (id.equals(s.id)) return true;
	return false;
    }

}

public class GeneTree {

    public Vector AllNodes = new Vector();
    private Vector InternalNodes = new Vector();
    private Vector Leafs = new Vector();
    private GTNode root = null;
    public boolean hasLengths=false;
    private StringCharacterIterator sci;
    private boolean bad_input_tree = false;
    private String ParTreeString="";
    private int PrettyTreeLinesBetweenAdjacentTerminalNodes = 1; // this needs to be odd
    private int PrettyTreeNextTerminalNodeRow = 1;
    public int GeneTreeHeightInChars = -1;
    public int GeneTreeWidthInChars = -1;
    private int force_min_branch_length=-1;

    public void ForceMinLength(int l) {
	force_min_branch_length=l;
    }

    public GeneTree() {}

    private GTNode MakeNewInternalNode() {
	GTNode p = new GTNode();
	p.id = InternalNodes.size()+1;
	InternalNodes.addElement(p);
	return p;
    }

    private GTNode MakeNewLeafNode() {
	GTNode p = new GTNode();
	p.id = Leafs.size()+1;
	p.isLeaf=true;
	Leafs.addElement(p);
	return p;
    }

    public String GeneratePrettyTree() { // this fxn will also do GeneTreeCanvas layout

	// this will always be rooted.

	// reset designations....

	for (int i=0; i<AllNodes.size(); ++i) {
	    GTNode p = (GTNode)AllNodes.elementAt(i);
	    p.PrettyTreeRow = -1;
	    p.PrettyTreeColumn = -1;
	}
	PrettyTreeNextTerminalNodeRow = 1;
	GeneTreeHeightInChars = -1;
	GeneTreeWidthInChars = -1;

	// establish row locations of terminal nodes.

	AssignTerminalNodePrettyTreeRow(root);

	// establish row locations of internal nodes.

	boolean keep_going = true;
	while (keep_going) {
	    keep_going = false;
	    for (int i=0; i<AllNodes.size(); ++i) {
		GTNode p = (GTNode)AllNodes.elementAt(i);
		if (p.PrettyTreeRow==-1) {
		    if (p.leftchild.PrettyTreeRow!=-1 && p.rightchild.PrettyTreeRow!=-1) {
			keep_going = true;
			p.PrettyTreeRow = (p.leftchild.PrettyTreeRow + p.rightchild.PrettyTreeRow)/2;
			break;
		    }
		}
	    }
	}

	// establish column locations for all nodes

	int min_horizontal_ascii_branch_length = (String.valueOf(AllNodes.size())).length()+1;
	if (force_min_branch_length>-1) {
	    if (min_horizontal_ascii_branch_length<force_min_branch_length) {
		min_horizontal_ascii_branch_length=force_min_branch_length;
	    }
	}

	int max_horizontal_ascii_branch_length = min_horizontal_ascii_branch_length+20;
	int no_lengths_ascii_branch_length = min_horizontal_ascii_branch_length+3;
	double max_branch_length = -1.0;
	double min_branch_length = -1.0;

	if (hasLengths) {
	    for (int i=0; i<AllNodes.size(); ++i) {
		GTNode p = (GTNode)AllNodes.elementAt(i);
		if (p.parent==null) continue;
		double tmp = p.length_to_parent;
		if (max_branch_length<-0.5 || tmp>max_branch_length) max_branch_length = tmp;
		if (min_branch_length<-0.5 || tmp<min_branch_length) min_branch_length = tmp;
	    }
	}

	// set root's horizontal position

	for (int i=0; i<AllNodes.size(); ++i) {
	    GTNode p = (GTNode)AllNodes.elementAt(i);
	    if (p.parent==null) {
		p.PrettyTreeColumn = min_horizontal_ascii_branch_length;
		break;
	    }
	}

	// set all other node's horizontal positions

	keep_going = true;
	while (keep_going) {
	    keep_going = false;
	    for (int i=0; i<AllNodes.size(); ++i) {
		GTNode p = (GTNode)AllNodes.elementAt(i);
		if (p.PrettyTreeColumn==-1 && p.parent.PrettyTreeColumn!=-1) {
		    if (hasLengths) {
			double tmp = p.length_to_parent;
			int extra_distance = min_horizontal_ascii_branch_length + (int)((double)(max_horizontal_ascii_branch_length-min_horizontal_ascii_branch_length)*(tmp-min_branch_length)/(max_branch_length-min_branch_length)+0.5);
			p.PrettyTreeColumn=p.parent.PrettyTreeColumn+extra_distance;
		    } else {
			p.PrettyTreeColumn=p.parent.PrettyTreeColumn+no_lengths_ascii_branch_length;
		    }
		    keep_going=true;
		}
	    }
	}

	int num_lines = PrettyTreeNextTerminalNodeRow-1-PrettyTreeLinesBetweenAdjacentTerminalNodes;
	GeneTreeHeightInChars = num_lines;
	BitSet layout[] = new BitSet[num_lines];
	char prettytree[][] = new char[num_lines][];
	for (int i=0; i<layout.length; ++i) layout[i] = new BitSet();
	String pt = "";

	// do prelayout...

	for (int i=0; i<AllNodes.size(); ++i) {
	    GTNode p = (GTNode)AllNodes.elementAt(i);
	    int c = p.PrettyTreeColumn;
	    int r = p.PrettyTreeRow;
	    if (p.isLeaf) {
		for (int j=0; j<=p.name.length(); ++j) layout[r-1].set(c+j); // ":name"
	    }
	    if (p.parent!=null) {
		int rp = p.parent.PrettyTreeRow;
		int cp = p.parent.PrettyTreeColumn;
		for (int j=cp+1; j<=c; ++j) layout[r-1].set(j-1);// horizontal -'s and node id
		int low_row = rp+1;
		int high_row = r;
		if (r<rp) {
		    low_row = r;
		    high_row = rp-1;
		}
		for (int j=low_row; j<=high_row; ++j) layout[j-1].set(cp-1); // vertical :'s...
	    } else {
		for (int j=1; j<=c; ++j) layout[r-1].set(j-1); // horizontal -'s and node id
	    }
	}

	// do real layout...

	for (int i=0; i<num_lines; ++i) {
	    prettytree[i] = new char[layout[i].length()];
	    if (layout[i].length()>GeneTreeWidthInChars) GeneTreeWidthInChars = layout[i].length();
	    for (int j=0; j<prettytree[i].length; ++j) prettytree[i][j]=' ';
	}
	for (int i=0; i<AllNodes.size(); ++i) {
	    GTNode p = (GTNode)AllNodes.elementAt(i);
	    int r = p.PrettyTreeRow;
	    int c = p.PrettyTreeColumn;
	    if (p.isLeaf) {
		prettytree[r-1][c]=':'; // c-1+1
		char na[] = p.name.toCharArray();
		for (int j=1; j<=p.name.length(); ++j) prettytree[r-1][c+j]=na[j-1]; // name
	    }
	    if (p.parent!=null) {
		int rp = p.parent.PrettyTreeRow;
		int cp = p.parent.PrettyTreeColumn;
		String node_id = String.valueOf(p.id);
		char ida[] = node_id.toCharArray();
		int num_dashes = c - cp - node_id.length();
		for (int j=0; j<num_dashes; ++j) prettytree[r-1][cp+j]='-'; // horizontal -'s
		for (int j=cp+num_dashes; j<c; ++j) prettytree[r-1][j]=ida[j-(cp+num_dashes)]; //node id
		int low_row = rp+1;
		int high_row = r;
		if (r<rp) {
		    low_row = r;
		    high_row = rp-1;
		}
		for (int j=low_row; j<=high_row; ++j) prettytree[j-1][cp-1]=':'; // vertical :'s
	    } else {
		String node_id = String.valueOf(p.id);
		char ida[] = node_id.toCharArray();
		int num_dashes = c - node_id.length();
		for (int j=0; j<num_dashes; ++j) prettytree[r-1][j]='-'; // horizontal -'s
		for (int j=num_dashes; j<c; ++j) prettytree[r-1][j]=ida[j-num_dashes]; // node id
	    }
	}
	for (int i=0; i<num_lines; ++i) pt+=new String(prettytree[i])+"\n";
	pt+="\n\n\n";
	int max_node_num_length = String.valueOf(AllNodes.size()).length();
	if (max_node_num_length<3) max_node_num_length=3;
	int length_length = 0;
	for (int j=0; j<Leafs.size(); ++j) {
	    GTNode p = (GTNode)AllNodes.elementAt(j);
	    if (p.parent!=null) {
		String len_string="";
		BigDecimal bd = new BigDecimal(p.length_to_parent);
		bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
		len_string = bd.toString();
		if (len_string.length()>length_length) {
		    length_length = len_string.length();
		}
	    }
	}
	if (length_length<6) length_length=6;
	int leftover = 30 - 2*length_length - 2*max_node_num_length - 2*2;
	if (leftover<1) leftover = 1;
	int linewidth = 32 + leftover + 2*length_length + 2*max_node_num_length + 2*2;
	int middle_spaces = linewidth - "Terminal Nodes".length() - "Internal Nodes".length();
	pt+="Terminal Nodes";
	for (int k=0; k<middle_spaces; ++k) pt+=" ";
	pt+="Internal Nodes\n";
	pt+="**************";
	for (int k=0; k<middle_spaces; ++k) pt+=" ";
	pt+="**************\n\n";
	pt+="name                            ";
	for (int k=0; k<max_node_num_length-3; ++k) pt+=" ";
	pt+="num  ";
	for (int k=0; k<length_length-6; ++k) pt+=" ";
	pt+="length";
	for (int k=0; k<leftover; ++k) pt+=" ";
	for (int k=0; k<max_node_num_length-3; ++k) pt+=" ";
	pt+="num  ";
	for (int k=0; k<length_length-6; ++k) pt+=" ";
	pt+="length\n";
	pt+="----                            ";
	for (int k=0; k<max_node_num_length; ++k) pt+="-";
	pt+="  ";
	for (int k=0; k<length_length; ++k) pt+="-";
	for (int k=0; k<leftover; ++k) pt+=" ";
	for (int k=0; k<max_node_num_length; ++k) pt+="-";
	pt+="  ";
	for (int k=0; k<length_length; ++k) pt+="-";
	pt+="\n";
	for (int j=0; j<Leafs.size(); ++j) {
	    GTNode p = (GTNode)AllNodes.elementAt(j);
	    pt+=p.name;
	    for (int k=p.name.length(); k<32; ++k) {pt+=" ";}
	    int num_size = String.valueOf(p.id).length();
	    for (int k=0; k<max_node_num_length-num_size; ++k) pt+=" ";
	    pt+=p.id;
	    pt+="  ";
	    String len_string="";
	    BigDecimal bd = new BigDecimal(p.length_to_parent);
	    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
	    len_string = bd.toString();
	    for (int k=0; k<length_length-len_string.length(); ++k) pt+=" ";
	    pt+=len_string;
	    if (j+Leafs.size()<AllNodes.size()) {
		GTNode pi = (GTNode)AllNodes.elementAt(j+Leafs.size());
		for (int k=0; k<leftover; ++k) pt+=" ";
		num_size = String.valueOf(pi.id).length();
		for (int k=0; k<max_node_num_length-num_size; ++k) pt+=" ";
		pt+=pi.id;
		pt+="  ";
		len_string="n/a";
		if (pi.parent!=null) {
		    bd = new BigDecimal(pi.length_to_parent);
		    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
		    len_string = bd.toString();
		}
		for (int k=0; k<length_length-len_string.length(); ++k) pt+=" ";
		pt+=len_string;
	    }
	    pt+="\n";
	}
	return pt;
    }

    private void AssignTerminalNodePrettyTreeRow(GTNode n) {
	if (n.isLeaf) {
	    n.PrettyTreeRow = PrettyTreeNextTerminalNodeRow;
	    PrettyTreeNextTerminalNodeRow+=1+PrettyTreeLinesBetweenAdjacentTerminalNodes;
	} else {
	    AssignTerminalNodePrettyTreeRow(n.leftchild);
	    AssignTerminalNodePrettyTreeRow(n.rightchild);
	}
    }

    public String GenerateParenthesisTree(boolean rooted, boolean showLengths) {
	ParTreeString = "";
	if (rooted || (root.leftchild.leftchild==null && root.rightchild.leftchild==null)) {
	    ParTreeString+="(";
	    GenerateParenthesisTreeForNode(root.leftchild, 0.0, showLengths);
	    ParTreeString+=",";
	    GenerateParenthesisTreeForNode(root.rightchild, 0.0, showLengths);
	    ParTreeString+=");\n";
	} else {
	    if (root.rightchild.leftchild!=null) {
		ParTreeString+="(";
		GenerateParenthesisTreeForNode(root.leftchild, root.rightchild.length_to_parent, showLengths);
		ParTreeString+=",";
		GenerateParenthesisTreeForNode(root.rightchild.leftchild, 0.0, showLengths);
		ParTreeString+=",";
		GenerateParenthesisTreeForNode(root.rightchild.rightchild, 0.0, showLengths);
		ParTreeString+=");\n";
	    } else {
		ParTreeString+="(";
		GenerateParenthesisTreeForNode(root.rightchild, root.leftchild.length_to_parent, showLengths);
		ParTreeString+=",";
		GenerateParenthesisTreeForNode(root.leftchild.leftchild, 0.0, showLengths);
		ParTreeString+=",";
		GenerateParenthesisTreeForNode(root.leftchild.rightchild, 0.0, showLengths);
		ParTreeString+=");\n";
	    }
	}
	return ParTreeString;
    }

    private void GenerateParenthesisTreeForNode(GTNode n, double extra, boolean showLengths) {
	if (n.isLeaf) {
	    String name = n.name;
	    ParTreeString+=name;
	} else {
	    ParTreeString+="(";
	    GenerateParenthesisTreeForNode(n.leftchild, 0.0, showLengths);
	    ParTreeString+=",";
	    GenerateParenthesisTreeForNode(n.rightchild, 0.0, showLengths);
	    ParTreeString+=")";
	}
	if (hasLengths && showLengths) {
	    ParTreeString+=":";
	    BigDecimal bd = new BigDecimal(n.length_to_parent+extra);
	    bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
	    ParTreeString+=bd.toString();
	}
    }

    private GTNode addElement(boolean isroot) {

	// recursive procedure adds nodes to user-defined tree

	GTNode pp;

	if (!isroot) {if (sci.next()==StringCharacterIterator.DONE) return null;}
	while (sci.current()==' ') {
	    if (sci.next()==StringCharacterIterator.DONE) break;
	}
	if (sci.current()==' ' || sci.current()==StringCharacterIterator.DONE) {
	    bad_input_tree=true;
	    return null;
	}

	if (sci.current()=='(') {

	    GTNode qq= MakeNewInternalNode();
	    qq.leftchild = addElement(false);
	    if (bad_input_tree) return null;
	    qq.leftchild.parent=qq;

	    while (sci.current()!=',') {
		if (sci.next()==StringCharacterIterator.DONE) break;
		if (sci.current()=='(') break;
		if (sci.current()==')') break;
		if (sci.current()==';') break;
	    }
	    if (sci.current()!=',') {
		bad_input_tree=true;
		return null;
	    }

	    qq.rightchild = addElement(false);
	    if (bad_input_tree) return null;
	    qq.rightchild.parent=qq;

	    if (sci.current()==',') {
		if (isroot) {
		    GTNode xx = MakeNewInternalNode();
		    xx.leftchild = qq.rightchild;
		    xx.leftchild.parent=xx;
		    qq.rightchild = xx;
		    xx.parent=qq;
		    xx.length_to_parent=0.0; // this is being assigned arbitrarily.
		    xx.rightchild = addElement(false);
		    if (bad_input_tree) return null;
		    xx.rightchild.parent=xx;
		} else {
		    bad_input_tree=true;  // trifurcation found that is not at root
		    return null;
		}
	    }

	    while (sci.current()!=')') {
		if (sci.next()==StringCharacterIterator.DONE) break;
		if (sci.current()=='(') break;
		if (sci.current()==',') break;
		if (sci.current()==';') break;
	    }
	    if (sci.current()!=')' || sci.next()==StringCharacterIterator.DONE) {
		bad_input_tree=true;
		return null;
	    }
	    pp = qq;
	} else {
	    // we are at the start of the name of a leaf...
	    // this should just grab the characters up to the next , ) or : as the name (sub'ing "_" and "\n" with " ")
	    String name = "";
	    char ch = sci.current();
	    while (ch!=',' && ch!=')' && ch!=':') {
		name+=ch;
		ch = sci.next();
		if (ch==StringCharacterIterator.DONE) {
		    bad_input_tree = true;
		    return null;
		}
	    }
	    pp = MakeNewLeafNode();
	    if (name.length()>GeneTree.NameLength) name = name.substring(0,GeneTree.NameLength);
	    pp.name = name;
	    // try to parse out the HNR id...
	    try {
		StringTokenizer strtoken = new StringTokenizer(name, "_");
		if (strtoken.hasMoreTokens()) strtoken.nextToken(); // genus
		if (strtoken.hasMoreTokens()) strtoken.nextToken(); // species
		if (strtoken.hasMoreTokens()) pp.HNRId = strtoken.nextToken(); // HNR id
		if (strtoken.hasMoreTokens()) pp.user_gene_name = strtoken.nextToken();
	    } catch (Exception ee) {}
	}
	if (sci.current()==':') {
	    String length_string="";
	    char ch = sci.next();
	    if (ch==StringCharacterIterator.DONE) {
		bad_input_tree = true;
		return null;
	    }
	    while (Character.isDigit(ch) || ch == '.' || ch=='-' || ch=='+' ) {
		length_string+=ch;
		ch = sci.next();
		if (ch==StringCharacterIterator.DONE) {
		    bad_input_tree = true;
		    return null;
		}
	    }
	    try {
		pp.length_to_parent = (new Double (length_string)).doubleValue();
		if (pp.length_to_parent<0.0) pp.length_to_parent=0.0;
	    } catch (Exception e) {
		bad_input_tree=true;
		return null;
	    }
	}
	return pp;
    }

    private void DigestInputTree() {
	root = addElement(true);
	if (bad_input_tree) return;
	// the current or next non-wsp character better be a ";"...
	while (sci.current()==' ') {
	    if (sci.next()==StringCharacterIterator.DONE) break;
	}
	if (sci.current()!=';') {
	    bad_input_tree=true;
	    return;
	}
	AllNodes = new Vector();
	for (int i=0; i<Leafs.size(); ++i) {
	    AllNodes.addElement((GTNode)Leafs.elementAt(i));
	}
	for (int i=0; i<InternalNodes.size(); ++i) {
	    AllNodes.addElement((GTNode)InternalNodes.elementAt(i));
	    ((GTNode)AllNodes.lastElement()).id = AllNodes.size();
	}
	AllNodes.trimToSize();
    }

    public void MidPointTree() {

	if (!hasLengths) return; // won't midpoint a tree w/o branch lengths

	// find the longest leaf<->leaf route .. put root at the midpoint of this

	double longest_distance=-1.0;
	GTNode leaf1 = null;
	GTNode leaf2 = null;
	double leaf1_distance=0.0;
	double leaf2_distance=0.0;
	int common_node_id=0;

	for (int i=0; i<Leafs.size(); ++i) {
	    GTNode l1 = (GTNode)AllNodes.elementAt(i);
	    Vector l1_node_ids_to_root = new Vector();
	    Vector l1_lengths_to_root = new Vector(); // distance to the corresponding node in l1_node_ids_to_root

	    GTNode tmp1 = l1;
	    while (tmp1.id!=root.id) {
		l1_node_ids_to_root.addElement(String.valueOf(tmp1.parent.id));
		l1_lengths_to_root.addElement(String.valueOf(tmp1.length_to_parent));
		tmp1=tmp1.parent;
	    }

	    for (int j=i+1; j<Leafs.size(); ++j) {
		GTNode l2 = (GTNode)AllNodes.elementAt(j);
		double distance_up=0.0;

		GTNode tmp2 = l2;
		while (tmp2.id!=root.id) {
		    String pid = String.valueOf(tmp2.parent.id);
		    distance_up+=tmp2.length_to_parent;
		    int l1_index = l1_node_ids_to_root.indexOf(pid);
		    if (l1_index>-1) {
			double tot_dist = distance_up;
			double l1d =0.0;
			for (int k=0; k<=l1_index; ++k) {
			    l1d+=(new Double((String)l1_lengths_to_root.elementAt(k))).doubleValue();
			    tot_dist+=(new Double((String)l1_lengths_to_root.elementAt(k))).doubleValue();
			}
			if (tot_dist>longest_distance) {
			    leaf1 = l1;
			    leaf2 = l2;
			    leaf1_distance = l1d;
			    leaf2_distance = distance_up;
			    longest_distance = tot_dist;
			    common_node_id = tmp2.parent.id;
			}
			break;
		    }
		    tmp2=tmp2.parent;
		}
	    }
	}

	// we have the longest path by here.

	// place start on the leaf with the longest side.
	double midpoint_loc = longest_distance/2.0;
	GTNode start = leaf1;
	if (leaf2_distance>leaf1_distance) start = leaf2;

	// find the 2 sides of the new root...
	double distance_up=0.0;
	GTNode tmp3 = start;
	while (tmp3.id!=root.id) {
	    int pid = tmp3.parent.id;
	    distance_up+=tmp3.length_to_parent;
	    if (pid==common_node_id) break;
	    if (distance_up>midpoint_loc) break;
	    tmp3 = tmp3.parent;
	}

	// tmp3 here is set to child of the parent/child between which will be placed the  new root....
	// distance_up is the distance to tmp3.parent from start

	double distance_overage = distance_up-midpoint_loc;
	ReRoot(tmp3, distance_overage);
    }

    public void ReRoot(GTNode new_root_rightchild, double distance_overage) {
	// distance_overage is the extra length between the true midpoint location and the new_root_leftchild node

	GTNode new_root_leftchild = new_root_rightchild.parent;

	// if root is in the same position, but if we need to redistribute distances
	if (new_root_leftchild.id==root.id) {
	    if (new_root_rightchild.id==root.rightchild.id) {
		if (distance_overage>0.0) { // if they are the same, we don't need to do anything
		    root.leftchild.length_to_parent += distance_overage;
		    root.rightchild.length_to_parent -= distance_overage;
		}
	    } else {
		if (distance_overage>0.0) { // if they are the same, we don't need to do anything
		    root.rightchild.length_to_parent += distance_overage;
		    root.leftchild.length_to_parent -= distance_overage;
		}
	    }
	    return;
	}

	// climb and redirect connections...
	GTNode tp = new_root_leftchild;
	GTNode tpp = tp.parent;

	if (tpp.id==root.id) {

	    boolean left = false;
	    if (tp.leftchild.id==new_root_rightchild.id) left=true;

	    if (root.rightchild.id==tp.id) {
		root.leftchild.parent = tp;
		if (left) tp.leftchild=root.leftchild;
		else tp.rightchild = root.leftchild;
		root.leftchild.length_to_parent +=tp.length_to_parent;
	    } else {
		root.rightchild.parent = tp;
		if (left) tp.leftchild=root.rightchild;
		else tp.rightchild = root.rightchild;
		root.rightchild.length_to_parent +=tp.length_to_parent;
	    }

	} else  {

	    // climb up to the old root, reversing directions...

	    double saved_l2p = tp.length_to_parent;
	    boolean left = false;
	    if (tp.leftchild.id==new_root_rightchild.id) left=true;

	    while (tpp.parent.id!=root.id) {

		GTNode next_parent= tpp.parent;
		boolean next_left = false;
		if (tpp.leftchild.id==tp.id) next_left = true;

		tpp.parent = tp;
		if (left) tp.leftchild = tpp;
		else tp.rightchild = tpp;
		double new_saved_l2p = tpp.length_to_parent;
		tpp.length_to_parent= saved_l2p;

		saved_l2p = new_saved_l2p;
		tp=tpp;
		tpp=next_parent;
		left = next_left;
	    }

	    // here
	    // tpp.parent = root
	    // tpp is a child of root
	    // tp is one below child of root
	    // saved_l2p = the distance between tpp and tp
	    // left = false if the former child of tp in this run was the rightchild of tp

	    // stitch out old root...

	    if (root.rightchild.id==tpp.id) {

		root.leftchild.length_to_parent += tpp.length_to_parent;
		root.leftchild.parent = tpp;

		boolean next_left = false;
		if (tpp.leftchild.id==tp.id) next_left = true;

		if (!next_left) tpp.rightchild = root.leftchild;
		else tpp.leftchild = root.leftchild;

	    } else {

		root.rightchild.length_to_parent += tpp.length_to_parent;
		root.rightchild.parent = tpp;

		boolean next_left = false;
		if (tpp.leftchild.id==tp.id) next_left = true;

		if (!next_left) tpp.rightchild = root.rightchild;
		else tpp.leftchild = root.rightchild;

	    }

	    tpp.parent=tp;
	    if (left) tp.leftchild = tpp;
	    else tp.rightchild = tpp;
	    tpp.length_to_parent= saved_l2p;

	}

	// place root in new position...

	root.rightchild = new_root_rightchild;
	root.leftchild = new_root_leftchild;
	root.leftchild.parent = root;
	root.rightchild.parent = root;
	if (distance_overage<=0.0) { // just in case of rounding issues
	    root.leftchild.length_to_parent = 0.0;
	} else {
	    root.leftchild.length_to_parent = distance_overage;
	    root.rightchild.length_to_parent -= root.leftchild.length_to_parent;
	}
    }

    private boolean HasLengths() {
	boolean some_with=false;
	boolean some_without=false;
	for (int i=0; i<AllNodes.size(); ++i) {
	    GTNode p = (GTNode)AllNodes.elementAt(i);
	    if (p.parent!=null) {
		if (p.length_to_parent>=-0.01) some_with=true;
		if (p.length_to_parent<-0.1) some_without=true;
	    }
	}
	if (some_with && some_without) bad_input_tree=true;
	hasLengths = some_with;
	return hasLengths;
    }

    private void treeConstruct (String parentreestr) {
	sci = new StringCharacterIterator(parentreestr);
	DigestInputTree();
	if (bad_input_tree) return;
	HasLengths();
    }

    public static void main(String[] args) {
	GeneTree g = new GeneTree();
	String paren = "((A:0,B:0.001716):.054795,C:.055411,D:.303295);";
	boolean chk = g.GenerateGeneTree(paren);
	if (chk) {
	    System.out.println(g.GenerateParenthesisTree(true, true));
	    System.out.println(g.GenerateParenthesisTree(false, true));
	    String pt2 = g.GeneratePrettyTree();
	    pt2 = "Tree not Rooted by Midpoint\n***********************\n\n"+pt2;
	    System.out.println(pt2);
	    g.MidPointTree(); // will only work if there are branch lengths
	    System.out.println(g.GenerateParenthesisTree(true, true));
	    System.out.println(g.GenerateParenthesisTree(false, true));
	    String pt = g.GeneratePrettyTree();
	    pt = "Tree Rooted by Midpoint\n***********************\n\n"+pt;
	    System.out.println(pt);
	}
	System.exit(0);
    }

    public boolean GenerateGeneTree(String parentreestr) {
	root = null;
	hasLengths = false;
	bad_input_tree = false;
	AllNodes = new Vector();
	InternalNodes = new Vector();
	Leafs = new Vector();
	String pstring= "";
	char[] psa = (parentreestr.trim()).toCharArray();
	for (int i=0; i<psa.length; ++i) {
	    if (Character.isWhitespace(psa[i])) pstring+=" ";
	    else pstring+=psa[i];
	}
	if (pstring.equals("")) {
	    bad_input_tree=true;
	} else {
	    treeConstruct(pstring);
	    if (bad_input_tree) AllNodes =  null;
	}
	if (AllNodes!=null && AllNodes.size()<3) {
	    bad_input_tree=true;
	    AllNodes = null;
	}
	return (!bad_input_tree);
    }

    private static final int NameLength = 29;

}




