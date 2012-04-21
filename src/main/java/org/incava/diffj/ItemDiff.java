package org.incava.diffj;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.sourceforge.pmd.ast.SimpleNode;
import net.sourceforge.pmd.ast.Token;
import org.incava.analysis.FileDiff;
import org.incava.analysis.FileDiffChange;
import org.incava.analysis.FileDiffCodeAdded;
import org.incava.analysis.FileDiffCodeDeleted;
import org.incava.analysis.FileDiffs;
import org.incava.analysis.Report;
import org.incava.ijdk.text.Location;
import org.incava.ijdk.text.LocationRange;
import org.incava.ijdk.util.*;
import org.incava.ijdk.util.diff.Diff;
import org.incava.ijdk.util.diff.Difference;
import org.incava.pmdx.*;

public class ItemDiff extends DiffComparator {    
    public static final String MODIFIER_REMOVED = "modifier removed: {0}";
    public static final String MODIFIER_ADDED = "modifier added: {0}";
    public static final String MODIFIER_CHANGED = "modifier changed from {0} to {1}";

    public static final String ACCESS_REMOVED = "access removed: {0}";
    public static final String ACCESS_ADDED = "access added: {0}";
    public static final String ACCESS_CHANGED = "access changed from {0} to {1}";
    
    public static final String CODE_CHANGED = "code changed in {0}";
    public static final String CODE_ADDED = "code added in {0}";
    public static final String CODE_REMOVED = "code removed in {0}";

    public static class TokenComparator extends DefaultComparator<Token> {
        public int doCompare(Token xt, Token yt) {
            int cmp = xt.kind < yt.kind ? -1 : (xt.kind > yt.kind ? 1 : 0);
            if (cmp == 0) {
                cmp = xt.image.compareTo(yt.image);
            }
            return cmp;
        }
    }

    public ItemDiff(Report report) {
        super(report);
    }

    public ItemDiff(FileDiffs differences) {
        super(differences);
    }

    /**
     * Returns a map from token types ("kinds", as java.lang.Integers), to the
     * token. This assumes that there are no leadking tokens of the same type
     * for the given node.
     */
    protected Map<Integer, Token> getModifierMap(SimpleNode node) {
        List<Token>         mods   = SimpleNodeUtil.getLeadingTokens(node);        
        Map<Integer, Token> byKind = new TreeMap<Integer, Token>();

        for (Token tk : mods) {
            byKind.put(Integer.valueOf(tk.kind), tk);
        }

        return byKind;
    }

    public void compareModifiers(SimpleNode aNode, SimpleNode bNode, int[] modifierTypes) {
        List<Token> aMods = SimpleNodeUtil.getLeadingTokens(aNode);
        List<Token> bMods = SimpleNodeUtil.getLeadingTokens(bNode);

        Map<Integer, Token> aByKind = getModifierMap(aNode);
        Map<Integer, Token> bByKind = getModifierMap(bNode);
        
        for (int mi = 0; mi < modifierTypes.length; ++mi) {
            Integer modInt = Integer.valueOf(modifierTypes[mi]);
            Token   aMod   = aByKind.get(modInt);
            Token   bMod   = bByKind.get(modInt);

            if (aMod == null) {
                if (bMod != null) {
                    changed(aNode.getFirstToken(), bMod, MODIFIER_ADDED, bMod.image);
                }
            }
            else if (bMod == null) {
                changed(aMod, bNode.getFirstToken(), MODIFIER_REMOVED, aMod.image);
            }
        }
    }

    public void compareAccess(SimpleNode aNode, SimpleNode bNode) {
        Token aAccess = ItemUtil.getAccess(aNode);
        Token bAccess = ItemUtil.getAccess(bNode);

        if (aAccess == null) {
            if (bAccess != null) {
                changed(aNode.getFirstToken(), bAccess, ACCESS_ADDED, bAccess.image);
            }
        }
        else if (bAccess == null) {
            changed(aAccess, bNode.getFirstToken(), ACCESS_REMOVED, aAccess.image);
        }
        else if (!aAccess.image.equals(bAccess.image)) {
            changed(aAccess, bAccess, ACCESS_CHANGED, aAccess.image, bAccess.image);
        }
    }

