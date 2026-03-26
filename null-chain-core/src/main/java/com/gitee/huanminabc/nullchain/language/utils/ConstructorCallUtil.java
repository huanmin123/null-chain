package com.gitee.huanminabc.nullchain.language.utils;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * new(...) 构造调用工具类。
 *
 * <p>集中处理参数解析、构造函数匹配与缓存，避免在多个语法节点里重复扫描反射信息。</p>
 */
public final class ConstructorCallUtil {

    private static final ConcurrentMap<Class<?>, Constructor<?>[]> PUBLIC_CONSTRUCTORS_CACHE =
        new ConcurrentHashMap<Class<?>, Constructor<?>[]>();
    private static final ConcurrentMap<ConstructorCacheKey, Constructor<?>> MATCHED_CONSTRUCTOR_CACHE =
        new ConcurrentHashMap<ConstructorCacheKey, Constructor<?>>();

    private ConstructorCallUtil() {
    }

    public static Object createInstanceFromNewExpression(List<Token> expTokens, Class<?> declaredType, String importType,
                                                         NfContext context, SyntaxNode syntaxNode) {
        int line = expTokens.isEmpty() ? -1 : expTokens.get(0).line;
        try {
            Class<?> actualType = resolveActualType(declaredType, importType, context, line, syntaxNode);
            ConstructorArguments arguments = parseConstructorArguments(expTokens, context, line, syntaxNode);
            Constructor<?> constructor = resolveConstructor(actualType, arguments.argumentTypes);
            return constructor.newInstance(arguments.arguments.toArray());
        } catch (NfException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new NfException(e, "Line:{} ,类型 {} 没有匹配的构造函数 , syntax: {}", line, importType, syntaxNode);
        } catch (Exception e) {
            throw new NfException(e, "Line:{} ,创建{}对象失败 , syntax: {}", line, importType, syntaxNode);
        }
    }

    private static Class<?> resolveActualType(Class<?> declaredType, String importType, NfContext context,
                                              int line, SyntaxNode syntaxNode) {
        if (!declaredType.isInterface()) {
            return declaredType;
        }
        Class<?> actualType = context.getInterfaceDefaultImpl(declaredType);
        if (actualType == null) {
            throw new NfException("Line:{} ,接口 {} 没有默认实现类，无法创建实例 , syntax: {}",
                line, importType, syntaxNode);
        }
        return actualType;
    }

    private static ConstructorArguments parseConstructorArguments(List<Token> expTokens, NfContext context,
                                                                 int line, SyntaxNode syntaxNode) {
        if (!hasConstructorArguments(expTokens)) {
            return ConstructorArguments.EMPTY;
        }

        int parenEnd = findMatchingRightParen(expTokens);
        if (parenEnd == -1) {
            throw new NfException("Line:{} ,new() 参数列表括号不匹配 , syntax: {}", line, syntaxNode);
        }

        List<Token> paramTokens = expTokens.subList(2, parenEnd);
        if (paramTokens.isEmpty()) {
            return ConstructorArguments.EMPTY;
        }

        List<List<Token>> paramExprs = splitTopLevelByComma(paramTokens);
        List<Object> args = new ArrayList<Object>(paramExprs.size());
        List<Class<?>> argTypes = new ArrayList<Class<?>>(paramExprs.size());

        for (List<Token> paramExpr : paramExprs) {
            Object argValue = NfCalculator.arithmetic(TokenUtil.mergeToken(paramExpr).toString(), context);
            args.add(argValue);
            argTypes.add(argValue != null ? argValue.getClass() : null);
        }
        return new ConstructorArguments(args, argTypes);
    }

    private static boolean hasConstructorArguments(List<Token> expTokens) {
        return expTokens.size() >= 2 && expTokens.get(1).type == TokenType.LPAREN;
    }

