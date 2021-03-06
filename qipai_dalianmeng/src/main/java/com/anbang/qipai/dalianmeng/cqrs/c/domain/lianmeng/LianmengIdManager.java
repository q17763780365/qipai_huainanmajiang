package com.anbang.qipai.dalianmeng.cqrs.c.domain.lianmeng;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LianmengIdManager {

    // id 存放的是相同的chaguanid。考虑到线程安全和查询效率。 不使用list，set 。而是使用map
    private Map<String, String> idMap = new ConcurrentHashMap<>();

    private static char[] charsForId = new char[]{'0', '1', '2', '3', '5', '6', '7', '8', '9'};

    /**
     * 创建联盟id
     */
    public String createLianmengId(long seed) {
        Random random = new Random(seed);
        String newId;
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int charIdx = random.nextInt(charsForId.length);
                sb.append(charsForId[charIdx]);
            }
            newId = sb.toString();
            if (idMap.containsKey(newId)) {
                continue;
            } else {
                break;
            }
        }
        idMap.put(newId, newId);
        return newId;
    }

    public Set<String> takeAllLianmengId() {
        return idMap.keySet();
    }

    public boolean hasLianmeng(String lianmengId) {
        return idMap.containsKey(lianmengId);
    }

    public void removeLianmengId(String lianmengId) {
        idMap.remove(lianmengId);
    }

    public Map<String, String> getIdMap() {
        return idMap;
    }

    public void setIdMap(Map<String, String> idMap) {
        this.idMap = idMap;
    }

}
