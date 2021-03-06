package org.incava.diffj.function;

import java.util.Iterator;
import java.util.List;
import net.sourceforge.pmd.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.ast.ASTFormalParameters;
import net.sourceforge.pmd.ast.ASTNameList;
import net.sourceforge.pmd.ast.JavaParserConstants;
import net.sourceforge.pmd.ast.Token;
import org.incava.diffj.element.Diffable;
import org.incava.diffj.element.Differences;
import org.incava.diffj.params.Parameters;
import org.incava.pmdx.CtorUtil;
import org.incava.pmdx.SimpleNodeUtil;

public class Ctor extends Function implements Diffable<Ctor> {
    public static final String CONSTRUCTOR_REMOVED = "constructor removed: {0}";
    public static final String CONSTRUCTOR_ADDED = "constructor added: {0}";

    private final ASTConstructorDeclaration ctor;

    public Ctor(ASTConstructorDeclaration ctor) {
        super(ctor);
        this.ctor = ctor;
    }

    public void diff(Ctor toCtor, Differences differences) {
        compareAccess(toCtor, differences);
        compareParameters(toCtor, differences);
        compareThrows(toCtor, differences);
        compareCode(toCtor, differences);
    }

    protected ASTFormalParameters getFormalParameters() {
        return CtorUtil.getParameters(ctor);
    }

    protected ASTNameList getThrowsList() {
        return CtorUtil.getThrowsList(ctor);
    }

    public String getName() {
        return CtorUtil.getFullName(ctor);
    }    

    protected List<Token> getCodeTokens() {
        // removes all tokens up to the first left brace. This is because ctors
        // don't have their own blocks, unlike methods.
        
        List<Token> children = SimpleNodeUtil.getChildTokens(ctor);
        
        Iterator<Token> it = children.iterator();
        while (it.hasNext()) {
            Token tk = it.next();
            if (tk.kind == JavaParserConstants.LBRACE) {
                break;
            }
            else {
                it.remove();
            }
        }

        return children;
    }

    public double getMatchScore(Ctor toCtor) {
        Parameters fromParams = getParameters();
        Parameters toParams = toCtor.getParameters();
        return fromParams.getMatchScore(toParams);
    }

    public String getAddedMessage() {
        return CONSTRUCTOR_ADDED;
    }

    public String getRemovedMessage() {
        return CONSTRUCTOR_REMOVED;
    }
}
