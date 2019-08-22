package cn.autolabor.util.lambda.function;


import cn.autolabor.util.Sugar;

public interface TaskLambdaFun {

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.NONE;
    }

    default boolean hasReturn() {
        return this instanceof TaskLambdaFunWithReturn;
    }

    @SuppressWarnings("unchecked")
    default Object invoke(Object... params) {
        switch (this.code()) {
            case FUN00:
                ((TaskLambdaFun00) this).run();
                break;
            case FUN10:
                return ((TaskLambdaFun10) this).run();
            case FUN01:
                ((TaskLambdaFun01) this).run(params[0]);
                break;
            case FUN11:
                return ((TaskLambdaFun11) this).run(params[0]);
            case FUN02:
                ((TaskLambdaFun02) this).run(params[0], params[1]);
                break;
            case FUN12:
                return ((TaskLambdaFun12) this).run(params[0], params[1]);
            case FUN03:
                ((TaskLambdaFun03) this).run(params[0], params[1], params[2]);
                break;
            case FUN13:
                return ((TaskLambdaFun13) this).run(params[0], params[1], params[2]);
            case FUN04:
                ((TaskLambdaFun04) this).run(params[0], params[1], params[2], params[3]);
                break;
            case FUN14:
                return ((TaskLambdaFun14) this).run(params[0], params[1], params[2], params[3]);
            case FUN05:
                ((TaskLambdaFun05) this).run(params[0], params[1], params[2], params[3], params[4]);
                break;
            case FUN15:
                return ((TaskLambdaFun15) this).run(params[0], params[1], params[2], params[3], params[4]);
            case FUN06:
                ((TaskLambdaFun06) this).run(params[0], params[1], params[2], params[3], params[4], params[5]);
                break;
            case FUN16:
                return ((TaskLambdaFun16) this).run(params[0], params[1], params[2], params[3], params[4], params[5]);
            default:
                throw Sugar.makeThrow("Not support more than 6 parameter methods");
        }
        return null;
    }

}
