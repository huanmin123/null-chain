package com.gitee.huanminabc.test.nullchain.entity;

import com.gitee.huanminabc.nullchain.NullExt;
import com.gitee.huanminabc.nullchain.common.NullNode;

import java.util.HashMap;

public class MapTest<K, V> extends HashMap<K, V> implements NullExt<NullNode<K, V>> {
}
