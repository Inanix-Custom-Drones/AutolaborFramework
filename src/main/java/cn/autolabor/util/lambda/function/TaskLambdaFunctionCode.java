package cn.autolabor.util.lambda.function;

public enum TaskLambdaFunctionCode {
    NONE(-1, -1),
    FUN00(0, 0), FUN01(0, 1), FUN02(0, 2), FUN03(0, 3), FUN04(0, 4), FUN05(0, 5), FUN06(0, 6),
    FUN10(1, 0), FUN11(1, 1), FUN12(1, 2), FUN13(1, 3), FUN14(1, 4), FUN15(1, 5), FUN16(1, 6);

    private int returnCount;
    private int paramsCount;

    TaskLambdaFunctionCode(int returnCount, int paramsCount) {
        this.returnCount = returnCount;
        this.paramsCount = paramsCount;
    }

    public int getReturnCount() {
        return returnCount;
    }

    public int getParamsCount() {
        return paramsCount;
    }
}
