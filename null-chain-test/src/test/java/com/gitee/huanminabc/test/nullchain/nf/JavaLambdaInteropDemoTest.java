package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.lambda.LambdaProxyFactory;
import com.gitee.huanminabc.nullchain.language.internal.FunDefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunRefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunTypeInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NF Lambda 与 Java 函数式接口完整集成演示
 *
 * @author huanmin
 * @date 2025/01/08
 */
@Slf4j
public class JavaLambdaInteropDemoTest {

    /**
     * 演示 1：手动创建 FunRefInfo 并转换为 Java Function
     */
    @Test
    @DisplayName("演示 1：手动创建 NF Lambda -> Java Function")
    public void demo1_ManualCreateLambda() {
        log.info("===== 演示 1：手动创建 NF Lambda -> Java Function =====");

        // 1. 手动创建 FunRefInfo
        FunRefInfo funRef = createSquareLambda();

        // 2. 创建 NfContext
        NfContext context = new NfContext();

        log.info("NF Lambda: {}", funRef);
        log.info("Lambda 类型: {}", funRef.getFunTypeInfo());

        // 3. 转换为 Java Function
        Function<Integer, Integer> function = LambdaProxyFactory.createProxy(
            funRef,
            Function.class,
            context,
            0
        );

        // 4. 在 Java 中调用
        Integer result = function.apply(5);
        log.info("Java Function.apply(5) = {}", result);
        assertEquals(25, result);

        log.info("✓ 演示 1 完成：NF Lambda 成功转换为 Java Function 并调用");
    }

    /**
     * 演示 2：NF Lambda -> Java Predicate
     */
    @Test
    @DisplayName("演示 2：NF Lambda -> Java Predicate")
    public void demo2_NfLambdaToJavaPredicate() {
        log.info("===== 演示 2：NF Lambda -> Java Predicate =====");

        // 1. 创建 NF Lambda
        FunRefInfo funRef = createIsEvenLambda();
        NfContext context = new NfContext();

        // 2. 转换为 Java Predicate
        Predicate<Integer> predicate = LambdaProxyFactory.createProxy(
            funRef,
            Predicate.class,
            context,
            0
        );

        // 3. 在 Java 中调用
        log.info("predicate.test(4) = {}", predicate.test(4)); // true
        log.info("predicate.test(5) = {}", predicate.test(5)); // false

        assertEquals(true, predicate.test(4));
        assertEquals(false, predicate.test(5));

        log.info("✓ 演示 2 完成：NF Lambda 成功转换为 Java Predicate 并调用");
    }

