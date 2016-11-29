package parse.ast.expression.primary;

import parse.visitor.SQLASTVisitor;
import util.SplitUtil;

import java.util.Map;

public class PlaceHolder extends PrimaryExpression {
    private final String name;
    private final String nameUp;//ID，VAL

    private final String[] nameUpArr;

    public PlaceHolder(String name, String nameUp) {
        this.name = name;
        this.nameUp = nameUp;
        this.nameUpArr = SplitUtil.split(nameUp, ',', true);
    }

    public String getName() {
        return name;
    }

    public String getNameUp() {
        return nameUp;
    }

    //by zcy
    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {

        if (this.nameUpArr.length == 1) {
            return parameters.get(nameUp);
        } else {
            StringBuilder str = new StringBuilder(parameters.get(this.nameUpArr[0]).toString());
            for (int i = 1; i < this.nameUpArr.length; ++i) {
                str.append(parameters.get(this.nameUpArr[i]).toString());
            }
            return (Object) str;
        }
    }
  
/*    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
    	return parameters.get(nameUp);//修改这里？？ parameters是：{VAL=part2,ID=513}， nameUP是"ID,VAL"
    }*/

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}