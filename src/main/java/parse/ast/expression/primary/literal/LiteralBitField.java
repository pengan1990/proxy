/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary.literal;

import parse.visitor.SQLASTVisitor;

/**

 */
public class LiteralBitField extends Literal {
    private final String text;
    private final String introducer;

    /**
     * @param introducer   e.g. "_latin1"
     * @param bitFieldText e.g. "01010"
     */
    public LiteralBitField(String introducer, String bitFieldText) {
        super();
        if (bitFieldText == null) throw new IllegalArgumentException("bit text is null");
        this.introducer = introducer;
        this.text = bitFieldText;
    }

    public String getText() {
        return text;
    }

    public String getIntroducer() {
        return introducer;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
