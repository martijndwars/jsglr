package org.spoofax.jsglr2.parser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;

import org.spoofax.jsglr2.actions.IAction;
import org.spoofax.jsglr2.actions.IReduce;
import org.spoofax.jsglr2.characters.Characters;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parsetable.IProduction;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.stack.StackLink;

public class ParserVisualisationObserver<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest> implements IParserObserver<StackNode, ParseForest> {
	
	List<String> jsonTrace = new ArrayList<String>();
	
	public void parseStart(Parse<StackNode, ParseForest> parse) {
		trace("{\"action\":\"start\",\"inputString\":\"" + parse.inputString +  "\"}");
	}
	
	public void parseCharacter(int character, Queue<StackNode> activeStacks) {
		trace("{\"action\":\"parseCharacter\",\"character\":\"" + Characters.charToString(character) + "\",\"activeStacks\":" + stackQueueToString(activeStacks) + "}");
	}
	
	public void createStackNode(StackNode stack) {
		trace("{\"action\":\"createStackNode\",\"stackNumber\":" + stack.stackNumber + ",\"stateNumber\":" + stack.state.stateNumber() + "}");
	}
	
	public void createStackLink(StackLink<StackNode, ParseForest> link) {
		trace("{\"action\":\"createStackLink\",\"linkNumber\":" + link.linkNumber + ",\"fromStack\":" + link.from.stackNumber + ",\"toStack\":" + link.to.stackNumber + ",\"parseNode\":" + link.parseForest.nodeNumber + ",\"descriptor\":\"" + escape(link.parseForest.descriptor()) + "\"}");
	}
	
	public void rejectStackLink(StackLink<StackNode, ParseForest> link) {
		trace("{\"action\":\"rejectStackLink\",\"linkNumber\":" + link.linkNumber + "}");
	}
    
	public void forActorStacks(Queue<StackNode> forActor, Queue<StackNode> forActorDelayed) {
        trace("{\"action\":\"forActorStacks\",\"forActor\":" + stackQueueToString(forActor) + ",\"forActorDelayed\":" + stackQueueToString(forActorDelayed) + "}");
    }
	
	public void actor(StackNode stack, Iterable<IAction> applicableActions) {
	    trace("{\"action\":\"actor\",\"stackNumber\":" + stack.stackNumber + "}");
	}
    
	public void skipRejectedStack(StackNode stack) {
        trace("{\"action\":\"skipRejectedStack\",\"stackNumber\":" + stack.stackNumber + "}");
    }
	
	public void addForShifter(ForShifterElement<StackNode, ParseForest> forShifterElement) {
	    trace("{\"action\":\"addForShifter\",\"stack\":" + forShifterElement.stack.stackNumber + ", \"state\":" + forShifterElement.state.stateNumber() + "}");
	}
	
	public void reduce(IReduce reduce, List<ParseForest> parseNodes, StackNode activeStackWithGotoState) {
	    trace("{\"action\":\"reduce\",\"parseNodes\":" + parseForestListToString(parseNodes) + ",\"activeStackWithGotoState\":" + (activeStackWithGotoState != null ? activeStackWithGotoState.stackNumber : -1) + "}");
	}
    
	public void directLinkFound(StackLink<StackNode, ParseForest> directLink) {
        trace("{\"action\":\"directLinkFound\",\"linkNumber\":" + (directLink != null ? directLink.linkNumber : -1) + "}");
    }
	
	public void accept(StackNode acceptingStack) {
		trace("{\"action\":\"acceptStackNode\",\"stackNumber\":" + acceptingStack.stackNumber + "}");
	}
	
	public void createParseNode(ParseForest parseNode, IProduction production) {
		trace("{\"action\":\"createParseNode\",\"nodeNumber\":" + parseNode.nodeNumber + ",\"production\":" + production.productionNumber() + ",\"term\":\"" + escape(production.descriptor()) + "\"}");
	}
	
	public void createDerivation(ParseForest[] parseNodes) {
		trace("{\"action\":\"createDerivation\",\"subTrees\":" + parseForestListToString(parseNodes) + "}");
	}
	
	public void createCharacterNode(ParseForest parseNode, int character) {
		trace("{\"action\":\"createCharacterNode\",\"nodeNumber\":" + parseNode.nodeNumber + ",\"character\":\"" + Characters.charToString(character) + "\"}");
	}
	
	public void addDerivation(AbstractParseForest parseNode) {
		trace("{\"action\":\"addDerivation\",\"parseNode\":" + parseNode.nodeNumber + "}");
	}
	
	public void shifter(ParseForest termNode, Queue<ForShifterElement<StackNode, ParseForest>> forShifter) {
        trace("{\"action\":\"shifter\",\"termNode\":" + termNode.nodeNumber + ",\"elements\":" + forShifterQueueToString(forShifter) + "}");
	}
	
	public void remark(String remark) {
		trace("{\"action\":\"remark\",\"remark\":\"" + remark + "\"}");
	}
	
	public void success(ParseSuccess<ParseForest> success) {
		trace("{\"action\":\"success\"}");
	}

	public void failure(ParseFailure<ParseForest> failure) {
		trace("{\"action\":\"failure\"}");
	}
	
	private void trace(String json) {
		jsonTrace.add(json);
	}
    
    public String toJson() {
        String res = "";
        
        for (String action : jsonTrace) {
            if (res.isEmpty())
                res += "\n\t" + action;
            else
                res += ",\n\t" + action;
        }
        
        return "[" + res + "\n]";
    }
    
    public void toJsonFile(String filename) throws FileNotFoundException {
        try(PrintWriter out = new PrintWriter(filename)) {
            out.println(toJson());
        }
    }
	
	private String escape(String string) {
		return string.replaceAll("\"", Matcher.quoteReplacement("\\\""));
	}
	
}
