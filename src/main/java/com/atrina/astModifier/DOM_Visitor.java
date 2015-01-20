package com.atrina.astModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.WhileLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.crawljax.core.CrawljaxController;


public class DOM_Visitor implements NodeVisitor {
	
	/* string[]--> [domNodeToLog,objectAndfunction]]*/
	private List<String[]> domRelatedAtExitPoint=new ArrayList<String[]>();
	private final Map<String, String> mapper = new HashMap<String, String>();

	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
//	private static HashMap<String,TreeSet<String>> domProps=new HashMap<String,TreeSet<String>>();
	private ArrayList<String> nodesNotTolook=new ArrayList<String>();
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;

	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}
	
	public List<String[]> getDomRelatedAtExitPoint(){
		return domRelatedAtExitPoint;
	}

	/**
	 * Abstract constructor to initialize the mapper variable.
	 */
	public DOM_Visitor() {

		/* add -<number of arguments> to also make sure number of arguments is the same */
		nodesNotTolook.add("send(new Array(");
		nodesNotTolook.add("new Array(");
		nodesNotTolook.add("addVariable");

		/* add -<number of arguments> to also make sure number of arguments is the same */
		mapper.put("addClass", "attr('class')");
		mapper.put("removeClass", "attr('class')");
		mapper.put("css", "css(%0)");
		mapper.put("attr", "attr(%0)");
		mapper.put("prop", "attr(%0)");
		mapper.put("css-2", "css(%0)");
		mapper.put("attr-2", "attr(%0)");
		mapper.put("prop-2", "attr(%0)");
/*		mapper.put("append", "text()");
		mapper.put("after", "parent().html()");
		mapper.put("appendTo", "html()");
		mapper.put("before","parent().html()");
		mapper.put("detach", "html()");
		mapper.put("remove", "html()");
		mapper.put("empty", "html()");
*/		mapper.put("height-1", "height()");
		mapper.put("width-1", "width()");
		mapper.put("insertBefore", "prev().html()");
		mapper.put("insertAfter", "next().html()");
		mapper.put("offset-1", "offset()");
		mapper.put("prepend", "html()");
		mapper.put("prependTo", "html()");
		mapper.put("html-1", "html()");
		mapper.put("setAttribute-2", "getAttribute(%0)");
		mapper.put("text-1", "text()");
		mapper.put("children", "children()");
		mapper.put("parent", "parent()");
/*		mapper.put("getElementById", "getElementById(%0).length");
		mapper.put("getElementsByTagName", "getElementsByTagName(%0).length");
		mapper.put("className", "className");
*/		
		
	}

	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
	//	System.out.print(code+"*******\n");
		return p.parse(code, null, 0);
		
	}

	/**
	 * Find out the function name of a certain node and return "anonymous" if it's an anonymous
	 * function.
	 * 
	 * @param f
	 *            The function node.
	 * @return The function name.
	 */
	protected String getFunctionName(FunctionNode f) {
		if (f==null)
			return "NoFunctionNode";
	/*	else if(f.getParent() instanceof LabeledStatement){
			return ((LabeledStatement)f.getParent()).shortName();
		}
	*/	else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}


	

	/**
	 * Create a new block node with two children.
	 * 
	 * @param node
	 *            The child.
	 * @return The new block.
	 */
	private Block createBlockWithNode(AstNode node) {
		Block b = new Block();

		b.addChild(node);

		return b;
	}

	/**
	 * @param node
	 *            The node we want to have wrapped.
	 * @return The (new) node parent (the block probably)
	 */
	private AstNode makeSureBlockExistsAround(AstNode node) {
		AstNode parent = node.getParent();

		if (parent instanceof IfStatement) {
			/* the parent is an if and there are no braces, so we should make a new block */
			IfStatement i = (IfStatement) parent;

			/* replace the if or the then, depending on what the current node is */
			if (i.getThenPart().equals(node)) {
				i.setThenPart(createBlockWithNode(node));
			} else if (i.getElsePart()!=null){
				if (i.getElsePart().equals(node))
					i.setElsePart(createBlockWithNode(node));
			}
			
		} else if (parent instanceof WhileLoop) {
			/* the parent is a while and there are no braces, so we should make a new block */
			/* I don't think you can find this in the real world, but just to be sure */
			WhileLoop w = (WhileLoop) parent;
			if (w.getBody().equals(node))
				w.setBody(createBlockWithNode(node));
		} else if (parent instanceof ForLoop) {
			/* the parent is a for and there are no braces, so we should make a new block */
			/* I don't think you can find this in the real world, but just to be sure */
			ForLoop f = (ForLoop) parent;
			if (f.getBody().equals(node))
				f.setBody(createBlockWithNode(node));
		}

		return node.getParent();
	}

	/**
	 * Actual visiting method.
	 * 
	 * @param node
	 *            The node that is currently visited.
	 * @return Whether to visit the children.
	 */
