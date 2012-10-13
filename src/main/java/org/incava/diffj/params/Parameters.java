package org.incava.diffj.params;

import java.util.Iterator;
import java.util.List;
import net.sourceforge.pmd.ast.ASTFormalParameter;
import net.sourceforge.pmd.ast.ASTFormalParameters;
import net.sourceforge.pmd.ast.Token;
import org.incava.diffj.Differences;
import org.incava.diffj.Messages;
import org.incava.pmdx.ParameterUtil;

public class Parameters {
    private final ASTFormalParameters params;
    
    public Parameters(ASTFormalParameters params) {
        this.params = params;
    }

    public void diff(Parameters toParams, Differences differences) {
        List<String> fromParamTypes = getParameterTypes();
        List<String> toParamTypes = toParams.getParameterTypes();

        if (hasParameters()) {
            if (toParams.hasParameters()) {
                compareEachParameter(toParams, differences);
            }
            else {
                markParametersRemoved(toParams, differences);
            }
        }
        else if (toParams.hasParameters()) {
            markParametersAdded(toParams, differences);
        }
    }

    protected boolean hasParameters() {
        return !getParameterTypes().isEmpty();
    }

    protected List<String> getParameterTypes() {
        return ParameterUtil.getParameterTypes(params);
    }

    protected List<Token> getParameterNames() {
        return ParameterUtil.getParameterNames(params);
    }

    protected List<ASTFormalParameter> getParameterList() {
        return ParameterUtil.getParameters(params);
    }

    protected ASTFormalParameter getParameter(int idx) {
        return ParameterUtil.getParameter(params, idx);
    }

    protected Token getParameterName(int idx) {
        return ParameterUtil.getParameterName(params, idx);
    }

    protected void markParametersAdded(Parameters toParams, Differences differences) {
        List<Token> names = toParams.getParameterNames();
        for (Token name : names) {
            differences.changed(params, name, Messages.PARAMETER_ADDED, name.image);
        }
    }

    protected void markParametersRemoved(Parameters toParams, Differences differences) {
        List<Token> names = getParameterNames();
        for (Token name : names) {
            differences.changed(name, toParams.params, Messages.PARAMETER_REMOVED, name.image);
        }
    }

    protected void markParameterTypeChanged(ASTFormalParameter fromFormalParam, Parameters toParams, int idx, Differences differences) {
        Parameter fromParam = new Parameter(fromFormalParam);
        ASTFormalParameter toFormalParam = toParams.getParameter(idx);
        Parameter toParam = new Parameter(toFormalParam);
        String fromType = fromParam.getParameterType();
        String toType = toParam.getParameterType();
        differences.changed(fromFormalParam, toFormalParam, Messages.PARAMETER_TYPE_CHANGED, fromType, toType);
    }

    protected void markParameterNameChanged(ASTFormalParameter fromFormalParam, Parameters toParams, int idx, Differences differences) {
        Parameter fromParam = new Parameter(fromFormalParam);
        Token fromNameTk = fromParam.getParameterName();
        Token toNameTk = toParams.getParameterName(idx);
        differences.changed(fromNameTk, toNameTk, Messages.PARAMETER_NAME_CHANGED, fromNameTk.image, toNameTk.image);
    }

    protected void checkForReorder(ASTFormalParameter fromFormalParam, int fromIdx, Parameters toParams, int toIdx, Differences differences) {
        Parameter fromParam = new Parameter(fromFormalParam);
        Token fromNameTk = fromParam.getParameterName();
        Token toNameTk = toParams.getParameterName(toIdx);
        if (fromNameTk.image.equals(toNameTk.image)) {
            differences.changed(fromNameTk, toNameTk, Messages.PARAMETER_REORDERED, fromNameTk.image, fromIdx, toIdx);
        }
        else {
            differences.changed(fromNameTk, toNameTk, Messages.PARAMETER_REORDERED_AND_RENAMED, fromNameTk.image, fromIdx, toIdx, toNameTk.image);
        }
    }

    protected void markReordered(ASTFormalParameter fromFormalParam, int fromIdx, Parameters toParams, int toIdx, Differences differences) {
        Parameter fromParam = new Parameter(fromFormalParam);
        Token fromNameTk = fromParam.getParameterName();
        ASTFormalParameter toParam = toParams.getParameter(toIdx);
        differences.changed(fromFormalParam, toParam, Messages.PARAMETER_REORDERED, fromNameTk.image, fromIdx, toIdx);
    }

    protected void markRemoved(ASTFormalParameter fromFormalParam, Parameters toParams, Differences differences) {
        Parameter fromParam = new Parameter(fromFormalParam);
        Token fromNameTk = fromParam.getParameterName();
        differences.changed(fromFormalParam, toParams.params, Messages.PARAMETER_REMOVED, fromNameTk.image);
    }

    public void compareEachParameter(Parameters toParams, Differences differences) {
        List<ASTFormalParameter> fromFormalParamList = getParameterList();
        List<ASTFormalParameter> toFormalParamList = toParams.getParameterList();

        int size = fromFormalParamList.size();

        ParameterComparator pc = new ParameterComparator(fromFormalParamList, toFormalParamList);
        List<ParameterMatch> matches = pc.getMatches();

        for (int idx = 0; idx < size; ++idx) {
            ASTFormalParameter fromFormalParam = fromFormalParamList.get(idx);
            ParameterMatch paramMatch = matches.get(idx);
            tr.Ace.cyan("idx", idx);
            tr.Ace.cyan("paramMatch", paramMatch);
            tr.Ace.onRed("isMatch", paramMatch.isMatch(idx));
            tr.Ace.onRed("isExactMatch", paramMatch.isExactMatch());

            if (paramMatch.isMatch(idx)) {
                continue;
            }
            else if (paramMatch.isTypeMatch(idx)) {
                markParameterNameChanged(fromFormalParam, toParams, idx, differences);
            }
            else if (paramMatch.isNameMatch(idx)) {
                markParameterTypeChanged(fromFormalParam, toParams, idx, differences);
            }
            else if (paramMatch.hasTypeMatch()) {
                checkForReorder(fromFormalParam, idx, toParams, paramMatch.getTypeMatch(), differences);
            }
            else if (paramMatch.hasNameMatch()) {
                markReordered(fromFormalParam, idx, toParams, paramMatch.getNameMatch(), differences);
            }
            else {
                markRemoved(fromFormalParam, toParams, differences);
            }
        }

        Iterator<ASTFormalParameter> toIt = toFormalParamList.iterator();
        for (int toIdx = 0; toIt.hasNext(); ++toIdx) {
            ASTFormalParameter toParm = toIt.next();
            if (toParm == null) {
                continue;
            }

            ASTFormalParameter toFormalParam = toParams.getParameter(toIdx);
            Parameter toParam = new Parameter(toFormalParam);
            Token toName = toParam.getParameterName();
            differences.changed(params, toFormalParam, Messages.PARAMETER_ADDED, toName.image);
        }
    }

    public double getMatchScore(Parameters toParams) {
        ParameterTypes fromTypes = new ParameterTypes(getParameterTypes());
        ParameterTypes toTypes = new ParameterTypes(toParams.getParameterTypes());
        return fromTypes.getMatchScore(toTypes);
    }
}