    /**
     * 演示 3：NF Lambda 用于 Stream.map()
     */
    @Test
    @DisplayName("演示 3：NF Lambda 用于 Java Stream.map()")
    public void demo3_NfLambdaWithStreamMap() {
        log.info("===== 演示 3：NF Lambda 用于 Stream.map() =====");

        // 1. 创建 NF Lambda
        FunRefInfo funRef = createDoublerLambda();
        NfContext context = new NfContext();

        // 2. 转换为 Java Function
        Function<Integer, Integer> doubler = LambdaProxyFactory.createProxy(
            funRef,
            Function.class,
            context,
            0
        );

        // 3. 用于 Java Stream
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> doubled = numbers.stream()
            .map(doubler)  // 使用 NF Lambda
            .collect(Collectors.toList());

        log.info("原始数据: {}", numbers);
        log.info("处理后: {}", doubled);
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), doubled);

        log.info("✓ 演示 3 完成：NF Lambda 成功用于 Stream.map()");
    }

    /**
     * 演示 4：多个 NF Lambda 组合使用
     */
    @Test
    @DisplayName("演示 4：多个 NF Lambda 组合用于 Stream 操作")
    public void demo4_MultipleLambdasWithStream() {
        log.info("===== 演示 4：多个 NF Lambda 组合使用 =====");

        // 1. 创建两个 NF Lambda
        FunRefInfo squareFun = createSquareLambda();
        FunRefInfo filterFun = createIsGreaterThanTenLambda();
        NfContext context = new NfContext();

        // 2. 转换为 Java 接口
        Function<Integer, Integer> square = LambdaProxyFactory.createProxy(
            squareFun, Function.class, context, 0
        );
        Predicate<Integer> isGreaterThan10 = LambdaProxyFactory.createProxy(
            filterFun, Predicate.class, context, 0
        );

        // 3. 组合使用
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> result = numbers.stream()
            .map(square)           // NF Lambda: 平方
            .filter(isGreaterThan10) // NF Lambda: 过滤 > 10
            .collect(Collectors.toList());

        log.info("原始数据: {}", numbers);
        log.info("map(平方).filter(>10): {}", result);
        assertEquals(Arrays.asList(16, 25), result);

        log.info("✓ 演示 4 完成：多个 NF Lambda 组合使用成功");
    }

    /**
     * 演示 5：完整的 Stream 链式调用
     */
    @Test
    @DisplayName("演示 5：完整的 Stream 链式调用")
    public void demo5_CompleteStreamPipeline() {
        log.info("===== 演示 5：完整的 Stream 链式调用 =====");

        // 1. 创建多个 Lambda
        FunRefInfo tripleFun = createTriplerLambda();
        FunRefInfo isPositiveFun = createIsPositiveLambda();
        NfContext context = new NfContext();

        // 2. 转换
        Function<Integer, Integer> triple = LambdaProxyFactory.createProxy(
            tripleFun, Function.class, context, 0
        );
        Predicate<Integer> isPositive = LambdaProxyFactory.createProxy(
            isPositiveFun, Predicate.class, context, 0
        );

        // 3. 完整的 Stream 链式调用
        List<Integer> numbers = Arrays.asList(-2, -1, 0, 1, 2, 3);
        List<Integer> result = numbers.stream()
            .map(triple)        // x3
            .filter(isPositive) // > 0
            .collect(Collectors.toList());

        log.info("原始: {}", numbers);
        log.info("map(x3).filter(>0): {}", result);
        assertEquals(Arrays.asList(3, 6, 9), result);

        log.info("✓ 演示 5 完成：完整 Stream 链式调用成功");
    }

    // ========== 辅助方法：手动创建 Lambda ==========

    private FunRefInfo createSquareLambda() {
        FunDefInfo funDef = new FunDefInfo();
        funDef.setFunctionName("square");

        // 参数
        List<FunDefInfo.FunParameter> params = new ArrayList<>();
        params.add(new FunDefInfo.FunParameter("x", "Integer", false));
        funDef.setParameters(params);

        // 返回类型
        funDef.setReturnTypes(Arrays.asList("Integer"));

        // 函数体（简化版，实际需要真实的语法节点）
        List<SyntaxNode> bodyNodes = new ArrayList<>();
        funDef.setBodyNodes(bodyNodes);

        // 类型信息
        FunTypeInfo typeInfo = new FunTypeInfo();
        typeInfo.setParameterTypes(Arrays.asList("Integer"));
        typeInfo.setReturnType("Integer");
        typeInfo.setArity(1);

        return FunRefInfo.createLambda(funDef, null, null, typeInfo);
    }

    private FunRefInfo createDoublerLambda() {
        FunDefInfo funDef = new FunDefInfo();
        funDef.setFunctionName("doubler");

        List<FunDefInfo.FunParameter> params = new ArrayList<>();
        params.add(new FunDefInfo.FunParameter("x", "Integer", false));
        funDef.setParameters(params);
        funDef.setReturnTypes(Arrays.asList("Integer"));

        FunTypeInfo typeInfo = new FunTypeInfo();
        typeInfo.setParameterTypes(Arrays.asList("Integer"));
        typeInfo.setReturnType("Integer");
        typeInfo.setArity(1);

        return FunRefInfo.createLambda(funDef, null, null, typeInfo);
    }

    private FunRefInfo createTriplerLambda() {
        FunDefInfo funDef = new FunDefInfo();
        funDef.setFunctionName("triple");

        List<FunDefInfo.FunParameter> params = new ArrayList<>();
        params.add(new FunDefInfo.FunParameter("x", "Integer", false));
        funDef.setParameters(params);
        funDef.setReturnTypes(Arrays.asList("Integer"));

        FunTypeInfo typeInfo = new FunTypeInfo();
        typeInfo.setParameterTypes(Arrays.asList("Integer"));
        typeInfo.setReturnType("Integer");
        typeInfo.setArity(1);

        return FunRefInfo.createLambda(funDef, null, null, typeInfo);
    }

    private FunRefInfo createIsEvenLambda() {
        FunDefInfo funDef = new FunDefInfo();
        funDef.setFunctionName("isEven");

        List<FunDefInfo.FunParameter> params = new ArrayList<>();
        params.add(new FunDefInfo.FunParameter("x", "Integer", false));
        funDef.setParameters(params);
        funDef.setReturnTypes(Arrays.asList("Boolean"));

        FunTypeInfo typeInfo = new FunTypeInfo();
        typeInfo.setParameterTypes(Arrays.asList("Integer"));
        typeInfo.setReturnType("Boolean");
        typeInfo.setArity(1);

        return FunRefInfo.createLambda(funDef, null, null, typeInfo);
    }

    private FunRefInfo createIsGreaterThanTenLambda() {
        FunDefInfo funDef = new FunDefInfo();
        funDef.setFunctionName("isGreaterThan10");

        List<FunDefInfo.FunParameter> params = new ArrayList<>();
        params.add(new FunDefInfo.FunParameter("x", "Integer", false));
        funDef.setParameters(params);
        funDef.setReturnTypes(Arrays.asList("Boolean"));

        FunTypeInfo typeInfo = new FunTypeInfo();
        typeInfo.setParameterTypes(Arrays.asList("Integer"));
        typeInfo.setReturnType("Boolean");
        typeInfo.setArity(1);

        return FunRefInfo.createLambda(funDef, null, null, typeInfo);
    }

    private FunRefInfo createIsPositiveLambda() {
        FunDefInfo funDef = new FunDefInfo();
        funDef.setFunctionName("isPositive");

        List<FunDefInfo.FunParameter> params = new ArrayList<>();
        params.add(new FunDefInfo.FunParameter("x", "Integer", false));
        funDef.setParameters(params);
        funDef.setReturnTypes(Arrays.asList("Boolean"));

        FunTypeInfo typeInfo = new FunTypeInfo();
        typeInfo.setParameterTypes(Arrays.asList("Integer"));
        typeInfo.setReturnType("Boolean");
        typeInfo.setArity(1);

        return FunRefInfo.createLambda(funDef, null, null, typeInfo);
    }
}