    private static int findMatchingRightParen(List<Token> expTokens) {
        int depth = 0;
        for (int i = 1; i < expTokens.size(); i++) {
            TokenType type = expTokens.get(i).type;
            if (type == TokenType.LPAREN) {
                depth++;
            } else if (type == TokenType.RPAREN) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static List<List<Token>> splitTopLevelByComma(List<Token> tokens) {
        List<List<Token>> result = new ArrayList<List<Token>>();
        List<Token> current = new ArrayList<Token>();
        int parenDepth = 0;
        int braceDepth = 0;

        for (Token token : tokens) {
            if (token.type == TokenType.LPAREN) {
                parenDepth++;
                current.add(token);
            } else if (token.type == TokenType.RPAREN) {
                parenDepth--;
                current.add(token);
            } else if (token.type == TokenType.LBRACE) {
                braceDepth++;
                current.add(token);
            } else if (token.type == TokenType.RBRACE) {
                braceDepth--;
                current.add(token);
            } else if (token.type == TokenType.COMMA && parenDepth == 0 && braceDepth == 0) {
                if (!current.isEmpty()) {
                    result.add(current);
                    current = new ArrayList<Token>();
                }
            } else {
                current.add(token);
            }
        }

        if (!current.isEmpty()) {
            result.add(current);
        }
        return result;
    }

    private static Constructor<?> resolveConstructor(Class<?> clazz, List<Class<?>> argTypes)
        throws NoSuchMethodException {
        ConstructorCacheKey cacheKey = new ConstructorCacheKey(clazz, argTypes);
        Constructor<?> cached = MATCHED_CONSTRUCTOR_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Constructor<?>[] constructors = PUBLIC_CONSTRUCTORS_CACHE.computeIfAbsent(clazz, Class::getConstructors);
        for (Constructor<?> constructor : constructors) {
            if (isConstructorMatch(constructor.getParameterTypes(), argTypes)) {
                Constructor<?> previous = MATCHED_CONSTRUCTOR_CACHE.putIfAbsent(cacheKey, constructor);
                return previous != null ? previous : constructor;
            }
        }
        throw new NoSuchMethodException("找不到匹配的构造函数，参数类型: " + argTypes);
    }

    private static boolean isConstructorMatch(Class<?>[] paramTypes, List<Class<?>> argTypes) {
        if (paramTypes.length != argTypes.size()) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> expectedType = paramTypes[i];
            Class<?> actualType = argTypes.get(i);

            if (actualType == null) {
                if (expectedType.isPrimitive()) {
                    return false;
                }
                continue;
            }

            if (!isTypeCompatible(actualType, expectedType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTypeCompatible(Class<?> actualType, Class<?> expectedType) {
        if (expectedType.equals(actualType)) {
            return true;
        }
        if (expectedType.isPrimitive()) {
            return isWrapperType(actualType, expectedType);
        }
        if (actualType.isPrimitive()) {
            return isWrapperType(expectedType, actualType);
        }
        return expectedType.isAssignableFrom(actualType);
    }

    private static boolean isWrapperType(Class<?> wrapperType, Class<?> primitiveType) {
        if (primitiveType == int.class) {
            return wrapperType == Integer.class;
        }
        if (primitiveType == long.class) {
            return wrapperType == Long.class;
        }
        if (primitiveType == double.class) {
            return wrapperType == Double.class;
        }
        if (primitiveType == float.class) {
            return wrapperType == Float.class;
        }
        if (primitiveType == boolean.class) {
            return wrapperType == Boolean.class;
        }
        if (primitiveType == byte.class) {
            return wrapperType == Byte.class;
        }
        if (primitiveType == short.class) {
            return wrapperType == Short.class;
        }
        if (primitiveType == char.class) {
            return wrapperType == Character.class;
        }
        return false;
    }

    private static final class ConstructorArguments {
        private static final ConstructorArguments EMPTY =
            new ConstructorArguments(Collections.emptyList(), Collections.<Class<?>>emptyList());

        private final List<Object> arguments;
        private final List<Class<?>> argumentTypes;

        private ConstructorArguments(List<Object> arguments, List<Class<?>> argumentTypes) {
            this.arguments = arguments;
            this.argumentTypes = argumentTypes;
        }
    }

    private static final class ConstructorCacheKey {
        private final Class<?> targetType;
        private final List<Class<?>> argumentTypes;

        private ConstructorCacheKey(Class<?> targetType, List<Class<?>> argumentTypes) {
            this.targetType = targetType;
            this.argumentTypes = Collections.unmodifiableList(new ArrayList<Class<?>>(argumentTypes));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ConstructorCacheKey)) {
                return false;
            }
            ConstructorCacheKey that = (ConstructorCacheKey) o;
            return Objects.equals(targetType, that.targetType) &&
                Objects.equals(argumentTypes, that.argumentTypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetType, argumentTypes);
        }
    }
}