    protected FileDiff replaceReference(String name, FileDiff fdiff, LocationRange fromLocRg, LocationRange toLocRg) {
        String   newMsg  = MessageFormat.format(CODE_CHANGED, name);
        FileDiff newDiff = new FileDiffChange(newMsg, fdiff.getFirstLocation().getStart(), fromLocRg.getEnd(), fdiff.getSecondLocation().getStart(), toLocRg.getEnd());
        
        getFileDiffs().remove(fdiff);
        
        add(newDiff);
        
        return newDiff;
    }

    protected FileDiff addReference(String name, String msg, LocationRange fromLocRg, LocationRange toLocRg) {
        tr.Ace.setVerbose(true);

        tr.Ace.yellow("msg", msg);

        String str = MessageFormat.format(msg, name);

        FileDiff fdiff = null;

        if (msg.equals(CODE_ADDED)) {
            // this will show as add when highlighted, as change when not.
            fdiff = new FileDiffCodeAdded(str, fromLocRg, toLocRg);
            tr.Ace.yellow("fdiff", fdiff);
        }
        else if (msg.equals(CODE_REMOVED)) {
            fdiff = new FileDiffCodeDeleted(str, fromLocRg, toLocRg);
            tr.Ace.blue("fdiff", fdiff);
        }
        else {
            fdiff = new FileDiffChange(str, fromLocRg, toLocRg);
            tr.Ace.green("fdiff", fdiff);
        }                    

        add(fdiff);

        tr.Ace.setVerbose(false);

        return fdiff;
    }

    protected LocationRange getLocationRange(List<Token> tokenList, Integer start, Integer end) {
        Token startTk, endTk;
        if (end == Difference.NONE) {
            endTk = startTk = getStart(tokenList, start);
        }
        else {
            startTk = tokenList.get(start);
            endTk = tokenList.get(end);
        }
        return new LocationRange(FileDiff.toBeginLocation(startTk), FileDiff.toEndLocation(endTk));
    }
    
    protected boolean isOnSameLine(FileDiff fdiff, LocationRange loc) {
        return fdiff != null && fdiff.getFirstLocation().getStart().getLine() == loc.getStart().getLine();
    }

    protected FileDiff processDifference(Difference diff, String fromName, List<Token> fromList, List<Token> toList, FileDiff prevFdiff) {
        tr.Ace.setVerbose(true);

        tr.Ace.green("diff", diff);

        int delStart = diff.getDeletedStart();
        int delEnd   = diff.getDeletedEnd();
        int addStart = diff.getAddedStart();
        int addEnd   = diff.getAddedEnd();

        if (delEnd == Difference.NONE && addEnd == Difference.NONE) {
            // WTF?
            return null;
        }

        LocationRange fromLocRg = getLocationRange(fromList, delStart, delEnd);
        LocationRange toLocRg = getLocationRange(toList, addStart, addEnd);

        String msg = delEnd == Difference.NONE ? CODE_ADDED : (addEnd == Difference.NONE ? CODE_REMOVED : CODE_CHANGED);        
        tr.Ace.onGreen("msg", msg);

        prevFdiff = isOnSameLine(prevFdiff, fromLocRg) ? 
            replaceReference(fromName, prevFdiff, fromLocRg, toLocRg) : 
            addReference(fromName, msg, fromLocRg, toLocRg);
        
        tr.Ace.yellow("prevFdiff", prevFdiff);
        return prevFdiff;
    }

    protected void compareCode(String fromName, List<Token> fromList, String toName, List<Token> toList) {
        Diff<Token> d = new Diff<Token>(fromList, toList, new TokenComparator());
        
        FileDiff fdiff = null;
        List<Difference> diffList = d.diff();

        for (Difference diff : diffList) {
            fdiff = processDifference(diff, fromName, fromList, toList, fdiff);
            if (fdiff == null) {
                return;
            }
        }
    }

    protected boolean onEntireLine(List<Token> tokens, int tkIdxStart, int tkIdxEnd, Token startTk, Token endTk) {
        Token   prevToken = tkIdxStart   > 0             ? tokens.get(tkIdxStart - 1) : null;
        Token   nextToken = tkIdxEnd + 1 < tokens.size() ? tokens.get(tkIdxEnd   + 1) : null;
        
        boolean onEntLine = ((prevToken == null || prevToken.endLine   < startTk.beginLine) &&
                             (nextToken == null || nextToken.beginLine > endTk.endLine));        

        return onEntLine;
    }
    
    protected Token getStart(List<Token> list, int start) {
        Token stToken = ListExt.get(list, start);
        if (stToken == null && list.size() > 0) {
            stToken = ListExt.get(list, -1);
            stToken = stToken.next;
        }
        return stToken;
    }
}