/*	private boolean domPropAdded=false;
	private FunctionNode enclosedFunc = null;
	private String objAndFunc = "";
*/	@Override
	public boolean visit(AstNode node) {
		
		if(!shouldVisitNode(node))
			return false;

		if (node instanceof Name) {
				
			
			if (node.getParent() instanceof PropertyGet
				        && node.getParent().getParent() instanceof FunctionCall && !node.getParent().toSource().contains("function")) {

					List<AstNode> arguments =
					        ((FunctionCall) node.getParent().getParent()).getArguments();

					String domNodeToLog;

					if (mapper.get(node.toSource()) != null
					        || mapper.get(node.toSource() + "-" + arguments.size()) != null) {
					
						PropertyGet g = (PropertyGet) node.getParent();
	                    
						String objectAndFunction = mapper.get(node.toSource());
						if (objectAndFunction == null) {
							objectAndFunction = mapper.get(node.toSource() + "-" + arguments.size());
						}


						if (node.toSource().equals("appendTo") || node.toSource().equals("prependTo") || node.toSource().equals("insertAfter") || node.toSource().equals("insertBefore")){
							domNodeToLog="$" + "(" + arguments.get(0).toSource() + ")";
							objectAndFunction="DIRECTACCESS";
							
						}

						else if(node.toSource().equals("children") || node.toSource().equals("parent")){
							domNodeToLog = g.getLeft().toSource()+ "." + objectAndFunction;
							objectAndFunction="DIRECTACCESS";
						}
				
						else{
							objectAndFunction = g.getLeft().toSource()+ "." + objectAndFunction;
							domNodeToLog=g.getLeft().toSource();
						
						}
						
						
					
							for (int i = 0; i < arguments.size(); i++) {
								objectAndFunction =
									objectAndFunction.replace("%" + i, arguments.get(i).toSource());
							}
						
						objectAndFunction=objectAndFunction.replace(" ", "____");
						String[] domStuff=new String[2];
						domStuff[0]=domNodeToLog;
						domStuff[1]=objectAndFunction;
						domRelatedAtExitPoint.add(domStuff);
	                    
					}
				
					else
						if(node.toSource().equals("css")) 
							if(arguments.size()==1 && arguments.get(0).toSource().startsWith("{")) {
							
								PropertyGet g = (PropertyGet) node.getParent();
								String objectAndFunction="";
									
								objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
								domNodeToLog=g.getLeft().toSource();
								
								String[] args=arguments.get(0).toSource().replace("{", "").replace("}","").split(",");
								for (int i=0; i<args.length; i++) {
							    	if (args[i].contains(":")){
							    		if (!args[i].split(":")[0].contains("'") && !args[i].split(":")[0].contains("\"")) {
							    			objectAndFunction+="(" + args[i].split(":")[0].replace(" ", "____")+ "'" + ")";
							    		}
							    		else {

							    			objectAndFunction+="(" + args[i].split(":")[0].replace(" ", "")+ ")";
							    		}	
							    		AstNode parent = makeSureBlockExistsAround(getLineNode(node));
										String[] domStuff=new String[2];
										domStuff[0]=domNodeToLog;
										domStuff[1]=objectAndFunction;
										domRelatedAtExitPoint.add(domStuff);
							    	
							    
										objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
									
							    	}
								}
								
						}
							else
								if (node.toSource().equals("attr")) {
									if(arguments.size()==1 && arguments.get(0).toSource().startsWith("{")) {
										
										PropertyGet g = (PropertyGet) node.getParent();
										String objectAndFunction="";
											
										objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
										domNodeToLog=g.getLeft().toSource();
										
										String[] args=arguments.get(0).toSource().replace("{", "").replace("}","").split(",");
										for (int i=0; i<args.length; i++) {
									    	if (args[i].contains(":")){

									   			objectAndFunction+="(" + args[i].split(":")[0].replace(" ", "") + ")";								    	
									    		AstNode parent = makeSureBlockExistsAround(getLineNode(node));
												String[] domStuff=new String[2];
												domStuff[0]=domNodeToLog;
												domStuff[1]=objectAndFunction;
												domRelatedAtExitPoint.add(domStuff);
							
									    		objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
											
									    	}
										}
									}
								}
					
				}
			}
	        
	        else if(node instanceof FunctionCall){
				if( ((FunctionCall)node).getTarget() instanceof Name){
				
					if(((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("$")
							|| ((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("jQuery")){
					
						if(((FunctionCall)node).getArguments().size()==1
								&& ((FunctionCall)node).getArguments().get(0) instanceof StringLiteral){
							
							String domNodeToLog=node.toSource();
							String objectAndFunction="DIRECTACCESS";
				    		AstNode parent = makeSureBlockExistsAround(getLineNode(node));
							String[] domStuff=new String[2];
							domStuff[0]=domNodeToLog;
							domStuff[1]=objectAndFunction;
							domRelatedAtExitPoint.add(domStuff);
				    	
							
						}
					}
					else if(((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("getElementById") ||
							((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("getElementsByTagName")){
						
						String domNodeToLog="document" + "." + node.toSource();
						String objectAndFunction="DIRECTACCESS";
			    		AstNode parent = makeSureBlockExistsAround(getLineNode(node));
						String[] domStuff=new String[2];
						domStuff[0]=domNodeToLog;
						domStuff[1]=objectAndFunction;
						domRelatedAtExitPoint.add(domStuff);
			    	
						
					}
				}
	        }
	        
	        
		
   
		
	


		/* have a look at the children of this node */
		return true;
	}

	private AstNode getLineNode(AstNode node) {
		while ((!(node instanceof ExpressionStatement) && !(node instanceof Assignment))
		        || node.getParent() instanceof ReturnStatement) {
			node = node.getParent();
		}
		return node;
	}


	
	private boolean shouldVisitNode(AstNode astnode){
		if (nodesNotTolook.size()==0)
			return true;
		String name="";
		if(astnode instanceof FunctionCall){
			
			FunctionCall funcCall=(FunctionCall) astnode;
			name=funcCall.getTarget().toSource();
		}
		else if(astnode instanceof FunctionNode){
			FunctionNode funcNode=(FunctionNode) astnode;
			name=funcNode.getName();
		}
		for (String node:nodesNotTolook){
			
			if (name.contains(node)){
				return false;
			}
		}
		
		return true;
	}
	


}
