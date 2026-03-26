package com.gitee.huanminabc.nullchain.language.internal;

import com.gitee.huanminabc.nullchain.language.token.Token;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * 基于起止下标的Token游标视图。
 *
 * <p>用于在解析阶段避免频繁的 remove(0) / subList(0, n).clear()
 * 触发数组搬移，同时保持 List 接口不变，便于平滑接入现有解析器实现。</p>
 */
public final class TokenCursor extends AbstractList<Token> implements RandomAccess {
    private final List<Token> backingTokens;
    private int start;
    private int end;

    private TokenCursor(List<Token> backingTokens) {
        this.backingTokens = backingTokens;
        this.start = 0;
        this.end = backingTokens.size();
    }

    public static List<Token> wrap(List<Token> tokens) {
        if (tokens instanceof TokenCursor) {
            return tokens;
        }
        return new TokenCursor(tokens);
    }

    @Override
    public Token get(int index) {
        rangeCheck(index);
        return backingTokens.get(start + index);
    }

    @Override
    public Token set(int index, Token element) {
        rangeCheck(index);
        return backingTokens.set(start + index, element);
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    public Token remove(int index) {
        rangeCheck(index);
        Token removed = backingTokens.get(start + index);
        if (index == 0) {
            start++;
        } else if (index == size() - 1) {
            end--;
        } else {
            backingTokens.remove(start + index);
            end--;
        }
        modCount++;
        return removed;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
            return;
        }
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", size=" + size());
        }

        int removeCount = toIndex - fromIndex;
        if (fromIndex == 0) {
            start += removeCount;
        } else if (toIndex == size()) {
            end -= removeCount;
        } else {
            backingTokens.subList(start + fromIndex, start + toIndex).clear();
            end -= removeCount;
        }
        modCount++;
    }

    private void rangeCheck(int index) {
        int size = size();
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        }
    }
}
